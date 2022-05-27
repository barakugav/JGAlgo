package com.ugav.algo;

import java.util.Arrays;
import java.util.Comparator;

public class SubtreeMergeFindmin<W> {

	/**
	 * Subtree Merge Find min is a data structure used in maximum weighted matching
	 * in general graphs. At any moment, a tree is maintain, divided into sub trees
	 * of continues nodes. AddLeaf operation is supported to add leaves to the tree.
	 * Merge operation can be used to union two adjacent sub trees into one, which
	 * doesn't change the actual tree structure, only the subtrees groups in it. The
	 * last two supported operations are addNonTreeEdge(u,v,weight) and
	 * findMinNonTreeEdge(), which add a edge with some weight without affecting the
	 * tree structure, and the findmin operation query for the non tree edge with
	 * minimum weight that connects two different subtrees.
	 *
	 * AddLeaf is O(1) amortized, merge is O(log n), addNonTreeEdge is O(1) and
	 * findMin is O(1). In total, this data structure support m operations on n
	 * nodes in time O(m + n log n).
	 */

	private int[] parent;
	private int[] depth;

	private final UnionFind uf;
	private SubTree<W>[] subtrees;

	private final HeapDirectAccessed<SubTree<W>> heap;

	private final LCADynamic lca;

	private final Comparator<? super W> weightCmp;
	private int timestamp;

	private static int[] EMPTY_INT_ARR = new int[0];
	private static SubTree<?>[] EMPTY_SUBTREE_ARR = new SubTree[0];

	public SubtreeMergeFindmin() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public SubtreeMergeFindmin(Comparator<? super W> weightCmp) {
		parent = EMPTY_INT_ARR;
		depth = EMPTY_INT_ARR;

		uf = new UnionFindArray();
		subtrees = (SubTree<W>[]) EMPTY_SUBTREE_ARR;

		lca = new LCAGabow2017();

		this.weightCmp = weightCmp != null ? weightCmp : Utils.getDefaultComparator();
		timestamp = 0;

		heap = new HeapFibonacci<>((t1, t2) -> {
			return this.weightCmp.compare(t1.minEdge.data.weight, t2.minEdge.data.weight);
		});
	}

	public int size() {
		return uf.size();
	}

	public int initTree() {
		if (size() != 0)
			throw new IllegalStateException("Tree is not empty");
		int root = lca.initTree();
		return newNode(-1, root);
	}

	public int addLeaf(int parent) {
		checkNodeId(parent);
		int u = lca.addLeaf(parent);
		return newNode(parent, u);
	}

	private int newNode(int p, int node) {
		int ufIdx = uf.make();
		assert ufIdx == node;

		if (node >= parent.length) {
			assert parent.length == depth.length;
			assert parent.length == subtrees.length;
			parent = Arrays.copyOf(parent, Math.max(parent.length * 2, 2));
			depth = Arrays.copyOf(depth, Math.max(depth.length * 2, 2));
			subtrees = Arrays.copyOf(subtrees, Math.max(subtrees.length * 2, 2));
		}

		parent[node] = p;
		depth[node] = p != -1 ? depth[p] + 1 : 0;
		subtrees[node] = new SubTree<>(node);
		return node;
	}

	@SuppressWarnings("unchecked")
	public void mergeSubTrees(int u, int v) {
		checkNodeId(u);
		checkNodeId(v);
		u = uf.find(u);
		v = uf.find(v);
		SubTree<W> U = subtrees[u], V = subtrees[v];
		if (U == V)
			return;

		/* assume U is above V */
		if (depth[U.top] > depth[V.top]) {
			SubTree<W> temp = U;
			U = V;
			V = temp;
		}
		if (parent[V.top] < 0 || subTree(parent[V.top]) != U)
			throw new IllegalArgumentException("Subtrees are not adjacent");

		/* update union find */
		int w = uf.union(u, v);
		subtrees[u] = subtrees[v] = null;
		subtrees[w] = U;

		U.size += V.size;
		int rank = U.rank(), maxSet = Math.max(U.edges.length, V.edges.length);
		EdgeList<W>[] uEdges = U.edges, vEdges = V.edges;
		U.edges = Arrays.copyOf(U.edges, maxSet);
		Arrays.fill(U.edges, null);
		if (U.heapHandle != null)
			heap.removeHandle(U.heapHandle);
		if (V.heapHandle != null)
			heap.removeHandle(V.heapHandle);
		U.heapHandle = V.heapHandle = null;
		U.minEdge = V.minEdge = null;

		/* fix current timestamp to identify redundant edges */
		final int t = ++timestamp;

		/* All edges in sets >= r+1 are good, just union */
		for (int setIdx = rank + 1; setIdx < maxSet; setIdx++) {
			EdgeList<W> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<W> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			EdgeList<W> el = concatenateEdgeLists(el1, el2);
			if (el == null)
				continue;
			U.edges[setIdx] = el;
			compareAgaintSubTreeMin(U, el.min);
		}
		/* Examine all edges in set <= r */
		for (int setIdx = 0; setIdx <= rank; setIdx++) {
			EdgeList<W> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<W> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			for (EdgeList<W> el : new EdgeList[] { el1, el2 }) {
				if (el == null)
					continue;
				for (EdgeNode<W> edge, next; (edge = el.head) != null; el.head = next) {
					next = edge.next;
					edge.next = null;

					SubTree<W> eU = subTree(edge.u), eV = subTree(edge.v);
					if (eU == eV) {
						/* edge is bad */
						edge.clear();
						continue;
					}

					/* assume eU is U */
					if (eU != U) {
						SubTree<W> temp = eU;
						eU = eV;
						eV = temp;
					}
					assert eU == U;

					if (eV.inEdgeTimestamp == t && weightCmp.compare(eV.inEdge.data.weight, edge.data.weight) <= 0) {
						/* edge is redundant */
						edge.clear();
						continue;
					}

					/* update inEdge */
					eV.inEdgeTimestamp = t;
					eV.inEdge = edge;

					/* add edge to the appropriate set */
					addEdgeNode(edge);
				}
				el.clear();
			}
		}
		Arrays.fill(uEdges, null);
		Arrays.fill(vEdges, null);
	}

	private EdgeList<W> concatenateEdgeLists(EdgeList<W> el1, EdgeList<W> el2) {
		if (el1 == null)
			return el2;
		if (el2 == null)
			return el1;

		el1.tail.next = el2.head;
		el1.tail = el2.tail;
		if (weightCmp.compare(el2.min.data.weight, el1.min.data.weight) < 0)
			el1.min = el2.min;

		/* clear el2 */
		el2.clear();
		return el1;
	}

	public void addNonTreeEdge(int u, int v, W weight) {
		checkNodeId(u);
		checkNodeId(v);
		if (u == v)
			return;
		/* assume u is above v */
		if (depth[u] > depth[v]) {
			int temp = u;
			u = v;
			v = temp;
		}
		Edge<W> edge = new Edge<>(u, v, weight);

		/* split edge into two edges (u, lca(u,v)), (v, lca(u,v)) */
		int l = lca.calcLCA(u, v);
		if (u != l) {
			addEdgeNode(new EdgeNode<>(u, l, edge));
			addEdgeNode(new EdgeNode<>(v, l, edge));
		} else {
			addEdgeNode(new EdgeNode<>(v, u, edge));
		}
	}

	private void addEdgeNode(EdgeNode<W> edge) {
		/* assume u is deeper */
		assert depth[edge.u] > depth[edge.v];

		SubTree<W> u = subTree(edge.u), v = subTree(edge.v);
		if (u == v)
			return; /* ignore edge within same sub tree */

		int span = Utils.log2(depth[edge.u] - depth[edge.v] + 1);
		int uRank = u.rank(), vRank = v.rank();

		if (uRank < span)
			/* long */
			addEdgeToSet(u, span, edge);
		else if (uRank <= vRank)
			/* up */
			addEdgeToSet(u, Math.max(uRank + 1, vRank), edge);
		else
			/* down */
			addEdgeToSet(v, uRank, edge);
	}

	private void addEdgeToSet(SubTree<W> v, int setIdx, EdgeNode<W> edge) {
		if (v.edges.length <= setIdx) {
			int newLen = v.edges.length;
			do {
				newLen = Math.max(newLen * 2, 2);
			} while (newLen <= setIdx);
			v.edges = Arrays.copyOf(v.edges, newLen);
		}
		if (v.edges[setIdx] == null)
			v.edges[setIdx] = new EdgeList<>();
		EdgeList<W> edgeList = v.edges[setIdx];

		if (edgeList.head == null) {
			edgeList.head = edgeList.tail = edgeList.min = edge;
			compareAgaintSubTreeMin(v, edge);
		} else {
			edgeList.tail.next = edge;
			edgeList.tail = edge;
			if (weightCmp.compare(edge.data.weight, edgeList.min.data.weight) < 0) {
				edgeList.min = edge;
				compareAgaintSubTreeMin(v, edge);
			}
		}
	}

	private void compareAgaintSubTreeMin(SubTree<W> v, EdgeNode<W> edge) {
		if (v.minEdge == null) {
			v.minEdge = edge;
			v.heapHandle = heap.insert(v);

		} else if (weightCmp.compare(edge.data.weight, v.minEdge.data.weight) < 0) {
			v.minEdge = edge;
			heap.decreaseKey(v.heapHandle, v);
		}
	}

	public int getParent(int v) {
		checkNodeId(v);
		return parent[v];
	}

	public boolean hasNonTreeEdge() {
		return !heap.isEmpty();
	}

	public Edge<W> findMinNonTreeEdge() {
		return heap.isEmpty() ? null : heap.findMin().minEdge.data;
	}

	public boolean isSameSubTree(int u, int v) {
		checkNodeId(u);
		checkNodeId(v);
		return uf.find(u) == uf.find(v);
	}

	public void clear() {
		int size = size();
		uf.clear();
		for (int i = 0; i < size; i++) {
			if (subtrees[i] == null)
				continue;
			subtrees[i].clear();
			subtrees[i] = null;
		}
		heap.clear();
		lca.clear();
		timestamp = 0;
	}

	private void checkNodeId(int node) {
		if (!(0 <= node && node < size()))
			throw new IllegalStateException("Illegal node identifier " + node);
	}

	public static class Edge<W> {
		public final int u, v;
		public final W weight;

		Edge(int u, int v, W weight) {
			this.u = u;
			this.v = v;
			this.weight = weight;
		}

		@Override
		public String toString() {
			return "(" + u + ", " + v + ", " + weight + ")";
		}

	}

	private SubTree<W> subTree(int node) {
		return subtrees[uf.find(node)];
	}

	private static class SubTree<W> {

		int top;
		int size;

		EdgeList<W>[] edges;
		EdgeNode<W> minEdge;
		HeapDirectAccessed.Handle<SubTree<W>> heapHandle;

		/* field used to detect redundant edges during merge */
		EdgeNode<W> inEdge;
		int inEdgeTimestamp;

		private static final EdgeList<?>[] EMPTY_EDGELIST_ARR = new EdgeList[0];

		@SuppressWarnings("unchecked")
		SubTree(int node) {
			top = node;
			size = 1;
			edges = (EdgeList<W>[]) EMPTY_EDGELIST_ARR;
		}

		int rank() {
			return Utils.log2(size);
		}

		void clear() {
			for (int i = 0; i < edges.length; i++) {
				if (edges[i] == null)
					continue;
				edges[i].clear();
				edges[i] = null;
			}
			if (minEdge != null)
				minEdge.clear();
			minEdge = null;
			heapHandle = null;
		}

	}

	private static class EdgeList<W> {
		EdgeNode<W> head, tail;
		EdgeNode<W> min;

		void clear() {
			head = tail = min = null;
		}
	}

	private static class EdgeNode<W> {
		final int u, v;
		final Edge<W> data;
		EdgeNode<W> next;

		EdgeNode(int u, int v, Edge<W> data) {
			this.u = u;
			this.v = v;
			this.data = data;
		}

		void clear() {
			next = null;
		}

		@Override
		public String toString() {
			return String.valueOf(data);
		}
	}

}
