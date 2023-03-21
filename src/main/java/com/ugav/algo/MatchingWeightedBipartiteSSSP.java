package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.EdgesWeight.DataIter;
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
	public IntCollection calcMaxMatching(Graph g0, WeightFunction w) {
		if (!(g0 instanceof GraphBipartite.Undirected))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite.DiGraph g = referenceGraph((GraphBipartite.Undirected) g0, w);
		EdgesWeight<Ref> edgeRef = g.edgesWeight("edgeRef");

		int n = g.verticesNum(), sn = g.svertices(), tn = g.tvertices();
		int s = g.newVertexT(), t = g.newVertexS();

		int[] match = new int[n];
		Arrays.fill(match, -1);

		double maxWeight = 1;
		for (int e = 0; e < g.edgesNum(); e++)
			maxWeight = Math.max(maxWeight, edgeRef.get(e).w);
		if (!Double.isFinite(maxWeight))
			throw new IllegalArgumentException("non finite weights");
		final double RemovedEdgeWeight = maxWeight * n;

		// Negate unmatched edges
		for (DataIter<Ref> it = edgeRef.iterator(); it.hasNext();) {
			it.nextEdge();
			Ref r = it.getData();
			r.w = -r.w;
		}
		// Connected unmatched vertices to fake vertices s,t
		for (int u = 0; u < sn; u++)
			edgeRef.set(g.addEdge(s, u), new Ref(-1, 0));
		for (int v = sn; v < sn + tn; v++)
			edgeRef.set(g.addEdge(v, t), new Ref(-1, 0));

		double[] potential = new double[n + 2];
		WeightFunction spWeightFunc = e -> edgeRef.get(e).w + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		SSSP.Result sp = new SSSPBellmanFord().calcDistances(g, e -> edgeRef.get(e).w, s);
		for (int v = 0; v < n + 2; v++)
			potential[v] = sp.distance(v);

		SSSP ssspAlgo = new SSSPDijkstra();

		for (;;) {
			sp = ssspAlgo.calcDistances(g, spWeightFunc, s);
			IntList augPath = sp.getPathTo(t);
			double augPathWeight = -(sp.distance(t) + potential[t]);
			if (augPath == null || augPathWeight >= RemovedEdgeWeight || augPathWeight < 0)
				break;

			IntIterator it = augPath.iterator();
			// 'remove' edge from S to new matched vertex
			edgeRef.get(it.nextInt()).w = RemovedEdgeWeight;
			for (;;) {
				int matchedEdge = it.nextInt();

				// Reverse newly matched edge
				g.reverseEdge(matchedEdge);
				Ref r = edgeRef.get(matchedEdge);
				match[g.edgeSource(matchedEdge)] = match[g.edgeTarget(matchedEdge)] = r.orig;
				r.w = -r.w;

				int unmatchedEdge = it.nextInt();

				if (!it.hasNext()) {
					// 'remove' edge from new matched vertex to T
					edgeRef.get(unmatchedEdge).w = RemovedEdgeWeight;
					break;
				}

				// Reverse newly unmatched edge
				g.reverseEdge(unmatchedEdge);
				r = edgeRef.get(unmatchedEdge);
				r.w = -r.w;
			}

			// Update potential based on the distances
			for (int u = 0; u < n; u++)
				potential[u] += sp.distance(u);
		}

		IntList res = new IntArrayList();
		for (int u = 0; u < n; u++) {
			int e = match[u];
			if (e != -1 && g0.edgeSource(e) == u)
				res.add(e);
		}

		return res;
	}

	@Override
	public IntCollection calcPerfectMaxMatching(Graph g, WeightFunction w) {
		throw new UnsupportedOperationException();
	}

	private static GraphBipartite.DiGraph referenceGraph(GraphBipartite.Undirected g, WeightFunction w) {
		int n = g.verticesNum();
		GraphBipartite.DiGraph g0 = new GraphBipartiteArrayDirected(g.svertices(), g.tvertices());
		EdgesWeight<Ref> edgeRef = g0.newEdgeWeight("edgeRef");

		for (int u = 0; u < n; u++) {
			if (!g.isVertexInS(u))
				continue;
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				double weight = w.weight(e);
				if (weight < 0)
					continue; // no reason to match negative edges
				int e0 = g0.addEdge(u, eit.v());
				edgeRef.set(e0, new Ref(e, weight));
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
