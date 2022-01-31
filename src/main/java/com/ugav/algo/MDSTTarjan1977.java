package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;

public class MDSTTarjan1977 implements MDST {

	/*
	 * O(mlogn)
	 */

	private MDSTTarjan1977() {
	}

	private static final MDSTTarjan1977 INSTANCE = new MDSTTarjan1977();

	public static MDSTTarjan1977 getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("rawtypes")
	private static final Edge HeavyEdge = new Edge() {

		@Override
		public int u() {
			return Integer.MAX_VALUE;
		}

		@Override
		public int v() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object val() {
			return null;
		}

		@Override
		public void val(Object v) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Edge twin() {
			return null;
		}
	};

	@SuppressWarnings("unchecked")
	private static <E> Edge<E> heavyEdge() {
		return (Edge<E>) HeavyEdge;
	}

	private static final double HeavyEdgeWeight = Double.MAX_VALUE;

	/**
	 * finds the MST rooted from any root
	 */
	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g0, Graph.WeightFunction<E> w) {
		if (!g0.isDirected())
			throw new IllegalArgumentException("Only directed MSTs are supported");
		if (g0.vertices() == 0 || g0.edges().isEmpty())
			return Collections.emptyList();
		Graph<Edge<E>> g = Graphs.referenceGraph(g0);

		// Connect new root to all vertices
		int n = g.vertices(), r = g.newVertex();
		for (int v = 0; v < n; v++)
			g.addEdge(r, v).val(heavyEdge());

		// Calc MST on new graph
		ContractedGraph<E> contractedGraph = contract(g, w);
		return expand(contractedGraph, r);
	}

	@Override
	public <E> Collection<Edge<E>> calcMST(Graph<E> g, Graph.WeightFunction<E> w, int root) {
		if (!g.isDirected())
			throw new IllegalArgumentException("Only directed MSTs are supported");
		if (g.vertices() == 0 || g.edges().isEmpty())
			return Collections.emptyList();

		ContractedGraph<E> contractedGraph = contract(Graphs.referenceGraph(g), w);
		return expand(contractedGraph, root);
	}

	private static <E> Collection<Edge<E>> expand(ContractedGraph<E> g, int root) {
		@SuppressWarnings("unchecked")
		Edge<Edge<E>>[] inEdge = new Edge[g.n];

		int[] roots = new int[g.n * 2];
		int rootsSize = 0;
		roots[rootsSize++] = root;

		while (rootsSize > 0) {
			int r = roots[--rootsSize];
			Edge<Edge<E>> e = g.inEdge[r];
			int v = e.v();
			inEdge[v] = e;

			int upTo = r != root ? g.parent[r] : -1;
			for (int prevChild = -1; v != upTo; v = g.parent[prevChild = v]) {
				int child = g.child[v];
				if (child == -1)
					continue;
				for (int c = child;;) {
					if (c != prevChild)
						roots[rootsSize++] = c;
					c = g.brother[c];
					if (c == -1 || c == child)
						break;
				}
			}
		}

		Collection<Edge<E>> mst = new ArrayList<>(g.n - 1);
		for (int v = 0; v < g.n; v++)
			if (v != root && inEdge[v].val() != HeavyEdge)
				mst.add(inEdge[v].val());
		return mst;
	}

	private static <E> void addEdgesUntilStronglyConnected(Graph<Edge<E>> g) {
		int n = g.vertices();

		Pair<Integer, int[]> pair = Graphs.findStrongConnectivityComponents(g);
		int N = pair.e1;
		int[] v2V = pair.e2;

		if (N > 1) {
			int[] V2v = new int[N];
			Arrays.fill(V2v, -1);
			for (int v = 0; v < n; v++) {
				int V = v2V[v];
				if (V2v[V] == -1)
					V2v[V] = v;
			}

			for (int V = 1; V < N; V++) {
				g.addEdge(V2v[0], V2v[V]).val(heavyEdge());
				g.addEdge(V2v[V], V2v[0]).val(heavyEdge());
			}
		}
	}

	private static <E> ContractedGraph<E> contract(Graph<Edge<E>> g, WeightFunction<E> w) {
		addEdgesUntilStronglyConnected(g);

		int n = g.vertices();
		int VMaxNum = n * 2; // max super vertex number

		UnionFindValue uf = new UnionFindValueArray(n);
		int[] ufIdxToV = new int[VMaxNum];
		for (int v = 0; v < n; v++)
			ufIdxToV[v] = v;

		@SuppressWarnings("unchecked")
		Heap<Edge<Edge<E>>>[] heap = new Heap[VMaxNum];
		Comparator<Edge<Edge<E>>> edgeComparator = (e1, e2) -> {
			double w1 = (e1.val() != HeavyEdge ? w.weight(e1.val()) : HeavyEdgeWeight) + uf.getValue(e1.v());
			double w2 = (e2.val() != HeavyEdge ? w.weight(e2.val()) : HeavyEdgeWeight) + uf.getValue(e2.v());
			return Utils.compare(w1, w2);
		};
		for (int v = 0; v < n; v++)
			heap[v] = new HeapFibonacci<>(edgeComparator);
		for (int u = 0; u < n; u++)
			for (Edge<Edge<E>> e : Utils.iterable(g.edges(u)))
				heap[e.v()].add(e);

		int[] parent = new int[VMaxNum];
		int[] child = new int[VMaxNum];
		int[] brother = new int[VMaxNum];
		Arrays.fill(parent, -1);
		Arrays.fill(child, -1);
		Arrays.fill(brother, -1);
		@SuppressWarnings("unchecked")
		Edge<Edge<E>>[] inEdge = new Edge[VMaxNum];

		boolean[] onPath = new boolean[VMaxNum];
		final int startVertex = 0;
		onPath[startVertex] = true;

		for (int a = startVertex;;) {
			// Find minimum edge that enters a
			int u;
			Edge<Edge<E>> e;
			do {
				// Assuming the graph is strongly connected, if heap is empty we are done
				if (heap[a].isEmpty())
					return new ContractedGraph<>(n, parent, child, brother, inEdge);
				e = heap[a].extractMin();
				u = ufIdxToV[uf.find(e.u())];
			} while (a == u);

			// Store the minimum edge entering a
			inEdge[a] = e;

			// Subtract w(e) from the weights of all edges entering a
			double ew = (e.val() != HeavyEdge ? w.weight(e.val()) : HeavyEdgeWeight) + uf.getValue(e.v());
			uf.addValue(a, -ew);

			if (!onPath[u]) {
				// Extend list
				brother[u] = a;
				onPath[u] = true;
				a = u;
			} else {
				// Create new super vertex
				int c = uf.make();
				heap[c] = new HeapFibonacci<>(edgeComparator);
				brother[c] = brother[u];
				brother[u] = a;
				child[c] = a;

				// Contract all children
				do {
					parent[a] = c;
					uf.union(c, a);
					onPath[a] = false;
					heap[c].meld(heap[a]);
					heap[a] = null;
					a = brother[a];
				} while (parent[a] == -1);

				ufIdxToV[uf.find(c)] = c;

				onPath[c] = true;
				a = c;
			}
		}
	}

	private static class ContractedGraph<E> {

		final int n;
		final int[] parent;
		final int[] child;
		final int[] brother;
		final Edge<Edge<E>>[] inEdge;

		ContractedGraph(int n, int[] parent, int[] child, int[] brother, Edge<Edge<E>>[] inEdge) {
			this.n = n;
			this.parent = parent;
			this.child = child;
			this.brother = brother;
			this.inEdge = inEdge;
		}

	}

}
