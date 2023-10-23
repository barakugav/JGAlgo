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

import java.util.Comparator;
import java.util.Iterator;
import com.jgalgo.internal.util.Assertions;

/**
 * A red black balanced binary search tree.
 * <p>
 * A red black tree is a balanced binary search tree that its height is always \(O(\log n)\). All operations are
 * performed in \(O(\log n)\) time.
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        <a href= "https://en.wikipedia.org/wiki/Red%E2%80%93black_tree">Wikipedia</a>
 * @author     Barak Ugav
 */
class RedBlackTree<K, V> extends BinarySearchTreeAbstract<K, V> {

	private int size;
	private Node<K, V> root;

	static final boolean Red = true;
	static final boolean Black = false;

	/**
	 * Constructs a new, empty red black tree, ordered according to the natural ordering of its keys.
	 * <p>
	 * All keys inserted into the tree must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this
	 * constraint (for example, the user attempts to insert a string element to a tree whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 */
	RedBlackTree() {
		this(null);
	}

	/**
	 * Constructs a new, empty red black tree, with keys ordered according to the specified comparator.
	 * <p>
	 * All keys inserted into the tree must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this tree. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the keys will be used.
	 */
	RedBlackTree(Comparator<? super K> comparator) {
		super(comparator);
		root = null;
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public HeapReference<K, V> insert(K key) {
		return insertNode(newNode(key));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new BinarySearchTrees.BSTIterator<>(root);
	}

	@Override
	public void clear() {
		if (root == null)
			return;
		BinarySearchTrees.clear(root);
		root = null;
		size = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		Assertions.Heaps.noMeldWithSelf(this, heap);
		Assertions.Heaps.meldWithSameImpl(RedBlackTree.class, heap);
		Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
		RedBlackTree<K, V> h = (RedBlackTree<K, V>) heap;
		if (h.isEmpty())
			return;
		if (isEmpty()) {
			root = h.root;
			size = h.size;
		} else {
			/* there is nothing smarter to do than 'addAll' */
			/* We use 'insertNode' instead of 'insert' to maintain user references to nodes */
			for (Node<K, V> node = h.root;;) {
				for (;;) {
					while (node.hasLeftChild())
						node = node.left;
					if (!node.hasRightChild())
						break;
					node = node.right;
				}
				Node<K, V> parent = node.parent;
				if (parent == null) {
					beforeNodeReuse(node);
					insertNode(node);
					break;
				} else {
					if (parent.right == node) {
						parent.right = null;
					} else {
						assert parent.left == node;
						parent.left = null;
					}
					node.parent = null;
					beforeNodeReuse(node);
					insertNode(node);
					node = parent;
				}
			}
		}
		h.root = null;
		h.size = 0;
	}

	void beforeNodeReuse(Node<K, V> node) {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException the current implementation doesn't support this operation
	 */
	@Override
	public BinarySearchTree<K, V> splitSmaller(K key) {
		// we can't perform efficient split because we don't know the size of each sub tree, and we won't be able to
		// determine the size splits
		// TODO consider implementing this in O(n)
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException the current implementation doesn't support this operation
	 */
	@Override
	public BinarySearchTree<K, V> splitGreater(K key) {
		// we can't perform efficient split because we don't know the size of each sub tree, and we won't be able to
		// determine the size splits
		// TODO consider implementing this in O(n)
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException the current implementation doesn't support this operation
	 */
	@Override
	public RedBlackTree<K, V> split(HeapReference<K, V> ref) {
		// we can't perform efficient split because we don't know the size of each sub tree, and we won't be able to
		// determine the size splits
		// TODO consider implementing this in O(n)
		throw new UnsupportedOperationException();
	}

	@Override
	public HeapReference<K, V> find(K key) {
		return BinarySearchTrees.find(root, c, key);
	}

	@Override
	public HeapReference<K, V> findMin() {
		if (root == null)
			throw new IllegalStateException();
		return BinarySearchTrees.findMin(root);
	}

	@Override
	public HeapReference<K, V> findMax() {
		if (root == null)
			throw new IllegalStateException();
		return BinarySearchTrees.findMax(root);
	}

	@Override
	public void decreaseKey(HeapReference<K, V> ref, K newKey) {
		Node<K, V> n = (Node<K, V>) ref;
		Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKey, c);
		remove(n);
		n.key = newKey;
		insertNode(n);
	}

	private Node<K, V> insertNode(Node<K, V> n) {
		assert n.parent == null;
		assert n.left == null;
		assert n.right == null;
		if (root == null) {
			n.color = Black;
			root = n;
			afterInsert(n);
		} else {
			BinarySearchTrees.insert(root, c, n);
			afterInsert(n);
			fixAfterInsert(n);
		}
		size++;
		return n;
	}

	private void fixAfterInsert(Node<K, V> n) {
		Node<K, V> parent = n.parent;
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

			Node<K, V> grandparent = parent.parent;
			Node<K, V> uncle = parent == grandparent.left ? grandparent.right : grandparent.left;

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

	private void removeNode(Node<K, V> n, Node<K, V> replace) {
		beforeRemove(n);
		Node<K, V> parent = n.parent;
		if (parent != null) {
			if (n == parent.left) {
				parent.left = replace;
			} else {
				assert n == parent.right;
				parent.right = replace;
			}
		} else {
			root = replace;
		}
		if (replace != null)
			replace.parent = parent;
		n.clearWithoutUserData();
		size--;
	}

	void removeNode(Node<K, V> n) {
		/* root with no children, just remove */
		if (n == root && n.left == null && n.right == null) {
			removeNode(n, null);
			return;
		}

		/* 2 children, switch place with a single child node */
		if (n.left != null && n.right != null)
			swap(n, BinarySearchTrees.getSuccessor(n));
		assert n.left == null || n.right == null;
		Node<K, V> parent = n.parent;

		/* Red node, just remove */
		if (n.color == Red) {
			assert n.left == null && n.right == null;
			removeNode(n, null);
			return;
		}

		/* Black node, single red child. Remove and make child black */
		if (n.left != null || n.right != null) {
			assert n.left != null ^ n.right != null;
			Node<K, V> child = n.left != null ? n.left : n.right;
			assert child.color = Red;
			assert child.left == null && child.right == null;
			child.color = Black;
			removeNode(n, child);
			return;
		}

		/* black node, no children. Remove node and fix short parent subtree in loop */
		boolean leftIsShortSide = n == parent.left;
		removeNode(n, null);
		fixAfterRemove(parent, leftIsShortSide);
	}

	@Override
	public void remove(HeapReference<K, V> ref) {
		if (root == null)
			throw new IllegalArgumentException("ref is not valid");
		removeNode((Node<K, V>) ref);
	}

	private void fixAfterRemove(Node<K, V> parent, boolean leftIsShortSide) {
		for (;;) {
			Node<K, V> sibling, d, c;
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

			Node<K, V> grandparent = parent.parent;
			/* Case 2: reached tree root, decrease the total black height by 1, done */
			if (grandparent == null)
				return;

			leftIsShortSide = parent == grandparent.left;
			parent = grandparent;
		}
	}

	private void rotate(Node<K, V> n, boolean left) {
		if (left)
			rotateLeft(n);
		else
			rotateRight(n);
	}

	private void rotateLeft(Node<K, V> n) {
		beforeRotateLeft(n);
		Node<K, V> parent = n.parent, child = n.right, grandchild = child.left;

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

	private void rotateRight(Node<K, V> n) {
		beforeRotateRight(n);
		Node<K, V> parent = n.parent, child = n.left, grandchild = child.right;

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

	void swap(Node<K, V> n1, Node<K, V> n2) {
		BinarySearchTrees.swap(n1, n2);
		if (n1 == root)
			root = n2;
		else if (n2 == root)
			root = n1;
		boolean color = n1.color;
		n1.color = n2.color;
		n2.color = color;
	}

	@Override
	public HeapReference<K, V> findOrSmaller(K key) {
		return BinarySearchTrees.findOrSmaller(root, c, key);
	}

	@Override
	public HeapReference<K, V> findOrGreater(K key) {
		return BinarySearchTrees.findOrGreater(root, c, key);
	}

	@Override
	public HeapReference<K, V> findSmaller(K key) {
		return BinarySearchTrees.findSmaller(root, c, key);
	}

	@Override
	public HeapReference<K, V> findGreater(K key) {
		return BinarySearchTrees.findGreater(root, c, key);
	}

	@Override
	public HeapReference<K, V> getPredecessor(HeapReference<K, V> ref) {
		return BinarySearchTrees.getPredecessor((Node<K, V>) ref);
	}

	@Override
	public HeapReference<K, V> getSuccessor(HeapReference<K, V> ref) {
		return BinarySearchTrees.getSuccessor((Node<K, V>) ref);
	}

	/**
	 * [experimental API] Get an iterator that iterate over all the nodes in a node's sub tree.
	 *
	 * @param  ref a reference to a node in the tree
	 * @return     an iterator that iterate over all the nodes in the node's sub tree
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Iterator<HeapReference<K, V>> subTreeIterator(HeapReference<K, V> ref) {
		return (Iterator) (new BinarySearchTrees.BSTIterator<>((Node<K, V>) ref));
	}

	static class Node<K, V> extends BinarySearchTrees.INode<K, Node<K, V>> implements HeapReference<K, V> {

		private boolean color;
		private V value;

		Node(K key) {
			super(key);
		}

		@Override
		public K key() {
			return key;
		}

		@Override
		public String toString() {
			// return "{" + (color == Red ? 'R' : 'B') + ":" + key + "}";
			return "{" + key + ":" + value + "}";
		}

		@Override
		public V value() {
			return value;
		}

		@Override
		public void setValue(V val) {
			value = val;
		}

	}

	/* Hooks for extended red black tree sub class */

	Node<K, V> newNode(K key) {
		return new Node<>(key);
	}

	void afterInsert(Node<K, V> n) {}

	void beforeRemove(Node<K, V> n) {}

	void beforeRotateLeft(Node<K, V> n) {}

	void beforeRotateRight(Node<K, V> n) {}

}
