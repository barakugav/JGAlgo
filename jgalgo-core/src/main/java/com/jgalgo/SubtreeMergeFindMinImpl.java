/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of the {@link SubtreeMergeFindMin} data structure.
 * <p>
 * AddLeaf is \(O(1)\) amortized, merge is \(O(\log n)\), addNonTreeEdge is \(O(1)\) and findMin is \(O(1)\). In total,
 * this data structure support \(m\) operations on \(n\) nodes in time \(O(m + n \log n)\).
 *
 * @author Barak Ugav
 */
class SubtreeMergeFindMinImpl<E> implements SubtreeMergeFindMin<E> {

	private NodeImpl<E>[] nodes;
	private final UnionFind uf;
	private final HeapReferenceable<SubTree<E>, Void> heap;
	private final LCADynamic lca;

	private final Comparator<? super E> weightCmp;
	private int timestamp;

	/**
	 * Create a new SMF data structure with the {@linkplain Comparable natural ordering} comparator for edge weights.
	 */
	SubtreeMergeFindMinImpl() {
		this(null);
	}

	/**
	 * Create a new SMF data structure with the provided comparator for edge weights.
	 *
	 * @param weightCmp comparator used to compare edge weights.
	 */
	SubtreeMergeFindMinImpl(Comparator<? super E> weightCmp) {
		this(weightCmp, HeapReferenceable.newBuilder());
	}

	/**
	 * Create a new SMF data structure with the provided comparator for edge weights and a custom heap implementation.
	 *
	 * @param weightCmp   comparator used to compare edge weights.
	 * @param heapBuilder heap builder used to provide a custom heap implementation.
	 */
	@SuppressWarnings("unchecked")
	SubtreeMergeFindMinImpl(Comparator<? super E> weightCmp, HeapReferenceable.Builder<?, ?> heapBuilder) {
		nodes = new NodeImpl[2];

		uf = UnionFind.newBuilder().build();
		lca = new LCADynamicGabowLinear();

		this.weightCmp = weightCmp != null ? weightCmp : Utils.getDefaultComparator();
		timestamp = 0;

		heap = heapBuilder.<SubTree<E>>keysTypeObj().valuesTypeVoid()
				.build((t1, t2) -> this.weightCmp.compare(t1.minEdge.data.data, t2.minEdge.data.data));
	}

	@Override
	public Node initTree() {
		if (size() != 0)
			throw new IllegalStateException("Tree is not empty");
		return newNode(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Node addLeaf(Node parent) {
		return newNode((NodeImpl<E>) parent);
	}

	private Node newNode(NodeImpl<E> parent) {
		NodeImpl<E> node = new NodeImpl<>(parent);

		/* Add to LCA data structure */
		node.lcaNode = parent == null ? lca.initTree() : lca.addLeaf(parent.lcaNode);
		node.lcaNode.setNodeData(node);

		/* Add to UF data structure */
		node.ufIdx = uf.make();

		if (node.ufIdx >= nodes.length)
			nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
		nodes[node.ufIdx] = node;

		return node;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSameSubTree(Node u, Node v) {
		return uf.find(((NodeImpl<E>) u).ufIdx) == uf.find(((NodeImpl<E>) v).ufIdx);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mergeSubTrees(Node u0, Node v0) {
		NodeImpl<E> u = (NodeImpl<E>) u0, v = (NodeImpl<E>) v0;
		SubTree<E> U = nodes[uf.find(u.ufIdx)].subtree, V = nodes[uf.find(v.ufIdx)].subtree;
		if (U == V)
			return;

		/* assume U is above V */
		if (U.top.depth > V.top.depth) {
			SubTree<E> temp = U;
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
		EdgeList<E>[] uEdges = U.edges, vEdges = V.edges;
		U.edges = Arrays.copyOf(U.edges, maxSet);
		Arrays.fill(U.edges, null);
		if (U.heapRef != null)
			heap.remove(U.heapRef);
		if (V.heapRef != null)
			heap.remove(V.heapRef);
		U.heapRef = V.heapRef = null;
		U.minEdge = V.minEdge = null;

		/* fix current timestamp to identify redundant edges */
		final int t = ++timestamp;

		/* All edges in sets >= r+1 are good, just union */
		for (int setIdx = rank + 1; setIdx < maxSet; setIdx++) {
			EdgeList<E> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<E> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			EdgeList<E> el = concatenateEdgeLists(el1, el2);
			if (el == null)
				continue;
			U.edges[setIdx] = el;
			compareAgainstSubTreeMin(U, el.min);
		}
		/* Examine all edges in set <= r */
		for (int setIdx = 0; setIdx <= rank; setIdx++) {
			EdgeList<E> el1 = uEdges.length > setIdx ? uEdges[setIdx] : null;
			EdgeList<E> el2 = vEdges.length > setIdx ? vEdges[setIdx] : null;
			for (EdgeList<E> el : new EdgeList[] { el1, el2 }) {
				if (el == null)
					continue;
				for (EdgeNode<E> edge, next; (edge = el.head) != null; el.head = next) {
					next = edge.next;
					edge.next = null;

					SubTree<E> eU = subTree(edge.u), eV = subTree(edge.v);
					if (eU == eV) {
						/* edge is bad */
						edge.clear();
						continue;
					}

					/* assume eU is U */
					if (eU != U) {
						SubTree<E> temp = eU;
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

	private EdgeList<E> concatenateEdgeLists(EdgeList<E> el1, EdgeList<E> el2) {
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
	public void addNonTreeEdge(Node u0, Node v0, E edgedata) {
		if (u0 == v0)
			return;
		@SuppressWarnings("unchecked")
		NodeImpl<E> u = (NodeImpl<E>) u0, v = (NodeImpl<E>) v0;

		/* assume u is above v */
		if (u.depth > v.depth) {
			NodeImpl<E> temp = u;
			u = v;
			v = temp;
		}
		Edge<E> edge = new Edge<>(u, v, edgedata);

		/* split edge into two edges (u, lca(u,v)), (v, lca(u,v)) */
		LCADynamic.Node l = lca.findLowestCommonAncestor(u.lcaNode, v.lcaNode);
		if (u.lcaNode != l) {
			addEdgeNode(new EdgeNode<>(u, l.getNodeData(), edge));
			addEdgeNode(new EdgeNode<>(v, l.getNodeData(), edge));
		} else {
			addEdgeNode(new EdgeNode<>(v, u, edge));
		}
	}

	private void addEdgeNode(EdgeNode<E> edge) {
		/* assume u is deeper */
		assert edge.u.depth > edge.v.depth;

		SubTree<E> u = subTree(edge.u), v = subTree(edge.v);
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

	private void addEdgeToSet(SubTree<E> v, int setIdx, EdgeNode<E> edge) {
		if (v.edges.length <= setIdx) {
			int newLen = v.edges.length;
			do {
				newLen = Math.max(newLen * 2, 2);
			} while (newLen <= setIdx);
			v.edges = Arrays.copyOf(v.edges, newLen);
		}
		if (v.edges[setIdx] == null)
			v.edges[setIdx] = new EdgeList<>();
		EdgeList<E> edgeList = v.edges[setIdx];

		if (edgeList.head == null) {
			edgeList.head = edgeList.tail = edgeList.min = edge;
			compareAgainstSubTreeMin(v, edge);
		} else {
			edgeList.tail.next = edge;
			edgeList.tail = edge;
			if (weightCmp.compare(edge.data.data, edgeList.min.data.data) < 0) {
				edgeList.min = edge;
				compareAgainstSubTreeMin(v, edge);
			}
		}
	}

	private void compareAgainstSubTreeMin(SubTree<E> v, EdgeNode<E> edge) {
		if (v.minEdge == null) {
			v.minEdge = edge;
			v.heapRef = heap.insert(v);

		} else if (weightCmp.compare(edge.data.data, v.minEdge.data.data) < 0) {
			v.minEdge = edge;
			heap.decreaseKey(v.heapRef, v);
		}
	}

	@Override
	public boolean hasNonTreeEdge() {
		return !heap.isEmpty();
	}

	@Override
	public MinEdge<E> findMinNonTreeEdge() {
		return heap.isEmpty() ? null : heap.findMin().key().minEdge.data;
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

	private SubTree<E> subTree(NodeImpl<E> node) {
		return nodes[uf.find(node.ufIdx)].subtree;
	}

	private static class NodeImpl<E> implements Node {

		private Object nodeData;
		private final NodeImpl<E> parent;
		private final int depth;
		private int ufIdx;
		private LCADynamic.Node lcaNode;
		private SubTree<E> subtree;

		NodeImpl(NodeImpl<E> parent) {
			this.parent = parent;
			depth = parent != null ? parent.depth + 1 : 0;
			subtree = new SubTree<>(this);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> V getNodeData() {
			return (V) nodeData;
		}

		@Override
		public void setNodeData(Object data) {
			nodeData = data;
		}

		@Override
		public Node getParent() {
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

	private static class SubTree<E> {

		NodeImpl<E> top;
		int size;

		EdgeList<E>[] edges;
		EdgeNode<E> minEdge;
		HeapReference<SubTree<E>, Void> heapRef;

		/* field used to detect redundant edges during merge */
		EdgeNode<E> inEdge;
		int inEdgeTimestamp;

		private static final EdgeList<?>[] EMPTY_EDGELIST_ARR = new EdgeList[0];

		@SuppressWarnings("unchecked")
		SubTree(NodeImpl<E> node) {
			top = node;
			size = 1;
			edges = (EdgeList<E>[]) EMPTY_EDGELIST_ARR;
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
			heapRef = null;
		}

	}

	private static class EdgeList<E> {
		EdgeNode<E> head, tail;
		EdgeNode<E> min;

		void clear() {
			head = tail = min = null;
		}
	}

	private static class EdgeNode<E> {
		final NodeImpl<E> u, v;
		final Edge<E> data;
		EdgeNode<E> next;

		EdgeNode(NodeImpl<E> u, NodeImpl<E> v, Edge<E> data) {
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

	private static class Edge<E> implements MinEdge<E> {
		public final Node u, v;
		public final E data;

		Edge(Node u, Node v, E data) {
			this.u = u;
			this.v = v;
			this.data = data;
		}

		@Override
		public String toString() {
			return "(" + u + ", " + v + ", " + data + ")";
		}

		@Override
		public Node source() {
			return u;
		}

		@Override
		public Node target() {
			return v;
		}

		@Override
		public E edgeData() {
			return data;
		}

	}

}
