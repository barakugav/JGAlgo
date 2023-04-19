package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Eulerian tour calculation algorithm.
 * <p>
 * An Eulerian tour is a tour that visits every edge exactly once (allowing for
 * revisiting vertices). For a connected undirected graph, if all vertices have
 * an even degree, an Eulerian cycle will be found. If exactly two vertices have
 * an odd degree, called {@code s,t}, an Eulerian tour that start at {@code s}
 * and ends at {@code t} exists. For any other vertices degrees an Eulerian tour
 * does not exists. For a strongly connected directed graph, the in-degree and
 * out-degree of each vertex must be equal for an Eulerian cycle to exists. If
 * exactly one vertex {@code s} has one more out-edge than in-edges, and one
 * vertex {@code t} has one more in-edge than out-edges, an Eulerian tour that
 * start at {@code s} and ends at {@code t} exists.
 * <p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Eulerian_path">Wikipedia</a>
 * @see TSPMetricMSTAppx
 * @see TSPMetricMatchingAppx
 * @author Barak Ugav
 */
public class EulerianTour {

	private EulerianTour() {
	}

	/**
	 * Calculate an Eulerian tour in the graph that visit all edges exactly once.
	 * <p>
	 * The graph is assumed to be (strongly) connected. Either a cycle or tour will
	 * be found, depending on the vertices degrees.
	 * <p>
	 * The running time and space of this function is {@code O(m + n)}.
	 *
	 * @param g a graph
	 * @return an Eulerian tour that visit all edges of the graph exactly once
	 * @throws IllegalArgumentException if there is no Eulerian tour in the graph
	 */
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
		Weights.Bool usedEdges = g.addEdgesWeights(usedEdgesKey, boolean.class);
		try {
			EdgeIter[] iters = new EdgeIter[n];
			for (int u = 0; u < n; u++)
				iters[u] = g.edgesOut(u);

			IntArrayList tour = new IntArrayList(g.edges().size());
			IntStack queue = new IntArrayList();

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

				int e = queue.popInt();
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
		for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
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
		Weights.Bool usedEdges = g.addEdgesWeights(usedEdgesKey, boolean.class);
		try {
			EdgeIter[] iters = new EdgeIter[n];
			for (int u = 0; u < n; u++)
				iters[u] = g.edgesOut(u);

			IntArrayList tour = new IntArrayList(g.edges().size());
			IntStack queue = new IntArrayList();

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

				int e = queue.popInt();
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
