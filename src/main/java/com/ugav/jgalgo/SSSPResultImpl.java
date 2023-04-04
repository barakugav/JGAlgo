package com.ugav.jgalgo;

import java.util.Arrays;

import com.ugav.jgalgo.SSSP.Result;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

class SSSPResultImpl implements Result {

	private final Graph g;
	private final int source;
	final double[] distances;
	final int[] backtrack;

	SSSPResultImpl(Graph g, int source) {
		this.g = g;
		this.source = source;
		int n = g.vertices().size();
		distances = new double[n];
		backtrack = new int[n];
		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		Arrays.fill(backtrack, -1);
	}

	@Override
	public double distance(int target) {
		return distances[target];
	}

	@Override
	public Path getPathTo(int target) {
		if (distances[target] == Double.POSITIVE_INFINITY)
			return null;
		IntArrayList path = new IntArrayList();
		if (g instanceof DiGraph) {
			for (int v = target;;) {
				int e = backtrack[v];
				if (e == -1)
					break;
				path.add(e);
				v = g.edgeSource(e);
			}
		} else {
			UGraph g = (UGraph) this.g;
			for (int v = target;;) {
				int e = backtrack[v];
				if (e == -1)
					break;
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
		}
		IntArrays.reverse(path.elements(), 0, path.size());
		return new Path(g, source, target, path);
	}

	@Override
	public boolean foundNegativeCycle() {
		return false;
	}

	@Override
	public Path getNegativeCycle() {
		throw new IllegalStateException("no negative cycle found");
	}

	@Override
	public String toString() {
		return Arrays.toString(distances);
	}

	static class Int implements SSSP.Result {

		private final Graph g;
		private final int source;
		final int[] distances;
		final int[] backtrack;

		Int(Graph g, int source) {
			this.g = g;
			this.source = source;
			int n = g.vertices().size();
			distances = new int[n];
			backtrack = new int[n];
			Arrays.fill(distances, Integer.MAX_VALUE);
			Arrays.fill(backtrack, -1);
		}

		@Override
		public double distance(int target) {
			int d = distances[target];
			return d != Integer.MAX_VALUE ? d : Double.POSITIVE_INFINITY;
		}

		@Override
		public Path getPathTo(int target) {
			if (distances[target] == Integer.MAX_VALUE)
				return null;
			IntArrayList path = new IntArrayList();
			for (int v = target;;) {
				int e = backtrack[v];
				if (e == -1)
					break;
				path.add(e);
				v = g.edgeEndpoint(e, v);
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return new Path(g, source, target, path);
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return Arrays.toString(distances);
		}
	}

}
