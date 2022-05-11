package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class MatchingWeightedBipartiteHungarianMethod implements MatchingWeighted {

	/*
	 * O(mn + n^2logn)
	 */

	private MatchingWeightedBipartiteHungarianMethod() {
	}

	private static final MatchingWeightedBipartiteHungarianMethod INSTANCE = new MatchingWeightedBipartiteHungarianMethod();

	public static MatchingWeightedBipartiteHungarianMethod getInstance() {
		return INSTANCE;
	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g0, WeightFunction<E> w) {
		if (!(g0 instanceof GraphBipartite) || g0.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite<E> g = (GraphBipartite<E>) g0;
		return new Worker<>(g, w).calcMaxMatching(false);
	}

	@Override
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g0, WeightFunction<E> w) {
		if (!(g0 instanceof GraphBipartite) || g0.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite<E> g = (GraphBipartite<E>) g0;
		return new Worker<>(g, w).calcMaxMatching(true);
	}

	private static class Worker<E> {

		private final GraphBipartite<E> g;
		private final WeightFunction<E> w;

		private final boolean[] inTree;

		private final Comparator<Edge<E>> edgeSlackComparator;
		private final HeapDirectAccessed<Edge<E>> nextTightEdge;
		private final HeapDirectAccessed.Handle<Edge<E>>[] nextTightEdgePerOutV;

		private double deltaTotal;
		private final double[] dualValBase;
		private final double[] dualVal0;

		@SuppressWarnings("unchecked")
		Worker(GraphBipartite<E> g, WeightFunction<E> w) {
			this.g = g;
			this.w = w;
			int n = g.vertices();

			inTree = new boolean[n];

			edgeSlackComparator = (e1, e2) -> Utils.compare(edgeSlack(e1), edgeSlack(e2));
			nextTightEdge = new HeapFibonacci<>(edgeSlackComparator);
			nextTightEdgePerOutV = new HeapDirectAccessed.Handle[n];

			dualValBase = new double[n];
			dualVal0 = new double[n];
		}

		Collection<Edge<E>> calcMaxMatching(boolean perfect) {
			int n = g.vertices();

			@SuppressWarnings("unchecked")
			Edge<E>[] parent = new Edge[n];
			@SuppressWarnings("unchecked")
			Edge<E>[] matched = new Edge[n];

			double maxWeight = Double.MIN_VALUE;
			for (Edge<E> e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e));
			final double delta1Threshold = maxWeight;
			for (int u = 0; u < n; u++)
				if (g.isVertexInS(u))
					dualValBase[u] = delta1Threshold;

			mainLoop: for (;;) {
				// Start growing tree from all unmatched vertices in S
				for (int u = 0; u < n; u++) {
					if (!g.isVertexInS(u) || matched[u] != null)
						continue;
					vertexAddedToTree(u);
					for (Edge<E> e : Utils.iterable(g.edges(u)))
						nextTightEdgeAdd(e);
				}

				currentTree: for (;;) {
					while (!nextTightEdge.isEmpty()) {
						Edge<E> e = nextTightEdge.findMin();

						if (inTree[e.v()]) {
							// Vertex already in tree, edge is irrelevant
							nextTightEdge.extractMin();
							continue;
						}

						// No more tight edges from the tree, go out and adjust dual values
						if (edgeSlack(e) > 0)
							break;

						// Edge is tight, add it to the tree
						nextTightEdge.extractMin();
						int v = e.v();
						parent[v] = e;
						vertexAddedToTree(v);

						Edge<E> matchedEdge = matched[v];
						if (matchedEdge == null) {
							for (;;) {
								// Augmenting path
								e = parent[v];
								matched[v] = e.twin();
								matched[v = e.u()] = e; // TODO don't set parent[odd vertex]
								e = parent[v];
								if (e == null)
									break currentTree;
								v = e.u();
							}
						}

						// Added odd vertex, immediately add it's matched edge and even vertex
						v = matchedEdge.v();
						parent[v] = matchedEdge;
						vertexAddedToTree(v);

						for (Edge<E> e1 : Utils.iterable(g.edges(v)))
							nextTightEdgeAdd(e1);
					}

					// Adjust dual values
					double delta1 = delta1Threshold - deltaTotal;
					double delta2 = nextTightEdge.isEmpty() ? -1 : edgeSlack(nextTightEdge.findMin());
					if ((!perfect && delta1 <= delta2) || delta2 == -1)
						break mainLoop;
					deltaTotal += delta2;
				}

				// Update dual values base
				for (int u = 0; u < n; u++)
					if (inTree[u])
						dualValBase[u] = dualVal(u);
				Arrays.fill(dualVal0, 0);

				// Reset tree
				Arrays.fill(inTree, false);
				Arrays.fill(parent, null);

				// Reset heap
				nextTightEdge.clear();
				Arrays.fill(nextTightEdgePerOutV, null);
			}

			List<Edge<E>> res = new ArrayList<>();
			for (int u = 0; u < n; u++)
				if (g.isVertexInS(u) && matched[u] != null)
					res.add(matched[u]);
			return res;
		}

		private void nextTightEdgeAdd(Edge<E> e) {
			int v = e.v();
			HeapDirectAccessed.Handle<Edge<E>> handle = nextTightEdgePerOutV[v];
			if (handle == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(e);
			else if (edgeSlackComparator.compare(e, handle.get()) < 0)
				nextTightEdge.decreaseKey(handle, e);
		}

		private double dualVal(int v) {
			return inTree[v] ? dualVal0[v] + (g.isVertexInS(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		private double edgeSlack(Edge<E> e) {
			return dualVal(e.u()) + dualVal(e.v()) - w.weight(e);
		}

		private void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (g.isVertexInS(v) ? deltaTotal : -deltaTotal);
			inTree[v] = true;
		}

	}

}
