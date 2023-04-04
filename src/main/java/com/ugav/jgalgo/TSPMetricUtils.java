package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class TSPMetricUtils {

	private TSPMetricUtils() {
	}

	static Path calcEulerianTourAndConvertToHamiltonianCycle(UGraph g, UGraph g1, Weights.Int edgeRef) {
		int n = g.vertices().size();

		/* Assert degree is actually even in the new graph */
		for (int u = 0; u < n; u++)
			assert g1.degree(u) % 2 == 0;

		/* Calculate Eulerian tour in the new graph */
		Path tour = EulerianTour.calcTour(g1);
		assert isValidCycle(g1, tour);
		assert isPathVisitEvery(g1, tour);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		int firstVertex = -1, lastVertex = -1;
		boolean[] visited = new boolean[n];
		for (EdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e0 = it.nextInt();
			int e = edgeRef.getInt(e0);
			final int u = it.u();
			if (firstVertex == -1)
				firstVertex = u;
			visited[u] = true;
			while (visited[it.v()] && it.hasNext()) {
				it.nextInt();
				e = g.getEdge(u, it.v());
			}
			cycle.add(e);
			lastVertex = it.v();
		}

		assert firstVertex == lastVertex;
		Path cycle0 = new Path(g, firstVertex, lastVertex, cycle);
		assert isValidCycle(g, cycle0);
		assert isPathVisitEvery(g, cycle0);
		return cycle0;
	}

	static IntList pathToVerticesList(Path edges) {
		IntList res = new IntArrayList();
		for (EdgeIter it = edges.edgeIter(); it.hasNext();) {
			it.nextInt();
			res.add(it.u());
		}
		return res;
	}

	private static boolean isValidCycle(UGraph g, Path path) {
		EdgeIter it = path.edgeIter();
		it.nextInt();
		final int begin = it.u();
		for (;;) {
			if (!it.hasNext())
				return it.v() == begin;
			int lastV = it.v();
			it.nextInt();
			if (lastV != it.u())
				return false;
		}
	}

	private static boolean isPathVisitEvery(UGraph g, Path path) {
		final int n = g.vertices().size();
		boolean[] visited = new boolean[n];
		for (IntIterator it = path.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			visited[u] = visited[v] = true;
		}
		for (int u = 0; u < n; u++)
			if (!visited[u])
				return false;
		return true;
	}

}
