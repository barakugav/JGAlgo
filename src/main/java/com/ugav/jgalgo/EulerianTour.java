package com.ugav.jgalgo;

import com.ugav.jgalgo.Utils.StackIntFixSize;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class EulerianTour {

	private EulerianTour() {
	}

	public static Path calcTour(Graph g) {
		return g instanceof DiGraph ? calcTourDirected((DiGraph) g) : calcTourUndirected((UGraph) g);
	}

	private static Path calcTourUndirected(UGraph g) {
		int n = g.vertices().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (degreeWithoutSelfLoops(g, u) % 2 == 0)
				continue;
			if (start == -1)
				start = u;
			else if (end == -1)
				end = u;
			else
				throw new IllegalArgumentException(
						"More than two vertices have an odd degree (" + start + ", " + end + ", " + u + ")");
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException(
					"Eulerian tour exists only if all vertices have even degree or only two vertices have odd degree");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		Object usedEdgesKey = new Object();
		Weights.Bool usedEdges = g.addEdgesWeight(usedEdgesKey).defVal(false).ofBools();
		try {
			EdgeIter[] iters = new EdgeIter[n];
			for (int u = 0; u < n; u++)
				iters[u] = g.edges(u);

			IntArrayList tour = new IntArrayList(g.edges().size());
			StackIntFixSize queue = new StackIntFixSize(g.edges().size());

			for (int u = end;;) {
				findCycle: for (;;) {
					int e, v;
					for (EdgeIter iter = iters[u];;) {
						if (!iter.hasNext())
							break findCycle;
						e = iter.nextInt();
						if (!usedEdges.getBool(e)) {
							v = iter.v();
							break;
						}
					}
					usedEdges.set(e, true);
					queue.push(e);
					u = v;
				}

				if (queue.isEmpty())
					break;

				int e = queue.pop();
				tour.add(e);
				u = g.edgeEndpoint(e, u);
			}

			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (!usedEdges.getBool(e))
					throw new IllegalArgumentException("Graph is not connected");
			}
			return new Path(g, start, end, tour);

		} finally {
			g.removeEdgesWeights(usedEdgesKey);
		}
	}

	private static int degreeWithoutSelfLoops(UGraph g, int u) {
		int d = 0;
		for (EdgeIter eit = g.edges(u); eit.hasNext();) {
			eit.nextInt();
			if (eit.v() != u)
				d++;
		}
		return d;
	}

	private static Path calcTourDirected(DiGraph g) {
		int n = g.vertices().size();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			int outD = g.degreeOut(u);
			int inD = g.degreeIn(u);
			if (outD == inD)
				continue;
			if (outD == inD + 1) {
				if (start == -1) {
					start = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra out edge (" + start + ", " + u + ")");
				}
			} else if (outD + 1 == inD) {
				if (end == -1) {
					end = u;
				} else {
					throw new IllegalArgumentException(
							"More than one vertex have an extra in edge (" + end + ", " + u + ")");
				}
			} else {
				throw new IllegalArgumentException(
						"Can't compute Eulerian tour with vertex degrees (" + u + ": in=" + inD + " out=" + outD + ")");
			}
		}
		if (start != -1 ^ end != -1)
			throw new IllegalArgumentException("Eulerian tour exists in a directed graph only if all vertices have "
					+ "equal in and out degree or only one have an extra in edge and one have an extra out edge");
		if (start == -1)
			start = 0;
		if (end == -1)
			end = 0;

		Object usedEdgesKey = new Object();
		Weights.Bool usedEdges = g.addEdgesWeight(usedEdgesKey).defVal(false).ofBools();
		try {
			EdgeIter[] iters = new EdgeIter[n];
			for (int u = 0; u < n; u++)
				iters[u] = g.edgesOut(u);

			IntArrayList tour = new IntArrayList(g.edges().size());
			StackIntFixSize queue = new StackIntFixSize(g.edges().size());

			for (int u = start;;) {
				findCycle: for (;;) {
					int e, v;
					for (EdgeIter iter = iters[u];;) {
						if (!iter.hasNext())
							break findCycle;
						e = iter.nextInt();
						if (!usedEdges.getBool(e)) {
							v = iter.v();
							break;
						}
					}
					usedEdges.set(e, true);
					queue.push(e);
					u = v;
				}

				if (queue.isEmpty())
					break;

				int e = queue.pop();
				tour.add(e);
				assert g.edgeTarget(e) == u;
				u = g.edgeSource(e);
			}

			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (!usedEdges.getBool(e))
					throw new IllegalArgumentException("Graph is not connected");
			}
			IntArrays.reverse(tour.elements(), 0, tour.size());
			return new Path(g, start, end, tour);

		} finally {
			g.removeEdgesWeights(usedEdgesKey);
		}
	}

}
