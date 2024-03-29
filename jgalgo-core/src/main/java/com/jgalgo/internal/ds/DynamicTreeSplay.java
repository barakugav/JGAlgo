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

package com.jgalgo.internal.ds;

/**
 * Dynamic trees implementation using splay trees.
 *
 * <p>
 * Each tree is represented as a set of paths where each path is a sequence of descending nodes in the tree. When an
 * element is accessed, the paths are split and merged so the path from the element to the root will be a single path in
 * the underlying representation. This implementation achieve amortized \(O(\log n)\) time for each operation.
 *
 * <p>
 * Based on 'A Data Structure for Dynamic Trees' by Sleator, D. D.; Tarjan, R. E (1983), although the original paper did
 * not use splay trees for the implementation.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Link/cut_tree">Wikipedia</a>
 * @author Barak Ugav
 */
class DynamicTreeSplay implements DynamicTree {

	private final double rootWeight;
	private final ObjObjSplayTree.SplayImpl<Object, SplayNode> impl;
	private final double eps;

	/**
	 * Create a new empty dynamic tree data structure.
	 *
	 * @param weightLimit a limit on the weights of the edges. The limit is an upper bound on the sum of each edge
	 *                        weight and the weights modification that are performed using
	 *                        {@link #addWeight(com.jgalgo.internal.ds.DynamicTree.Vertex, double)}.
	 */
	DynamicTreeSplay(double weightLimit) {
		this(new SplayImplWithRelativeWeights(), weightLimit);
	}

	DynamicTreeSplay(ObjObjSplayTree.SplayImpl<Object, SplayNode> impl, double weightLimit) {
		this.rootWeight = weightLimit;
		this.impl = impl;
		this.eps = weightLimit * 1e-9;
	}

	@Override
	public SplayNode makeTree() {
		SplayNode node = newNode();
		node.weightDiff = rootWeight;
		return node;
	}

	@Override
	public DynamicTree.Vertex findRoot(DynamicTree.Vertex v) {
		SplayNode n = (SplayNode) v;
		if (!n.isLinked())
			return n;
		splay(n);
		return splay(BinarySearchTrees.findMax(n));
	}

	@Override
	public MinEdge findMinEdge(DynamicTree.Vertex v) {
		SplayNode n = (SplayNode) v;
		if (!n.isLinked())
			return null;
		splay(n);
		double w = n.getWeight();
		if (!n.hasRightChild() || w < n.right.getMinWeight(w))
			return n.isLinked() ? new MinEdgeRes(n, w) : null;

		for (SplayNode p = n.right;;) {
			double w1 = p.getWeight(w);
			if (p.hasRightChild() && p.getMinWeight(w) >= p.right.getMinWeight(w1) - eps) {
				p = p.right;
				w = w1;
			} else if (Math.abs(w1 - p.getMinWeight(w)) <= eps) {
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
	public void addWeight(DynamicTree.Vertex v, double w) {
		SplayNode n = (SplayNode) v;
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
	public void link(DynamicTree.Vertex child, DynamicTree.Vertex parent, double w) {
		if (child != findRoot(child))
			throw new IllegalArgumentException("child node must be a root");
		if (child == findRoot(parent))
			throw new IllegalArgumentException("Both nodes are in the same tree");
		if (w >= rootWeight / 2)
			throw new IllegalArgumentException("Weight is over the limit");
		SplayNode t1 = splay((SplayNode) child);
		SplayNode t2 = splay((SplayNode) parent);

		assert !t1.isLinked() && !t1.hasRightChild();

		double oldWeight = t1.getWeight();
		t1.weightDiff = w;
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
	public void cut(DynamicTree.Vertex v) {
		SplayNode n = splay((SplayNode) v);
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
	public void clear() {}

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

		double minp = Math.max(0, parent.getWeight() - n.getMinWeight(parent.getWeight()));
		if (parent.hasRightChild())
			minp = Math.max(minp, parent.getWeight() - parent.right.getMinWeight(parent.getWeight()));
		parent.minWeightDiff = minp;

		return parent;
	}

	SplayNode newNode() {
		return new SplayNode();
	}

	void beforeCut(SplayNode n) {}

	void afterLink(SplayNode n) {}

	static class SplayImplWithRelativeWeights extends ObjObjSplayTree.SplayImpl<Object, SplayNode> {

		@Override
		void beforeRotate(SplayNode n) {
			super.beforeRotate(n);

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

	}

	static class SplayNode extends ObjObjSplayTree.BaseNode<Object, SplayNode> implements DynamicTree.Vertex {

		SplayNode userParent;

		/* Parent outside of the splay tree */
		SplayNode tparent;
		/* weight - p.weight */
		double weightDiff;
		/* weight - min_{x in subtree} {x.weight} */
		double minWeightDiff;

		SplayNode() {
			super(null);
			userParent = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V> V getData() {
			return (V) key;
		}

		@Override
		public void setData(Object data) {
			this.key = data;
		}

		@Override
		public Vertex getParent() {
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

	private static class MinEdgeRes implements MinEdge {

		final SplayNode u;
		final double w;

		MinEdgeRes(SplayNode u, double w) {
			this.u = u;
			this.w = w;
		}

		@Override
		public SplayNode source() {
			return u;
		}

		@Override
		public double weight() {
			return w;
		}

	}

	@Override
	public <ExtT extends DynamicTreeExtension> ExtT getExtension(Class<ExtT> extensionType) {
		return null;
	}

}
