/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.IndexHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntComparator;

/**
 * Kuhn's Hungarian method for maximum weighted matching in bipartite graphs.
 *
 * <p>
 * The running time of the algorithm is \(O(m n + n^2 \log n)\) and it uses linear space.
 *
 * <p>
 * Based on 'The Hungarian method for the assignment problem' by Kuhn, H.W. (1955). The original paper stated a running
 * time of \(O(n^3)\), but by using heaps with {@code decreaseKey} operations in \(O(1)\) the running time can be
 * reduced to \(O(m n + n^2 \log n)\), as done in this implementation.
 *
 * @author Barak Ugav
 */
class MatchingWeightedBipartiteHungarianMethod extends Matchings.AbstractMaximumMatchingImpl {

	/**
	 * Create a new maximum weighted matching object.
	 */
	MatchingWeightedBipartiteHungarianMethod() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link BipartiteGraphs#VertexBiPartitionWeightKey}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		IWeightsBool partition = Assertions.onlyBipartite(g);
		return new Worker(g, partition, w).computeMaxMatching(false);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link #setBipartiteVerticesWeightKey(String)}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		IWeightsBool partition = Assertions.onlyBipartite(g);
		return new Worker(g, partition, w).computeMaxMatching(true);
	}

	private static class Worker {

		private final IndexGraph g;
		private final IWeightsBool partition;
		private final IWeightFunction w;

		private final Bitmap inTree;

		private final IntComparator edgeSlackComparator;
		private final IndexHeap nextTightEdgeHeap;
		private final int[] nextTightEdge;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		Worker(IndexGraph g, IWeightsBool partition, IWeightFunction w) {
			Assertions.onlyBipartite(g, partition);

			this.g = g;
			this.partition = partition;
			w = WeightFunctions.localEdgeWeightFunction(g, w);
			this.w = w != null ? w : IWeightFunction.CardinalityWeightFunction;
			int n = g.vertices().size();

			inTree = new Bitmap(n);

			edgeSlackComparator = (e1, e2) -> Double.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = new int[n];
			nextTightEdgeHeap = IndexHeap
					.newInstance(n, (v1, v2) -> edgeSlackComparator.compare(nextTightEdge[v1], nextTightEdge[v2]));

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		IMatching computeMaxMatching(boolean perfect) {
			final int n = g.vertices().size();
			final int EdgeNone = -1;

			int[] parent = new int[n];
			int[] matched = new int[n];
			Arrays.fill(matched, EdgeNone);

			final double maxWeight = range(g.edges().size()).mapToDouble(w::weight).max().orElse(Double.MIN_VALUE);
			final double delta1Threshold = maxWeight;
			for (int u : range(n))
				if (partition.get(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				Arrays.fill(parent, EdgeNone);

				// Start growing tree from all unmatched vertices in S
				for (int u : range(n)) {
					if (!partition.get(u) || matched[u] != EdgeNone)
						continue;
					vertexAddedToTree(u);
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (!inTree.get(v))
							nextTightEdgeAdd(e, v);
					}
				}

				currentTree: for (;;) {
					while (nextTightEdgeHeap.isNotEmpty()) {
						int v = nextTightEdgeHeap.findMin();

						if (inTree.get(v)) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdgeHeap.extractMin(); /* remove(v) */
							continue;
						}
						int e = nextTightEdge[v];

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdgeHeap.extractMin(); /* remove(v) */
						parent[v] = e;
						vertexAddedToTree(v);

						int matchedEdge = matched[v];
						if (matchedEdge == EdgeNone) {
							for (;;) {
								// Augmenting path
								e = parent[v];
								matched[v] = matched[v = g.edgeEndpoint(e, v)] = e;
								// TODO don't set parent[odd vertex]
								e = parent[v];
								if (e == EdgeNone)
									break currentTree;
								v = g.edgeEndpoint(e, v);
							}
						}

						// Added odd vertex, immediately add it's matched edge and even vertex
						v = g.edgeEndpoint(matchedEdge, v);
						parent[v] = matchedEdge;
						vertexAddedToTree(v);

						for (IEdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
							int e1 = eit.nextInt();
							int w = eit.targetInt();
							if (!inTree.get(w))
								nextTightEdgeAdd(e1, w);
						}
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 =
							nextTightEdgeHeap.isEmpty() ? -1 : edgeSlack(nextTightEdge[nextTightEdgeHeap.findMin()]);
					if ((!perfect && delta1 <= delta2) || delta2 == -1)
						break mainLoop;
					deltaTotal += delta2;
				}

				// Update dual values base
				for (int u : range(n))
					if (inTree.get(u))
						dualValBase[u] = dualVal(u);
				Arrays.fill(dualVal0, 0);

				// Reset tree
				inTree.clear();

				// Reset heap
				nextTightEdgeHeap.clear();
			}

			return new Matchings.MatchingImpl(g, matched);
		}

		private void nextTightEdgeAdd(int e, int v) {
			if (!nextTightEdgeHeap.isInserted(v)) {
				nextTightEdge[v] = e;
				nextTightEdgeHeap.insert(v);
			} else if (edgeSlackComparator.compare(e, nextTightEdge[v]) < 0) {
				nextTightEdge[v] = e;
				nextTightEdgeHeap.decreaseKey(v);
			}
		}

		private double dualVal(int v) {
			return inTree.get(v) ? dualVal0[v] + (partition.get(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		private double edgeSlack(int e) {
			return dualVal(g.edgeSource(e)) + dualVal(g.edgeTarget(e)) - w.weight(e);
		}

		private void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (partition.get(v) ? deltaTotal : -deltaTotal);
			inTree.set(v);
		}

	}

}
