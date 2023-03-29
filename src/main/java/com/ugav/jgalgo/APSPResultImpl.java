package com.ugav.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

abstract class APSPResultImpl implements APSP.Result {

	private APSPResultImpl() {
	}

	abstract void setDistance(int source, int target, double distance);

	abstract int getEdgeTo(int source, int target);

	abstract void setEdgeTo(int source, int target, int edge);

	private static abstract class Abstract extends APSPResultImpl {

		private final int[][] edges;

		Abstract(Graph g) {
			int n = g.vertices().size();
			edges = new int[n][n];
			for (int v = 0; v < n; v++) {
				Arrays.fill(edges[v], -1);
			}
		}

		@Override
		int getEdgeTo(int source, int target) {
			return edges[source][target];
		}

		@Override
		void setEdgeTo(int source, int target, int edge) {
			edges[source][target] = edge;
		}
	}

	static class Undirected extends Abstract {

		private final UGraph g;
		private final int n;
		private final double[] distances;

		Undirected(UGraph g) {
			super(g);
			this.g = g;
			n = g.vertices().size();
			distances = new double[n * (n - 1) / 2];
			Arrays.fill(distances, Double.POSITIVE_INFINITY);
		}

		private int index(int u, int v) {
			assert u != v;
			if (u > v) {
				int temp = u;
				u = v;
				v = temp;
			}
			/* index mapping assume always u < v (strictly!) */
			return (2 * n - u - 1) * u / 2 + v - u - 1;
		}

		@Override
		void setDistance(int source, int target, double distance) {
			distances[index(source, target)] = distance;
		}

		@Override
		public double distance(int source, int target) {
			return source != target ? distances[index(source, target)] : 0;
		}

		@Override
		public IntList getPath(int source, int target) {
			if (distance(source, target) == Double.POSITIVE_INFINITY)
				return null;
			IntList path = new IntArrayList();
			while (source != target) {
				int e = getEdgeTo(source, target);
				assert e != -1;
				path.add(e);
				source = g.edgeEndpoint(e, source);
			}
			return path;
		}

	}

	static class Directed extends Abstract {
		private final DiGraph g;
		private final double[][] distances;

		Directed(DiGraph g) {
			super(g);
			this.g = g;
			int n = g.vertices().size();
			distances = new double[n][n];
			for (int v = 0; v < n; v++) {
				Arrays.fill(distances[v], Double.POSITIVE_INFINITY);
				setDistance(v, v, 0);
			}
		}

		@Override
		void setDistance(int source, int target, double distance) {
			distances[source][target] = distance;
		}

		@Override
		public double distance(int source, int target) {
			return distances[source][target];
		}

		@Override
		public IntList getPath(int source, int target) {
			if (distance(source, target) == Double.POSITIVE_INFINITY)
				return null;
			IntList path = new IntArrayList();
			while (source != target) {
				int e = getEdgeTo(source, target);
				assert e != -1;
				assert source == g.edgeSource(e);
				path.add(e);
				source = g.edgeTarget(e);
			}
			return path;
		}
	}

}
