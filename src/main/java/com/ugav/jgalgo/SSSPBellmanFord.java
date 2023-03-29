package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class SSSPBellmanFord implements SSSP {

	/*
	 * O(m n)
	 */

	public SSSPBellmanFord() {
	}

	@Override
	public SSSP.Result calcDistances(Graph g0, EdgeWeightFunc w, int source) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		DiGraph g = (DiGraph) g0;
		int n = g.vertices().size();
		Result res = new Result(g);
		if (n == 0)
			return res;
		res.distances[source] = 0;

		for (int i = 0; i < n - 1; i++) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			double d = res.distances[u] + w.weight(e);
			if (d < res.distances[v]) {
				res.negCycle = true;
				return res;
			}
		}

		return res;
	}

	private static class Result extends SSSPResultImpl {

		private boolean negCycle;

		private Result(Graph g) {
			super(g);
		}

		@Override
		public double distance(int v) {
			if (negCycle)
				throw new IllegalStateException();
			return super.distance(v);
		}

		@Override
		public IntList getPathTo(int v) {
			if (negCycle)
				throw new IllegalStateException();
			return super.getPathTo(v);
		}

		@Override
		public boolean foundNegativeCycle() {
			return negCycle;
		}

		@Override
		public IntList getNegativeCycle() {
			if (negCycle)
				throw new UnsupportedOperationException();
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return negCycle ? "[NegCycle]" : super.toString();
		}

	}

}
