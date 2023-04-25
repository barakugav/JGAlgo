package com.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Connectivity components calculations.
 *
 * @author Barak Ugav
 */
public class Connectivity {

	private Connectivity() {
	}

	/**
	 * Find all connectivity components in an undirected graph.
	 * <p>
	 * A connectivity component is a maximal set of vertices for which for any pair
	 * of vertices {@code u, v} in the set there exist a path from {@code u} to
	 * {@code v} (and from {@code v} to {@code u}, as the graph is undirected).
	 * <p>
	 * This function runs in linear time.
	 *
	 * @param g an undirected graph
	 * @return a result object containing the partition of the vertices to
	 *         connectivity components
	 */
	public static Connectivity.Result findConnectivityComponents(UGraph g) {
		int n = g.vertices().size();
		IntStack stack = new IntArrayList();

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;

			final int compIdx = compNum++;
			stack.push(root);
			comp[root] = compIdx;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (comp[v] != -1) {
						assert comp[v] == compIdx;
						continue;
					}
					comp[v] = compIdx;
					stack.push(v);
				}
			}
		}
		return new Connectivity.Result(compNum, comp);
	}

	/**
	 * Find all strongly connected components in a directed graph.
	 * <p>
	 * A strongly connected component is a maximal set of vertices for which for any
	 * pair of vertices {@code u, v} in the set there exist a path from {@code u} to
	 * {@code v} and from {@code v} to {@code u}.
	 * <p>
	 * This function runs in linear time.
	 *
	 * @param g a directed graph
	 * @return a result object containing the partition of the vertices to
	 *         strongly connected components
	 */
	public static Connectivity.Result findStrongConnectivityComponents(DiGraph g) {
		// implementation of Tarjan's strongly connected components algorithm
		// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

		int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		int[] dfsPath = new int[n];
		EdgeIter[] edges = new EdgeIter[n];

		int[] c = new int[n];
		IntStack s = new IntArrayList();
		IntStack p = new IntArrayList();
		int cNext = 1;

		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;
			dfsPath[0] = root;
			edges[0] = g.edgesOut(root);
			c[root] = cNext++;
			s.push(root);
			p.push(root);

			dfs: for (int depth = 0;;) {
				for (EdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (c[v] == 0) {
						c[v] = cNext++;
						s.push(v);
						p.push(v);

						dfsPath[++depth] = v;
						edges[depth] = g.edgesOut(v);
						continue dfs;
					} else if (comp[v] == -1)
						while (c[p.topInt()] > c[v])
							p.popInt();
				}
				int u = dfsPath[depth];
				if (p.topInt() == u) {
					int v;
					do {
						v = s.popInt();
						comp[v] = compNum;
					} while (v != u);
					compNum++;
					p.popInt();
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return new Connectivity.Result(compNum, comp);
	}

	/**
	 * Result object for connectivity components calculation.
	 * <p>
	 * The result object contains the partition of the vertices into the
	 * connectivity components (strongly for directed graph). Each connectivity
	 * component (CC) is assigned a unique integer number in range [0, ccNum), and
	 * each vertex can be queried for its CC using {@link #getVertexCc(int)}.
	 *
	 * @author Barak Ugav
	 */
	public static class Result {
		private final int ccNum;
		private final int[] vertexToCC;

		private Result(int ccNum, int[] vertexToCC) {
			this.ccNum = ccNum;
			this.vertexToCC = vertexToCC;
		}

		/**
		 * Get the connectivity component containing a vertex.
		 *
		 * @param v a vertex in the graph
		 * @return index of the connectivity component containing the vertex, in range
		 *         [0, ccNum)
		 */
		public int getVertexCc(int v) {
			return vertexToCC[v];
		}

		/**
		 * Get the number of connectivity components in the graph.
		 *
		 * @return the number of connectivity components in the graph, non negative
		 *         number
		 */
		public int getNumberOfCC() {
			return ccNum;
		}

		@Override
		public String toString() {
			return Arrays.toString(vertexToCC);
		}
	}

}
