package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class APSPFloydWarshall implements APSP {

	/**
	 * Compute all pairs shortest paths in O(n^3)
	 */

	@Override
	public APSP.Result calcDistances(Graph g, EdgeWeightFunc w) {
		return g instanceof DiGraph ? calcDistancesDirected((DiGraph) g, w) : calcDistancesUndirected((UGraph) g, w);
	}

	private static APSP.Result calcDistancesUndirected(UGraph g, EdgeWeightFunc w) {
		APSPResultImpl.Abstract res = new APSPResultImpl.Undirected(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u == v)
				continue;
			double ew = w.weight(e);
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
		}
		detectNegCycle(res, n);
		return res;
	}

	private static APSP.Result calcDistancesDirected(DiGraph g, EdgeWeightFunc w) {
		APSPResultImpl.Abstract res = new APSPResultImpl.Directed(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u == v)
				continue;
			double ew = w.weight(e);
			if (ew < res.distance(u, v)) {
				res.setDistance(u, v, ew);
				res.setEdgeTo(u, v, e);
			}
		}
		;
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
		}
		detectNegCycle(res, n);
		return res;
	}

	private static void detectNegCycle(APSPResultImpl.Abstract res, int n) {
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				if (u == v)
					continue;
				double d1 = res.distance(u, v);
				double d2 = res.distance(v, u);
				if (d1 == Double.POSITIVE_INFINITY || d2 == Double.POSITIVE_INFINITY)
					continue;
				if (d1 + d2 < 0) {
					IntList negCycle = new IntArrayList();
					negCycle.addAll(res.getPath(u, v));
					negCycle.addAll(res.getPath(v, u));
					res.setNegCycle(new Path(res.graph(), u, u, negCycle));
					return;
				}
			}
		}
	}

}
