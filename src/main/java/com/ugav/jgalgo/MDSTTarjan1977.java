package com.ugav.jgalgo;

import java.util.Arrays;
import java.util.Objects;

import com.ugav.jgalgo.Utils.StackIntFixSize;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MDSTTarjan1977 implements MDST {

	/*
	 * O(m log n)
	 */

	private Heap.Builder heapBuilder = HeapPairing::new;
	private static final int HeavyEdge = 0xffffffff;
	private static final double HeavyEdgeWeight = Double.MAX_VALUE;
	private static final Object EdgeRefWeightKey = new Object();

	public MDSTTarjan1977() {
	}

	public void setHeapBuilder(Heap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	/**
	 * finds the MST rooted from any root
	 */
	@Override
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("Only directed graphs are supported");
		if (g0.vertices().size() == 0 || g0.edges().size() == 0)
			return IntLists.emptyList();
		DiGraph g = Graphs.referenceGraph((DiGraph) g0, EdgeRefWeightKey);
		Weights.Int edgeRefs = g.edgesWeight(EdgeRefWeightKey);

		// Connect new root to all vertices
		int n = g.vertices().size(), r = g.addVertex();
		for (int v = 0; v < n; v++)
			edgeRefs.set(g.addEdge(r, v), HeavyEdge);

		// Calc MST on new graph
		ContractedGraph contractedGraph = contract(g, w);
		return expand(g, contractedGraph, r);
	}

	@Override
	public IntCollection calcMST(Graph g0, EdgeWeightFunc w, int root) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("Only directed graphs are supported");
		if (g0.vertices().size() == 0 || g0.edges().size() == 0)
			return IntLists.emptyList();
		DiGraph g = Graphs.referenceGraph((DiGraph) g0, EdgeRefWeightKey);

		ContractedGraph contractedGraph = contract(g, w);
		return expand(g, contractedGraph, root);
	}

	private static IntCollection expand(DiGraph g, ContractedGraph cg, int root) {
		int[] inEdge = new int[cg.n];

		StackIntFixSize roots = new StackIntFixSize(cg.n * 2);
		roots.push(root);

		while (!roots.isEmpty()) {
			int r = roots.pop();
			int e = cg.inEdge[r];
			int v = g.edgeTarget(e);
			inEdge[v] = e;

			int upTo = r != root ? cg.parent[r] : -1;
			for (int prevChild = -1; v != upTo; v = cg.parent[prevChild = v]) {
				int child = cg.child[v];
				if (child == -1)
					continue;
				for (int c = child;;) {
					if (c != prevChild)
						roots.push(c);
					c = cg.brother[c];
					if (c == -1 || c == child)
						break;
				}
			}
		}

		Weights.Int edgeRefs = g.edgesWeight(EdgeRefWeightKey);
		IntCollection mst = new IntArrayList(cg.n - 1);
		for (int v = 0; v < cg.n; v++) {
			int e = edgeRefs.getInt(inEdge[v]);
			if (v != root && e != HeavyEdge)
				mst.add(e);
		}
		return mst;
	}

	private static void addEdgesUntilStronglyConnected(DiGraph g) {
		Connectivity.Result connectivityRes = Connectivity.findStrongConnectivityComponents(g);
		int N = connectivityRes.ccNum;
		int[] v2V = connectivityRes.vertexToCC;
		if (N <= 1)
			return;

		int[] V2v = new int[N];
		Arrays.fill(V2v, -1);
		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			int V = v2V[v];
			if (V2v[V] == -1)
				V2v[V] = v;
		}

		Weights.Int edgeRefs = g.edgesWeight(EdgeRefWeightKey);
		for (int V = 1; V < N; V++) {
			edgeRefs.set(g.addEdge(V2v[0], V2v[V]), HeavyEdge);
			edgeRefs.set(g.addEdge(V2v[V], V2v[0]), HeavyEdge);
		}
	}

	private ContractedGraph contract(DiGraph g, EdgeWeightFunc w0) {
		addEdgesUntilStronglyConnected(g);

		int n = g.vertices().size();
		int VMaxNum = n * 2; // max super vertex number

		UnionFindValue uf = new UnionFindValueArray(n);
		int[] ufIdxToV = new int[VMaxNum];
		for (int v = 0; v < n; v++)
			ufIdxToV[v] = v;

		Weights.Int edgeRefs = g.edgesWeight(EdgeRefWeightKey);
		EdgeWeightFunc w = e -> {
			int e0 = edgeRefs.getInt(e);
			return (e0 != HeavyEdge ? w0.weight(e0) : HeavyEdgeWeight) + uf.getValue(g.edgeTarget(e));
		};
		@SuppressWarnings("unchecked")
		Heap<Integer>[] heap = new Heap[VMaxNum];
		for (int v = 0; v < n; v++)
			heap[v] = heapBuilder.build(w);
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				heap[eit.v()].add(Integer.valueOf(e));
			}
		}

		int[] parent = new int[VMaxNum];
		int[] child = new int[VMaxNum];
		int[] brother = new int[VMaxNum];
		Arrays.fill(parent, -1);
		Arrays.fill(child, -1);
		Arrays.fill(brother, -1);
		int[] inEdge = new int[VMaxNum];

		boolean[] onPath = new boolean[VMaxNum];
		final int startVertex = 0;
		onPath[startVertex] = true;

		for (int a = startVertex;;) {
			// Find minimum edge that enters a
			int u, e;
			do {
				// Assuming the graph is strongly connected, if heap is empty we are done
				if (heap[a].isEmpty())
					return new ContractedGraph(n, parent, child, brother, inEdge);
				e = heap[a].extractMin().intValue();
				u = ufIdxToV[uf.find(g.edgeSource(e))];
			} while (a == u);

			// Store the minimum edge entering a
			inEdge[a] = e;

			// Subtract w(e) from the weights of all edges entering a
			double ew = w.weight(e);
			uf.addValue(a, -ew);

			if (!onPath[u]) {
				// Extend list
				brother[u] = a;
				onPath[u] = true;
				a = u;
			} else {
				// Create new super vertex
				int c = uf.make();
				heap[c] = heapBuilder.build(w);
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

	private static class ContractedGraph {

		final int n;
		final int[] parent;
		final int[] child;
		final int[] brother;
		final int[] inEdge;

		ContractedGraph(int n, int[] parent, int[] child, int[] brother, int[] inEdge) {
			this.n = n;
			this.parent = parent;
			this.child = child;
			this.brother = brother;
			this.inEdge = inEdge;
		}

	}

}
