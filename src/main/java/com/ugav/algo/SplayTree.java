package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;

import com.ugav.algo.BSTUtils.NeighborType;

public class SplayTree<E> extends BSTAbstract<E> {

	private Node<E> root;
	private int size;
	private final Comparator<? super E> c;

	public SplayTree() {
		this(null);
	}

	public SplayTree(Comparator<? super E> c) {
		this.c = c != null ? c : Utils.getDefaultComparator();
		root = null;
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Iterator<E> iterator() {
		return new BSTUtils.BSTIterator<>(root);
	}

	@Override
	public void clear() {
		if (root == null)
			return;
		BSTUtils.clear(root);
		root = null;
		size = 0;
	}

	@Override
	public Handle<E> insert(E e) {
		Node<E> n = new Node<>(e);
		insertNode(n);
		size++;
		return n;
	}

	private void insertNode(Node<E> n) {
		if (root == null) {
			root = n;
		} else {
			BSTUtils.insert(root, c, n);
			splay(n);
		}
	}

	@Override
	public E extractMin() {
		if (root == null)
			throw new IllegalStateException();
		Node<E> n = BSTUtils.findMin(root);
		E ret = n.val;
		removeHandle(n);
		return ret;
	}

	@Override
	public E extractMax() {
		if (root == null)
			throw new IllegalStateException();
		Node<E> n = BSTUtils.findMax(root);
		E ret = n.val;
		removeHandle(n);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(Heap<? extends E> h0) {
		if (h0.isEmpty())
			return;
		if (!(h0 instanceof SplayTree)) {
			super.meld(h0);
			return;
		}

		SplayTree<? extends E> h = (SplayTree<? extends E>) h0;
		if (c != h.c) {
			super.meld(h);
			return;
		}

		if (root == null) {
			root = (Node<E>) h.root;
			size = h.size;
			h.root = null;
			h.size = 0;
			return;
		}

		Node<E> t1max = (Node<E>) findMaxHandle();
		Node<E> t2min = (Node<E>) h.findMinHandle();
		if (c.compare(t1max.get(), t2min.get()) <= 0) {
			/* all elements in this tree are <= than all elements in other tree */
			assert root == t1max;
			assert h.root == t2min;
			assert root.right == null;
			root.right = (Node<E>) h.root;
			root.right.parent = root;
			size += h.size;
			h.root = null;
			h.size = 0;
			return;
		}

		Node<E> t1min = (Node<E>) findMinHandle();
		Node<E> t2max = (Node<E>) h.findMaxHandle();
		if (c.compare(t1min.get(), t2max.get()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			assert root == t1min;
			assert h.root == t2max;
			assert root.left == null;
			root.left = (Node<E>) h.root;
			root.left.parent = root;
			size += h.size;
			h.root = null;
			h.size = 0;
			return;
		}

		/* there is nothing smarter to do than regular meld */
		super.meld(h);
	}

	@Override
	public Handle<E> findHanlde(E e) {
		return splay(BSTUtils.find(root, c, e));
	}

	@Override
	public Handle<E> findMinHandle() {
		if (root == null)
			throw new IllegalStateException();
		return splay(BSTUtils.findMin(root));
	}

	@Override
	public Handle<E> findMaxHandle() {
		if (root == null)
			throw new IllegalStateException();
		return splay(BSTUtils.findMax(root));
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> n = (Node<E>) handle;
		removeHandle(n);
		n.val = e;
		insertNode(n);
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		Node<E> n = (Node<E>) handle;
		if (n.left != null && n.right != null) {
			/* Node has two children, swap node with successor */
			Node<E> swap = BSTUtils.findSuccessor(n);
			E old = swap.val;
			swap.val = n.val;
			n.val = old;
			n = swap;
		}

		if (n.left == null) {
			replace(n, n.right);
		} else {
			assert n.right == null;
			replace(n, n.left);
		}
		n.clear();
		size--;
	}

	private void replace(Node<E> u, Node<E> v) {
		/* replaces u with v */
		Node<E> p = u.parent;
		if (p == null)
			root = v;
		else if (p.left == u)
			p.left = v;
		else
			p.right = v;
		if (v != null)
			v.parent = p;
	}

	@Override
	public Handle<E> findOrPredecessor(E e) {
		return splay(BSTUtils.findOrNeighbor(root, c, e, NeighborType.Predecessor));
	}

	@Override
	public Handle<E> findOrSuccessor(E e) {
		return splay(BSTUtils.findOrNeighbor(root, c, e, NeighborType.Successor));
	}

	@Override
	public Handle<E> findPredecessor(Handle<E> handle) {
		return splay(BSTUtils.findPredecessor((Node<E>) handle));
	}

	@Override
	public Handle<E> findSuccessor(Handle<E> handle) {
		return splay(BSTUtils.findSuccessor((Node<E>) handle));
	}

	private Node<E> splay(Node<E> n) {
		if (n == null || n.parent == null)
			return n;
		for (;;) {
			Node<E> parent = n.parent, grandparent = parent.parent;

			if (grandparent == null) {
				/* zig */
				rotate(n);
				return root = n;
			}

			boolean isNLeft = parent.left == n;
			boolean isPLeft = grandparent.left == parent;
			if (isNLeft == isPLeft) {
				/* zig-zig */
				rotate(parent);
				rotate(n);
			} else {
				/* zig-zag */
				rotate(n);
				rotate(n);
			}

			if (n.parent == null)
				return root = n;
		}
	}

	private void rotate(Node<E> n) {
		Node<E> parent = n.parent, grandparent = parent.parent;
		if (parent.left == n) {
			parent.left = n.right;
			if (parent.left != null)
				parent.left.parent = parent;
			n.right = parent;

		} else {
			parent.right = n.left;
			if (parent.right != null)
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

	private static class Node<E> extends BSTUtils.Node<E, Node<E>> implements Handle<E> {

		Node(E e) {
			super(e);
		}

		@Override
		public E get() {
			return val;
		}
	}

}
