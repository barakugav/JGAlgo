package com.jgalgo;

public class SSSPDag implements SSSP {

	@Override
	public SSSPDag.Result calcDistances(Graph g, EdgeWeightFunc w, int source) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("Only DAG graphs are supported");
		SSSPResultImpl res = new SSSPResultImpl(g, source);
		res.distances[source] = 0;

		int[] topolSort = TopologicalOrder.calcTopologicalSortingDAG((DiGraph) g);
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
