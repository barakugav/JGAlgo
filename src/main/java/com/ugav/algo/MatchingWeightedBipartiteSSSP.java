package com.ugav.algo;

import com.ugav.algo.EdgeData.DataIter;
import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingWeightedBipartiteSSSP implements MatchingWeighted {

	/*
	 * O(m n + n^2 log n)
	 */

	public MatchingWeightedBipartiteSSSP() {
	}

	@Override
	public IntCollection calcMaxMatching(Graph<?> g0, WeightFunction w) {
		if (!(g0 instanceof GraphBipartite.Undirected<?>))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite.Directed<Ref> g = referenceGraph((GraphBipartite.Undirected<?>) g0, w);

		int n = g.vertices(), sn = g.svertices(), tn = g.tvertices();
		int s = g.newVertexT(), t = g.newVertexS();

		@SuppressWarnings("unchecked")
		int[] match = new int[n];

		// Negate unmatched edges
		for (DataIter<Ref> it = g.edgeData().iterator(); it.hasNext();) {
			it.nextEdge();
			Ref r = it.getData();
			r.w = -r.w;
		}
		// Connected unmatched vertices to fake vertices s,t
		final Ref zeroEdgeData = new Ref(-1, 0);
		for (int u = 0; u < sn; u++)
			g.edgeData().set(g.addEdge(s, u), zeroEdgeData);
		for (int v = sn; v < sn + tn; v++)
			g.edgeData().set(g.addEdge(v, t), zeroEdgeData);

		double[] potential = new double[n + 2];
		WeightFunction spWeightFunc = e -> g.edgeData().get(e).w + potential[g.getEdgeSource(e)]
				- potential[g.getEdgeTarget(e)];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		SSSP.Result sp = new SSSPBellmanFord().calcDistances(g, e -> g.edgeData().get(e).w, s);
		for (int v = 0; v < n + 2; v++)
			potential[v] = sp.distance(v);

		SSSP ssspAlgo = new SSSPDijkstra();

		do {
			sp = ssspAlgo.calcDistances(g, spWeightFunc, s);
			IntList augPath = sp.getPathTo(t);
			double augPathWeight = -(sp.distance(t) + potential[t]);
			if (augPath == null || augPathWeight < 0)
				break;

			IntIterator it = augPath.iterator();
			// remove edge from S to new matched vertex
			g.removeEdge(it.nextInt());
			for (;;) {
				int matchedEdge = it.nextInt();

				// Reverse newly matched edge
				g.removeEdge(matchedEdge);
				Ref r = matchedEdge.data();
				match[g.getEdgeSource(matchedEdge)] = match[g.getEdgeTarget(matchedEdge)] = r.orig;
				r.w = -r.w;
				g.addEdge(matchedEdge.v(), matchedEdge.u()).setData(r);

				int unmatchedEdge = it.nextInt();

				if (!it.hasNext()) {
					// remove edge from new matched vertex to T
					g.removeEdge(unmatchedEdge);
					break;
				}

				// Reverse newly unmatched edge
				g.removeEdge(unmatchedEdge);
				r = unmatchedEdge.data();
				r.w = -r.w;
				g.addEdge(unmatchedEdge.v(), unmatchedEdge.u()).setData(r);
			}

			// Update potential based on the distances
			for (int u = 0; u < n; u++)
				potential[u] += sp.distance(u);
		} while (true);

		IntList res = new IntArrayList();
		for (int i = 0; i < n; i++) {
			Edge<E> e = match[i];
			if (e != null && e.u() == i)
				res.add(e);
		}

		return res;
	}

	@Override
	public IntCollection calcPerfectMaxMatching(Graph<?> g, WeightFunction w) {
		throw new UnsupportedOperationException();
	}

	private static <E> GraphBipartite.Directed<Ref> referenceGraph(GraphBipartite.Undirected<E> g, WeightFunction w) {
		int n = g.vertices();
		GraphBipartite.Directed<Ref> g0 = new GraphBipartiteArrayDirected<>(g.svertices(), g.tvertices());

		for (int u = 0; u < n; u++) {
			if (!g.isVertexInS(u))
				continue;
			for (EdgeIter<?> eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				double weight = w.weight(e);
				if (weight < 0)
					continue; // no reason to match negative edges
				int e0 = g0.addEdge(u, eit.v());
				g0.edgeData().set(e0, new Ref(e, weight));
			}
		}
		return g0;
	}

	private static class Ref {

		public final int orig;
		public double w;

		public Ref(int e, double w) {
			orig = e;
			this.w = w;
		}

		@Override
		public int hashCode() {
			return orig;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref o = (Ref) other;
			return orig == o.orig;
		}

		@Override
		public String toString() {
			return orig != -1 ? String.valueOf(orig) : Double.toString(w);
		}

	}

}
