package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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

	private static class DualValues<E> {

		final GraphBipartite<E> g;
		final WeightFunction<E> w;

		double deltaTotal;
		final double[] dualValBase;
		final double[] dualVal0;
		final boolean[] inTree;

		DualValues(GraphBipartite<E> g, WeightFunction<E> w) {
			this.g = g;
			this.w = w;

			int n = g.vertices();
			dualValBase = new double[n];
			dualVal0 = new double[n];
			inTree = new boolean[n];

			double maxWeight = Double.MIN_VALUE;
			for (Edge<E> e : g.edges())
				maxWeight = Math.max(maxWeight, w.weight(e));
			for (int u = 0; u < n; u++)
				if (g.isVertexInS(u))
					dualValBase[u] = maxWeight;
		}

		double dualVal(int v) {
			return inTree[v] ? dualVal0[v] + (g.isVertexInS(v) ? -deltaTotal : deltaTotal) : dualValBase[v];
		}

		double edgeSlack(Edge<E> e) {
			return dualVal(e.u()) + dualVal(e.v()) - w.weight(e);
		}

		void vertexAddedToTree(int v) {
			dualVal0[v] = dualValBase[v] + (g.isVertexInS(v) ? deltaTotal : -deltaTotal);
			inTree[v] = true;
		}

		void updateDualValsBases() {
			int n = g.vertices();
			for (int u = 0; u < n; u++)
				if (inTree[u])
					dualValBase[u] = dualVal(u);
			Arrays.fill(dualVal0, 0);
			Arrays.fill(inTree, false);
		}

	}

	@Override
	public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g0, WeightFunction<E> w) {
		if (!(g0 instanceof GraphBipartite) || g0.isDirected())
			throw new IllegalArgumentException("Only undirected bipartite graphs are supported");
		GraphBipartite<E> g = (GraphBipartite<E>) g0;

		int n = g.vertices();

		boolean[] inTree = new boolean[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] parent = new Edge[n];
		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];

		double maxWeight = Double.MIN_VALUE;
		for (Edge<E> e : g.edges())
			maxWeight = Math.max(maxWeight, w.weight(e));
		DualValues<E> dual = new DualValues<>(g, w);

		Comparator<Edge<E>> comparator = (e1, e2) -> Double.compare(dual.edgeSlack(e1), dual.edgeSlack(e2));
		Heap<Edge<E>> nextTightEdge = new HeapFibonacci<>(comparator);
		@SuppressWarnings("unchecked")
		Heap.Handle<Edge<E>>[] nextTightEdgePerOutV = new Heap.Handle[n];
		Consumer<Edge<E>> nextTightEdgeAdd = e -> {
			int v = e.v();
			Heap.Handle<Edge<E>> handle = nextTightEdgePerOutV[v];
			if (handle == null)
				nextTightEdgePerOutV[v] = nextTightEdge.insert(e);
			else if (comparator.compare(e, handle.get()) < 0)
				nextTightEdge.decreaseKey(handle, e);
		};

		mainLoop: for (;;) {

			// Start growing tree from all unmatched vertices in S
			for (int u = 0; u < n; u++) {
				if (!g.isVertexInS(u) || matched[u] != null)
					continue;
				inTree[u] = true;
				dual.vertexAddedToTree(u);
				for (Edge<E> e : Utils.iterable(g.edges(u)))
					nextTightEdgeAdd.accept(e);
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
					if (dual.edgeSlack(e) > 0)
						break;

					// Edge is tight, add it to the tree
					nextTightEdge.extractMin();
					int v = e.v();
					inTree[v] = true;
					parent[v] = e;
					dual.vertexAddedToTree(v);

					Edge<E> matchedEdge = matched[v];
					if (matchedEdge == null) {
						for (;;) {
							// Augmenting path
							e = parent[v];
							matched[v] = e.twin();
							matched[v = e.u()] = e;
							e = parent[v];
							if (e == null)
								break currentTree;
							v = e.u();
						}
					}

					// Added odd vertex, immediately add it's matched edge and even vertex
					v = matchedEdge.v();
					parent[v] = matchedEdge;
					inTree[v] = true;
					dual.vertexAddedToTree(v);

					for (Edge<E> e1 : Utils.iterable(g.edges(v)))
						nextTightEdgeAdd.accept(e1);
				}

				// Adjust dual values
				double delta1 = maxWeight - dual.deltaTotal;
				double delta2 = nextTightEdge.isEmpty() ? -1 : dual.edgeSlack(nextTightEdge.findMin());
				if (delta1 <= delta2 || delta2 == -1)
					break mainLoop;
				dual.deltaTotal += delta2;
			}

			dual.updateDualValsBases();

			// reset tree
			Arrays.fill(inTree, false);
			Arrays.fill(parent, null);

			// reset heap
			nextTightEdge.clear();
			Arrays.fill(nextTightEdgePerOutV, null);
		}

		List<Edge<E>> res = new ArrayList<>();
		for (int u = 0; u < n; u++)
			if (g.isVertexInS(u) && matched[u] != null)
				res.add(matched[u]);
		return res;
	}

	@Override
	public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
		// TODO Auto-generated method stub
		return null;
	}

}
