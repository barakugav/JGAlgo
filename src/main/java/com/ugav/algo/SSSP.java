package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;

/* Single Source Shortest Path */
public interface SSSP {

	public Result calcDistances(Graph g, WeightFunction w, int source);

	public static interface Result {

		public double distance(int v);

		public IntList getPathTo(int v);

		public boolean foundNegativeCycle();

		public IntList getNegativeCycle();

	}

	static class SSSPResultsImpl implements Result {

		private final Graph g;
		private final double[] distances;
		private final int[] backtrack;

		SSSPResultsImpl(Graph g, double[] distances, int[] backtrack) {
			this.g = g;
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int v) {
			return distances[v];
		}

		@Override
		public IntList getPathTo(int v) {
			if (distances[v] == Double.POSITIVE_INFINITY)
				return null;
			IntArrayList path = new IntArrayList();
			if (g instanceof DiGraph) {
				for (;;) {
					int e = backtrack[v];
					if (e == -1)
						break;
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				UGraph g = (UGraph) this.g;
				for (;;) {
					int e = backtrack[v];
					if (e == -1)
						break;
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return path;
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public IntList getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return Arrays.toString(distances);
		}

	}

}
