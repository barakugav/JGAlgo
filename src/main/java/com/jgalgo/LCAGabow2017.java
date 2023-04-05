package com.jgalgo;

import java.util.Arrays;

import com.jgalgo.LCAGabowSimple.CharacteristicAncestors;

public class LCAGabow2017<V> implements LCADynamic<V> {

	/**
	 * This implementation is a dynamic LCA implementation from Gabow17, which
	 * allows addLeaf and LCA queries, with addLeaf O(1) amortized and LCA query
	 * O(1).
	 *
	 * This is an extension to the simple LCA algorithm of Gabow. It uses the simple
	 * implementation as a black box, and adds two layers of trees, where each
	 * layers have less nodes by a factor of O(log n), decreasing the total time
	 * from O(m + log^2 n) to O(m + n).
	 *
	 * implementation note: in the original paper, Gabow stated to use look tables
	 * for the bit tricks (lsb, msb). It's possible to do so, using BitsLookupTable,
	 * but the standard Java implementation already perform these operations in
	 * constant time (less than 10 operations).
	 */

	private int nodes2Num;
	private final LCAGabowSimple<Node0<V>> lca0;

	private static final int SUB_TREE_MAX_SIZE = Integer.SIZE;

	public LCAGabow2017() {
		lca0 = new LCAGabowSimple<>();
	}

	@Override
	public Node<V> initTree(V nodeData) {
		if (size() != 0)
			throw new IllegalStateException();
		return newNode2(null, nodeData);
	}

	@Override
	public Node<V> addLeaf(Node<V> parent, V nodeData) {
		return newNode2((Node2<V>) parent, nodeData);
	}

	private Node2<V> newNode2(Node2<V> parent, V nodeData) {
		Node2<V> node = new Node2<>(parent, nodeData);

		if (parent == null || parent.subTree.isFull()) {
			/* make the new node a root of a new sub tree */
			node.subTree = new Node1<>(node);
		} else {
			/* add new node to the parent sub tree */
			node.subTree = parent.subTree;
			node.ancestorsBitmap = parent.ancestorsBitmap;
		}

		node.subTree.addNode(node);
		node.ancestorsBitmap |= ithBit(node.idWithinSubTree);

		if (node.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Node2<V> topParent = node.subTree.top.parent;
			Node1<V> tparent = topParent != null ? topParent.subTree : null;
			addFullNode1(node.subTree, tparent);
		}

		return node;
	}

	private void addFullNode1(Node1<V> node, Node1<V> parent) {
		if (parent == null || parent.subTree.isFull()) {
			/* make the new node a root of a new sub tree */
			node.subTree = new Node0<>(node);
		} else {
			/* add new node to the parent sub tree */
			node.subTree = parent.subTree;
			node.ancestorsBitmap = parent.ancestorsBitmap;
		}

		node.subTree.addNode(node);
		node.ancestorsBitmap |= ithBit(node.idWithinSubTree);

		if (node.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Node1<V> topParent = node.subTree.top.getParent();
			Node0<V> tparent = topParent != null ? topParent.subTree : null;
			addFullNode0(node.subTree, tparent);
		}
	}

	private void addFullNode0(Node0<V> node, Node0<V> parent) {
		if (parent == null) {
			node.lcaId = lca0.initTree(node);
		} else {
			node.lcaId = lca0.addLeaf(parent.lcaId, node);
		}
	}

	private Node2<V> calcLCA(Node2<V> x2, Node2<V> y2) {
		if (x2.subTree != y2.subTree) {
			if (!x2.subTree.isFull())
				x2 = x2.subTree.top.parent;
			if (!y2.subTree.isFull())
				y2 = y2.subTree.top.parent;

			/* Calculate CAs in the next level tree */
			Node1<V> x1 = x2.subTree, y1 = y2.subTree;
			Node1<V> ax1 = null, ay1 = null;
			if (x1.subTree != y1.subTree) {
				if (!x1.subTree.isFull()) {
					ax1 = x1.subTree.top;
					x1 = x1.subTree.top.getParent();
				}
				if (!y1.subTree.isFull()) {
					ay1 = y1.subTree.top;
					y1 = y1.subTree.top.getParent();
				}

				/* Calculate CAs in the next level tree */
				Node0<V> x0 = x1.subTree, y0 = y1.subTree;
				CharacteristicAncestors<Node0<V>> ca0 = lca0.calcCA(x0.lcaId, y0.lcaId);
				Node0<V> a0 = ca0.a.getNodeData(), ax0 = ca0.ax.getNodeData(), ay0 = ca0.ay.getNodeData();

				if (a0 != ax0) {
					ax1 = ax0.top;
					x1 = ax0.top.getParent();
				}
				if (a0 != ay0) {
					ay1 = ay0.top;
					y1 = ay0.top.getParent();
				}
			}

			assert x1.subTree == y1.subTree;
			/* calculate LCA within sub tree */
			int commonAncestors1 = x1.ancestorsBitmap & y1.ancestorsBitmap;
			Node1<V> a1 = x1.subTree.nodes[31 - Integer.numberOfLeadingZeros(commonAncestors1)];
			if (a1 != x1) {
				int x1UncommonAncestors = x1.ancestorsBitmap & ~y1.ancestorsBitmap;
				ax1 = x1.subTree.nodes[Integer.numberOfTrailingZeros(x1UncommonAncestors)];
			} else if (ax1 == null) {
				ax1 = x1;
			}
			if (a1 != y1) {
				int x1UncommonAncestors = ~x1.ancestorsBitmap & y1.ancestorsBitmap;
				ay1 = x1.subTree.nodes[Integer.numberOfTrailingZeros(x1UncommonAncestors)];
			} else if (ay1 == null) {
				ay1 = y1;
			}

			if (a1 != ax1)
				x2 = ax1.top.parent;
			if (a1 != ay1)
				y2 = ay1.top.parent;
		}

		assert x2.subTree == y2.subTree;
		/* calculate LCA within sub tree */
		int commonAncestors = x2.ancestorsBitmap & y2.ancestorsBitmap;
		return x2.subTree.nodes[31 - Integer.numberOfLeadingZeros(commonAncestors)];
	}

	@Override
	public Node<V> calcLCA(Node<V> x, Node<V> y) {
		return calcLCA((Node2<V>) x, (Node2<V>) y);
	}

	@Override
	public int size() {
		return nodes2Num;
	}

	@Override
	public void clear() {
		lca0.clear();
	}

	private static class Node2<V> implements LCADynamic.Node<V> {
		V nodeData;

		/* level 2 info */
		final Node2<V> parent;
		Node1<V> subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		Node2(Node2<V> parent, V nodeData) {
			this.parent = parent;
			this.nodeData = nodeData;
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

//		void clear() {
//			subTree = null;
//		}
	}

	private static class Node1<V> {
		/* level 2 info */
		final Node2<V> top;
		Node2<V>[] nodes;
		int size;

		/* level 1 info */
		Node0<V> subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		@SuppressWarnings("unchecked")
		Node1(Node2<V> top) {
			this.top = top;
			nodes = new Node2[4];
		}

		Node1<V> getParent() {
			return top.parent != null ? top.parent.subTree : null;
		}

		void addNode(Node2<V> node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

//		void clear() {
//			nodes = null;
//			subTree = null;
//		}
	}

	private static class Node0<V> {
		/* level 1 info */
		final Node1<V> top;
		Node1<V>[] nodes;
		int size;

		/* level 0 info */
		LCADynamic.Node<Node0<V>> lcaId;

		@SuppressWarnings("unchecked")
		Node0(Node1<V> top) {
			this.top = top;
			nodes = new Node1[4];
		}

		void addNode(Node1<V> node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

//		void clear() {
//			nodes = null;
//		}
	}

	private static int ithBit(int b) {
		assert 0 <= b && b < Integer.SIZE;
		return 1 << b;
	}

}
