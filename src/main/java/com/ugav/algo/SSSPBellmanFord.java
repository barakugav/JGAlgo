package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class SSSPBellmanFord implements SSSP {

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
			return new Result<>(distances, backtrack);

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
				return new ResultNegative<>();
		}

		return new Result<>(distances, backtrack);
	}

	private static class Result<E> implements SSSP.Result<E> {

		private final double[] distances;
		private final Edge<E>[] backtrack;

		Result(double[] distances, Edge<E>[] backtrack) {
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int t) {
			return distances[t];
		}

		@Override
		public Collection<Edge<E>> getPathTo(int t) {
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
			return false;
		}

		@Override
		public Collection<Edge<E>> getNegativeCircle() {
			throw new IllegalArgumentException("no negative circle found");
		}

	}

	private static class ResultNegative<E> implements SSSP.Result<E> {

		ResultNegative() {
		}

		@Override
		public double distance(int t) {
			throw new IllegalStateException();
		}

		@Override
		public Collection<Edge<E>> getPathTo(int t) {
			throw new IllegalStateException();
		}

		@Override
		public boolean foundNegativeCircle() {
			return true;
		}

		@Override
		public Collection<Edge<E>> getNegativeCircle() {
			throw new UnsupportedOperationException();
		}

	}

}
