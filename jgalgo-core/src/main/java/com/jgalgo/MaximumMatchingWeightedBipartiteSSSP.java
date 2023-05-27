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
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Maximum weighted matching algorithm using {@link SSSP} for bipartite graphs.
 * <p>
 * The running time of this algorithm is \(O(m n + n^2 \log n)\) and it uses linear space. If a different {@link SSSP}
 * algorithm is provided using {@link #setSsspAlgo(SSSP)} the running time will be \(O(n)\) times the running time of
 * the shortest path algorithm on a graph of size \(O(n)\).
 *
 * @author Barak Ugav
 */
class MaximumMatchingWeightedBipartiteSSSP implements MaximumMatchingWeighted {

	private Object bipartiteVerticesWeightKey = Weights.DefaultBipartiteWeightKey;
	private SSSP ssspAlgo = new SSSPDijkstra();
	private static final Object EdgeRefWeightKey = new Utils.Obj("refToOrig");
	private static final Object EdgeWeightKey = new Utils.Obj("weight");

	/**
	 * Create a new maximum weighted matching object.
	 */
	MaximumMatchingWeightedBipartiteSSSP() {}

	/**
	 * Set the {@link SSSP} algorithm used by this algorithm.
	 * <p>
	 * The shortest path algorithm should support non negative floating points weights. The default implementation uses
	 * {@link SSSPDijkstra}.
	 *
	 * @param algo an shortest path algorithm
	 */
	public void setSsspAlgo(SSSP algo) {
		ssspAlgo = Objects.requireNonNull(algo);
	}

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
		ArgumentCheck.onlyBipartite(g, partition);

		int[] match = computeMaxMatching(g, w, partition);
		return new MatchingImpl(g, match);
	}

	private int[] computeMaxMatching(Graph gOrig, EdgeWeightFunc w0, Weights.Bool partition) {
		final int n = gOrig.vertices().size();
		Graph g = GraphBuilder.newDirected().expectedVerticesNum(n + 2).build();
		for (int v = 0; v < n; v++)
			g.addVertex();
		Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		Weights.Double w = g.addEdgesWeights(EdgeWeightKey, double.class);

		for (int u = 0; u < n; u++) {
			if (!partition.getBool(u))
				continue;
			for (EdgeIter eit = gOrig.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				double weight = w0.weight(e);
				if (weight < 0)
					continue; // no reason to match negative edges
				int e0 = g.addEdge(u, eit.target());
				edgeRef.set(e0, e);
				w.set(e0, weight);
			}
		}

		final int s = g.addVertex(), t = g.addVertex();

		int[] match = new int[n];
		Arrays.fill(match, -1);

		double maxWeight = 1;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			maxWeight = Math.max(maxWeight, w.weight(e));
		}
		if (!Double.isFinite(maxWeight))
			throw new IllegalArgumentException("non finite weights");
		final double RemovedEdgeWeight = maxWeight * n;

		// Negate unmatched edges
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			w.set(e, -w.weight(e));
		}
		// Connected unmatched vertices to fake vertices s,t
		for (int u = 0; u < n; u++) {
			if (partition.getBool(u)) {
				w.set(g.addEdge(s, u), 0);
			} else {
				w.set(g.addEdge(u, t), 0);
			}
		}

		double[] potential = new double[n + 2];
		EdgeWeightFunc spWeightFunc = e -> w.weight(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		SSSP.Result sp = new SSSPBellmanFord().computeShortestPaths(g, w, s);
		for (int v = 0; v < n + 2; v++)
			potential[v] = sp.distance(v);

		for (;;) {
			sp = ssspAlgo.computeShortestPaths(g, spWeightFunc, s);
			Path augPath = sp.getPath(t);
			double augPathWeight = -(sp.distance(t) + potential[t]);
			if (augPath == null || augPathWeight >= RemovedEdgeWeight || augPathWeight < 0)
				break;

			// avoid using augPath.iterator() as we modify the graph during iteration
			IntIterator it = new IntArrayList(augPath).iterator();
			// 'remove' edge from S to new matched vertex
			w.set(it.nextInt(), RemovedEdgeWeight);
			for (;;) {
				int matchedEdge = it.nextInt();

				// Reverse newly matched edge
				g.reverseEdge(matchedEdge);
				int eOrig = edgeRef.getInt(matchedEdge);
				match[g.edgeSource(matchedEdge)] = match[g.edgeTarget(matchedEdge)] = eOrig;
				w.set(matchedEdge, -w.weight(matchedEdge));

				int unmatchedEdge = it.nextInt();

				if (!it.hasNext()) {
					// 'remove' edge from new matched vertex to T
					w.set(unmatchedEdge, RemovedEdgeWeight);
					break;
				}

				// Reverse newly unmatched edge
				g.reverseEdge(unmatchedEdge);
				w.set(unmatchedEdge, -w.weight(unmatchedEdge));
			}

			// Update potential based on the distances
			for (int u = 0; u < n; u++)
				potential[u] += sp.distance(u);
		}

		return match;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException this implementation does not support perfect matching computation
	 */
	@Deprecated
	@Override
	public Matching computeMaximumWeightedPerfectMatching(Graph g, EdgeWeightFunc w) {
		throw new UnsupportedOperationException();
	}

}
