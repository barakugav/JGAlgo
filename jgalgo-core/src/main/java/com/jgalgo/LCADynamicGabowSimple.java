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

/**
 * Gabow implementation for dynamic LCA data structure with \(O(\log^2 n)\) amortized time for {@code addLeaf()}
 * operation.
 * <p>
 * The running time of this algorithm for \(m\) operations over \(n\) nodes is \(O(m + n \log^2 n)\) and it uses linear
 * space. More specifically, the {@link #addLeaf(LCADynamic.Node)} operation is perform in \(O(\log^2 n)\) amortized
 * time and {@link #findLowestCommonAncestor(LCADynamic.Node, LCADynamic.Node)} is perform in constant time.
 * <p>
 * This implementation is used by the better linear LCA algorithm {@link LCADynamicGabowLinear} and rarely should be
 * used directly.
 * <p>
 * Based on the simpler data structure presented in 'Data Structures for Weighted Matching and Nearest Common Ancestors
 * with Linking' by Harold N. Gabow (1990).
 *
 * @author Barak Ugav
 */
class LCADynamicGabowSimple implements LCADynamic {

	private int nodesNum;

	/* Hyper parameters */
	private static final double alpha = 6.0 / 5.0;
	private static final double beta = 2 / (2 * alpha - 1);
	private static final int e = 4;
	private static final int c = 5;

	private static final NodeImpl[] EMPTY_NODE_ARR = new NodeImpl[0];

	/**
	 * Create a new dynamic LCA data structure that contains zero nodes.
	 */
	LCADynamicGabowSimple() {
		nodesNum = 0;
	}

	@Override
	public Node initTree() {
		if (size() != 0)
			throw new IllegalStateException();
		return newNode(null);
	}

	@Override
	public Node addLeaf(Node parent) {
		return newNode((NodeImpl) parent);
	}

	private Node newNode(NodeImpl parent) {
		nodesNum++;
		NodeImpl node = new NodeImpl(parent);

		node.isApex = true;
		if (parent != null) {
			parent.addChild(node);
			node.cParent = parent.getPathApex();
		}

		NodeImpl lastAncestorRequireCompress = null;
		for (NodeImpl a = node; a != null; a = a.cParent) {
			a.size++;
			if (a.isRequireRecompress())
				lastAncestorRequireCompress = a;
		}
		recompress(lastAncestorRequireCompress);

		return node;
	}

	private void recompress(NodeImpl subtreeRoot) {
		/* first, compute the size of each node subtree */
		computeSize(subtreeRoot);

		/* actual recompress */
		buildCompressedTree(subtreeRoot);
		assert subtreeRoot.isApex;

		/* recompute ancestor tables */
		computeAncestorTables(subtreeRoot);
	}

	private int computeSize(NodeImpl node) {
		int size = 1;
		for (int i = 0; i < node.childrenNum; i++)
			size += computeSize(node.children[i]);
		return node.size = size;
	}

	private void buildCompressedTree(NodeImpl node) {
		node.cParent = node.isRoot() ? null : node.parent.getPathApex();

		node.path = node.isApex ? EMPTY_NODE_ARR : null;
		node.pathSize = 0;
		node.pathIdx = node.getPathApex().pathSize;
		node.getPathApex().addToPath(node);

		node.sigma = node.isApex ? node.size : 1;
		double /* integer */ v = pow(node.sigma, e);
		node.idxLowerFat = node.isRoot() ? 0 : node.cParent.idxUpperFatMaxChild;
		node.idxUpperFat = node.idxLowerFat + c * v;
		node.idxLower = node.idxLowerFat + v;
		node.idxUpper = node.idxUpperFat - v;
		assert node.isRoot() || node.idxUpperFat <= node.cParent.idxUpperFat;
		if (!node.isRoot())
			node.cParent.idxUpperFatMaxChild = node.idxUpperFat;

		node.idxUpperFatMaxChild = node.idxLower + 1;

		for (int i = 0; i < node.childrenNum; i++) {
			NodeImpl child = node.children[i];
			child.isApex = child.size <= node.size / 2;
			buildCompressedTree(child);
		}
	}

	private void computeAncestorTables(NodeImpl node) {
		int ancestorTableSize = logBetaFloor(c * pow(nodesNum, e));
		node.ancestorTableInit(ancestorTableSize);

		int tableIdx = 0;
		for (NodeImpl a = node, last = null;; a = a.cParent) {
			for (; (c - 2) * pow(a.sigma, e) >= pow(beta, tableIdx); tableIdx++)
				if (last != null)
					node.ancestorTable[tableIdx] = last;
			if (a.isRoot()) {
				for (; tableIdx < ancestorTableSize; tableIdx++)
					if ((c - 2) * pow(a.sigma, e) < pow(beta, tableIdx))
						node.ancestorTable[tableIdx] = a;
				break;
			}
			last = a;
		}

		for (int i = 0; i < node.childrenNum; i++)
			computeAncestorTables(node.children[i]);
	}

	private static double /* integer */ pow(double /* integer */ a, double /* integer */ b) {
		return assertOverflowDouble(Math.pow(a, b));
	}

	private static int logBetaFloor(double x) {
		return assertOverflowInt(Math.floor(Math.log(x) / Math.log(beta)));
	}

	private static double assertOverflowDouble(double x) {
		assert Double.isFinite(x);
		return x;
	}

	private static int assertOverflowInt(double x) {
		assert Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE;
		return (int) x;
	}

	private static CharacteristicAncestors0 calcCACompressed(NodeImpl x, NodeImpl y) {
		if (x == y)
			return new CharacteristicAncestors0(x, x, x);
		int i = logBetaFloor(Math.abs(x.idxLower - y.idxLower));

		NodeImpl[] a = new NodeImpl[2];
		NodeImpl[] az = new NodeImpl[2];
		for (int zIdx = 0; zIdx < 2; zIdx++) {
			NodeImpl z = zIdx == 0 ? x : y;
			NodeImpl z0 = zIdx == 0 ? y : x;

			NodeImpl v = z.ancestorTable[i];
			NodeImpl w = v != null ? v.cParent : z;

			NodeImpl b, bz;
			if ((c - 2) * pow(w.sigma, e) > Math.abs(x.idxLower - y.idxLower)) {
				b = w;
				bz = v != null ? v : z;
			} else {
				b = w.cParent;
				bz = w;
			}

			if (b.idxLower <= z0.idxLower && z0.idxLower < b.idxUpper) { /* b is an ancestor of z0 */
				a[zIdx] = b;
				az[zIdx] = bz;
			} else {
				a[zIdx] = b.cParent;
				az[zIdx] = b;
			}
		}

		NodeImpl ax = az[0], ay = az[1];
		assert a[0] == a[1];
		assert ax == a[0] || ax.cParent == a[0];
		assert ay == a[0] || ay.cParent == a[0];
		return new CharacteristicAncestors0(a[0], ax, ay);
	}

	private static CharacteristicAncestors0 calcCA0(NodeImpl x, NodeImpl y) {
		if (x == y)
			return new CharacteristicAncestors0(x, x, x);
		CharacteristicAncestors0 cac = calcCACompressed(x, y);

		/* c is an apex of path P */
		NodeImpl c = cac.a, cx = cac.ax, cy = cac.ay;
		assert c == c.getPathApex();

		/* bz is the first ancestor of cz on P */
		NodeImpl bx = cx != c && cx.isApex ? cx.parent : cx;
		NodeImpl by = cy != c && cy.isApex ? cy.parent : cy;
		assert c == bx.getPathApex();
		assert c == by.getPathApex();

		/* a is the shallower vertex of bx and by */
		NodeImpl a = bx.pathIdx < by.pathIdx ? bx : by;

		NodeImpl ax = a != bx ? a.getPathApex().path[a.pathIdx + 1] : cx;
		NodeImpl ay = a != by ? a.getPathApex().path[a.pathIdx + 1] : cy;

		assert ax == a || ax.parent == a;
		assert ay == a || ay.parent == a;
		return new CharacteristicAncestors0(a, ax, ay);
	}

	@Override
	public Node findLowestCommonAncestor(Node x, Node y) {
		return calcCA0((NodeImpl) x, (NodeImpl) y).a;
	}

	CharacteristicAncestors calcCA(Node x, Node y) {
		CharacteristicAncestors0 ca = calcCA0((NodeImpl) x, (NodeImpl) y);
		return new CharacteristicAncestors(ca.a, ca.ax, ca.ay);
	}

	@Override
	public int size() {
		return nodesNum;
	}

	@Override
	public void clear() {
		nodesNum = 0;
	}

	private static class NodeImpl implements Node {
		/* --- user tree data --- */
		Object nodeData;
		/* tree parent */
		final NodeImpl parent;
		/* children nodes of this node */
		NodeImpl[] children;
		int childrenNum;
		/* number of nodes in subtree */
		int size;

		/* --- compressed tree data --- */
		/* parent in the compressed tree */
		NodeImpl cParent;
		/* If the node is apex, contains all the nodes in it's path, else null */
		NodeImpl[] path;
		int pathSize;
		/* Index of the node within it's path */
		int pathIdx;
		/* p */
		double /* integer */ idxLower;
		/* q */
		double /* integer */ idxUpper;
		/* p bar */
		double /* integer */ idxLowerFat;
		/* q bar */
		double /* integer */ idxUpperFat;
		/* Q bar */
		double /* integer */ idxUpperFatMaxChild;
		/* sigma */
		int sigma;
		/* flag for head (shallower) of path node */
		boolean isApex;
		/* ancestor table */
		NodeImpl[] ancestorTable;

		NodeImpl(NodeImpl parent) {
			this.parent = parent;
			children = EMPTY_NODE_ARR;
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

		boolean isRoot() {
			assert !(parent == null ^ cParent == null);
			return parent == null;
		}

		void addChild(NodeImpl c) {
			if (childrenNum >= children.length)
				children = Arrays.copyOf(children, Math.max(children.length * 2, 2));
			children[childrenNum++] = c;
		}

		NodeImpl getPathApex() {
			return isApex ? this : cParent;
		}

		void addToPath(NodeImpl c) {
			if (pathSize >= path.length)
				path = Arrays.copyOf(path, Math.max(path.length * 2, 2));
			path[pathSize++] = c;
		}

		boolean isRequireRecompress() {
			return size >= alpha * sigma;
		}

		void ancestorTableInit(int size) {
			if (ancestorTable != null && ancestorTable.length >= size)
				Arrays.fill(ancestorTable, null);
			else
				ancestorTable = new NodeImpl[size];
		}

		@Override
		public String toString() {
			return "V" + (isApex ? "*" : "") + "<" + getNodeData() + ">";
		}

		// void clear() {
		// parent = cParent = null;
		// Arrays.fill(children, 0, childrenNum, null);
		// Arrays.fill(path, 0, pathSize, null);
		// Arrays.fill(ancestorTable, null);
		// children = path = ancestorTable = null;
		// }

	}

	private static class CharacteristicAncestors0 {
		NodeImpl a, ax, ay;

		CharacteristicAncestors0(NodeImpl a, NodeImpl ax, NodeImpl ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

	static class CharacteristicAncestors {
		final Node a, ax, ay;

		CharacteristicAncestors(Node a, Node ax, Node ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

}
