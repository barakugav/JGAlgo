package com.ugav.jgalgo;

import java.util.Arrays;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public class MatchingWeightedBipartiteSSSP implements MatchingWeighted {

	/*
	 * O(m n + n^2 log n)
	 */

	private Object bipartiteVerticesWeightKey = VerticesWeights.DefaultBipartiteWeightKey;
	private SSSP ssspAlgo = new SSSPDijkstra();
	private static final Object EdgeRefWeightKey = new Object();

	public MatchingWeightedBipartiteSSSP() {
	}

	public void setSsspAlgo(SSSP algo) {
		ssspAlgo = Objects.requireNonNull(algo);
	}

	public void setBipartiteVerticesWeightKey(Object key) {
		bipartiteVerticesWeightKey = key;
	}

	@Override
	public IntCollection calcMaxMatching(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof UGraph))
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		Weights.Bool partition = g0.verticesWeight(bipartiteVerticesWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight " + bipartiteVerticesWeightKey);
		DiGraph g = referenceGraph((UGraph) g0, partition, w);
		Weights<Ref> edgeRef = g.edgesWeight(EdgeRefWeightKey);

		int n = g.vertices().size();
		int s = g.addVertex(), t = g.addVertex();

		int[] match = new int[n];
		Arrays.fill(match, -1);

		double maxWeight = 1;
		for (IntIterator it = g.edges().iterator(); it.hasNext(); ) {
			int e = it.nextInt();
			maxWeight = Math.max(maxWeight, edgeRef.get(e).w);
		}
		if (!Double.isFinite(maxWeight))
			throw new IllegalArgumentException("non finite weights");
		final double RemovedEdgeWeight = maxWeight * n;

		// Negate unmatched edges
		for (IntIterator it = g.edges().iterator(); it.hasNext(); ) {
			int e = it.nextInt();
			Ref r = edgeRef.get(e);
			r.w = -r.w;
		}
		// Connected unmatched vertices to fake vertices s,t
		for (int u = 0; u < n; u++) {
			if (partition.getBool(u)) {
				edgeRef.set(g.addEdge(s, u), new Ref(-1, 0));
			} else {
				edgeRef.set(g.addEdge(u, t), new Ref(-1, 0));
			}
		}

		double[] potential = new double[n + 2];
		EdgeWeightFunc spWeightFunc = e -> edgeRef.get(e).w + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		SSSP.Result sp = new SSSPBellmanFord().calcDistances(g, e -> edgeRef.get(e).w, s);
		for (int v = 0; v < n + 2; v++)
			potential[v] = sp.distance(v);

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
	public IntCollection calcPerfectMaxMatching(Graph g, EdgeWeightFunc w) {
		throw new UnsupportedOperationException();
	}

	private static DiGraph referenceGraph(UGraph g, Weights.Bool partition, EdgeWeightFunc w) {
		int n = g.vertices().size();
		DiGraph g0 = new GraphArrayDirected(g.vertices().size());
		Weights<Ref> edgeRef = EdgesWeights.ofObjs(g0, EdgeRefWeightKey);

		for (int u = 0; u < n; u++) {
			if (!partition.getBool(u))
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
