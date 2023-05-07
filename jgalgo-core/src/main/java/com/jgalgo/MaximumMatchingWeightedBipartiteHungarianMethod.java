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

package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Kuhn's Hungarian method for maximum weighted matching in bipartite graphs.
 * <p>
 * The running time of the algorithm is \(O(m n + n^2 \log n)\) and it uses linear space.
 * <p>
 * Based on 'The Hungarian method for the assignment problem' by Kuhn, H.W. (1955). The original paper stated a running
 * time of \(O(n^3)\), but by using heaps with {@code decreaseKey} operations in \(O(1)\) the running time can be
 * reduced to \(O(m n + n^2 \log n)\), as done in this implementation.
 *
 * @author Barak Ugav
 */
class MaximumMatchingWeightedBipartiteHungarianMethod implements MaximumMatchingWeighted {

	private Object bipartiteVerticesWeightKey = Weights.DefaultBipartiteWeightKey;
	private HeapReferenceable.Builder heapBuilder = HeapPairing::new;

	/**
	 * Create a new maximum weighted matching object.
	 */
	MaximumMatchingWeightedBipartiteHungarianMethod() {}

	/**
	 * Set the key used to get the bipartiteness property of vertices.
	 * <p>
	 * The algorithm run on bipartite graphs and expect the user to provide the vertices partition by a boolean vertices
	 * weights using {@link Graph#getVerticesWeights(Object)}. By default, the weights are searched using the key
	 * {@link Weights#DefaultBipartiteWeightKey}. To override this default behavior, use this function to choose a
	 * different key.
	 *
	 * @param key an object key that will be used to get the bipartite vertices partition by
	 *                {@code g.verticesWeight(key)}.
	 */
	public void setBipartiteVerticesWeightKey(Object key) {
		bipartiteVerticesWeightKey = key;
	}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	public void setHeapBuilder(HeapReferenceable.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link #setBipartiteVerticesWeightKey(Object)}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	public Matching computeMaximumWeightedMatching(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyUndirected(g);
		Weights.Bool partition = g.getVerticesWeights(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		return new Worker(g, partition, w).computeMaxMatching(false);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link #setBipartiteVerticesWeightKey(Object)}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	public Matching computeMaximumWeightedPerfectMatching(Graph g, EdgeWeightFunc w) {
		ArgumentCheck.onlyUndirected(g);
		Weights.Bool partition = g.getVerticesWeights(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		return new Worker(g, partition, w).computeMaxMatching(true);
	}

	private class Worker {

		private final Graph g;
		private final Weights.Bool partition;
		private final EdgeWeightFunc w;

		private final BitSet inTree;

		private final IntComparator edgeSlackComparator;
		private final HeapReferenceable<Integer> nextTightEdge;
		private final HeapReference<Integer>[] nextTightEdgePerOutV;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		@SuppressWarnings("unchecked")
		Worker(Graph g, Weights.Bool partition, EdgeWeightFunc w) {
			ArgumentCheck.onlyBipartite(g, partition);

			this.g = g;
			this.partition = partition;
			this.w = w;
			int n = g.vertices().size();

			inTree = new BitSet(n);

			edgeSlackComparator = (e1, e2) -> Double.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = heapBuilder.build(edgeSlackComparator);
			nextTightEdgePerOutV = new HeapReference[n];

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		Matching computeMaxMatching(boolean perfect) {
			final int n = g.vertices().size();
			final int EdgeNone = -1;

			int[] parent = new int[n];
			int[] matched = new int[n];
			Arrays.fill(matched, EdgeNone);

			double maxWeight = Double.MIN_VALUE;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxWeight = Math.max(maxWeight, w.weight(e));
			}
			final double delta1Threshold = maxWeight;
			for (int u = 0; u < n; u++)
				if (partition.getBool(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				Arrays.fill(parent, EdgeNone);

				// Start growing tree from all unmatched vertices in S
				for (int u = 0; u < n; u++) {
					if (!partition.getBool(u) || matched[u] != EdgeNone)
						continue;
					vertexAddedToTree(u);
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						nextTightEdgeAdd(u, e);
					}
				}

				currentTree: for (;;) {
					while (!nextTightEdge.isEmpty()) {
						HeapReference<Integer> minRef = nextTightEdge.findMinRef();
						int e = minRef.get().intValue();
						int u0 = g.edgeSource(e), v0 = g.edgeTarget(e);

						if (inTree.get(u0) && inTree.get(v0)) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdge.removeRef(minRef);
							continue;
						}
						int v = inTree.get(u0) ? v0 : u0;

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdge.removeRef(minRef);
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

						for (EdgeIter eit = g.edgesOut(v); eit.hasNext();) {
							int e1 = eit.nextInt();
							nextTightEdgeAdd(v, e1);
						}
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 = nextTightEdge.isEmpty() ? -1 : edgeSlack(nextTightEdge.findMin().intValue());
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

			return new MatchingImpl(g, matched);
		}

		private void nextTightEdgeAdd(int u, int e) {
			int v = g.edgeEndpoint(e, u);
			HeapReference<Integer> ref = nextTightEdgePerOutV[v];
			if (ref == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(Integer.valueOf(e));
			else if (edgeSlackComparator.compare(e, ref.get().intValue()) < 0)
				nextTightEdge.decreaseKey(ref, Integer.valueOf(e));
		}

		private double dualVal(int v) {
			return inTree.get(v) ? dualVal0[v] + (partition.getBool(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		private double edgeSlack(int e) {
			return dualVal(g.edgeSource(e)) + dualVal(g.edgeTarget(e)) - w.weight(e);
		}

		private void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (partition.getBool(v) ? deltaTotal : -deltaTotal);
			inTree.set(v);
		}

	}

}
