package com.ugav.algo;

import java.util.Arrays;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class SSSPBellmanFord implements SSSP {

	/*
	 * O(m n)
	 */

	public SSSPBellmanFord() {
	}

	@Override
	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int source) {
		if (!g.isDirected())
			throw new IllegalArgumentException("only directed graphs are supported");
		int n = g.vertices();
		double[] distances = new double[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];

		if (n == 0)
			return Result.success(distances, backtrack);

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[source] = 0;

		for (int i = 0; i < n - 1; i++) {
			for (Edge<E> e : g.edges()) {
				int u = e.u(), v = e.v();
				double d = distances[u] + w.weight(e);
				if (d < distances[v]) {
					distances[v] = d;
					backtrack[v] = e;
				}
			}
		}

		for (Edge<E> e : g.edges()) {
			int u = e.u(), v = e.v();
			double d = distances[u] + w.weight(e);
			if (d < distances[v])
				return Result.negCycle();
		}

		return Result.success(distances, backtrack);
	}

	private static class Result<E> extends SSSPResultsImpl<E> {

		private final boolean negCycle;

		private Result(double[] distances, Edge<E>[] backtrack, boolean negCycle) {
			super(distances, backtrack);
			this.negCycle = negCycle;
		}

		@Override
		public double distance(int v) {
			if (negCycle)
				throw new IllegalStateException();
			return super.distance(v);
		}

		@Override
		public List<Edge<E>> getPathTo(int v) {
			if (negCycle)
				throw new IllegalStateException();
			return super.getPathTo(v);
		}

		@Override
		public boolean foundNegativeCycle() {
			return negCycle;
		}

		@Override
		public List<Edge<E>> getNegativeCycle() {
			if (negCycle)
				throw new UnsupportedOperationException();
			else
				throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return negCycle ? "[NegCycle]" : super.toString();
		}

		static <E> Result<E> success(double[] distances, Edge<E>[] backtrack) {
			return new Result<>(distances, backtrack, false);
		}

		static <E> Result<E> negCycle() {
			return new Result<>(null, null, true);
		}

	}

}
