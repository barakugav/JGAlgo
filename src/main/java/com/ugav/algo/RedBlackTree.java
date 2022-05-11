package com.ugav.algo;

import java.util.Comparator;
import java.util.Iterator;

import com.ugav.algo.BSTUtils.NeighborType;

public class RedBlackTree<E> extends HeapAbstractDirectAccessed<E> implements BST<E> {

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

	@Override
	public Handle<E> insert(E e) {
		Node<E> n = new Node<>(e);
		insertNode(n);
		return n;
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
	public void meld(Heap<? extends E> h) {
		// TODO
		super.meld(h);
	}

	@Override
	public Handle<E> findHanlde(E e) {
		return BSTUtils.find(root, c, e);
	}

	@Override
	public Handle<E> findMinHandle() {
		if (root == null)
			throw new IllegalStateException();
		return BSTUtils.findMin(root);
	}

	@Override
	public void decreaseKey(Handle<E> handle, E e) {
		Node<E> n = (Node<E>) handle;
		removeHandle(n);
		n.val = e;
		insertNode(n);
	}

	private void insertNode(Node<E> n) {
		if (root == null) {
			n.color = Black;
			root = n;
		} else {
			BSTUtils.insert(root, c, n);
			fixAfterInsert(n);
		}
		size++;
	}

	private void fixAfterInsert(Node<E> n) {
		Node<E> parent = n.parent;
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
			Node<E> swap = BSTUtils.findSuccessor(n);
			E old = swap.val;
			swap.val = n.val;
			n.val = old;
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

	@Override
	public Handle<E> findOrPredecessor(E e) {
		return BSTUtils.findOrNeighbor(root, c, e, NeighborType.Predecessor);
	}

	@Override
	public Handle<E> findOrSuccessor(E e) {
		return BSTUtils.findOrNeighbor(root, c, e, NeighborType.Successor);
	}

	@Override
	public Handle<E> findPredecessor(Handle<E> handle) {
		return BSTUtils.findPredecessor((Node<E>) handle);
	}

	@Override
	public Handle<E> findSuccessor(Handle<E> handle) {
		return BSTUtils.findSuccessor((Node<E>) handle);
	}

	private static class Node<E> extends BSTUtils.Node<E, Node<E>> implements Handle<E> {

		private boolean color;

		Node(E e) {
			super(e);
		}

		@Override
		public E get() {
			return val;
		}

		@Override
		public String toString() {
			return "{" + (color == Red ? 'R' : 'B') + ":" + val + "}";
		}

	}

}
