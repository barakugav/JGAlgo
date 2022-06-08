package com.ugav.algo;

import java.util.Arrays;

public class LCAGabowSimple<V> implements LCADynamic<V> {

	/**
	 * This implementation is a dynamic LCA implementation from Gabow17, which
	 * allows addLeaf and LCA queries, with addLeaf O(log^2 n) amortized and LCA
	 * query O(1).
	 */

	private int nodesNum;

	/* Hyper parameters */
	private static final double alpha = 6.0 / 5.0;
	private static final double beta = 2 / (2 * alpha - 1);
	private static final int e = 4;
	private static final int c = 5;

	@SuppressWarnings("rawtypes")
	private static final NodeImpl[] EMPTY_NODE_ARR = new NodeImpl[0];

	public LCAGabowSimple() {
		nodesNum = 0;
	}

	@Override
	public Node<V> initTree(V val) {
		if (size() != 0)
			throw new IllegalStateException();
		return newNode(null, val);
	}

	@Override
	public Node<V> addLeaf(Node<V> parent, V val) {
		return newNode((NodeImpl<V>) parent, val);
	}

	private Node<V> newNode(NodeImpl<V> parent, V val) {
		nodesNum++;
		NodeImpl<V> node = new NodeImpl<>(parent, val);

		node.isApex = true;
		if (parent != null) {
			parent.addChild(node);
			node.cParent = parent.getPathApex();
		}

		NodeImpl<V> lastAncestorRequireCompress = null;
		for (NodeImpl<V> a = node; a != null; a = a.cParent) {
			a.size++;
			if (a.isRequireRecompress())
				lastAncestorRequireCompress = a;
		}
		recompress(lastAncestorRequireCompress);

		return node;
	}

	private void recompress(NodeImpl<V> subtreeRoot) {
		/* first, compute the size of each node subtree */
		computeSize(subtreeRoot);

		/* actual recompress */
		buildCompressedTree(subtreeRoot);
		assert subtreeRoot.isApex;

		/* recompute ancestor tables */
		computeAcestorTables(subtreeRoot);
	}

	private int computeSize(NodeImpl<V> node) {
		int size = 1;
		for (int i = 0; i < node.childrenNum; i++)
			size += computeSize(node.children[i]);
		return node.size = size;
	}

	@SuppressWarnings("unchecked")
	private void buildCompressedTree(NodeImpl<V> node) {
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
			NodeImpl<V> child = node.children[i];
			child.isApex = child.size <= node.size / 2;
			buildCompressedTree(child);
		}
	}

	private void computeAcestorTables(NodeImpl<V> node) {
		int ancestorTableSize = logBetaFloor(c * pow(nodesNum, e));
		node.ancestorTableInit(ancestorTableSize);

		int tableIdx = 0;
		for (NodeImpl<V> a = node, last = null;; a = a.cParent) {
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
		assert Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE;
		return (int) x;
	}

	@SuppressWarnings("unchecked")
	private CharacteristicAncestors0<V> calcCACompressed(NodeImpl<V> x, NodeImpl<V> y) {
		if (x == y)
			return new CharacteristicAncestors0<>(x, x, x);
		int i = logBetaFloor(Math.abs(x.idxLower - y.idxLower));

		NodeImpl<V>[] a = new NodeImpl[2];
		NodeImpl<V>[] az = new NodeImpl[2];
		for (int zIdx = 0; zIdx < 2; zIdx++) {
			NodeImpl<V> z = zIdx == 0 ? x : y;
			NodeImpl<V> z0 = zIdx == 0 ? y : x;

			NodeImpl<V> v = z.ancestorTable[i];
			NodeImpl<V> w = v != null ? v.cParent : z;

			NodeImpl<V> b, bz;
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

		NodeImpl<V> ax = az[0], ay = az[1];
		assert a[0] == a[1];
		assert ax == a[0] || ax.cParent == a[0];
		assert ay == a[0] || ay.cParent == a[0];
		return new CharacteristicAncestors0<>(a[0], ax, ay);
	}

	private CharacteristicAncestors0<V> calcCA0(NodeImpl<V> x, NodeImpl<V> y) {
		if (x == y)
			return new CharacteristicAncestors0<>(x, x, x);
		CharacteristicAncestors0<V> cac = calcCACompressed(x, y);

		/* c is an apex of path P */
		NodeImpl<V> c = cac.a, cx = cac.ax, cy = cac.ay;
		assert c == c.getPathApex();

		/* bz is the first ancestor of cz on P */
		NodeImpl<V> bx = cx != c && cx.isApex ? cx.parent : cx;
		NodeImpl<V> by = cy != c && cy.isApex ? cy.parent : cy;
		assert c == bx.getPathApex();
		assert c == by.getPathApex();

		/* a is the shallower vertex of bx and by */
		NodeImpl<V> a = bx.pathIdx < by.pathIdx ? bx : by;

		NodeImpl<V> ax = a != bx ? a.getPathApex().path[a.pathIdx + 1] : cx;
		NodeImpl<V> ay = a != by ? a.getPathApex().path[a.pathIdx + 1] : cy;

		assert ax == a || ax.parent == a;
		assert ay == a || ay.parent == a;
		return new CharacteristicAncestors0<>(a, ax, ay);
	}

	@Override
	public Node<V> calcLCA(Node<V> x, Node<V> y) {
		return calcCA0((NodeImpl<V>) x, (NodeImpl<V>) y).a;
	}

	public CharacteristicAncestors<V> calcCA(Node<V> x, Node<V> y) {
		CharacteristicAncestors0<V> ca = calcCA0((NodeImpl<V>) x, (NodeImpl<V>) y);
		return new CharacteristicAncestors<>(ca.a, ca.ax, ca.ay);
	}

	@Override
	public int size() {
		return nodesNum;
	}

	@Override
	public void clear() {
		nodesNum = 0;
	}

	private static class NodeImpl<V> implements Node<V> {
		/* --- user tree data --- */
		V nodeVal;
		/* tree parent */
		final NodeImpl<V> parent;
		/* children nodes of this node */
		NodeImpl<V>[] children;
		int childrenNum;
		/* number of nodes in subtree */
		int size;

		/* --- compressed tree data --- */
		/* parent in the compressed tree */
		NodeImpl<V> cParent;
		/* If the node is apex, contains all the nodes in it's path, else null */
		NodeImpl<V>[] path;
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
		NodeImpl<V>[] ancestorTable;

		@SuppressWarnings("unchecked")
		NodeImpl(NodeImpl<V> parent, V val) {
			this.parent = parent;
			nodeVal = val;
			children = EMPTY_NODE_ARR;
		}

		@Override
		public V getNodeData() {
			return nodeVal;
		}

		@Override
		public void setNodeData(V val) {
			nodeVal = val;
		}

		@Override
		public Node<V> getParent() {
			return parent;
		}

		boolean isRoot() {
			assert !(parent == null ^ cParent == null);
			return parent == null;
		}

		void addChild(NodeImpl<V> c) {
			if (childrenNum >= children.length)
				children = Arrays.copyOf(children, Math.max(children.length * 2, 2));
			children[childrenNum++] = c;
		}

		NodeImpl<V> getPathApex() {
			return isApex ? this : cParent;
		}

		void addToPath(NodeImpl<V> c) {
			if (pathSize >= path.length)
				path = Arrays.copyOf(path, Math.max(path.length * 2, 2));
			path[pathSize++] = c;
		}

		boolean isRequireRecompress() {
			return size >= alpha * sigma;
		}

		@SuppressWarnings("unchecked")
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

//		void clear() {
//			parent = cParent = null;
//			Arrays.fill(children, 0, childrenNum, null);
//			Arrays.fill(path, 0, pathSize, null);
//			Arrays.fill(ancestorTable, null);
//			children = path = ancestorTable = null;
//		}

	}

	private static class CharacteristicAncestors0<V> {
		NodeImpl<V> a, ax, ay;

		CharacteristicAncestors0(NodeImpl<V> a, NodeImpl<V> ax, NodeImpl<V> ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

	public static class CharacteristicAncestors<V> {
		public final Node<V> a, ax, ay;

		CharacteristicAncestors(Node<V> a, Node<V> ax, Node<V> ay) {
			this.a = a;
			this.ax = ax;
			this.ay = ay;
		}
	}

}
