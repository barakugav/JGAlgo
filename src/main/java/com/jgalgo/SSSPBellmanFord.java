package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;

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
		Result res = new Result(g, source);
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

		private Result(Graph g, int source) {
			super(g, source);
		}

		@Override
		public double distance(int target) {
			if (negCycle)
				throw new IllegalStateException();
			return super.distance(target);
		}

		@Override
		public Path getPathTo(int target) {
			if (negCycle)
				throw new IllegalStateException();
			return super.getPathTo(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return negCycle;
		}

		@Override
		public Path getNegativeCycle() {
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
