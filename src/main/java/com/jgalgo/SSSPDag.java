package com.jgalgo;

/**
 * Linear Single Source Shortest Path (SSSP) algorithm for directed acyclic
 * graphs (DAG).
 * <p>
 * The algorithm first compute a topological sorting of the vertices in linear
 * time, and then traverse the vertices in that order and determine the distance
 * for each one of them.
 *
 * @see TopologicalOrder
 * @author Barak Ugav
 */
public class SSSPDag implements SSSP {

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if graph is not directed or contains cycles
	 */
	@Override
	public SSSPDag.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("Only DAG graphs are supported");
		SSSPResultImpl res = new SSSPResultImpl(g, source);
		res.distances[source] = 0;

		int[] topolSort = TopologicalOrder.computeTopologicalSortingDAG((DiGraph) g);
		boolean sourceSeen = false;
		for (int u : topolSort) {
			if (!sourceSeen) {
				if (u != source)
					continue;
				sourceSeen = true;
			}
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		return res;
	}

}
