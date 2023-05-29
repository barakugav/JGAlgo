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

import java.util.BitSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * Bellman–Ford algorithm for Single Source Shortest Path (SSSP) with negative weights in directed graphs.
 * <p>
 * Compute the shortest paths from a single source to all other vertices with weight function of arbitrary values. The
 * algorithm runs in \(O(n m)\) time and uses linear space.
 * <p>
 * In case there are only positive weights, use {@link ShortestPathSingleSourceDijkstra}. In case the weights are
 * integers, use {@link ShortestPathSingleSourceGoldberg}.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class ShortestPathSingleSourceBellmanFord implements ShortestPathSingleSource {

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceBellmanFord() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public ShortestPathSingleSource.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		ArgumentCheck.onlyDirected(g);
		if (w == null)
			w = EdgeWeightFunc.CardinalityEdgeWeightFunction;
		int n = g.vertices().size();
		Result res = new Result(g, source);
		res.distances[source] = 0;

		for (int i = 0; i < n - 1; i++) {
			for (int e : g.edges()) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		for (int v = 0; v < n; v++) {
			int e = res.backtrack[v];
			if (e == -1)
				continue;
			int u = g.edgeSource(e);
			double d = res.distances[u] + w.weight(e);
			if (d < res.distances[v]) {
				BitSet visited = new BitSet(n);
				visited.set(v);
				while (!visited.get(u)) {
					visited.set(u);
					e = res.backtrack[u];
					u = g.edgeSource(e);
				}

				IntArrayList negCycle = new IntArrayList();
				for (int p = u;;) {
					e = res.backtrack[p];
					negCycle.add(e);
					p = g.edgeSource(e);
					if (p == u)
						break;
				}
				IntArrays.reverse(negCycle.elements(), 0, negCycle.size());
				res.negCycle = new PathImpl(g, u, u, negCycle);
				return res;
			}
		}

		return res;
	}

	private static class Result extends ShortestPathSingleSourceUtils.ResultImpl {

		private Path negCycle;

		private Result(Graph g, int source) {
			super(g, source);
		}

		@Override
		public double distance(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return super.distance(target);
		}

		@Override
		public Path getPath(int target) {
			if (foundNegativeCycle())
				throw new IllegalStateException();
			return super.getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return negCycle != null;
		}

		@Override
		public Path getNegativeCycle() {
			if (!foundNegativeCycle())
				throw new IllegalStateException("no negative cycle found");
			return negCycle;
		}

		@Override
		public String toString() {
			return foundNegativeCycle() ? "[NegCycle=" + negCycle + "]" : super.toString();
		}

	}

}
