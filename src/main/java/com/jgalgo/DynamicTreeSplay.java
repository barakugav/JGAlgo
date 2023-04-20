package com.jgalgo;

public class DynamicTreeSplay<V, E> implements DynamicTree<V, E> {

	private final double rootWeight;
	private final SplayTree.SplayImpl<V, SplayNode<V, E>> impl;
	private static final double EPS = 0.00001;

	public DynamicTreeSplay(double weightLimit) {
		this(new SplayImplWithRelativeWeights<>(), weightLimit);
	}

	DynamicTreeSplay(SplayTree.SplayImpl<V, SplayNode<V, E>> impl, double weightLimit) {
		this.rootWeight = weightLimit;
		this.impl = impl;
	}

	@Override
	public SplayNode<V, E> makeTree(V nodeData) {
		SplayNode<V, E> node = newNode(nodeData);
		node.weightDiff = rootWeight;
		return node;
	}

	@Override
	public Node<V, E> findRoot(Node<V, E> v) {
		SplayNode<V, E> n = (SplayNode<V, E>) v;
		if (!n.isLinked())
			return n;
		splay(n);
		return splay(BSTUtils.findMax(n));
	}

	@Override
	public MinEdge<V, E> findMinEdge(Node<V, E> v) {
		SplayNode<V, E> n = (SplayNode<V, E>) v;
		if (!n.isLinked())
			return null;
		splay(n);
		double w = n.getWeight();
		if (!n.hasRightChild() || w < n.right.getMinWeight(w))
			return n.isLinked() ? new MinEdgeRes<>(n, w, n.getEdgeData()) : null;

		for (SplayNode<V, E> p = n.right;;) {
			double w1 = p.getWeight(w);
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1) - EPS) {
				p = p.right;
				w = w1;
			} else if (Math.abs(w1 - p.getMinWeight(w)) <= EPS) {
				impl.splay(p); /* perform splay to pay */
				if (!p.isLinked())
					throw new IllegalArgumentException("weightLimit was too small");
				return new MinEdgeRes<>(p, w1, p.getEdgeData());
			} else {
				assert p.hasLeftChild();
				p = p.left;
				w = w1;
			}
		}
	}

	@Override
	public void addWeight(Node<V, E> v, double w) {
		SplayNode<V, E> n = (SplayNode<V, E>) v;
		if (!n.isLinked())
			return;
		splay(n);
		assert n.isRoot();
		if (!n.hasRightChild())
			return;

		n.weightDiff += w;
		if (n.hasLeftChild()) {
			n.left.weightDiff -= w;

			double nW = n.getWeight();
			double minW = n.hasRightChild() ? Math.min(nW, n.right.getMinWeight(nW)) : nW;
			double minWl = n.left.getMinWeight(nW);
			n.minWeightDiff = nW - Math.min(minW, minWl);
		}

	}

	@Override
	public void link(Node<V, E> u, Node<V, E> v, double w, E edgeData) {
		if (u != findRoot(u))
			throw new IllegalArgumentException("u must be a root");
		if (u == findRoot(v))
			throw new IllegalArgumentException("Both nodes are in the same tree");
		if (w >= rootWeight / 2)
			throw new IllegalArgumentException("Weight is over the limit");
		SplayNode<V, E> t1 = splay((SplayNode<V, E>) u);
		SplayNode<V, E> t2 = splay((SplayNode<V, E>) v);

		assert !t1.isLinked() && !t1.hasRightChild();

		double oldWeight = t1.getWeight();
		t1.weightDiff = w;
		t1.minWeightDiff = 0;
		if (t1.hasLeftChild()) {
			t1.left.weightDiff += oldWeight - t1.getWeight();
			t1.minWeightDiff = t1.getWeight() - Math.min(t1.getMinWeight(), t1.left.getMinWeight(t1.getWeight()));
		}

		t1.tparent = t2;
		t1.link(t2, edgeData);
		afterLink(t1);
	}

	@Override
	public void cut(Node<V, E> v) {
		SplayNode<V, E> n = splay((SplayNode<V, E>) v);
		if (!n.hasRightChild())
			return;
		beforeCut(n);

		double origW = n.getWeight();
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
	public void evert(Node<V, E> v) {
		// TODO
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Deprecated
	@Override
	public int size(Node<V, E> v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
	}

	SplayNode<V, E> splay(SplayNode<V, E> n) {
		/* Splice all ancestors of in */
		for (SplayNode<V, E> p = n; p != null;)
			p = splice(p);

		impl.splay(n);
		assert n.isRoot();
		assert n.tparent == null;
		return n;
	}

	SplayNode<V, E> splice(SplayNode<V, E> n) {
		impl.splay(n);
		assert n.isRoot();

		if (n.tparent == null)
			return null;
		SplayNode<V, E> parent = n.tparent;
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

		double minp = Math.max(0, parent.getWeight() - n.getMinWeight(parent.getWeight()));
		if (parent.hasRightChild())
			minp = Math.max(minp, parent.getWeight() - parent.right.getMinWeight(parent.getWeight()));
		parent.minWeightDiff = minp;

		return parent;
	}

	SplayNode<V, E> newNode(V nodeData) {
		return new SplayNode<>(nodeData);
	}

	void beforeCut(SplayNode<V, E> n) {
	}

	void afterLink(SplayNode<V, E> n) {
	}

	static class SplayImplWithRelativeWeights<V, E> extends SplayTree.SplayImpl<V, SplayNode<V, E>> {

		@Override
		void beforeRotate(SplayNode<V, E> n) {
			super.beforeRotate(n);

			SplayNode<V, E> parent = n.parent;

			double origN = n.weightDiff, origP = parent.weightDiff;
			n.weightDiff = origN + origP;
			parent.weightDiff = -origN;

			double minn = 0, minp = 0;

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

	static class SplayNode<V, E> extends SplayTree.Node<V, SplayNode<V, E>> implements Node<V, E> {

		SplayNode<V, E> userParent;
		E edgeData;

		/* Parent outside of the splay tree */
		SplayNode<V, E> tparent;
		/* weight - p.weight */
		double weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		double minWeightDiff;

		SplayNode(V nodeData) {
			super(nodeData);
			userParent = null;
		}

		@Override
		public V getNodeData() {
			return data;
		}

		@Override
		public void setNodeData(V data) {
			this.data = data;
		}

		@Override
		public E getEdgeData() {
			return edgeData;
		}

		@Override
		public void setEdgeData(E data) {
			edgeData = data;
		}

		@Override
		public Node<V, E> getParent() {
			return userParent;
		}

		boolean isLinked() {
			return userParent != null;
		}

		void link(SplayNode<V, E> p, E edgeData) {
			userParent = p;
			this.edgeData = edgeData;
		}

		void unlink() {
			userParent = null;
			edgeData = null;
		}

		double getWeight() {
			assert isRoot();
			return getWeight(0);
		}

		double getWeight(double parentWeight) {
			return parentWeight + weightDiff;
		}

		double getMinWeight() {
			assert isRoot();
			return getMinWeight(0);
		}

		double getMinWeight(double parentWeight) {
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

	private static class MinEdgeRes<V, E> implements MinEdge<V, E> {

		final SplayNode<V, E> u;
		final double w;
		final E data;

		MinEdgeRes(SplayNode<V, E> u, double w, E data) {
			this.u = u;
			this.w = w;
			this.data = data;
		}

		@Override
		public SplayNode<V, E> u() {
			return u;
		}

		@Override
		public double weight() {
			return w;
		}

		@Override
		public E getData() {
			return data;
		}

	}

}
