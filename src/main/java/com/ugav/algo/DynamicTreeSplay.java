package com.ugav.algo;

import java.util.Comparator;

public class DynamicTreeSplay<V, E> implements DynamicTree<V, E> {

	private final double rootWeight;
	private final SplayImpl<V, E> impl = new SplayImpl<>();

	public DynamicTreeSplay(double weightLimit) {
		this.rootWeight = weightLimit;
	}

	@Override
	public SplayNode<V, E> makeTree(V val) {
		SplayNode<V, E> node = new SplayNode<>(val);
		node.weightDiff = rootWeight;
		return node;
	}

	@Override
	public Node<V, E> findRoot(Node<V, E> v) {
		SplayNode<V, E> n = (SplayNode<V, E>) v;
		if (!n.isLinked())
			return n;
		splay(n);
		return impl.findMaxNode(n);
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
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1)) {
				p = p.right;
				w = w1;
			} else if (w1 == p.getMinWeight(w)) {
				impl.splay(p); /* perform splay to pay */
				return p.isLinked() ? new MinEdgeRes<>(p, w1, p.getEdgeData()) : null;
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
	public void link(Node<V, E> u, Node<V, E> v, double w, E val) {
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
		t1.link(t2, val);
	}

	@Override
	public void cut(Node<V, E> v) {
		SplayNode<V, E> n = splay((SplayNode<V, E>) v);
		if (!n.hasRightChild())
			return;

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

	@Override
	public void clear() {
	}

	private SplayNode<V, E> splay(SplayNode<V, E> n) {
		/* Splice all ancestors of in */
		for (SplayNode<V, E> p = n; p != null;)
			p = splice(p);

		impl.splay(n);
		assert n.isRoot();
		assert n.tparent == null;
		return n;
	}

	private SplayNode<V, E> splice(SplayNode<V, E> n) {
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

	private static class SplayImpl<V, E> extends SplayTree.Impl<V, SplayNode<V, E>> {

		@Override
		SplayNode<V, E> insert(SplayNode<V, E> root, Comparator<? super V> c, SplayNode<V, E> n) {
			throw new UnsupportedOperationException();
		}

		@Override
		SplayNode<V, E> remove(SplayNode<V, E> n) {
			throw new UnsupportedOperationException();
		}

		@Override
		SplayNode<V, E> meld(SplayNode<V, E> t1, SplayNode<V, E> t2) {
			throw new UnsupportedOperationException();
		}

		@Override
		Pair<SplayNode<V, E>, SplayNode<V, E>> split(SplayNode<V, E> n) {
			throw new UnsupportedOperationException();
		}

		@Override
		void beforeRotate(SplayNode<V, E> n) {
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

		@Override
		SplayNode<V, E> newNode(Object v) {
			throw new UnsupportedOperationException();
		}

	}

	private static class SplayNode<V, E> extends SplayTree.Impl.Node<V, SplayNode<V, E>> implements Node<V, E> {

		SplayNode<V, E> userParent;
		E edgeVal;

		/* Parent outside of the splay tree */
		SplayNode<V, E> tparent;
		/* weight - p.weight */
		double weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		double minWeightDiff;

		SplayNode(V val) {
			super(val);
			userParent = null;
		}

		@Override
		public V getNodeData() {
			return val;
		}

		@Override
		public void setNodeData(V val) {
			this.val = val;
		}

		@Override
		public E getEdgeData() {
			return edgeVal;
		}

		@Override
		public void setEdgeData(E val) {
			edgeVal = val;
		}

		@Override
		public Node<V, E> getParent() {
			return userParent;
		}

		boolean isLinked() {
			return userParent != null;
		}

		void link(SplayNode<V, E> p, E val) {
			userParent = p;
			edgeVal = val;
		}

		void unlink() {
			userParent = null;
			edgeVal = null;
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
		final E val;

		MinEdgeRes(SplayNode<V, E> u, double w, E val) {
			this.u = u;
			this.w = w;
			this.val = val;
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
		public E val() {
			return val;
		}

	}

}
