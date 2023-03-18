package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Graph.WeightFunction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MDSTTarjan1977 implements MDST {

	/*
	 * O(m log n)
	 */

	public MDSTTarjan1977() {
	}

	private static final int HeavyEdge = 0xffffffff;

	private static final double HeavyEdgeWeight = Double.MAX_VALUE;

	/**
	 * finds the MST rooted from any root
	 */
	@Override
	public IntCollection calcMST(Graph<?> g0, WeightFunction w) {
		if (!(g0 instanceof Graph.Directed<?>))
			throw new IllegalArgumentException("Only directed graphs are supported");
		if (g0.vertices() == 0 || g0.edges() == 0)
			return IntLists.emptyList();
		Graph.Directed<Integer> g = Graphs.referenceGraph((Graph.Directed<?>) g0);
		Graph.EdgeData.Int edgeRefs = (Graph.EdgeData.Int) g.edgeData();

		// Connect new root to all vertices
		int n = g.vertices(), r = g.newVertex();
		for (int v = 0; v < n; v++)
			edgeRefs.set(g.addEdge(r, v), HeavyEdge);

		// Calc MST on new graph
		ContractedGraph contractedGraph = contract(g, w);
		return expand(g, contractedGraph, r);
	}

	@Override
	public IntCollection calcMST(Graph<?> g0, WeightFunction w, int root) {
		if (!(g0 instanceof Graph.Directed<?>))
			throw new IllegalArgumentException("Only directed graphs are supported");
		if (g0.vertices() == 0 || g0.edges() == 0)
			return IntLists.emptyList();
		Graph.Directed<Integer> g = Graphs.referenceGraph((Graph.Directed<?>) g0);

		ContractedGraph contractedGraph = contract(g, w);
		return expand(g, contractedGraph, root);
	}

	private static IntCollection expand(Graph.Directed<Integer> g, ContractedGraph cg, int root) {
		int[] inEdge = new int[cg.n];

		int[] roots = new int[cg.n * 2];
		int rootsSize = 0;
		roots[rootsSize++] = root;

		while (rootsSize > 0) {
			int r = roots[--rootsSize];
			int e = cg.inEdge[r];
			int v = g.getEdgeTarget(e);
			inEdge[v] = e;

			int upTo = r != root ? cg.parent[r] : -1;
			for (int prevChild = -1; v != upTo; v = cg.parent[prevChild = v]) {
				int child = cg.child[v];
				if (child == -1)
					continue;
				for (int c = child;;) {
					if (c != prevChild)
						roots[rootsSize++] = c;
					c = cg.brother[c];
					if (c == -1 || c == child)
						break;
				}
			}
		}

		Graph.EdgeData.Int edgeRefs = (Graph.EdgeData.Int) g.edgeData();
		IntCollection mst = new IntArrayList(cg.n - 1);
		for (int v = 0; v < cg.n; v++) {
			int e = edgeRefs.getInt(inEdge[v]);
			if (v != root && e != HeavyEdge)
				mst.add(e);
		}
		return mst;
	}

	private static void addEdgesUntilStronglyConnected(Graph.Directed<Integer> g) {
		int n = g.vertices();

		Pair<Integer, int[]> pair = Graphs.findStrongConnectivityComponents(g);
		int N = pair.e1.intValue();
		int[] v2V = pair.e2;

		if (N > 1) {
			int[] V2v = new int[N];
			Arrays.fill(V2v, -1);
			for (int v = 0; v < n; v++) {
				int V = v2V[v];
				if (V2v[V] == -1)
					V2v[V] = v;
			}

			Graph.EdgeData.Int edgeRefs = (Graph.EdgeData.Int) g.edgeData();
			for (int V = 1; V < N; V++) {
				edgeRefs.set(g.addEdge(V2v[0], V2v[V]), HeavyEdge);
				edgeRefs.set(g.addEdge(V2v[V], V2v[0]), HeavyEdge);
			}
		}
	}

	private static ContractedGraph contract(Graph.Directed<Integer> g, WeightFunction w0) {
		addEdgesUntilStronglyConnected(g);

		int n = g.vertices();
		int VMaxNum = n * 2; // max super vertex number

		UnionFindValue uf = new UnionFindValueArray(n);
		int[] ufIdxToV = new int[VMaxNum];
		for (int v = 0; v < n; v++)
			ufIdxToV[v] = v;

		WeightFunction w = e -> (e != HeavyEdge ? w0.weight(e) : HeavyEdgeWeight) + uf.getValue(g.getEdgeTarget(e));
		IntComparator edgeComparator = new Graphs.EdgeWeightComparator(w);
		@SuppressWarnings("unchecked")
		Heap<Integer>[] heap = new Heap[VMaxNum];
		for (int v = 0; v < n; v++)
			heap[v] = new HeapFibonacci<>(edgeComparator);
		for (int u = 0; u < n; u++) {
			for (EdgeIter<?> eit = g.edgesOut(u); eit.hasNext();) {
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
		int[] inEdge = new int[VMaxNum]; // TODO

		boolean[] onPath = new boolean[VMaxNum];
		final int startVertex = 0;
		onPath[startVertex] = true;

		for (int a = startVertex;;) {
			// Find minimum edge that enters a
			int u;
			int e;
			do {
				// Assuming the graph is strongly connected, if heap is empty we are done
				if (heap[a].isEmpty())
					return new ContractedGraph(n, parent, child, brother, inEdge);
				e = heap[a].extractMin().intValue();
				u = ufIdxToV[uf.find(g.getEdgeSource(e))];
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
