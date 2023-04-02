package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class TSPMetricUtils {

	private TSPMetricUtils() {
	}

	static IntList calcEulerianTourAndConvertToHamiltonianCycle(UGraph g, UGraph g1, Weights.Int edgeRef) {
		int n = g.vertices().size();

		/* Assert degree is actually even in the new graph */
		for (int u = 0; u < n; u++)
			assert g1.degree(u) % 2 == 0;

		/* Calculate Eulerian tour in the new graph */
		IntList tour = EulerianTour.calcTour(g1);
		assert isValidCycle(g1, tour);
		assert isPathVisitEvery(g1, tour);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		boolean[] visited = new boolean[n];
		for (PathIter it = PathIter.of(g1, tour); it.hasNext();) {
			int e0 = it.nextEdge();
			int e = edgeRef.getInt(e0);
			final int u = it.u();
			visited[u] = true;
			while (visited[it.v()] && it.hasNext()) {
				it.nextEdge();
				e = g.getEdge(u, it.v());
			}
			cycle.add(e);
		}
		assert isValidCycle(g, cycle);
		assert isPathVisitEvery(g, cycle);

		return cycle;
	}

	static IntList edgeListToVerticesList(UGraph g, IntList edges) {
		IntList res = new IntArrayList();
		for (PathIter it = PathIter.of(g, edges); it.hasNext();) {
			it.nextEdge();
			res.add(it.u());
		}
		return res;
	}

	private static boolean isValidCycle(UGraph g, IntList path) {
		PathIter it = PathIter.of(g, path);
		it.nextEdge();
		final int begin = it.u();
		for (;;) {
			if (!it.hasNext())
				return it.v() == begin;
			int lastV = it.v();
			it.nextEdge();
			if (lastV != it.u())
				return false;
		}
	}

	private static boolean isPathVisitEvery(UGraph g, IntList path) {
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
