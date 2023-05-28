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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * The Floyd Warshall algorithm for all pairs shortest path.
 * <p>
 * Calculate the shortest path between each pair of vertices in a graph in \(O(n^3)\) time using \(O(n^2)\) space.
 * Negative weights are supported.
 *
 * @author Barak Ugav
 */
class APSPFloydWarshall implements APSP {

	/**
	 * Create a new APSP algorithm object.
	 */
	APSPFloydWarshall() {}

	@Override
	public APSP.Result computeAllShortestPaths(Graph g, EdgeWeightFunc w) {
		if (w == null)
			w = EdgeWeightFunc.CardinalityEdgeWeightFunction;
		return g.getCapabilities().directed() ? computeAPSPDirected(g, w) : computeAPSPUndirected(g, w);
	}

	private static APSP.Result computeAPSPUndirected(Graph g, EdgeWeightFunc w) {
		APSPResultImpl.Abstract res = new APSPResultImpl.Undirected(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double ew = w.weight(e);
			if (u == v) {
				if (ew < 0) {
					res.setNegCycle(new Path(g, u, u, IntList.of(e)));
					return res;
				}
				continue;
			}
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
				res.setEdgeTo(v, u, e);
			}
		}
		int n = g.vertices().size();
		for (int k = 0; k < n; k++) {
			/* Calc shortest path between each pair (u,v) by using vertices 1,2,..,k */
			for (int u = 0; u < n; u++) {
				for (int v = u + 1; v < n; v++) {
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
			if (detectNegCycle(res, n, k))
				return res;
		}
		return res;
	}

	private static APSP.Result computeAPSPDirected(Graph g, EdgeWeightFunc w) {
		APSPResultImpl.Abstract res = new APSPResultImpl.Directed(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double ew = w.weight(e);
			if (u == v) {
				if (ew < 0) {
					res.setNegCycle(new Path(g, u, u, IntList.of(e)));
					return res;
				}
				continue;
			}
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
			}
		}
		int n = g.vertices().size();
		for (int k = 0; k < n; k++) {
			/* Calc shortest path between each pair (u,v) by using vertices 1,2,..,k */
			for (int u = 0; u < n; u++) {
				for (int v = 0; v < n; v++) {
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
			if (detectNegCycle(res, n, k))
				return res;
		}
		return res;
	}

	private static boolean detectNegCycle(APSPResultImpl.Abstract res, int n, int k) {
		for (int u = 0; u < n; u++) {
			double d1 = res.distance(u, k);
			double d2 = res.distance(k, u);
			if (d1 == Double.POSITIVE_INFINITY || d2 == Double.POSITIVE_INFINITY)
				continue;
			if (d1 + d2 < 0) {
				IntList negCycle = new IntArrayList();
				negCycle.addAll(res.getPath(u, k));
				negCycle.addAll(res.getPath(k, u));
				res.setNegCycle(new Path(res.graph(), u, u, negCycle));
				return true;
			}
		}
		return false;
	}

}
