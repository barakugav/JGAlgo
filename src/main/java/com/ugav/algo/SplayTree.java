package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;

public class SplayTree<E> extends BSTAbstract<E> {

	private NodeSized<E> root;
	private final SplayImplWithSize<E> impl = new SplayImplWithSize<>();

	public SplayTree() {
		this(null);
	}

	public SplayTree(Comparator<? super E> c) {
		super(c);
		root = null;
	}

	@Override
	public int size() {
		return root != null ? root.size : 0;
	}

	@Override
	public Iterator<? extends Handle<E>> handleIterator() {
		return new BSTUtils.BSTIterator<>(root);
	}

	@Override
	public Handle<E> insert(E e) {
		return insertNode(new NodeSized<>(e));
	}

	private NodeSized<E> insertNode(NodeSized<E> n) {
		if (root == null)
			return root = n;

		BSTUtils.insert(root, c, n);
		for (NodeSized<E> p = n.parent; p != null; p = p.parent)
			p.size++;
		return root = impl.splay(n);
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		NodeSized<E> n = (NodeSized<E>) handle;

		if (n.hasLeftChild() && n.hasRightChild()) {
			/* Node has two children, swap node with successor */
			NodeSized<E> swap = BSTUtils.getSuccessor(n);
			E old = swap.data;
			swap.data = n.data;
			n.data = old;
			n = swap;
		}

		NodeSized<E> child;
		if (!n.hasLeftChild()) {
			replace(n, child = n.right);
		} else {
			assert !n.hasRightChild();
			replace(n, child = n.left);
		}

		/* Decrease ancestors size by 1 */
		for (NodeSized<E> p = n.parent; p != null; p = p.parent)
			p.size--;

		NodeSized<E> parent = n.parent;
		n.clear();
		root = impl.splay(child != null ? child : parent);

	}

	private void replace(NodeSized<E> u, NodeSized<E> v) {
		/* replaces u with v */
		if (u.parent != null) {
			if (u.isLeftChild()) {
				u.parent.left = v;
			} else {
				assert u.isRightChild();
				u.parent.right = v;
			}
		}
		if (v != null)
			v.parent = u.parent;
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		NodeSized<E> n = (NodeSized<E>) handle;
		removeHandle(n);
		n.data = e;
		insertNode(n);
	}

	@Override
	public Handle<E> findHanlde(E e) {
		NodeSized<E> n = BSTUtils.find(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findMinHandle() {
		checkTreeNotEmpty();
		return root = impl.splay(BSTUtils.findMin(root));
	}

	@Override
	public Handle<E> findMaxHandle() {
		checkTreeNotEmpty();
		return root = impl.splay(BSTUtils.findMax(root));
	}

	@Override
	public Handle<E> findOrSmaller(E e) {
		NodeSized<E> n = BSTUtils.findOrSmaller(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findOrGreater(E e) {
		NodeSized<E> n = BSTUtils.findOrGreater(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findSmaller(E e) {
		NodeSized<E> n = BSTUtils.findSmaller(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findGreater(E e) {
		NodeSized<E> n = BSTUtils.findGreater(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> getPredecessor(Handle<E> handle) {
		NodeSized<E> n = BSTUtils.getPredecessor((NodeSized<E>) handle);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> getSuccessor(Handle<E> handle) {
		NodeSized<E> n = BSTUtils.getSuccessor((NodeSized<E>) handle);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public E extractMin() {
		checkTreeNotEmpty();
		NodeSized<E> n = BSTUtils.findMin(root);
		E ret = n.data;
		removeHandle(n);
		return ret;
	}

	@Override
	public E extractMax() {
		checkTreeNotEmpty();
		NodeSized<E> n = BSTUtils.findMax(root);
		E ret = n.data;
		removeHandle(n);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(Heap<? extends E> h0) {
		if (h0 == this || h0.isEmpty())
			return;
		SplayTree<E> h;
		if (!(h0 instanceof SplayTree) || (h = (SplayTree<E>) h0).c != c) {
			super.meld(h0);
			return;
		}
		if (isEmpty()) {
			root = h.root;
			h.root = null;
			return;
		}

		E min1, max1, min2, max2;
		if (c.compare(max1 = findMax(), min2 = h.findMin()) <= 0) {
			/* all elements in this tree are <= than all elements in other tree */
			root = meld(this, h);
		} else if (c.compare(min1 = findMin(), max2 = h.findMax()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			root = meld(h, this);
		} else {
			int minCmp = c.compare(min1, min2);
			int maxCmp = c.compare(max1, max2);
			SplayTree<E> hLow = null, hHigh = null;

			if (minCmp < 0) {
				hLow = this.splitSmaller(min2);
			} else if (minCmp > 0) {
				hLow = h.splitSmaller(min1);
			}
			if (maxCmp < 0) {
				hHigh = h.splitGreater(max1);
			} else if (maxCmp > 0) {
				hHigh = this.splitGreater(max2);
			}

			/* there is nothing smarter to do than regular meld for the shared range */
			super.meld(h);

			if (hLow != null) {
				assert c.compare(hLow.findMax(), findMin()) < 0;
				root = meld(hLow, this);
			}
			if (hHigh != null) {
				assert c.compare(hHigh.findMin(), findMax()) > 0;
				root = meld(this, hHigh);
			}
		}
		h.root = null;
	}

	private static <E> NodeSized<E> meld(SplayTree<E> t1, SplayTree<E> t2) {
		/* Assume all nodes in t1 are smaller than all nodes in t2 */

		/* Splay t1 max and t2 min */
		NodeSized<E> n1 = (NodeSized<E>) t1.findMaxHandle();
		assert n1.isRoot();
		assert !n1.hasRightChild();

		n1.right = t2.root;
		t2.root.parent = n1;
		n1.size += t2.root.size;
		return n1;
	}

	@Override
	public SplayTree<E> splitSmaller(E e) {
		SplayTree<E> newTree = new SplayTree<>(c);
		NodeSized<E> pred = (NodeSized<E>) findSmaller(e);
		if (pred == null)
			return newTree;
		assert pred.isRoot();

		if ((root = pred.right) != null) {
			pred.size -= pred.right.size;
			pred.right.parent = null;
			pred.right = null;
		}

		newTree.root = pred;
		return newTree;
	}

	@Override
	public SplayTree<E> splitGreater(E e) {
		SplayTree<E> newTree = new SplayTree<>(c);
		NodeSized<E> succ = (NodeSized<E>) findGreater(e);
		if (succ == null)
			return newTree;
		assert succ.isRoot();

		if ((root = succ.left) != null) {
			succ.size -= succ.left.size;
			succ.left.parent = null;
			succ.left = null;
		}

		newTree.root = succ;
		return newTree;
	}

	@Override
	public SplayTree<E> split(Handle<E> handle) {
		SplayTree<E> newTree = new SplayTree<>(c);

		NodeSized<E> n = root = impl.splay((NodeSized<E>) handle);
		assert n.isRoot();
		if (!n.hasRightChild())
			return newTree;

		NodeSized<E> newRoot = n.right;
		n.right.parent = null;
		n.right = null;
		n.size -= newRoot.size;

		newTree.root = newRoot;
		return newTree;
	}

	@Override
	public void clear() {
		if (root == null)
			return;
		BSTUtils.clear(root);
		root = null;
	}

	static class Node<E, N extends Node<E, N>> extends BSTUtils.Node<E, N> implements Handle<E> {

		Node(E e) {
			super(e);
		}

		@Override
		public E get() {
			return data;
		}

	}

	static abstract class SplayImpl<E, N extends Node<E, N>> {

		SplayImpl() {
		}

		N splay(N n) {
			if (n == null || n.isRoot())
				return n;
			for (;;) {
				N parent = n.parent;

				if (parent.isRoot()) {
					/* zig */
					rotate(n);
					return n;
				}

				if (n.isLeftChild() == parent.isLeftChild()) {
					/* zig-zig */
					rotate(parent);
					rotate(n);
				} else {
					/* zig-zag */
					rotate(n);
					rotate(n);
				}

				if (n.isRoot())
					return n;
			}
		}

		private void rotate(N n) {
			N parent = n.parent, grandparent = parent.parent;

			beforeRotate(n);

			if (n.isLeftChild()) {

				parent.left = n.right;
				if (parent.hasLeftChild())
					parent.left.parent = parent;
				n.right = parent;

			} else {
				assert n.isRightChild();

				parent.right = n.left;
				if (parent.hasRightChild())
					parent.right.parent = parent;
				n.left = parent;
			}

			n.parent = grandparent;
			parent.parent = n;
			if (grandparent != null) {
				if (grandparent.left == parent)
					grandparent.left = n;
				else
					grandparent.right = n;
			}
		}

		void beforeRotate(N n) {
		}

	}

	private static class NodeSized<E> extends Node<E, NodeSized<E>> {

		int size;

		NodeSized(E e) {
			super(e);
			size = 1;
		}

		@Override
		void clear() {
			super.clear();
			size = 1;
		}

	}

	private static class SplayImplWithSize<E> extends SplayImpl<E, NodeSized<E>> {

		@Override
		void beforeRotate(NodeSized<E> n) {
			super.beforeRotate(n);

			NodeSized<E> parent = n.parent;
			int parentOldSize = parent.size;

			if (n.isLeftChild()) {
				parent.size = parentOldSize - n.size + (n.hasRightChild() ? n.right.size : 0);
			} else {
				assert n.isRightChild();
				parent.size = parentOldSize - n.size + (n.hasLeftChild() ? n.left.size : 0);
			}

			n.size = parentOldSize;
		}

	}

	private void checkTreeNotEmpty() {
		if (root == null)
			throw new IllegalStateException("Tree is empty");
	}

}
