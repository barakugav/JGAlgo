package com.ugav.algo;

import java.util.Arrays;

public class LCAGabowSimple implements LCADynamic {

	/**
	 * This implementation is a dynamic LCA implementation from Gabow17, which
	 * allows addLeaf and LCA queries, with addLeaf O(log^2 n) amortized and LCA
	 * query O(1).
	 */

	/* The number of nodes in the tree */
	private int n;
	/* Array of the tree nodes, indexed by id */
	private Node[] nodes;

	/* Hyper parameters */
	private static final double alpha = 6.0 / 5.0;
	private static final double beta = 2 / (2 * alpha - 1);
	private static final int e = 4;
	private static final int c = 5;

	private static final Node[] EMPTY_NODE_ARR = new Node[0];

	private static class Node {
		/* ID of the node */
		final int id;

		/* --- user tree data --- */
		/* tree parent */
		Node parent;
		/* children nodes of this node */
		Node[] children;
		int childrenNum;
		/* number of nodes in subtree */
		int size;

		/* --- compressed tree data --- */
		/* parent in the compressed tree */
		Node cParent;
		/* If the node is apex, contains all the nodes in it's path, else null */
		Node[] path;
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
		Node[] ancestorTable;

		Node(int id) {
			this.id = id;
			children = EMPTY_NODE_ARR;
		}

		boolean isRoot() {
			assert !(parent == null ^ cParent == null);
			return parent == null;
		}

		void addChild(Node c) {
			if (childrenNum >= children.length)
				children = Arrays.copyOf(children, Math.max(children.length * 2, 2));
			children[childrenNum++] = c;
		}

		Node getPathApex() {
			return isApex ? this : cParent;
		}

		void addToPath(Node c) {
			if (pathSize >= path.length)
				path = Arrays.copyOf(path, Math.max(path.length * 2, 2));
			path[pathSize++] = c;
		}

		boolean isRequireRecompress() {
			return size >= alpha * sigma;
		}

		void ancestorTableInit(int size) {
			ancestorTable = new Node[size];
		}

		@Override
		public String toString() {
			return "V" + (isApex ? "*" : "") + id;
		}

	}

	public LCAGabowSimple() {
		n = 0;
		nodes = EMPTY_NODE_ARR;
	}

	@Override
	public int initTree() {
		if (n != 0)
			throw new IllegalStateException();
		return newNode(null).id;
	}

	@Override
	public int addLeaf(int parent) {
		return newNode(nodes[parent]).id;
	}

	private Node newNode(Node parent) {
		Node node = new Node(n++);
		if (node.id >= nodes.length)
			nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
		nodes[node.id] = node;

		node.isApex = true;
		if (parent != null) {
			node.parent = parent;
			parent.addChild(node);
			node.cParent = parent.getPathApex();
		}

		Node lastAncestorRequireCompress = null;
		for (Node a = node; a != null; a = a.cParent) {
			a.size++;
			if (a.isRequireRecompress())
				lastAncestorRequireCompress = a;
		}
		recompress(lastAncestorRequireCompress);

		return node;
	}

	private void recompress(Node subtreeRoot) {
		/* first, compute the size of each node subtree */
		computeSize(subtreeRoot);

		/* actual recompress */
		buildCompressedTree(subtreeRoot);
		assert subtreeRoot.isApex;

		/* recompute ancestor tables */
		computeAcestorTables(subtreeRoot);
	}

	private int computeSize(Node node) {
		int size = 1;
		for (int i = 0; i < node.childrenNum; i++)
			size += computeSize(node.children[i]);
		return node.size = size;
	}

	private void buildCompressedTree(Node node) {
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
			Node child = node.children[i];
			child.isApex = child.size <= node.size / 2;
			buildCompressedTree(child);
		}
	}

	private void computeAcestorTables(Node node) {
		int ancestorTableSize = logBetaFloor(c * pow(n, e));
		node.ancestorTableInit(ancestorTableSize);

		int tableIdx = 0;
		for (Node a = node, last = null;; a = a.cParent) {
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
			computeAcestorTables(node.children[i]);
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
		assert x >= Integer.MIN_VALUE;
		assert x <= Integer.MAX_VALUE;
		return (int) x;
	}

	private static CharacteristicAncestors0 calcCACompressed(Node x, Node y) {
		if (x == y)
			return new CharacteristicAncestors0(x, x, x);
		int i = logBetaFloor(Math.abs(x.idxLower - y.idxLower));

		Node[] a = new Node[2];
		Node[] az = new Node[2];
		for (int zIdx = 0; zIdx < 2; zIdx++) {
			Node z = zIdx == 0 ? x : y;
			Node z0 = zIdx == 0 ? y : x;

			Node v = z.ancestorTable[i];
			Node w = v != null ? v.cParent : z;

			Node b, bz;
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

		Node ax = az[0], ay = az[1];
		assert a[0] == a[1];
		assert ax == a[0] || ax.cParent == a[0];
		assert ay == a[0] || ay.cParent == a[0];
		return new CharacteristicAncestors0(a[0], ax, ay);
	}

	private static CharacteristicAncestors0 calcCA0(Node x, Node y) {
		if (x == y)
			return new CharacteristicAncestors0(x, x, x);
		CharacteristicAncestors0 cac = calcCACompressed(x, y);

		/* c is an apex of path P */
		Node c = cac.a, cx = cac.ax, cy = cac.ay;
		assert c == c.getPathApex();

		/* bz is the first ancestor of cz on P */
		Node bx = cx != c && cx.isApex ? cx.parent : cx;
		Node by = cy != c && cy.isApex ? cy.parent : cy;
		assert c == bx.getPathApex();
		assert c == by.getPathApex();

		/* a is the shallower vertex of bx and by */
		Node a = bx.pathIdx < by.pathIdx ? bx : by;

		Node ax = a != bx ? a.getPathApex().path[a.pathIdx + 1] : cx;
		Node ay = a != by ? a.getPathApex().path[a.pathIdx + 1] : cy;

		assert ax == a || ax.parent == a;
		assert ay == a || ay.parent == a;
		return new CharacteristicAncestors0(a, ax, ay);
	}

	@Override
	public int calcLCA(int x, int y) {
		return calcCA0(nodes[x], nodes[y]).a.id;
	}

	public CharacteristicAncestors calcLC(int x, int y) {
		CharacteristicAncestors0 ca = calcCA0(nodes[x], nodes[y]);
		return new CharacteristicAncestors(ca.a.id, ca.ax.id, ca.ay.id);
	}

	private static class CharacteristicAncestors0 {
		Node a, ax, ay;

		CharacteristicAncestors0(Node a, Node ax, Node ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

	public static class CharacteristicAncestors {
		public final int a, ax, ay;

		CharacteristicAncestors(int a, int ax, int ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

}
