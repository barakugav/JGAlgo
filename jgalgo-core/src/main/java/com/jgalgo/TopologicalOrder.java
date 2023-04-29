package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Algorithm that calculate a topological order of graph vertices.
 * <p>
 * A topological ordering of a directed graph is a linear ordering of its
 * vertices such that for every directed edge {@code (u, v)}, {@code u} comes
 * before {@code v} in the ordering. A topological ordering exist if and only if
 * the graph is directed and acyclic (DAG).
 * <p>
 * This algorithm compute the topological ordering of a given DAG graph in
 * linear time and space.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Topological_sorting">Wikipedia</a>
 * @author Barak Ugav
 */
public class TopologicalOrder {
	private TopologicalOrder() {
	}

	/**
	 * Compute the topological order of a DAG vertices.
	 * <p>
	 * This function runs in linear time.
	 *
	 * @param g a directed acyclic graph (DAG).
	 * @return an array of size {@code n} with the vertices of the graph order in
	 *         the topological order.
	 * @throws IllegalArgumentException if the graph is not DAG
	 */
	public static int[] computeTopologicalSortingDAG(DiGraph g) {
		int n = g.vertices().size();
		int[] inDegree = new int[n];
		IntPriorityQueue queue = new IntArrayFIFOQueue();
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// calc in degree of all vertices
		for (int v = 0; v < n; v++)
			inDegree[v] = g.degreeIn(v);

		// Find vertices with zero in degree and insert them to the queue
		for (int v = 0; v < n; v++)
			if (inDegree[v] == 0)
				queue.enqueue(v);

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			topolSort[topolSortSize++] = u;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (--inDegree[v] == 0)
					queue.enqueue(v);
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return topolSort;
	}

}
