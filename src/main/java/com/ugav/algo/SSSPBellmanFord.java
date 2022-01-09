package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class SSSPBellmanFord implements SSSP {

	/*
	 * O(mn)
	 */

	private SSSPBellmanFord() {
	}

	private static final SSSPBellmanFord INSTANCE = new SSSPBellmanFord();

	public static SSSPBellmanFord getInstace() {
		return INSTANCE;
	}

	@Override
	public <E> SSSP.Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int s) {
		if (!g.isDirected())
			throw new IllegalArgumentException("only directed graphs are supported");
		int n = g.vertices();
		double[] distances = new double[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];

		if (n == 0)
			return Result.success(distances, backtrack);

		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[s] = 0;

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

	private static class Result<E> implements SSSP.Result<E> {

		private final double[] distances;
		private final Edge<E>[] backtrack;
		private final boolean negCycle;

		private Result(double[] distances, Edge<E>[] backtrack, boolean negCycle) {
			this.distances = distances;
			this.backtrack = backtrack;
			this.negCycle = negCycle;
		}

		@Override
		public double distance(int t) {
			return distances[t];
		}

		@Override
		public List<Edge<E>> getPathTo(int t) {
			List<Edge<E>> path = new ArrayList<>();
			for (int v = t;;) {
				Edge<E> e = backtrack[v];
				if (e == null)
					break;
				path.add(e);
				v = e.u();
			}
			Collections.reverse(path);
			return path;
		}

		@Override
		public boolean foundNegativeCircle() {
			return negCycle;
		}

		@Override
		public List<Edge<E>> getNegativeCircle() {
			if (negCycle)
				throw new UnsupportedOperationException();
			else
				throw new IllegalStateException("no negative circle found");
		}

		@Override
		public String toString() {
			return negCycle ? "[NegCycle]" : Arrays.toString(distances);
		}

		static <E> Result<E> success(double[] distances, Edge<E>[] backtrack) {
			return new Result<>(distances, backtrack, false);
		}

		static <E> Result<E> negCycle() {
			return new Result<>(null, null, true);
		}

	}

}
