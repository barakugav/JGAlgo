package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import com.ugav.algo.BSTUtils.NeighborType;
import com.ugav.algo.SplayTree.Impl.Node;

public class SplayTree<E> extends BSTAbstract<E> {

	private NodeBasic<E> root;
	private final ImplBasic<E> impl = new ImplBasic<>();

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
		return insertNode(impl.newNode(e));
	}

	private NodeBasic<E> insertNode(NodeBasic<E> n) {
		return root = (root == null ? n : impl.insert(root, c, n));
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		root = impl.remove((NodeBasic<E>) handle);
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		NodeBasic<E> n = (NodeBasic<E>) handle;
		removeHandle(n);
		n.val = e;
		insertNode(n);
	}

	@Override
	public Handle<E> findHanlde(E e) {
		return root = impl.findNode(root, c, e);
	}

	@Override
	public Handle<E> findMinHandle() {
		checkTreeNotEmpty();
		return root = impl.findMinNode(root);
	}

	@Override
	public Handle<E> findMaxHandle() {
		checkTreeNotEmpty();
		return root = impl.findMaxNode(root);
	}

	@Override
	public Handle<E> findOrPredecessor(E e) {
		NodeBasic<E> n = impl.findOrPredecessor(root, c, e);
		return n == null ? null : (root = n);
	}

	@Override
	public Handle<E> findOrSuccessor(E e) {
		NodeBasic<E> n = impl.findOrSuccessor(root, c, e);
		return n == null ? null : (root = n);
	}

	@Override
	public Handle<E> findPredecessor(Handle<E> handle) {
		NodeBasic<E> n = impl.findPredecessor((NodeBasic<E>) handle);
		return n == null ? null : (root = n);
	}

	@Override
	public Handle<E> findSuccessor(Handle<E> handle) {
		NodeBasic<E> n = impl.findSuccessor((NodeBasic<E>) handle);
		return n == null ? null : (root = n);
	}

	@Override
	public E extractMin() {
		checkTreeNotEmpty();
		NodeBasic<E> n = BSTUtils.findMin(root);
		E ret = n.val;
		removeHandle(n);
		return ret;
	}

	@Override
	public E extractMax() {
		checkTreeNotEmpty();
		NodeBasic<E> n = BSTUtils.findMax(root);
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
			root = impl.meld(root, h.root);
			h.root = null;
		} else if (c.compare(findMinHandle().get(), h.findMaxHandle().get()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			root = impl.meld(h.root, root);
			h.root = null;
		} else {
			/* there is nothing smarter to do than regular meld */
			super.meld(h);
		}
	}

	@Override
	public SplayTree<E> split(Handle<E> handle) {
		Pair<NodeBasic<E>, NodeBasic<E>> p = impl.split((NodeBasic<E>) handle);
		root = p.e1;

		SplayTree<E> newTree = new SplayTree<>(c);
		newTree.root = p.e2;

		return newTree;
	}

	@Override
	public void clear() {
		if (root == null)
			return;
		BSTUtils.clear(root);
		root = null;
	}

	static abstract class Impl<E, N extends Node<E, N>> {

		Impl() {
		}

		N insert(N root, Comparator<? super E> c, N n) {
			BSTUtils.insert(root, c, n);
			for (N p = n.parent; p != null; p = p.parent)
				p.size++;
			afterInsert(n);
			return splay(n);
		}

		N remove(N n) {
			if (n.hasLeftChild() && n.hasRightChild()) {
				/* Node has two children, swap node with successor */
				N swap = BSTUtils.findSuccessor(n);
				beforeValSwap(n, swap);
				E old = swap.val;
				swap.val = n.val;
				n.val = old;
				n = swap;
			}

			beforeRemove(n);

			N child;
			if (!n.hasLeftChild()) {
				replace(n, child = n.right);
			} else {
				assert !n.hasRightChild();
				replace(n, child = n.left);
			}

			/* Decrease ancestors size by 1 */
			for (N p = n.parent; p != null; p = p.parent)
				p.size--;

			N parent = n.parent;
			n.clear();
			return splay(child != null ? child : parent);
		}

		private void replace(N u, N v) {
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

		N findNode(N root, Comparator<? super E> c, E e) {
			return splay(BSTUtils.find(root, c, e));
		}

		N findMinNode(N root) {
			return splay(BSTUtils.findMin(root));
		}

		N findMaxNode(N root) {
			return splay(BSTUtils.findMax(root));
		}

		N findOrPredecessor(N root, Comparator<? super E> c, E e) {
			return splay(BSTUtils.findOrNeighbor(root, c, e, NeighborType.Predecessor));
		}

		N findOrSuccessor(N root, Comparator<? super E> c, E e) {
			return splay(BSTUtils.findOrNeighbor(root, c, e, NeighborType.Successor));
		}

		N findPredecessor(N node) {
			return splay(BSTUtils.findPredecessor(node));
		}

		N findSuccessor(N node) {
			return splay(BSTUtils.findSuccessor(node));
		}

		N meld(N t1, N t2) {
			/* Assume all nodes in t1 are smaller than all nodes in t2 */
			if (t1 == t2 || t1 == null || t2 == null)
				return t1 != null ? t1 : t2;

			/* Splay t1 max and t2 min */
			t1 = findMaxNode(t1);
			t2 = findMinNode(t2);
			assert t1.isRoot() && t2.isRoot();
			assert !t1.hasRightChild();
			beforeMeld(t1, t2);
			t1.right = t2;
			t2.parent = t1;
			t1.size += t2.size;
			return t1;
		}

		Pair<N, N> split(N n) {
			Objects.requireNonNull(n);
			splay(n);
			assert n.isRoot();

			beforeSplit(n);

			N newRoot = n.right;
			if (newRoot != null) {
				n.right = null;
				n.size -= newRoot.size;
			}

			return Pair.of(n, newRoot);
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
			int parentOldSize = parent.size;

			beforeRotate(n);

			if (n.isLeftChild()) {
				parent.size = parentOldSize - n.size + (n.right != null ? n.right.size : 0);

				parent.left = n.right;
				if (parent.hasLeftChild())
					parent.left.parent = parent;
				n.right = parent;

			} else {
				assert n.isRightChild();
				parent.size = parentOldSize - n.size + (n.left != null ? n.left.size : 0);

				parent.right = n.left;
				if (parent.hasRightChild())
					parent.right.parent = parent;
				n.left = parent;
			}

			n.parent = grandparent;
			n.size = parentOldSize;
			parent.parent = n;
			if (grandparent != null) {
				if (grandparent.left == parent)
					grandparent.left = n;
				else
					grandparent.right = n;
			}
		}

		void afterInsert(N n) {
		}

		void beforeValSwap(N a, N b) {
		}

		void beforeRemove(N n) {
		}

		void beforeMeld(N t1, N t2) {
		}

		void beforeSplit(N t1) {
		}

		void beforeRotate(N n) {
		}

		abstract N newNode(E e);

		static class Node<E, N extends Node<E, N>> extends BSTUtils.Node<E, N> implements Handle<E> {

			int size;

			Node(E e) {
				super(e);
				size = 1;
			}

			@Override
			public E get() {
				return val;
			}

			@Override
			void clear() {
				super.clear();
				size = 1;
			}
		}

	}

	private static class NodeBasic<E> extends Impl.Node<E, NodeBasic<E>> {

		NodeBasic(E e) {
			super(e);
		}

	}

	private static class ImplBasic<E> extends Impl<E, NodeBasic<E>> {

		@Override
		NodeBasic<E> newNode(E e) {
			return new NodeBasic<>(e);
		}
	}

	private void checkTreeNotEmpty() {
		if (root == null)
			throw new IllegalStateException("Tree is empty");
	}

}
