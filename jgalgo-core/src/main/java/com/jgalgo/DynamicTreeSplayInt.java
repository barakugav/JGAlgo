package com.jgalgo;

/**
 * Dynamic trees with integer edges weights implementation using splay trees.
 * <p>
 * Each tree is represented as a set of paths where each path is a sequence of
 * descending nodes in the tree. When an element is accessed, the paths are
 * split and merged so the path from the element to the root will be a single
 * path in the underlying representation. This implementation achieve amortized
 * \(O(\log n)\) time for each operation.
 * <p>
 * Based on 'A Data Structure for Dynamic Trees' by Sleator, D. D.; Tarjan, R. E
 * (1983), although the original paper did not use splay trees for the
 * implementation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Link/cut_tree">Wikipedia</a>
 * @author Barak Ugav
 */
class DynamicTreeSplayInt implements DynamicTree {

	private final int rootWeight;
	private final SplayTree.SplayImpl<Object, SplayNode> impl;

	/**
	 * Create a new empty dynamic tree data structure.
	 *
	 * @param weightLimit a limit on the weights of the edges. The limit is an upper
	 *                    bound on the sum of each edge weight and the weights
	 *                    modification that are performed using
	 *                    {@link #addWeight(com.jgalgo.DynamicTree.Node, double)}.
	 */
	DynamicTreeSplayInt(int weightLimit) {
		this(new SplayImplWithRelativeWeights(), weightLimit);
	}

	DynamicTreeSplayInt(SplayTree.SplayImpl<Object, SplayNode> impl, int weightLimit) {
		this.rootWeight = weightLimit;
		this.impl = impl;
	}

	@Override
	public SplayNode makeTree() {
		SplayNode node = newNode();
		node.weightDiff = rootWeight;
		return node;
	}

	@Override
	public Node findRoot(Node v) {
		SplayNode n = (SplayNode) v;
		if (!n.isLinked())
			return n;
		splay(n);
		return splay(BinarySearchTrees.findMax(n));
	}

	@Override
	public MinEdge findMinEdge(Node v) {
		SplayNode n = (SplayNode) v;
		if (!n.isLinked())
			return null;
		splay(n);
		int w = n.getWeight();
		if (!n.hasRightChild() || w < n.right.getMinWeight(w))
			return n.isLinked() ? new MinEdgeRes(n, w) : null;

		for (SplayNode p = n.right;;) {
			int w1 = p.getWeight(w);
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1)) {
				p = p.right;
				w = w1;
			} else if (Math.abs(w1 - p.getMinWeight(w)) <= 0) {
				impl.splay(p); /* perform splay to pay */
				if (!p.isLinked())
					throw new IllegalArgumentException("weightLimit was too small");
				return new MinEdgeRes(p, w1);
			} else {
				assert p.hasLeftChild();
				p = p.left;
				w = w1;
			}
		}
	}

	@Override
	public void addWeight(Node v, double w) {
		int wInt = (int) w;
		if (w != wInt)
			throw new IllegalArgumentException("weight is not an integer");

		SplayNode n = (SplayNode) v;
		if (!n.isLinked())
			return;
		splay(n);
		assert n.isRoot();
		if (!n.hasRightChild())
			return;

		n.weightDiff += wInt;
		if (n.hasLeftChild()) {
			n.left.weightDiff -= wInt;

			int nW = n.getWeight();
			int minW = n.hasRightChild() ? Math.min(nW, n.right.getMinWeight(nW)) : nW;
			int minWl = n.left.getMinWeight(nW);
			n.minWeightDiff = nW - Math.min(minW, minWl);
		}

	}

	@Override
	public void link(Node child, Node parent, double w) {
		int wInt = (int) w;
		if (w != wInt)
			throw new IllegalArgumentException("weight is not an integer");
		if (child != findRoot(child))
			throw new IllegalArgumentException("child node must be a root");
		if (child == findRoot(parent))
			throw new IllegalArgumentException("Both nodes are in the same tree");
		if (wInt >= rootWeight / 2)
			throw new IllegalArgumentException("Weight is over the limit");
		SplayNode t1 = splay((SplayNode) child);
		SplayNode t2 = splay((SplayNode) parent);

		assert !t1.isLinked() && !t1.hasRightChild();

		int oldWeight = t1.getWeight();
		t1.weightDiff = wInt;
		t1.minWeightDiff = 0;
		if (t1.hasLeftChild()) {
			t1.left.weightDiff += oldWeight - t1.getWeight();
			t1.minWeightDiff = t1.getWeight() - Math.min(t1.getMinWeight(), t1.left.getMinWeight(t1.getWeight()));
		}

		t1.tparent = t2;
		t1.link(t2);
		afterLink(t1);
	}

	@Override
	public void cut(Node v) {
		SplayNode n = splay((SplayNode) v);
		if (!n.hasRightChild())
			return;
		beforeCut(n);

		int origW = n.getWeight();
		n.right.weightDiff += origW;
		n.weightDiff = rootWeight;
		n.minWeightDiff = 0;
		if (n.hasLeftChild()) {
			n.left.weightDiff += origW - n.getWeight();
			n.minWeightDiff = n.getWeight() - Math.min(n.getMinWeight(), n.left.getMinWeight(n.getWeight()));
		}

		n.right.parent = null;
		n.right = null;
		n.unlink();
	}

	@Override
	public void clear() {
	}

	SplayNode splay(SplayNode n) {
		/* Splice all ancestors of in */
		for (SplayNode p = n; p != null;)
			p = splice(p);

		impl.splay(n);
		assert n.isRoot();
		assert n.tparent == null;
		return n;
	}

	SplayNode splice(SplayNode n) {
		impl.splay(n);
		assert n.isRoot();

		if (n.tparent == null)
			return null;
		SplayNode parent = n.tparent;
		impl.splay(parent);
		assert parent.isRoot();

		n.parent = parent;
		n.tparent = null;
		n.weightDiff -= parent.getWeight();
		if (parent.hasLeftChild()) {
			parent.left.parent = null;
			parent.left.tparent = parent;
			parent.left.weightDiff += parent.getWeight();
		}
		parent.left = n;

		int minp = Math.max(0, parent.getWeight() - n.getMinWeight(parent.getWeight()));
		if (parent.hasRightChild())
			minp = Math.max(minp, parent.getWeight() - parent.right.getMinWeight(parent.getWeight()));
		parent.minWeightDiff = minp;

		return parent;
	}

	SplayNode newNode() {
		return new SplayNode();
	}

	void beforeCut(SplayNode n) {
	}

	void afterLink(SplayNode n) {
	}

	static class SplayImplWithRelativeWeights extends SplayTree.SplayImpl<Object, SplayNode> {

		@Override
		void beforeRotate(SplayNode n) {
			super.beforeRotate(n);

			SplayNode parent = n.parent;

			int origN = n.weightDiff, origP = parent.weightDiff;
			n.weightDiff = origN + origP;
			parent.weightDiff = -origN;

			int minn = 0, minp = 0;

			if (n.isLeftChild()) {
				if (n.hasRightChild())
					n.right.weightDiff += origN;

				if (n.hasRightChild())
					minp = Math.max(minp, n.right.minWeightDiff - n.right.weightDiff);
				if (parent.hasRightChild())
					minp = Math.max(minp, parent.right.minWeightDiff - parent.right.weightDiff);

				if (n.hasLeftChild())
					minn = Math.max(minn, n.left.minWeightDiff - n.left.weightDiff);
			} else {
				assert n.isRightChild();
				if (n.hasLeftChild())
					n.left.weightDiff += origN;

				if (n.hasLeftChild())
					minp = Math.max(minp, n.left.minWeightDiff - n.left.weightDiff);
				if (parent.hasLeftChild())
					minp = Math.max(minp, parent.left.minWeightDiff - parent.left.weightDiff);

				if (n.hasRightChild())
					minn = Math.max(minn, n.right.minWeightDiff - n.right.weightDiff);
			}

			parent.minWeightDiff = minp;
			minn = Math.max(minn, parent.minWeightDiff - parent.weightDiff);
			n.minWeightDiff = minn;

			n.tparent = parent.tparent;
			parent.tparent = null;
		}

	}

	static class SplayNode extends SplayTree.Node<Object, SplayNode> implements Node {

		SplayNode userParent;

		/* Parent outside of the splay tree */
		SplayNode tparent;
		/* weight - p.weight */
		int weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		int minWeightDiff;

		SplayNode() {
			super(null);
			userParent = null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> V getNodeData() {
			return (V) data;
		}

		@Override
		public void setNodeData(Object data) {
			this.data = data;
		}

		@Override
		public Node getParent() {
			return userParent;
		}

		boolean isLinked() {
			return userParent != null;
		}

		void link(SplayNode p) {
			userParent = p;
		}

		void unlink() {
			userParent = null;
		}

		int getWeight() {
			assert isRoot();
			return getWeight(0);
		}

		int getWeight(int parentWeight) {
			return parentWeight + weightDiff;
		}

		int getMinWeight() {
			assert isRoot();
			return getMinWeight(0);
		}

		int getMinWeight(int parentWeight) {
			return getWeight(parentWeight) - minWeightDiff;
		}

		@Override
		void clear() {
			super.clear();
			userParent = null;
			tparent = null;
			weightDiff = minWeightDiff = 0;
		}

	}

	private static class MinEdgeRes implements MinEdge {

		final SplayNode u;
		final int w;

		MinEdgeRes(SplayNode u, int w) {
			this.u = u;
			this.w = w;
		}

		@Override
		public SplayNode u() {
			return u;
		}

		@Override
		public double weight() {
			return w;
		}

	}

	@Override
	public <Ext extends DynamicTreeExtension> Ext getExtension(Class<Ext> extensionType) {
		return null;
	}

}
