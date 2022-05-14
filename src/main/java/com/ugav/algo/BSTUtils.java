package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

class BSTUtils {

	private BSTUtils() {
		throw new InternalError();
	}

	static class Node<E, N extends Node<E, N>> {
		E val;
		N parent;
		N right;
		N left;

		Node(E e) {
			this.val = e;
			parent = right = left = null;
		}

		void clear() {
			parent = left = right = null;
			val = null;
		}

		@Override
		public String toString() {
			return "<" + val + ">";
		}
	}

	static <E, N extends Node<E, N>> N find(N root, Comparator<? super E> c, E e) {
		return findOrNeighbor(root, c, e, NeighborType.None);
	}

	static enum NeighborType {
		None, Predecessor, Successor,
	}

	static <E, N extends Node<E, N>> N findOrNeighbor(N root, Comparator<? super E> c, E e, NeighborType neighborType) {
		if (root == null)
			return null;
		for (N p = root;;) {
			int cmp = c.compare(e, p.val);
			if (cmp == 0)
				return p;
			if (cmp < 0) {
				if (p.left == null)
					return findNeighbor(p, neighborType);
				p = p.left;
			} else {
				if (p.right == null)
					return findNeighbor(p, neighborType);
				p = p.right;
			}
		}
	}

	private static <E, N extends Node<E, N>> N findNeighbor(N n, NeighborType neighborType) {
		switch (neighborType) {
		case None:
			return null;
		case Predecessor:
			return findPredecessor(n);
		case Successor:
			return findSuccessor(n);
		default:
			throw new IllegalArgumentException("Unexpected value: " + neighborType);
		}
	}

	static <E, N extends Node<E, N>> N findMin(N root) {
		for (N p = root;; p = p.left)
			if (p.left == null)
				return p;
	}

	static <E, N extends Node<E, N>> N findMax(N root) {
		for (N p = root;; p = p.right)
			if (p.right == null)
				return p;
	}

	static <E, N extends Node<E, N>> N findPredecessor(N n) {
		/* predecessor in left sub tree */
		if (n.left != null)
			for (N p = n.left;; p = p.right)
				if (p.right == null)
					return p;

		/* predecessor is some ancestor */
		for (N p = n, parent; (parent = p.parent) != null; p = parent)
			if (p == parent.right)
				return parent;
		return null;
	}

	static <E, N extends Node<E, N>> N findSuccessor(N n) {
		return findSuccessorInSubtree(n, null);
	}

	static <E, N extends Node<E, N>> N findSuccessorInSubtree(N n, N subtreeRoot) {
		N subtreeParent = subtreeRoot != null ? subtreeRoot.parent : null;

		/* successor in right sub tree */
		if (n.right != null)
			for (N p = n.right;; p = p.left)
				if (p.left == null)
					return p;

		/* successor is some ancestor */
		for (N p = n, parent; (parent = p.parent) != subtreeParent; p = parent)
			if (p == parent.left)
				return parent;
		return null;
	}

	static <E, N extends Node<E, N>> void insert(N root, Comparator<? super E> c, N n) {
		for (N parent = root;;) {
			int cmp = c.compare(n.val, parent.val);
			if (cmp <= 0) {
				if (parent.left == null) {
					parent.left = n;
					n.parent = parent;
					return;
				}
				parent = parent.left;
			} else {
				if (parent.right == null) {
					parent.right = n;
					n.parent = parent;
					return;
				}
				parent = parent.right;
			}
		}
	}

	static <E, N extends Node<E, N>> void clear(N root) {
		for (N p = root; p != null;) {
			for (;;) {
				if (p.left != null) {
					p = p.left;
					continue;
				}
				if (p.right != null) {
					p = p.right;
					continue;
				}
				break;
			}
			N parent = p.parent;
			p.clear();
			p = parent;
		}
	}

	static class BSTIterator<E, N extends Node<E, N>> implements Iterator<N> {

		private final N subtreeRoot;
		private N n;

		BSTIterator(N subtreeRoot) {
			this.subtreeRoot = subtreeRoot;
			n = subtreeRoot == null ? null : findMin(subtreeRoot);
		}

		@Override
		public boolean hasNext() {
			return n != null;
		}

		@Override
		public N next() {
			if (!hasNext())
				throw new NoSuchElementException();
			N ret = n;
			n = findSuccessorInSubtree(n, subtreeRoot);
			return ret;
		}

	}

}
