package com.ugav.algo;

import java.util.Arrays;
import java.util.Comparator;

public class DynamicTreeSplay implements DynamicTree {

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
		SplayNode node = impl.newNode(Integer.valueOf(id));
		nodes[id] = node;

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
		return impl.findMaxNode(n).id().intValue();
	}

	@Override
	public Pair<Integer, Double> findMinEdge(int v) {
		checkIdentifier(v);
		SplayNode n = splay(nodes[v]);
		double w = n.getWeight();
		if (!n.hasRightChild() || w < n.right.getMinWeight(w))
			return Pair.of(n.id(), w <= rootWeight / 2 ? Double.valueOf(w) : null);

		for (SplayNode p = n.right;;) {
			double w1 = p.getWeight(w);
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1)) {
				p = p.right;
				w = w1;
			} else if (w1 == p.getMinWeight(w)) {
				impl.splay(p); /* perform splay to pay */
				return Pair.of(p.id(), w1 <= rootWeight / 2 ? Double.valueOf(w1) : null);
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
	public void link(int u, int v, double w) {
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

		assert !t1.hasRightChild() && t1.getWeight() >= rootWeight / 2;

		double oldWeight = t1.getWeight();
		t1.weightDiff = w;
		t1.minWeightDiff = 0;
		if (t1.hasLeftChild()) {
			t1.left.weightDiff += oldWeight - t1.getWeight();
			t1.minWeightDiff = t1.getWeight() - Math.min(t1.getMinWeight(), t1.left.getMinWeight(t1.getWeight()));
		}

		t1.tparent = t2;
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
	}

	@Override
	public void evert(int v) {
		// TODO
		throw new UnsupportedOperationException();
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

	private static class SplayImpl extends SplayTree.Impl<Integer, SplayNode> {

		@Override
		SplayNode insert(SplayNode root, Comparator<? super Integer> c, SplayNode n) {
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
		SplayNode newNode(Integer v) {
			return new SplayNode(v);
		}

	}

	private static class SplayNode extends SplayTree.Impl.Node<Integer, SplayNode> {

		/* TODO use Node elements field as tparent */

		SplayNode tparent;
		/* weight - p.weight */
		double weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		double minWeightDiff;

		SplayNode(Integer id) {
			super(id);
		}

		Integer id() {
			return get();
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

	}

}
