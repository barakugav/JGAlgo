package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import com.ugav.algo.Graph.WeightFunction;

public class MatchingWeightedBipartiteSSSP implements MatchingWeighted {

	/*
	 * O(m n + n^2 log n)
	 */

	public MatchingWeightedBipartiteSSSP() {
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g0, WeightFunction<E> w) {
		if (!(g0 instanceof GraphBipartite.Undirected<?>))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite<Ref<E>> g = referenceGraph((GraphBipartite<E>) g0, w);

		int n = g.vertices(), sn = g.svertices(), tn = g.tvertices();
		int s = g.newVertexT(), t = g.newVertexS();

		@SuppressWarnings("unchecked")
		Edge<E>[] match = new Edge[n];

		// Negate unmatched edges
		for (Edge<Ref<E>> e : g.edges())
			e.data().w = -e.data().w;
		// Connected unmatched vertices to fake vertices s,t
		final Ref<E> zeroEdgeData = new Ref<>(null, 0);
		for (int u = 0; u < sn; u++)
			g.addEdge(s, u).setData(zeroEdgeData);
		for (int v = sn; v < sn + tn; v++)
			g.addEdge(v, t).setData(zeroEdgeData);

		double[] potential = new double[n + 2];
		WeightFunction<Ref<E>> spWeightFunc = e -> e.data().w + potential[e.u()] - potential[e.v()];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		SSSP.Result<Ref<E>> sp = new SSSPBellmanFord().calcDistances(g, e -> e.data().w, s);
		for (int v = 0; v < n + 2; v++)
			potential[v] = sp.distance(v);

		SSSP ssspAlgo = new SSSPDijkstra();

		do {
			sp = ssspAlgo.calcDistances(g, spWeightFunc, s);
			List<Edge<Ref<E>>> augPath = sp.getPathTo(t);
			double augPathWeight = -(sp.distance(t) + potential[t]);
			if (augPath == null || augPathWeight < 0)
				break;

			Iterator<Edge<Ref<E>>> it = augPath.iterator();
			// remove edge from S to new matched vertex
			g.removeEdge(it.next());
			for (;;) {
				Edge<Ref<E>> matchedEdge = it.next();

				// Reverse newly matched edge
				g.removeEdge(matchedEdge);
				Ref<E> r = matchedEdge.data();
				match[matchedEdge.u()] = match[matchedEdge.v()] = r.orig;
				r.w = -r.w;
				g.addEdge(matchedEdge.v(), matchedEdge.u()).setData(r);

				Edge<Ref<E>> unmatchedEdge = it.next();

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

		List<Edge<E>> res = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Edge<E> e = match[i];
			if (e != null && e.u() == i)
				res.add(e);
		}

		return res;
	}

	@Override
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
		throw new UnsupportedOperationException();
	}

	private static <E> GraphBipartite<Ref<E>> referenceGraph(GraphBipartite<E> g, WeightFunction<E> w) {
		int n = g.vertices();
		GraphBipartite<Ref<E>> g0 = new GraphBipartiteArrayDirectedOld<>(g.svertices(), g.tvertices());

		for (int u = 0; u < n; u++) {
			if (!g.isVertexInS(u))
				continue;
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				double weight = w.weight(e);
				if (weight < 0)
					continue; // no reason to match negative edges
				Ref<E> v = new Ref<>(e, weight);
				g0.addEdge(e.u(), e.v()).setData(v);
			}
		}
		return g0;
	}

	private static class Ref<E> {

		public final Edge<E> orig;
		public double w;

		public Ref(Edge<E> e, double w) {
			orig = e;
			this.w = w;
		}

		@Override
		public int hashCode() {
			return orig.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref<?> o = (Ref<?>) other;
			return orig.equals(o.orig);
		}

		@Override
		public String toString() {
			return orig != null ? String.valueOf(orig.data()) : Double.toString(w);
		}

	}

}
