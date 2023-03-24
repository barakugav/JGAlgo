package com.ugav.algo;

import java.util.Arrays;

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
		double[] distances = new double[n];
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		if (n == 0)
			return Result.success(g, distances, backtrack);

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[source] = 0;

		for (int i = 0; i < n - 1; i++) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double d = distances[u] + w.weight(e);
				if (d < distances[v]) {
					distances[v] = d;
					backtrack[v] = e;
				}
			}
		}

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			double d = distances[u] + w.weight(e);
			if (d < distances[v])
				return Result.negCycle(g);
		}

		return Result.success(g, distances, backtrack);
	}

	private static class Result extends SSSPResultsImpl {

		private final boolean negCycle;

		private Result(Graph g, double[] distances, int[] backtrack, boolean negCycle) {
			super(g, distances, backtrack);
			this.negCycle = negCycle;
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
			else
				throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return negCycle ? "[NegCycle]" : super.toString();
		}

		static Result success(Graph g, double[] distances, int[] backtrack) {
			return new Result(g, distances, backtrack, false);
		}

		static Result negCycle(Graph g) {
			return new Result(g, null, null, true);
		}

	}

}
