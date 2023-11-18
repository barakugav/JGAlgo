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

import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.IntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
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

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.newBuilder();

	/**
	 * Create a new maximum weighted matching object.
	 */
	MatchingWeightedBipartiteHungarianMethod() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link BipartiteGraphs#VertexBiPartitionWeightKey}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		IWeightsBool partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + BipartiteGraphs.VertexBiPartitionWeightKey);
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
		Assertions.Graphs.onlyUndirected(g);
		IWeightsBool partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + BipartiteGraphs.VertexBiPartitionWeightKey);
		return new Worker(g, partition, w).computeMaxMatching(true);
	}

	private class Worker {

		private final IndexGraph g;
		private final IWeightsBool partition;
		private final IWeightFunction w;

		private final Bitmap inTree;

		private final IntComparator edgeSlackComparator;
		private final IntReferenceableHeap nextTightEdge;
		private final IntReferenceableHeap.Ref[] nextTightEdgePerOutV;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		Worker(IndexGraph g, IWeightsBool partition, IWeightFunction w) {
			Assertions.Graphs.onlyBipartite(g, partition);

			this.g = g;
			this.partition = partition;
			w = WeightFunctions.localEdgeWeightFunction(g, w);
			this.w = w != null ? w : IWeightFunction.CardinalityWeightFunction;
			int n = g.vertices().size();

			inTree = new Bitmap(n);

			edgeSlackComparator = (e1, e2) -> Double.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, edgeSlackComparator);
			nextTightEdgePerOutV = new IntReferenceableHeap.Ref[n];

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		IMatching computeMaxMatching(boolean perfect) {
			final int n = g.vertices().size();
			final int EdgeNone = -1;

			int[] parent = new int[n];
			int[] matched = new int[n];
			Arrays.fill(matched, EdgeNone);

			double maxWeight = Double.MIN_VALUE;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				maxWeight = Math.max(maxWeight, w.weight(e));
			final double delta1Threshold = maxWeight;
			for (int u = 0; u < n; u++)
				if (partition.get(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				Arrays.fill(parent, EdgeNone);

				// Start growing tree from all unmatched vertices in S
				for (int u = 0; u < n; u++) {
					if (!partition.get(u) || matched[u] != EdgeNone)
						continue;
					vertexAddedToTree(u);
					for (int e : g.outEdges(u))
						nextTightEdgeAdd(u, e);
				}

				currentTree: for (;;) {
					while (nextTightEdge.isNotEmpty()) {
						IntReferenceableHeap.Ref minRef = nextTightEdge.findMin();
						int e = minRef.key();
						int u0 = g.edgeSource(e), v0 = g.edgeTarget(e);

						if (inTree.get(u0) && inTree.get(v0)) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdge.remove(minRef);
							continue;
						}
						int v = inTree.get(u0) ? v0 : u0;

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdge.remove(minRef);
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

						for (int e1 : g.outEdges(v))
							nextTightEdgeAdd(v, e1);
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 = nextTightEdge.isEmpty() ? -1 : edgeSlack(nextTightEdge.findMin().key());
					if ((!perfect && delta1 <= delta2) || delta2 == -1)
						break mainLoop;
					deltaTotal += delta2;
				}

				// Update dual values base
				for (int u = 0; u < n; u++)
					if (inTree.get(u))
						dualValBase[u] = dualVal(u);
				Arrays.fill(dualVal0, 0);

				// Reset tree
				inTree.clear();

				// Reset heap
				nextTightEdge.clear();
				Arrays.fill(nextTightEdgePerOutV, null);
			}

			return new Matchings.MatchingImpl(g, matched);
		}

		private void nextTightEdgeAdd(int u, int e) {
			int v = g.edgeEndpoint(e, u);
			IntReferenceableHeap.Ref ref = nextTightEdgePerOutV[v];
			if (ref == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(e);
			else if (edgeSlackComparator.compare(e, ref.key()) < 0)
				nextTightEdge.decreaseKey(ref, e);
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
