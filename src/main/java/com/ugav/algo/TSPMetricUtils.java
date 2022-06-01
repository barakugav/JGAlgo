package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ugav.algo.Graph.Edge;

class TSPMetricUtils {

	private TSPMetricUtils() {
		throw new InternalError();
	}

	static <E> List<Edge<E>> calcEulerianAndConvertToHamiltonianCycle(Graph<E> g, Graph<E> g1) {
		int n = g.vertices();

		/* Assert degree is actually even in the new graph */
		int[] degree = Graphs.calcDegree(g1);
		for (int u = 0; u < n; u++)
			assert degree[u] % 2 == 0;

		/* Calculate Eulerian in the new graph */
		List<Edge<E>> tour = Graphs.calcEulerianTour(g1);
		assertValidCycle(tour);
		assertPathVisitEvery(tour, n);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		List<Edge<E>> cycle = new ArrayList<>(n);
		boolean[] visited = new boolean[n];
		for (int i = 0; i < tour.size(); i++) {
			Edge<E> e = tour.get(i);
			visited[e.u()] = true;
			while (visited[e.v()] && i < tour.size() - 1) {
				Edge<E> next = tour.get(++i);
				e = g.getEdge(e.u(), next.v());
				if (e == null)
					break;
			}
			if (e != null)
				cycle.add(e);
		}
		assertValidCycle(cycle);
		assertPathVisitEvery(cycle, n);

		return cycle;
	}

	private static void assertValidCycle(Collection<? extends Edge<?>> path) {
		Iterator<? extends Edge<?>> it = path.iterator();
		assert it.hasNext();
		Edge<?> begin = it.next();
		for (Edge<?> e = begin;;) {
			if (!it.hasNext()) {
				assert e.v() == begin.u();
				return;
			}
			Edge<?> next = it.next();
			assert e.v() == next.u();
			e = next;
		}
	}

	private static void assertPathVisitEvery(Collection<? extends Edge<?>> path, int n) {
		boolean[] visited = new boolean[n];
		for (Edge<?> e : path)
			visited[e.u()] = visited[e.v()] = true;
		for (int u = 0; u < n; u++)
			assert visited[u];
	}

}
