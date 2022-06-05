package com.ugav.algo;

import java.util.Arrays;
import java.util.Comparator;

public class DynamicTreeSplay<E> implements DynamicTree<E> {

	private SplayNode[] nodes;
	private int nodesCount;
	private final double rootWeight;
	private final SplayImpl impl = new SplayImpl();

	public DynamicTreeSplay(double weightLimit) {
		nodes = new SplayNode[2];
		nodesCount = 0;
		this.rootWeight = weightLimit;
	}

	@Override
	public int makeTree() {
		if (nodesCount >= nodes.length)
			nodes = Arrays.copyOf(nodes, Math.max(nodes.length * 2, 2));
		int id = nodesCount++;
		SplayNode node = nodes[id] = new SplayNode(id);

		node.weightDiff = rootWeight;

		return id;
	}

	private void checkIdentifier(int v) {
		if (!(0 <= v && v < nodesCount))
			throw new IllegalArgumentException("Illegal node identifier");
	}

	@Override
	public int findRoot(int v) {
		checkIdentifier(v);
		SplayNode n = splay(nodes[v]);
		return impl.findMaxNode(n).id;
	}

	@Override
	public MinEdge<E> findMinEdge(int v) {
		checkIdentifier(v);
		SplayNode n = splay(nodes[v]);
		double w = n.getWeight();
		if (!n.hasRightChild() || w < n.right.getMinWeight(w))
			return n.isLinked() ? new MinEdgeRes<>(n.id, n.userParent, w, n.get()) : null;

		for (SplayNode p = n.right;;) {
			double w1 = p.getWeight(w);
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1)) {
				p = p.right;
				w = w1;
			} else if (w1 == p.getMinWeight(w)) {
				impl.splay(p); /* perform splay to pay */
				return p.isLinked() ? new MinEdgeRes<>(p.id, p.userParent, w1, p.get()) : null;
			} else {
				assert p.hasLeftChild();
				p = p.left;
				w = w1;
			}
		}
	}

	@Override
	public void addWeight(int v, double w) {
		checkIdentifier(v);
		SplayNode n = splay(nodes[v]);
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
	public void link(int u, int v, double w, E val) {
		checkIdentifier(u);
		checkIdentifier(v);
		if (u != findRoot(u))
			throw new IllegalArgumentException("u must be a root");
		if (u == findRoot(v))
			throw new IllegalArgumentException("Both nodes are in the same tree");
		if (w >= rootWeight / 2)
			throw new IllegalArgumentException("Weight is over the limit");
		SplayNode t1 = splay(nodes[u]);
		SplayNode t2 = splay(nodes[v]);

		assert !t1.isLinked() && !t1.hasRightChild();

		double oldWeight = t1.getWeight();
		t1.weightDiff = w;
		t1.minWeightDiff = 0;
		if (t1.hasLeftChild()) {
			t1.left.weightDiff += oldWeight - t1.getWeight();
			t1.minWeightDiff = t1.getWeight() - Math.min(t1.getMinWeight(), t1.left.getMinWeight(t1.getWeight()));
		}

		t1.tparent = t2;
		t1.link(t2.id, val);
	}

	@Override
	public void cut(int v) {
		checkIdentifier(v);
		SplayNode n = splay(nodes[v]);
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
	public void evert(int v) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int getParent(int v) {
		checkIdentifier(v);
		return nodes[v].userParent;
	}

	@Override
	public void clear() {
		SplayNode[] ns = nodes;
		int len = nodesCount;
		for (int i = 0; i < len; i++) {
			ns[i].clear();
			ns[i] = null;
		}
		nodesCount = 0;
	}

	private SplayNode splay(SplayNode n) {
		/* Splice all ancestors of in */
		for (SplayNode p = n; p != null;)
			p = splice(p);

		impl.splay(n);
		assert n.isRoot();
		assert n.tparent == null;
		return n;
	}

	private SplayNode splice(SplayNode n) {
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

		double minp = Math.max(0, parent.getWeight() - n.getMinWeight(parent.getWeight()));
		if (parent.hasRightChild())
			minp = Math.max(minp, parent.getWeight() - parent.right.getMinWeight(parent.getWeight()));
		parent.minWeightDiff = minp;

		return parent;
	}

	private static class SplayImpl extends SplayTree.Impl<Object, SplayNode> {

		@Override
		SplayNode insert(SplayNode root, Comparator<? super Object> c, SplayNode n) {
			throw new UnsupportedOperationException();
		}

		@Override
		SplayNode remove(SplayNode n) {
			throw new UnsupportedOperationException();
		}

		@Override
		SplayNode meld(SplayNode t1, SplayNode t2) {
			throw new UnsupportedOperationException();
		}

		@Override
		Pair<SplayNode, SplayNode> split(SplayNode n) {
			throw new UnsupportedOperationException();
		}

		@Override
		void beforeRotate(SplayNode n) {
			SplayNode parent = n.parent;

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
		SplayNode newNode(Object v) {
			throw new UnsupportedOperationException();
		}

	}

	private static class SplayNode extends SplayTree.Impl.Node<Object, SplayNode> {

		final int id;
		int userParent;

		/* Parent outside of the splay tree */
		SplayNode tparent;
		/* weight - p.weight */
		double weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		double minWeightDiff;

		private static final int NO_USER_PARENT = -1;

		SplayNode(int id) {
			super(null);
			this.id = id;
			userParent = NO_USER_PARENT;
		}

		boolean isLinked() {
			return userParent != NO_USER_PARENT;
		}

		void link(int p, Object val) {
			userParent = p;
			this.val = val;
		}

		void unlink() {
			userParent = NO_USER_PARENT;
			val = null;
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
			userParent = NO_USER_PARENT;
			tparent = null;
			weightDiff = minWeightDiff = 0;
		}

	}

	private static class MinEdgeRes<E> implements MinEdge<E> {

		final int u;
		final int v;
		final double w;
		final E val;

		@SuppressWarnings("unchecked")
		MinEdgeRes(int u, int v, double w, Object val) {
			this.u = u;
			this.v = v;
			this.w = w;
			this.val = (E) val;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return v;
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
