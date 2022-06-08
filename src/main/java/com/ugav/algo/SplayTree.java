package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;

import com.ugav.algo.BSTUtils.NeighborType;

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
			NodeSized<E> swap = BSTUtils.findSuccessor(n);
			E old = swap.val;
			swap.val = n.val;
			n.val = old;
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
		n.val = e;
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
	public Handle<E> findOrPredecessor(E e) {
		NodeSized<E> n = BSTUtils.findOrNeighbor(root, c, e, NeighborType.Predecessor);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findOrSuccessor(E e) {
		NodeSized<E> n = BSTUtils.findOrNeighbor(root, c, e, NeighborType.Successor);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findPredecessor(Handle<E> handle) {
		NodeSized<E> n = BSTUtils.findPredecessor((NodeSized<E>) handle);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public Handle<E> findSuccessor(Handle<E> handle) {
		NodeSized<E> n = BSTUtils.findSuccessor((NodeSized<E>) handle);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public E extractMin() {
		checkTreeNotEmpty();
		NodeSized<E> n = BSTUtils.findMin(root);
		E ret = n.val;
		removeHandle(n);
		return ret;
	}

	@Override
	public E extractMax() {
		checkTreeNotEmpty();
		NodeSized<E> n = BSTUtils.findMax(root);
		E ret = n.val;
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

		if (c.compare(findMaxHandle().get(), h.findMinHandle().get()) <= 0) {
			/* all elements in this tree are <= than all elements in other tree */
			root = meld(root, h.root);
			h.root = null;
		} else if (c.compare(findMinHandle().get(), h.findMaxHandle().get()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			root = meld(h.root, root);
			h.root = null;
		} else {
			/* there is nothing smarter to do than regular meld */
			super.meld(h);
		}
	}

	private NodeSized<E> meld(NodeSized<E> t1, NodeSized<E> t2) {
		/* Assume all nodes in t1 are smaller than all nodes in t2 */

		/* Splay t1 max and t2 min */
		t1 = impl.splay(BSTUtils.findMax(t1));
		t2 = impl.splay(BSTUtils.findMax(t2));
		assert t1.isRoot() && t2.isRoot();
		assert !t1.hasRightChild();

		t1.right = t2;
		t2.parent = t1;
		t1.size += t2.size;
		return t1;
	}

	@Override
	public SplayTree<E> split(Handle<E> handle) {
		SplayTree<E> newTree = new SplayTree<>(c);

		NodeSized<E> n = root = impl.splay((NodeSized<E>) handle);
		assert n.isRoot();
		if (!n.hasRightChild())
			return newTree;

		NodeSized<E> newRoot = n.right;
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
			return val;
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
