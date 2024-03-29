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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * The Floyd Warshall algorithm for all pairs shortest path.
 *
 * <p>
 * Calculate the shortest path between each pair of vertices in a graph in \(O(n^3)\) time using \(O(n^2)\) space.
 * Negative weights are supported.
 *
 * @author Barak Ugav
 */
class ShortestPathAllPairsFloydWarshall extends ShortestPathAllPairsUtils.AbstractImpl {

	/**
	 * Create a new APSP algorithm object.
	 */
	ShortestPathAllPairsFloydWarshall() {}

	@Override
	ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w) {
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);
		return g.isDirected() ? computeAPSPDirected(g, w) : computeAPSPUndirected(g, w);
	}

	@Override
	ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w) {
		return computeAllShortestPaths(g, w);
	}

	private static ShortestPathAllPairs.IResult computeAPSPUndirected(IndexGraph g, IWeightFunction w) {
		ShortestPathAllPairsUtils.IndexResult.AllVertices res = new ShortestPathAllPairsUtils.IndexResult.Undirected(g);
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double ew = w.weight(e);
			if (ew < 0) {
				if (u == v) {
					throw new NegativeCycleException(g, IPath.valueOf(g, u, u, Fastutil.list(e)));
				} else {
					throw new NegativeCycleException(g, IPath.valueOf(g, u, u, Fastutil.list(e, e)));
				}
			}
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
				res.setEdgeTo(v, u, e);
			}
		}
		int n = g.vertices().size();
		for (int k : range(n)) {
			/* Calc shortest path between each pair (u,v) by using vertices 1,2,..,k */
			for (int u : range(n)) {
				for (int v : range(u + 1, n)) {
					double duk = res.distance(u, k);
					if (duk == Double.POSITIVE_INFINITY)
						continue;

					double dkv = res.distance(k, v);
					if (dkv == Double.POSITIVE_INFINITY)
						continue;

					double dis = duk + dkv;
					if (dis < res.distance(u, v)) {
						res.setDistance(u, v, dis);
						res.setEdgeTo(u, v, res.getEdgeTo(u, k));
						res.setEdgeTo(v, u, res.getEdgeTo(v, k));
					}
				}
			}
		}
		return res;
	}

	private static ShortestPathAllPairs.IResult computeAPSPDirected(IndexGraph g, IWeightFunction w) {
		ShortestPathAllPairsUtils.IndexResult.AllVertices res = new ShortestPathAllPairsUtils.IndexResult.Directed(g);
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double ew = w.weight(e);
			if (u == v) {
				if (ew < 0)
					throw new NegativeCycleException(g, IPath.valueOf(g, u, u, Fastutil.list(e)));
				continue;
			}
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
			}
		}
		int n = g.vertices().size();
		for (int k : range(n)) {
			/* Calc shortest path between each pair (u,v) by using vertices 1,2,..,k */
			for (int u : range(n)) {
				for (int v : range(n)) {
					double duk = res.distance(u, k);
					if (duk == Double.POSITIVE_INFINITY)
						continue;
					double dkv = res.distance(k, v);
					if (dkv == Double.POSITIVE_INFINITY)
						continue;
					double dis = duk + dkv;
					if (dis < res.distance(u, v)) {
						res.setDistance(u, v, dis);
						res.setEdgeTo(u, v, res.getEdgeTo(u, k));
					}
				}
			}
			detectNegCycle(res, n, k);
		}
		return res;
	}

	private static void detectNegCycle(ShortestPathAllPairsUtils.IndexResult.AllVertices res, int n, int k) {
		for (int u : range(n)) {
			double d1 = res.distance(u, k);
			double d2 = res.distance(k, u);
			if (d1 == Double.POSITIVE_INFINITY || d2 == Double.POSITIVE_INFINITY)
				continue;
			if (d1 + d2 < 0) {
				IntList negCycle = new IntArrayList();
				negCycle.addAll(res.getPath(u, k).edges());
				negCycle.addAll(res.getPath(k, u).edges());
				throw new NegativeCycleException(res.g, IPath.valueOf(res.g, u, u, negCycle));
			}
		}
	}

}
