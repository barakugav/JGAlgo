package com.ugav.algo;

import java.util.Arrays;
import java.util.Comparator;

public class SubtreeMergeFindminImpl<V, E> implements SubtreeMergeFindmin<V, E> {

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

	private NodeImpl<V, E>[] nodes;
	private final UnionFind uf;
	private final HeapDirectAccessed<SubTree<V, E>> heap;
	private final LCADynamic<NodeImpl<V, E>> lca;

	private final Comparator<? super E> weightCmp;
	private int timestamp;

	public SubtreeMergeFindminImpl() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public SubtreeMergeFindminImpl(Comparator<? super E> weightCmp) {
		nodes = new NodeImpl[2];

		uf = new UnionFindArray();
		lca = new LCAGabow2017<>();

		this.weightCmp = weightCmp != null ? weightCmp : Utils.getDefaultComparator();
		timestamp = 0;

		heap = new HeapFibonacci<>((t1, t2) -> this.weightCmp.compare(t1.minEdge.data.data, t2.minEdge.data.data));
	}

	@Override
	public Node<V> initTree(V nodeData) {
		if (size() != 0)
			throw new IllegalStateException("Tree is not empty");
		return newNode(null, nodeData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Node<V> addLeaf(Node<V> parent, V nodeData) {
		return newNode((NodeImpl<V, E>) parent, nodeData);
	}

	private Node<V> newNode(NodeImpl<V, E> parent, V nodeData) {
		NodeImpl<V, E> node = new NodeImpl<>(parent, nodeData);

		/* Add to LCA data structure */
		node.lcaNode = parent == null ? lca.initTree(node) : lca.addLeaf(parent.lcaNode, node);

		/* Add to UF data structure */
		node.ufIdx = uf.make();

		if (node.ufIdx >= nodes.length)
			nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
		nodes[node.ufIdx] = node;

		return node;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSameSubTree(Node<V> u, Node<V> v) {
		return uf.find(((NodeImpl<V, E>) u).ufIdx) == uf.find(((NodeImpl<V, E>) v).ufIdx);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mergeSubTrees(Node<V> u0, Node<V> v0) {
		NodeImpl<V, E> u = (NodeImpl<V, E>) u0, v = (NodeImpl<V, E>) v0;
		SubTree<V, E> U = nodes[uf.find(u.ufIdx)].subtree, V = nodes[uf.find(v.ufIdx)].subtree;
		if (U == V)
			return;

		/* assume U is above V */
		if (U.top.depth > V.top.depth) {
			SubTree<V, E> temp = U;
			U = V;
			V = temp;
		}
		if (V.top.parent == null || subTree(V.top.parent) != U)
			throw new IllegalArgumentException("Subtrees are not adjacent");

		/* update union find */
		int w = uf.union(U.top.ufIdx, V.top.ufIdx);
		nodes[w].subtree = U;

		U.size += V.size;
		int rank = U.rank(), maxSet = Math.max(U.edges.length, V.edges.length);
		EdgeList<V, E>[] uEdges = U.edges, vEdges = V.edges;
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
			EdgeList<V, E> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<V, E> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			EdgeList<V, E> el = concatenateEdgeLists(el1, el2);
			if (el == null)
				continue;
			U.edges[setIdx] = el;
			compareAgaintSubTreeMin(U, el.min);
		}
		/* Examine all edges in set <= r */
		for (int setIdx = 0; setIdx <= rank; setIdx++) {
			EdgeList<V, E> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<V, E> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			for (EdgeList<V, E> el : new EdgeList[] { el1, el2 }) {
				if (el == null)
					continue;
				for (EdgeNode<V, E> edge, next; (edge = el.head) != null; el.head = next) {
					next = edge.next;
					edge.next = null;

					SubTree<V, E> eU = subTree(edge.u), eV = subTree(edge.v);
					if (eU == eV) {
						/* edge is bad */
						edge.clear();
						continue;
					}

					/* assume eU is U */
					if (eU != U) {
						SubTree<V, E> temp = eU;
						eU = eV;
						eV = temp;
					}
					assert eU == U;

					if (eV.inEdgeTimestamp == t && weightCmp.compare(eV.inEdge.data.data, edge.data.data) <= 0) {
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

	private EdgeList<V, E> concatenateEdgeLists(EdgeList<V, E> el1, EdgeList<V, E> el2) {
		if (el1 == null)
			return el2;
		if (el2 == null)
			return el1;

		el1.tail.next = el2.head;
		el1.tail = el2.tail;
		if (weightCmp.compare(el2.min.data.data, el1.min.data.data) < 0)
			el1.min = el2.min;

		/* clear el2 */
		el2.clear();
		return el1;
	}

	@Override
	public void addNonTreeEdge(Node<V> u0, Node<V> v0, E edgedata) {
		if (u0 == v0)
			return;
		@SuppressWarnings("unchecked")
		NodeImpl<V, E> u = (NodeImpl<V, E>) u0, v = (NodeImpl<V, E>) v0;

		/* assume u is above v */
		if (u.depth > v.depth) {
			NodeImpl<V, E> temp = u;
			u = v;
			v = temp;
		}
		Edge<V, E> edge = new Edge<>(u, v, edgedata);

		/* split edge into two edges (u, lca(u,v)), (v, lca(u,v)) */
		LCADynamic.Node<NodeImpl<V, E>> l = lca.calcLCA(u.lcaNode, v.lcaNode);
		if (u.lcaNode != l) {
			addEdgeNode(new EdgeNode<>(u, l.getNodeData(), edge));
			addEdgeNode(new EdgeNode<>(v, l.getNodeData(), edge));
		} else {
			addEdgeNode(new EdgeNode<>(v, u, edge));
		}
	}

	private void addEdgeNode(EdgeNode<V, E> edge) {
		/* assume u is deeper */
		assert edge.u.depth > edge.v.depth;

		SubTree<V, E> u = subTree(edge.u), v = subTree(edge.v);
		if (u == v)
			return; /* ignore edge within same sub tree */

		int span = Utils.log2(edge.u.depth - edge.v.depth + 1);
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

	private void addEdgeToSet(SubTree<V, E> v, int setIdx, EdgeNode<V, E> edge) {
		if (v.edges.length <= setIdx) {
			int newLen = v.edges.length;
			do {
				newLen = Math.max(newLen * 2, 2);
			} while (newLen <= setIdx);
			v.edges = Arrays.copyOf(v.edges, newLen);
		}
		if (v.edges[setIdx] == null)
			v.edges[setIdx] = new EdgeList<>();
		EdgeList<V, E> edgeList = v.edges[setIdx];

		if (edgeList.head == null) {
			edgeList.head = edgeList.tail = edgeList.min = edge;
			compareAgaintSubTreeMin(v, edge);
		} else {
			edgeList.tail.next = edge;
			edgeList.tail = edge;
			if (weightCmp.compare(edge.data.data, edgeList.min.data.data) < 0) {
				edgeList.min = edge;
				compareAgaintSubTreeMin(v, edge);
			}
		}
	}

	private void compareAgaintSubTreeMin(SubTree<V, E> v, EdgeNode<V, E> edge) {
		if (v.minEdge == null) {
			v.minEdge = edge;
			v.heapHandle = heap.insert(v);

		} else if (weightCmp.compare(edge.data.data, v.minEdge.data.data) < 0) {
			v.minEdge = edge;
			heap.decreaseKey(v.heapHandle, v);
		}
	}

	@Override
	public boolean hasNonTreeEdge() {
		return !heap.isEmpty();
	}

	@Override
	public MinEdge<V, E> findMinNonTreeEdge() {
		return heap.isEmpty() ? null : heap.findMin().minEdge.data;
	}

	@Override
	public int size() {
		return uf.size();
	}

	@Override
	public void clear() {
		int size = size();
		uf.clear();
		for (int i = 0; i < size; i++) {
			nodes[i].clear();
			nodes[i] = null;
		}
		heap.clear();
		lca.clear();
		timestamp = 0;
	}

	private SubTree<V, E> subTree(NodeImpl<V, E> node) {
		return nodes[uf.find(node.ufIdx)].subtree;
	}

	private static class NodeImpl<V, E> implements Node<V> {

		private V nodeData;
		private final NodeImpl<V, E> parent;
		private final int depth;
		private int ufIdx;
		private LCADynamic.Node<NodeImpl<V, E>> lcaNode;
		private SubTree<V, E> subtree;

		NodeImpl(NodeImpl<V, E> parent, V nodeData) {
			this.parent = parent;
			this.nodeData = nodeData;
			depth = parent != null ? parent.depth + 1 : 0;
			subtree = new SubTree<>(this);
		}

		@Override
		public V getNodeData() {
			return nodeData;
		}

		@Override
		public void setNodeData(V data) {
			nodeData = data;
		}

		@Override
		public Node<V> getParent() {
			return parent;
		}

		void clear() {
			nodeData = null;
			lcaNode = null;
			if (subtree != null)
				subtree.clear();
			subtree = null;
		}

	}

	private static class SubTree<V, E> {

		NodeImpl<V, E> top;
		int size;

		EdgeList<V, E>[] edges;
		EdgeNode<V, E> minEdge;
		HeapDirectAccessed.Handle<SubTree<V, E>> heapHandle;

		/* field used to detect redundant edges during merge */
		EdgeNode<V, E> inEdge;
		int inEdgeTimestamp;

		private static final EdgeList<?, ?>[] EMPTY_EDGELIST_ARR = new EdgeList[0];

		@SuppressWarnings("unchecked")
		SubTree(NodeImpl<V, E> node) {
			top = node;
			size = 1;
			edges = (EdgeList<V, E>[]) EMPTY_EDGELIST_ARR;
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

	private static class EdgeList<V, E> {
		EdgeNode<V, E> head, tail;
		EdgeNode<V, E> min;

		void clear() {
			head = tail = min = null;
		}
	}

	private static class EdgeNode<V, E> {
		final NodeImpl<V, E> u, v;
		final Edge<V, E> data;
		EdgeNode<V, E> next;

		EdgeNode(NodeImpl<V, E> u, NodeImpl<V, E> v, Edge<V, E> data) {
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

	private static class Edge<V, E> implements MinEdge<V, E> {
		public final Node<V> u, v;
		public final E data;

		Edge(Node<V> u, Node<V> v, E data) {
			this.u = u;
			this.v = v;
			this.data = data;
		}

		@Override
		public String toString() {
			return "(" + u + ", " + v + ", " + data + ")";
		}

		@Override
		public Node<V> u() {
			return u;
		}

		@Override
		public Node<V> v() {
			return v;
		}

		@Override
		public E edgeData() {
			return data;
		}

	}

}
