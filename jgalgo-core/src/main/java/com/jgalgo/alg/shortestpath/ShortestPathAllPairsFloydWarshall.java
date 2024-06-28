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

package com.jgalgo.alg.shortestpath;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
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
public class ShortestPathAllPairsFloydWarshall extends ShortestPathAllPairsAbstract {

	/**
	 * Create a APSP algorithm.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathAllPairs#newInstance()} to get a default implementation for the
	 * {@link ShortestPathAllPairs} interface, or {@link ShortestPathAllPairs#builder()} for more customization options.
	 */
	public ShortestPathAllPairsFloydWarshall() {}

	@Override
	protected ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w) {
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);
		final boolean directed = g.isDirected();

		ShortestPathAllPairsAbstract.IndexResult res = new ShortestPathAllPairsAbstract.IndexResult(g);
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double ew = w.weight(e);
			if (ew < 0) {
				if (u == v) {
					throw new NegativeCycleException(g, IPath.valueOf(g, u, u, Fastutil.list(e)));
				} else if (!directed) {
					throw new NegativeCycleException(g, IPath.valueOf(g, u, u, Fastutil.list(e, e)));
				}
			}
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
				if (!directed)
					res.setEdgeTo(v, u, e);
			}
		}
		int n = g.vertices().size();
		for (int k : range(n)) {
			/* Calc shortest path between each pair (u,v) by using vertices 1,2,..,k */
			for (int u : range(n)) {
				for (int v : range(directed ? 0 : u + 1, n)) {
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
						if (!directed)
							res.setEdgeTo(v, u, res.getEdgeTo(v, k));
					}
				}
			}
			if (directed)
				detectNegCycle(res, n, k);
		}
		return res;
	}

	private static void detectNegCycle(ShortestPathAllPairsAbstract.IndexResult res, int n, int k) {
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

	@Override
	protected ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w) {
		return computeAllShortestPaths(g, w);
	}

}
