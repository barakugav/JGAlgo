package com.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

public class Connectivity {

	/**
	 * Find all connectivity components in the graph
	 *
	 * The connectivity components (CC) are groups of vertices where it's possible
	 * to reach each one from one another.
	 *
	 * This function support undirected graphs only
	 *
	 * @param g an undirected graph
	 * @return (CC number, [vertex] to [CC])
	 * @throws IllegalArgumentException if the graph is directed
	 */
	public static Connectivity.Result findConnectivityComponents(UGraph g) {
		int n = g.vertices().size();
		IntStack stack = new IntArrayList();

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;

			stack.push(r);
			comp[r] = compNum;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (comp[v] != -1)
						continue;
					comp[v] = compNum;
					stack.push(v);
				}
			}
			compNum++;
		}
		return new Result(compNum, comp);
	}

	/**
	 * Find all strong connectivity components
	 *
	 * The connectivity components (CC) are groups of vertices where it's possible
	 * to reach each one from one another.
	 *
	 * This function is specifically for directed graphs.
	 *
	 * @param g a directed graph
	 * @return (CC number, [vertex] to [CC])
	 */
	public static Connectivity.Result findStrongConnectivityComponents(DiGraph g) {
		int n = g.vertices().size();

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		int[] dfsPath = new int[n];
		EdgeIter[] edges = new EdgeIter[n];

		int[] c = new int[n];
		int[] s = new int[n];
		int[] p = new int[n];
		int cNext = 1, sSize = 0, pSize = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;
			dfsPath[0] = r;
			edges[0] = g.edgesOut(r);
			c[r] = cNext++;
			s[sSize++] = p[pSize++] = r;

			dfs: for (int depth = 0;;) {
				for (EdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (c[v] == 0) {
						c[v] = cNext++;
						s[sSize++] = p[pSize++] = v;

						dfsPath[++depth] = v;
						edges[depth] = g.edgesOut(v);
						continue dfs;
					} else if (comp[v] == -1)
						while (c[p[pSize - 1]] > c[v])
							pSize--;
				}
				int u = dfsPath[depth];
				if (p[pSize - 1] == u) {
					int v;
					do {
						v = s[--sSize];
						comp[v] = compNum;
					} while (v != u);
					compNum++;
					pSize--;
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return new Result(compNum, comp);
	}

	public static class Result {
		public int ccNum;
		public int[] vertexToCC;

		public Result(int ccNum, int[] vertexToCC) {
			this.ccNum = ccNum;
			this.vertexToCC = vertexToCC;
		}

		public int getVertexCcIndex(int v) {
			return vertexToCC[v];
		}
	}

}
