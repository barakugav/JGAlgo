package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class TSPMetricUtils {

	private TSPMetricUtils() {
		throw new InternalError();
	}

	static IntList calcEulerianTourAndConvertToHamiltonianCycle(Graph.Undirected<?> g, Graph.Undirected<?> g1,
			EdgeData.Int edgeRef) {
		int n = g.vertices();

		/* Assert degree is actually even in the new graph */
		int[] degree = Graphs.calcDegree(g1);
		for (int u = 0; u < n; u++)
			assert degree[u] % 2 == 0;

		/* Calculate Eulerian tour in the new graph */
		IntList tour = Graphs.calcEulerianTour(g1);
		assertValidCycle(g1, tour);
		assertPathVisitEvery(g1, tour);

		/* Use shortcuts to convert to a Hamiltonian cycle */
		IntList cycle = new IntArrayList(n);
		boolean[] visited = new boolean[n];

		for (PathIter it = new PathIter(g, tour); it.hasNext();) {
			int e = it.nextEdge();
			visited[it.u()] = true;
			while (visited[it.v()] && it.hasNext()) {
				int lastU = it.u();
				it.nextEdge();
				e = g.getEdge(lastU, it.v());
				if (e == -1)
					break;
			}
			if (e != -1)
				cycle.add(edgeRef.getInt(e));
		}
		assertValidCycle(g, cycle);
		assertPathVisitEvery(g, cycle);

		return cycle;
	}

	static IntList edgeListToVerticesList(Graph.Undirected<?> g, IntList edges) {
		IntList res = new IntArrayList();
		for (PathIter it = new PathIter(g, edges); it.hasNext();) {
			it.nextEdge();
			res.add(it.u());
		}
		return res;
	}

	private static void assertValidCycle(Graph.Undirected<?> g, IntList path) {
		PathIter it = new PathIter(g, path);
		assert it.hasNext();
		final int begin = it.u();
		for (;;) {
			if (!it.hasNext()) {
				assert it.v() == begin;
				return;
			}
			int lastV = it.v();
			it.nextEdge();
			assert lastV == it.u();
		}
	}

	private static void assertPathVisitEvery(Graph.Undirected<?> g, IntList path) {
		final int n = g.vertices();
		boolean[] visited = new boolean[n];
		for (IntIterator it = path.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			visited[u] = visited[v] = true;
		}
		for (int u = 0; u < n; u++)
			assert visited[u];
	}

	private static class PathIter {

		private final Graph.Undirected<?> g;
		private final IntIterator it;
		private int e = -1, v = -1;

		PathIter(Graph.Undirected<?> g, IntList path) {
			this.g = g;
			if (path.size() == 1) {
				v = g.getEdgeTarget(path.getInt(0));
			} else {
				int e0 = path.getInt(0), e1 = path.getInt(1);
				int u0 = g.getEdgeSource(e0), v0 = g.getEdgeTarget(e0);
				int u1 = g.getEdgeSource(e1), v1 = g.getEdgeTarget(e1);
				if (u0 == u1 || u0 == v1) {
					v = u0;
				} else {
					v = v0;
					assert (v0 == u1 || v0 == v1) : "not a path";
				}
			}
			it = path.iterator();
		}

		boolean hasNext() {
			return it.hasNext();
		}

		int nextEdge() {
			e = it.nextInt();
			assert v == g.getEdgeSource(e) || v == g.getEdgeTarget(e);
			v = g.getEdgeEndpoint(e, v);
			return e;
		}

		int u() {
			return g.getEdgeEndpoint(e, v);
		}

		int v() {
			return v;
		}

	}

}
