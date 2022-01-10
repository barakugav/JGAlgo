package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

/* Single Source Shortest Path */
public interface SSSP {

	/**
	 * Calculate the distances from a given source to all other vertices in the
	 * graph
	 *
	 * @param g      a graph
	 * @param w      weight function
	 * @param source a source vertices
	 * @return a result object
	 */
	public <E> Result<E> calcDistances(Graph<E> g, WeightFunction<E> w, int source);

	public static interface Result<E> {

		/**
		 * Get the distance from the source vertex to a given vertex
		 *
		 * This function should be called only if foundNegativeCycle is false.
		 *
		 * @param v target vertex
		 * @return the sum of the weights in the shortest path to the target vertex or
		 *         Double.POSITIVE_INFINITY if no path found.
		 */
		public double distance(int v);

		/**
		 * Get the path from the source to a vertex
		 *
		 * This function should be called only if foundNegativeCycle is false.
		 *
		 * @param v target vertex
		 * @return list of edges that represent the path from source to the given
		 *         vertex. If there is no path, null will be returned.
		 */
		public List<Graph.Edge<E>> getPathTo(int v);

		/**
		 * Checks if the SSSP algorithm found a negative cycle
		 *
		 * @return true if a negative cycle found
		 */
		public boolean foundNegativeCycle();

		/**
		 * Get the negative cycle edges found by the SSSP algorithm
		 *
		 * This function should be called only if foundNegativeCycle is true.
		 *
		 * @return list of edges of the negative cycle found
		 */
		public List<Graph.Edge<E>> getNegativeCycle();

	}

	static class SSSPResultsImpl<E> implements Result<E> {

		private final double[] distances;
		private final Edge<E>[] backtrack;

		SSSPResultsImpl(double[] distances, Edge<E>[] backtrack) {
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int v) {
			return distances[v];
		}

		@Override
		public List<Edge<E>> getPathTo(int v) {
			if (distances[v] == Double.POSITIVE_INFINITY)
				return null;
			List<Edge<E>> path = new ArrayList<>();
			for (;;) {
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
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public List<Edge<E>> getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return Arrays.toString(distances);
		}

	}

}
