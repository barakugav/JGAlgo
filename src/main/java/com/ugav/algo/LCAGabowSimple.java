package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LCAGabowSimple implements LCADynamic {

	private int n;
	private Node[] nodes;

	private static final double alpha = 6.0 / 5.0;
	private static final double beta = 2 / (2 * alpha - 1);
	private static final int e = 4;
	private static final int c = 5;

	private static class Node {
		final int id;

		/* --- user tree data --- */
		/* tree parent */
		Node parent;
		/* all children */
		final List<Node> children;
		/* number of nodes in subtree */
		int size;

		/* --- compressed tree data --- */
		/* parent in the compressed tree */
		Node cParent;
		/*  */
		List<Node> path;
		int pathIdx;
		/* p */
		int idxLower;
		/* q */
		int idxUpper;
		/* p bar */
		int idxLowerFat;
		/* q bar */
		int idxUpperFat;
		/* Q bar */
		int idxUpperFatMaxChild;
		/* s */
		int csize;
		/* sigma */
		int sigma;
		/* flag for head (shallower) of path node */
		boolean isApex;
		/* ancestor table */
		final List<Node> ancestorTable;

		Node(int id) {
			this.id = id;
			children = new ArrayList<>();
			ancestorTable = new ArrayList<>();
		}

		boolean isRoot() {
			assert !(parent == null ^ cParent == null);
			return parent == null;
		}

		boolean isRequireCompress() {
			return csize >= alpha * sigma;
		}

		Node getPathApex() {
			return isApex ? this : cParent;
		}

		void ancestorTableInit(int size) {
			ancestorTable.clear();
			for (int i = 0; i < size; i++)
				ancestorTable.add(null);
		}

	}

	public LCAGabowSimple() {
		n = 0;
		nodes = new Node[16];
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
		if (node.id > nodes.length) {
			int aLen = Math.max(nodes.length * 2, 2);
			nodes = Arrays.copyOf(nodes, aLen);
		}
		nodes[node.id] = node;

		node.isApex = true;
		if (parent != null) {
			node.parent = parent;
			parent.children.add(node);
			node.cParent = parent.getPathApex();
		}

		Node lastAncestorRequireCompress = null;
		for (Node a = node; a != null; a = a.cParent) {
			a.csize++;
			if (a.isRequireCompress())
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
		for (Node child : node.children)
			size += computeSize(child);
		return node.size = size;
	}

	private void buildCompressedTree(Node node) {
		if (node.isRoot()) {
			node.isApex = true;
			node.cParent = null;
		} else {
			node.isApex = node.size <= node.parent.size / 2;
			node.cParent = node.parent.getPathApex();
		}

		if (node.isApex) {
			node.path = new ArrayList<>();
		} else {
			node.path = null;
			node.pathIdx = node.getPathApex().path.size();
			node.getPathApex().path.add(node);
		}

		node.sigma = node.csize = node.isApex ? node.size : 1;
		int v = pow(node.sigma, e);
		node.idxLowerFat = node.isRoot() ? 0 : node.cParent.idxUpperFatMaxChild;
		node.idxUpperFat = node.idxLowerFat + c * v;
		node.idxLower = node.idxLowerFat + v;
		node.idxUpper = node.idxUpperFat - v;
		assert node.isRoot() || node.idxUpperFat <= node.cParent.idxUpperFat;
		if (!node.isRoot())
			node.cParent.idxUpperFatMaxChild = node.idxUpperFat;

		node.idxUpperFatMaxChild = node.idxLower + 1;
		for (Node child : node.children)
			buildCompressedTree(child);
	}

	private void computeAcestorTables(Node node) {
		int ancestorTableSize = logBetaFloor(c * pow(n, e));
		node.ancestorTableInit(ancestorTableSize);

		int tableIdx = 0;
		for (Node a = node, last = null;; a = a.cParent) {
			for (; (c - 2) * pow(a.sigma, e) >= Math.pow(beta, tableIdx); tableIdx++)
				if (last != null)
					node.ancestorTable.set(tableIdx, last);
			if (a.isRoot()) {
				for (; tableIdx < ancestorTableSize; tableIdx++)
					if ((c - 2) * pow(a.sigma, e) < Math.pow(beta, tableIdx))
						node.ancestorTable.set(tableIdx, a);
				break;
			}
			last = a;
		}

		for (Node child : node.children)
			computeAcestorTables(child);
	}

	private static int pow(int a, int b) {
		return assertOverflow(Math.pow(a, b));
	}

	private static int logBetaFloor(double x) {
		return assertOverflow(Math.floor(Math.log(x) / Math.log(beta)));
	}

	private static int assertOverflow(double x) {
		assert x >= Integer.MIN_VALUE;
		assert x <= Integer.MAX_VALUE;
		return (int) x;
	}

	private static CharacteristicAncestors calcCACompressed(Node x, Node y) {
		if (x == y)
			return new CharacteristicAncestors(x, x, x);
		int i = logBetaFloor(Math.abs(x.idxLower - y.idxLower));

		Node[] a = new Node[2];
		Node[] az = new Node[2];
		for (int zIdx = 0; zIdx < 2; zIdx++) {
			Node z = zIdx == 0 ? x : y;

			Node v = z.ancestorTable.get(i);
			Node w = v != null ? v.cParent : z;

			Node b, bz;
			if ((c - 2) * pow(w.sigma, e) > Math.abs(x.idxLower - y.idxLower)) {
				b = w;
				bz = v != null ? v : z;
			} else {
				b = w.cParent;
				bz = w;
			}

			if (b.idxLower < y.idxLower && y.idxLower < b.idxUpper) { /* b is an ancestor of y */
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
		return new CharacteristicAncestors(a[0], ax, ay);
	}

	private static CharacteristicAncestors calcCA(Node x, Node y) {
		if (x == y)
			return new CharacteristicAncestors(x, x, x);
		CharacteristicAncestors cac = calcCACompressed(x, y);

		/* c is an apex of path P */
		Node c = cac.a, cx = cac.ax, cy = cac.ay;
		assert c == c.getPathApex();

		/* bz is the first ancestor of cz on P */
		Node bx = cx.isApex ? cx.parent : cx;
		Node by = cy.isApex ? cy.parent : cy;
		assert bx != by;
		assert c == bx.getPathApex();
		assert c == by.getPathApex();

		/* a is the shallower vertex of bx and by */
		Node a = bx.pathIdx < by.pathIdx ? bx : by;

		Node ax = a != bx ? a.getPathApex().path.get(a.pathIdx + 1) : cx;
		Node ay = a != by ? a.getPathApex().path.get(a.pathIdx + 1) : cy;

		assert ax == a || ax.parent == a;
		assert ay == a || ay.parent == a;
		return new CharacteristicAncestors(a, ax, ay);
	}

	@Override
	public int calcLCA(int u, int v) {
		Node x = nodes[u], y = nodes[v];
		return calcCA(x, y).a.id;
	}

	private static class CharacteristicAncestors {
		Node a, ax, ay;

		CharacteristicAncestors(Node a, Node ax, Node ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

}
