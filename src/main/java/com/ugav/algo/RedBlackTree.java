package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RedBlackTree<E> extends HeapAbstract<E> {

	private int size;
	private Node<E> root;
	private final Comparator<? super E> c;

	private static final boolean Red = true;
	private static final boolean Black = false;

	public RedBlackTree() {
		this(null);
	}

	public RedBlackTree(Comparator<? super E> c) {
		root = null;
		size = 0;
		this.c = c != null ? c : Utils.getDefaultComparator();
	}

	@Override
	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return findHanlde((E) o) != null;
	}

	@Override
	public E findMin() {
		return findMinHandle().get();
	}

	@Override
	public E extractMin() {
		Handle<E> h = findMinHandle();
		E e = h.get();
		removeHandle(h);
		return e;
	}

	@Override
	public Handle<E> insert(E e) {
		Node<E> n = new Node<>(e);
		insert(n);
		return n;
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		Handle<E> h = findHanlde((E) o);
		if (h == null)
			return false;
		removeHandle(h);
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iter<>(root);
	}

	@Override
	public void clear() {
		for (Node<E> p = root; p != null;) {
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
			Node<E> parent = p.parent;
			p.clear();
			p = parent;
		}
		root = null;
		size = 0;
	}

	@Override
	public void meld(Heap<? extends E> h) {
		// TODO
		super.meld(h);
	}

	@Override
	public boolean isHandlesSupported() {
		return true;
	}

	@Override
	public Handle<E> findHanlde(E e) {
		for (Node<E> p = root; p != null;) {
			int cmp = c.compare(e, p.e);
			if (cmp == 0)
				return p;
			if (cmp < 0)
				p = p.left;
			else
				p = p.right;
		}
		return null;
	}

	@Override
	public Handle<E> findMinHandle() {
		if (root == null)
			throw new IllegalStateException();
		Node<E> p;
		for (p = root; p.left != null; p = p.left)
			;
		return p;
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> n = (Node<E>) handle;
		removeHandle(n);
		n.e = e;
		insert(n);
	}

	private void insert(Node<E> n) {
		if (root == null) {
			n.color = Black;
			root = n;
		} else {
			Node<E> parent;
			for (parent = root;;) {
				int cmp = c.compare(n.e, parent.e);
				if (cmp <= 0) {
					if (parent.left == null) {
						parent.left = n;
						break;
					}
					parent = parent.left;
				} else {
					if (parent.right == null) {
						parent.right = n;
						break;
					}
					parent = parent.right;
				}
			}
			n.parent = parent;
			fixAfterInsert(parent, n);
		}
		size++;
	}

	private void fixAfterInsert(Node<E> parent, Node<E> n) {
		n.color = Red;

		do {
			/* Case 1: parent is black, all the requirements are satisfied */
			if (parent.color == Black)
				return;

			/* Case 4: parent is Red and root */
			if (parent == root) {
				parent.color = Black;
				return;
			}

			Node<E> grandparent = parent.parent;
			Node<E> uncle = parent == grandparent.left ? grandparent.right : grandparent.left;

			/* Case 5,6: parent is Red, uncle is Black */
			if (uncle == null || uncle.color == Black) {

				/* Case 5: n is an inner grandchild of grandparent */
				if (n == parent.left) {
					if (parent == grandparent.right) {
						/* Case 5a: left inner grandchild */
						rotateRight(parent);
						n = parent;
						parent = grandparent.right;
					}
				} else {
					if (parent == grandparent.left) {
						/* Case 5b: right inner grandchild */
						rotateLeft(parent);
						n = parent;
						parent = grandparent.left;
					}
				}

				assert (n == parent.left && parent == grandparent.left)
						|| (n == parent.right && parent == grandparent.right);
				/* Case 6: n is an outer grandchild of grandparent */
				if (parent == grandparent.left)
					rotateRight(grandparent);
				else
					rotateLeft(grandparent);
				parent.color = Black;
				grandparent.color = Red;
				return;
			}

			/* Case 2: parent is Red, uncle is Red */
			assert grandparent.color == Black;
			parent.color = uncle.color = Black;
			grandparent.color = Red;

			n = grandparent;
		} while ((parent = n.parent) != null);

		/* Case 3: we exited the loop through the main condition, n is now a Red root */
	}

	private void rotateLeft(Node<E> n) {
		Node<E> parent = n.parent, child = n.right, grandchild = child.left;

		n.right = grandchild;
		if (grandchild != null)
			grandchild.parent = n;

		child.left = n;
		n.parent = child;
		child.parent = parent;

		if (parent != null) {
			if (n == parent.left) {
				parent.left = child;
			} else {
				assert n == parent.right;
				parent.right = child;
			}
		} else {
			root = child;
		}
	}

	private void rotateRight(Node<E> n) {
		Node<E> parent = n.parent, child = n.left, grandchild = child.right;

		n.left = grandchild;
		if (grandchild != null)
			grandchild.parent = n;

		child.right = n;
		n.parent = child;
		child.parent = parent;

		if (parent != null) {
			if (n == parent.left) {
				parent.left = child;
			} else {
				assert n == parent.right;
				parent.right = child;
			}
		} else {
			root = child;
		}
	}

	private void rotate(Node<E> n, boolean left) {
		if (left)
			rotateLeft(n);
		else
			rotateRight(n);
	}

	@Override
	public void removeHandle(Handle<E> handle) {
		if (root == null)
			throw new IllegalArgumentException("hanlde is not valid");
		Node<E> n = (Node<E>) handle;

		/* root with no children, just remove */
		if (n == root && n.left == null && n.right == null) {
			n.clear();
			root = null;
			size--;
			return;
		}

		/* 2 children, switch place with a single child node */
		if (n.left != null && n.right != null) {
			Node<E> swap = (Node<E>) findPredecessorHandle(n);
			if (swap == null)
				swap = (Node<E>) findSuccessorHandle(n);

			E old = swap.e;
			swap.e = n.e;
			n.e = old;
			n = swap;
		}
		assert n.left == null || n.right == null;
		Node<E> parent = n.parent;

		/* Red node, just remove */
		if (n.color == Red) {
			assert n.left == null && n.right == null;
			if (n == parent.left) {
				parent.left = null;
			} else {
				assert n == parent.right;
				parent.right = null;
			}
			n.clear();
			size--;
			return;
		}

		/* Black node, single red child. Remove and make child black */
		if (n.left != null || n.right != null) {
			assert n.left != null ^ n.right != null;
			Node<E> child = n.left != null ? n.left : n.right;
			assert child.color = Red;
			assert child.left == null && child.right == null;
			child.color = Black;
			child.parent = parent;
			if (parent != null) {
				if (n == parent.left) {
					parent.left = child;
				} else {
					assert n == parent.right;
					parent.right = child;
				}
			} else {
				root = child;
			}
			n.clear();
			size--;
			return;
		}

		/* black node, no children. Remove node and fix short parent subtree in loop */
		boolean leftIsShortSide;
		if (leftIsShortSide = (n == parent.left)) {
			parent.left = null;
		} else {
			assert n == parent.right;
			parent.right = null;
		}
		n.clear();
		size--;
		fixAfterRemove(parent, leftIsShortSide);
	}

	private void fixAfterRemove(Node<E> parent, boolean leftIsShortSide) {
		for (;;) {
			Node<E> sibling, d, c;
			if (leftIsShortSide) {
				sibling = parent.right;
				d = sibling.right;
				c = sibling.left;
			} else {
				sibling = parent.left;
				d = sibling.left;
				c = sibling.right;
			}

			/* Case 3: sibling is Red */
			if (sibling.color == Red) {
				rotate(parent, leftIsShortSide);
				assert parent.color == Black;
				parent.color = Red;
				sibling.color = Black;

				sibling = c;
				if (leftIsShortSide) {
					d = sibling.right;
					c = sibling.left;
				} else {
					d = sibling.left;
					c = sibling.right;
				}
			}

			/* Case 6: sibling is Black, d (distant nephew) is red */
			if (d != null && d.color == Red) {
				rotate(parent, leftIsShortSide);
				sibling.color = parent.color;
				parent.color = Black;
				d.color = Black;
				return;
			}

			/* Case 5: sibling is Black, c (close nephew) is red */
			if (c != null && c.color == Red) {
				rotate(sibling, !leftIsShortSide);
				sibling.color = Red;
				c.color = Black;
				rotate(parent, leftIsShortSide);
				c.color = parent.color;
				parent.color = Black;
				sibling.color = Black;
				return;
			}

			/* Case 4: sibling, c, d Black, parent is Red */
			if (parent.color == Red) {
				sibling.color = Red;
				parent.color = Black;
				return;
			}

			/* Case 1: sibling, c, d, parent are Black */
			sibling.color = Red;

			Node<E> grandparent = parent.parent;
			/* Case 2: reached tree root, decrease the total black height by 1, done */
			if (grandparent == null)
				return;

			leftIsShortSide = parent == grandparent.left;
			parent = grandparent;
		}
	}

	public Handle<E> findPredecessorHandle(Handle<E> handle) {
		Node<E> n = (Node<E>) handle;
		/* predecessor in left sub tree */
		if (n.left != null)
			for (Node<E> p = n.left;; p = p.right)
				if (p.right == null)
					return p;

		/* predecessor is some ancestor */
		for (Node<E> p = n, parent; (parent = p.parent) != null; p = parent)
			if (p == parent.right)
				return parent;
		return null;
	}

	public Handle<E> findSuccessorHandle(Handle<E> handle) {
		Node<E> n = (Node<E>) handle;
		/* successor in right sub tree */
		if (n.right != null)
			for (Node<E> p = n.right;; p = p.left)
				if (p.left == null)
					return p;

		/* successor is some ancestor */
		for (Node<E> p = n, parent; (parent = p.parent) != null; p = parent)
			if (p == parent.left)
				return parent;
		return null;
	}

	private static class Node<E> implements Handle<E> {

		private E e;
		private boolean color;
		private Node<E> parent;
		private Node<E> right;
		private Node<E> left;

		Node(E e) {
			this.e = e;
			parent = right = left = null;
		}

		@Override
		public E get() {
			return e;
		}

		@Override
		public String toString() {
			return "{" + (color == Red ? 'R' : 'B') + ":" + e + "}";
		}

		void clear() {
			parent = left = right = null;
			e = null;
		}

	}

	private static class Iter<E> implements Iterator<E> {

		Node<E> n;

		Iter(Node<E> root) {
			if (root == null)
				n = null;
			else
				for (n = root; n.left != null; n = n.left)
					;
		}

		@Override
		public boolean hasNext() {
			return n != null;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			E ret = n.get();

			if (n.right != null) {
				for (n = n.right; n.left != null; n = n.left)
					;
			} else {
				for (Node<E> prev;;) {
					n = (prev = n).parent;
					if (n == null || n.left == prev)
						break;
				}
			}

			return ret;
		}

	}

}
