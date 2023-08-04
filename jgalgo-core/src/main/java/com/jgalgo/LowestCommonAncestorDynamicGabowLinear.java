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

import com.jgalgo.LowestCommonAncestorDynamicGabowSimple.CharacteristicAncestors;

/**
 * Gabow linear dynamic LCA data structure.
 * <p>
 * The algorithm use {@link LowestCommonAncestorDynamicGabowSimple} as a base, but uses two layers of bit tricks to remove the \(O(\log^2
 * n)\) factor of the simpler data structure. Each layer have less nodes than the previous one by a factor of \(O(\log
 * n)\), until the simpler data structure is used on \(O(n / \log^2 n)\) nodes. This implementation is much faster in
 * practice and always should be used over the simpler one.
 * <p>
 * The running time of this algorithm for \(m\) operations is \(O(n + m)\) and it uses linear space. More specifically,
 * the {@link #addLeaf(LowestCommonAncestorDynamic.Node)} operation is perform in \(O(1)\) amortized time and
 * {@link #findLowestCommonAncestor(LowestCommonAncestorDynamic.Node, LowestCommonAncestorDynamic.Node)} is perform in
 * constant time.
 * <p>
 * Based on 'Data Structures for Weighted Matching and Nearest Common Ancestors with Linking' by Harold N. Gabow (1990).
 *
 * @author Barak Ugav
 */
class LowestCommonAncestorDynamicGabowLinear implements LowestCommonAncestorDynamic {

	/*
	 * implementation note: in the original paper, Gabow stated to use look tables for the bit tricks (lsb, msb). It's
	 * possible to do so, using BitsLookupTable, but the standard Java implementation already perform these operations
	 * in constant time (less than 10 operations).
	 */

	private int nodes2Num;
	private final LowestCommonAncestorDynamicGabowSimple lca0;

	private static final int SUB_TREE_MAX_SIZE = Integer.SIZE;

	/**
	 * Create a new dynamic LCA data structure that contains zero nodes.
	 */
	LowestCommonAncestorDynamicGabowLinear() {
		lca0 = new LowestCommonAncestorDynamicGabowSimple();
	}

	@Override
	public Node initTree() {
		if (size() != 0)
			throw new IllegalStateException();
		return newNode2(null);
	}

	@Override
	public Node addLeaf(Node parent) {
		return newNode2((Node2) parent);
	}

	private Node2 newNode2(Node2 parent) {
		Node2 node = new Node2(parent);

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
			(node.lcaId = lca0.initTree()).setNodeData(node);
		} else {
			(node.lcaId = lca0.addLeaf(parent.lcaId)).setNodeData(node);
		}
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
				CharacteristicAncestors ca0 = lca0.calcCA(x0.lcaId, y0.lcaId);
				Node0 a0 = ca0.a.getNodeData(), ax0 = ca0.ax.getNodeData(), ay0 = ca0.ay.getNodeData();

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
			Node1 a1 = x1.subTree.nodes[31 - Integer.numberOfLeadingZeros(commonAncestors1)];
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
	public Node findLowestCommonAncestor(Node x, Node y) {
		return calcLCA((Node2) x, (Node2) y);
	}

	@Override
	public int size() {
		return nodes2Num;
	}

	@Override
	public void clear() {
		lca0.clear();
	}

	private static class Node2 implements LowestCommonAncestorDynamic.Node {
		Object nodeData;

		/* level 2 info */
		final Node2 parent;
		Node1 subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		Node2(Node2 parent) {
			this.parent = parent;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <D> D getNodeData() {
			return (D) nodeData;
		}

		@Override
		public void setNodeData(Object data) {
			nodeData = data;
		}

		@Override
		public Node getParent() {
			return parent;
		}

		// void clear() {
		// subTree = null;
		// }
	}

	private static class Node1 {
		/* level 2 info */
		final Node2 top;
		Node2[] nodes;
		int size;

		/* level 1 info */
		Node0 subTree;
		int idWithinSubTree;
		int ancestorsBitmap;

		Node1(Node2 top) {
			this.top = top;
			nodes = new Node2[4];
		}

		Node1 getParent() {
			return top.parent != null ? top.parent.subTree : null;
		}

		void addNode(Node2 node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		// void clear() {
		// nodes = null;
		// subTree = null;
		// }
	}

	private static class Node0 {
		/* level 1 info */
		final Node1 top;
		Node1[] nodes;
		int size;

		/* level 0 info */
		LowestCommonAncestorDynamic.Node lcaId;

		Node0(Node1 top) {
			this.top = top;
			nodes = new Node1[4];
		}

		void addNode(Node1 node) {
			assert size < SUB_TREE_MAX_SIZE;
			node.idWithinSubTree = size++;
			if (node.idWithinSubTree >= nodes.length)
				nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
			nodes[node.idWithinSubTree] = node;
		}

		boolean isFull() {
			return size == SUB_TREE_MAX_SIZE;
		}

		// void clear() {
		// nodes = null;
		// }
	}

	private static int ithBit(int b) {
		assert 0 <= b && b < Integer.SIZE;
		return 1 << b;
	}

}
