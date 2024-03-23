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

package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * Bellman–Ford algorithm for Single Source Shortest Path (SSSP) with negative weights in directed graphs.
 *
 * <p>
 * Compute the shortest paths from a single source to all other vertices with weight function of arbitrary values. The
 * algorithm runs in \(O(n m)\) time and uses linear space.
 *
 * <p>
 * In case there are only positive weights, use {@link ShortestPathSingleSourceDijkstra}. In case the weights are
 * integers, use {@link ShortestPathSingleSourceGoldberg}.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class ShortestPathSingleSourceBellmanFord extends ShortestPathSingleSourceUtils.AbstractImpl {

	ShortestPathSingleSourceBellmanFord() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		Assertions.onlyDirected(g);

		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);

		/*
		 * The implementation is based on the classical Bellman-Ford algorithm, with an additional heuristic. When we
		 * manage to find a shortest path to some vertex v, we mark v as 'modified'. In the next iteration we will not
		 * iterate over all the edges of the graph (as stated in the original algorithm), but rather only the edges of
		 * the modified vertices. Although this optimization is very simple and doesn't appear to increase the
		 * complexity of the algorithm, the original algorithm is so simple in the first place that even these few
		 * additional operations may slow down the performance. Therefore, we use the optimization only when we believe
		 * it is worth it, namely when the number of modified vertices is small enough (<=n/4). If we performed a round
		 * in which the number of modified vertices was too large, we avoid this optimization for 1 rounds, if it fail
		 * in the next time the optimization is performed, we avoid it for 2 rounds, than 4 rounds, 8, ect. The speedup
		 * in some cases can be up to 100x (for example RecursiveMatrix(0.57, 0.19, 0.19, 0.05)), and the slowdown in
		 * the worst case is negligible.
		 *
		 * TODO Yen's improvement: https://en.wikipedia.org/wiki/Yen%27s_algorithm
		 */

		final int n = g.vertices().size();
		ShortestPathSingleSourceUtils.IndexResult res = new ShortestPathSingleSourceUtils.IndexResult(g, source);
		res.distances[source] = 0;

		FIFOQueueIntNoReduce modified = new FIFOQueueIntNoReduce();
		Bitmap isModified = new Bitmap(n);
		isModified.set(source);
		modified.enqueue(source);
		for (int nextOptRound = 0, nextOptGap = 1, round = 0; round < n; round++) {
			if (modified.isEmpty()) {
				if (round != nextOptRound) {
					/* classical by-the-book Bellman-Ford */
					for (int e : range(g.edges().size())) {
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						double d = res.distances[u] + w.weight(e);
						if (d < res.distances[v]) {
							res.distances[v] = d;
							res.backtrack[v] = e;
						}
					}
				} else {
					/* identical to the classic implementation, but we record the vertices we update */
					for (int e : range(g.edges().size())) {
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						double d = res.distances[u] + w.weight(e);
						if (d < res.distances[v]) {
							res.distances[v] = d;
							res.backtrack[v] = e;
							if (!isModified.get(v)) {
								isModified.set(v);
								modified.enqueue(v);
							}
						}
					}
					if (modified.isEmpty())
						break; /* no vertices were updated, we are done */
					if (modified.size() <= n / 4) {
						/* few vertices were updated, seems worth it, perform the same optimization next round */
						nextOptGap = 1;
					} else {
						/* too many vertices were updated, avoid this optimization */
						nextOptGap *= 2;
					}
					nextOptRound = round + nextOptGap;
				}
			} else {
				for (int u : modified)
					isModified.clear(u);
				if (round != nextOptRound) {
					/* Iterate only over the vertices that were updated last round */
					/* (not the classical implementation) */
					for (int k = modified.size(); k-- > 0;) {
						int u = modified.dequeueInt();
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int v = eit.targetInt();
							double d = res.distances[u] + w.weight(e);
							if (d < res.distances[v]) {
								res.distances[v] = d;
								res.backtrack[v] = e;
							}
						}
					}
				} else {
					/* Iterate only over the vertices that were updated last round, and record the vertices we update */
					/* (not the classical implementation) */
					for (int k = modified.size(); k-- > 0;) {
						int u = modified.dequeueInt();
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int v = eit.targetInt();
							double d = res.distances[u] + w.weight(e);
							if (d < res.distances[v]) {
								res.distances[v] = d;
								res.backtrack[v] = e;
								if (!isModified.get(v)) {
									isModified.set(v);
									modified.enqueue(v);
								}
							}
						}
					}
					if (modified.isEmpty())
						break; /* no vertices were updated, we are done */
					if (modified.size() <= n / 4) {
						/* few vertices were updated, seems worth it, perform the same optimization next round */
						nextOptGap = 1;
					} else {
						/* too many vertices were updated, avoid this optimization */
						nextOptGap *= 2;
					}
					nextOptRound = round + nextOptGap;
				}
			}
		}

		/* search for a negative cycle */
		for (int v : range(n)) {
			int e = res.backtrack[v];
			if (e < 0)
				continue;
			int u = g.edgeSource(e);
			double d = res.distances[u] + w.weight(e);
			if (d < res.distances[v]) {
				/* negative cycle found */

				Bitmap visited = new Bitmap(n);
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
				throw new NegativeCycleException(g, IPath.valueOf(g, u, u, negCycle));
			}
		}

		return res;
	}

}
