package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.LCAGabowSimple.CharacteristicAncestors;

public class LCAGabow2017 implements LCADynamic {

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

	/* nodes of layer 2, the actual user tree */
	private int nodes2Num;
	private Node2[] nodes2;

	/* nodes of layer 1, O(n / log n) nodes */
	private int nodes1Num;
	private Node1[] nodes1;

	/* nodes of layer 0, O(n / log^2 n) nodes */
	private int nodes0Num;
	private Node0[] nodes0;

	/* underlying LCA implementation for layer 0 */
	private final LCAGabowSimple lca0;

	private static final int SUB_TREE_MAX_SIZE = 32;

	private static final Node2[] EMPTY_NODE2_ARR = new Node2[0];
	private static final Node1[] EMPTY_NODE1_ARR = new Node1[0];
	private static final Node0[] EMPTY_NODE0_ARR = new Node0[0];

	public LCAGabow2017() {
		nodes2Num = 0;
		nodes2 = EMPTY_NODE2_ARR;
		nodes1Num = 0;
		nodes1 = EMPTY_NODE1_ARR;
		nodes0Num = 0;
		nodes0 = EMPTY_NODE0_ARR;
		lca0 = new LCAGabowSimple();
	}

	@Override
	public int initTree() {
		if (size() != 0)
			throw new IllegalStateException();
		return newNode2(null).id2;
	}

	@Override
	public int addLeaf(int parent) {
		if (!(0 <= parent && parent < nodes2Num))
			throw new IllegalArgumentException("invalid parent id " + parent);
		return newNode2(nodes2[parent]).id2;
	}

	private Node2 newNode2(Node2 parent) {
		Node2 node = new Node2(nodes2Num++, parent);
		if (node.id2 >= nodes2.length)
			nodes2 = Arrays.copyOf(nodes2, Math.max(nodes2.length * 2, 2));
		nodes2[node.id2] = node;

		if (parent == null || parent.subTree.isFull()) {
			/* make the new node a root of a new sub tree */
			node.subTree = new Node1(node);
		} else {
			/* add new node to the parent sub tree */
			node.subTree = parent.subTree;
			node.ancestorsBitmap = parent.ancestorsBitmap;
		}

		node.subTree.addNode(node);
		node.ancestorsBitmap |= ithBit(node.idWithinSubTree);

		if (node.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Node2 topParent = node.subTree.top.parent;
			Node1 tparent = topParent != null ? topParent.subTree : null;
			addFullNode1(node.subTree, tparent);
		}

		return node;
	}

	private void addFullNode1(Node1 node, Node1 parent) {
		node.id1 = nodes1Num++;
		if (node.id1 >= nodes1.length)
			nodes1 = Arrays.copyOf(nodes1, Math.max(nodes1.length * 2, 2));
		nodes1[node.id1] = node;

		if (parent == null || parent.subTree.isFull()) {
			/* make the new node a root of a new sub tree */
			node.subTree = new Node0(node);
		} else {
			/* add new node to the parent sub tree */
			node.subTree = parent.subTree;
			node.ancestorsBitmap = parent.ancestorsBitmap;
		}

		node.subTree.addNode(node);
		node.ancestorsBitmap |= ithBit(node.idWithinSubTree);

		if (node.subTree.isFull()) {
			/* new full sub tree, add to next level tree */
			Node1 topParent = node.subTree.top.getParent();
			Node0 tparent = topParent != null ? topParent.subTree : null;
			addFullNode0(node.subTree, tparent);
		}
	}

	private void addFullNode0(Node0 node, Node0 parent) {
		if (parent == null) {
			assert nodes0Num == 0;
			node.id0 = lca0.initTree();
		} else {
			node.id0 = lca0.addLeaf(parent.id0);
		}
		assert node.id0 == nodes0Num;
		nodes0Num++;

		if (node.id0 >= nodes0.length)
			nodes0 = Arrays.copyOf(nodes0, Math.max(nodes0.length * 2, 2));
		nodes0[node.id0] = node;
	}

	private Node2 calcLCA(Node2 x2, Node2 y2) {
		if (x2.subTree != y2.subTree) {
			if (!x2.subTree.isFull())
				x2 = x2.subTree.top.parent;
			if (!y2.subTree.isFull())
				y2 = y2.subTree.top.parent;

			/* Calculate CAs in the next level tree */
			Node1 x1 = x2.subTree, y1 = y2.subTree;
			Node1 ax1 = null, ay1 = null;
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
				Node0 x0 = x1.subTree, y0 = y1.subTree;
				CharacteristicAncestors ca0 = lca0.calcCA(x0.id0, y0.id0);
				Node0 a0 = nodes0[ca0.a], ax0 = nodes0[ca0.ax], ay0 = nodes0[ca0.ay];

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
			Node1 a1 = nodes1[x1.subTree.nodes[31 - Integer.numberOfLeadingZeros(commonAncestors1)]];
			if (a1 != x1) {
				int x1UncommonAncestors = x1.ancestorsBitmap & ~y1.ancestorsBitmap;
				ax1 = nodes1[x1.subTree.nodes[Integer.numberOfTrailingZeros(x1UncommonAncestors)]];
			} else if (ax1 == null) {
				ax1 = x1;
			}
			if (a1 != y1) {
				int x1UncommonAncestors = ~x1.ancestorsBitmap & y1.ancestorsBitmap;
				ay1 = nodes1[x1.subTree.nodes[Integer.numberOfTrailingZeros(x1UncommonAncestors)]];
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
		return nodes2[x2.subTree.nodes[31 - Integer.numberOfLeadingZeros(commonAncestors)]];
	}

	@Override
	public int calcLCA(int x, int y) {
		return calcLCA(nodes2[x], nodes2[y]).id2;
	}

	@Override
	public int size() {
		return nodes2Num;
	}

	@Override
	public void clear() {
		lca0.clear();

		for (int i = 0; i < nodes0Num; i++)
			nodes0[i].clear();
		Arrays.fill(nodes0, 0, nodes0Num, null);
		nodes0Num = 0;

		for (int i = 0; i < nodes1Num; i++)
			nodes1[i].clear();
		Arrays.fill(nodes1, 0, nodes1Num, null);
		nodes1Num = 0;

		for (int i = 0; i < nodes2Num; i++)
			nodes2[i].clear();
		Arrays.fill(nodes2, 0, nodes2Num, null);
		nodes2Num = 0;
	}

	private static class Node2 {
		/* level 2 info */
		final int id2;
		final Node2 parent;
		Node1 subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		Node2(int id, Node2 parent) {
			this.id2 = id;
			this.parent = parent;
		}

		void clear() {
			subTree = null;
		}
	}

	private static class Node1 {
		/* level 2 info */
		final Node2 top;
		int[] nodes;
		int size;

		/* level 1 info */
		int id1;
		Node0 subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		Node1(Node2 top) {
			this.top = top;
			id1 = -1;
			nodes = new int[4];
		}

		Node1 getParent() {
			return top.parent != null ? top.parent.subTree : null;
		}

		void addNode(Node2 node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node.id2;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		void clear() {
			nodes = null;
			subTree = null;
		}
	}

	private static class Node0 {
		/* level 1 info */
		final Node1 top;
		int[] nodes;
		int size;

		/* level 0 info */
		int id0;

		Node0(Node1 top) {
			this.top = top;
			id0 = -1;
			nodes = new int[4];
		}

		void addNode(Node1 node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node.id1;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		void clear() {
			nodes = null;
		}
	}

	private static int ithBit(int b) {
		assert 0 <= b && b < Integer.SIZE;
		return 1 << b;
	}

}
