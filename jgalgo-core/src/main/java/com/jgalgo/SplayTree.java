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

package com.jgalgo;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * Splay binary search tree.
 * <p>
 * The splay tree is a binary search tree which is not strictly balanced, in contrast to {@link RedBlackTree}. The splay
 * tree has a property that recent accessed elements are closer to the root, and therefore faster to access. All
 * operations have a time complexity of \(O(\log n)\) amortized time.
 * <p>
 * The splay tree is specifically efficient for splits and joins operations.
 * <p>
 * Based on 'Self-Adjusting Binary Search Trees' by Sleator and Tarjan (1985).
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Splay_tree">Wikipedia</a>
 * @author Barak Ugav
 */
public class SplayTree<E> extends BinarySearchTreeAbstract<E> {

	private NodeSized<E> root;
	private final SplayImplWithSize<E> impl = new SplayImplWithSize<>();
	private final Set<HeapReference<E>> refsSet;

	/**
	 * Constructs a new, empty splay tree, sorted according to the natural ordering of its elements.
	 * <p>
	 * All elements inserted into the tree must implement the {@link Comparable} interface. Furthermore, all such
	 * elements must be <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a {@code ClassCastException}
	 * for any elements {@code e1} and {@code e2} in the tree. If the user attempts to insert an element to the tree
	 * that violates this constraint (for example, the user attempts to insert a string element to a tree whose elements
	 * are integers), the {@code insert} call will throw a {@code ClassCastException}.
	 */
	public SplayTree() {
		this(null);
	}

	/**
	 * Constructs a new, empty splay tree, sorted according to the specified comparator.
	 * <p>
	 * All elements inserted into the tree must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(e1, e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and
	 * {@code e2} in the tree. If the user attempts to insert an element to the tree that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this tree. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the elements will be used.
	 */
	public SplayTree(Comparator<? super E> comparator) {
		super(comparator);
		root = null;

		refsSet = new AbstractSet<>() {

			@Override
			public int size() {
				return SplayTree.this.size();
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Iterator<HeapReference<E>> iterator() {
				return (Iterator) new BinarySearchTrees.BSTIterator<>(root);
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean remove(Object o) {
				SplayTree.this.removeRef((HeapReference<E>) o);
				return true;
			}

			@Override
			public void clear() {
				SplayTree.this.clear();
			}
		};
	}

	@Override
	public int size() {
		return root != null ? root.size : 0;
	}

	@Override
	public Set<HeapReference<E>> refsSet() {
		return refsSet;
	}

	@Override
	public HeapReference<E> insert(E e) {
		return insertNode(new NodeSized<>(e));
	}

	private NodeSized<E> insertNode(NodeSized<E> n) {
		assert n.parent == null;
		assert n.left == null;
		assert n.right == null;
		if (root == null)
			return root = n;

		BinarySearchTrees.insert(root, c, n);
		for (NodeSized<E> p = n.parent; p != null; p = p.parent)
			p.size++;
		return root = impl.splay(n);
	}

	@Override
	public void removeRef(HeapReference<E> ref) {
		NodeSized<E> n = (NodeSized<E>) ref;

		/* If the node has two children, swap with successor */
		if (n.hasLeftChild() && n.hasRightChild())
			swap(n, BinarySearchTrees.getSuccessor(n));

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

	private void swap(NodeSized<E> n1, NodeSized<E> n2) {
		BinarySearchTrees.swap(n1, n2);
		if (n1 == root)
			root = n2;
		else if (n2 == root)
			root = n1;
		int size = n1.size;
		n1.size = n2.size;
		n2.size = size;
	}

	@Override
	public void decreaseKey(HeapReference<E> ref, E e) {
		NodeSized<E> n = (NodeSized<E>) ref;
		makeSureDecreaseKeyIsSmaller(n.data, e);
		removeRef(n);
		n.data = e;
		insertNode(n);
	}

	@Override
	public HeapReference<E> findRef(E e) {
		NodeSized<E> n = BinarySearchTrees.find(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> findMinRef() {
		checkTreeNotEmpty();
		return root = impl.splay(BinarySearchTrees.findMin(root));
	}

	@Override
	public HeapReference<E> findMaxRef() {
		checkTreeNotEmpty();
		return root = impl.splay(BinarySearchTrees.findMax(root));
	}

	@Override
	public HeapReference<E> findOrSmaller(E e) {
		NodeSized<E> n = BinarySearchTrees.findOrSmaller(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> findOrGreater(E e) {
		NodeSized<E> n = BinarySearchTrees.findOrGreater(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> findSmaller(E e) {
		NodeSized<E> n = BinarySearchTrees.findSmaller(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> findGreater(E e) {
		NodeSized<E> n = BinarySearchTrees.findGreater(root, c, e);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> getPredecessor(HeapReference<E> ref) {
		NodeSized<E> n = BinarySearchTrees.getPredecessor((NodeSized<E>) ref);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<E> getSuccessor(HeapReference<E> ref) {
		NodeSized<E> n = BinarySearchTrees.getSuccessor((NodeSized<E>) ref);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public E extractMin() {
		checkTreeNotEmpty();
		NodeSized<E> n = BinarySearchTrees.findMin(root);
		E ret = n.data;
		removeRef(n);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(Heap<? extends E> heap) {
		makeSureNoMeldWithSelf(heap);
		makeSureMeldWithSameImpl(SplayTree.class, heap);
		makeSureEqualComparatorBeforeMeld(heap);
		SplayTree<E> h = (SplayTree<E>) heap;
		if (h.isEmpty())
			return;
		if (isEmpty()) {
			root = h.root;
			h.root = null;
			return;
		}

		E min1, max1, min2, max2;
		if (compare(max1 = findMax(), min2 = h.findMin()) <= 0) {
			/* all elements in this tree are <= than all elements in other tree */
			root = meld(this, h);
		} else if (compare(min1 = findMin(), max2 = h.findMax()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			root = meld(h, this);
		} else {
			int minCmp = compare(min1, min2);
			int maxCmp = compare(max1, max2);
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

			if (h.root != null) {
				/* there is nothing smarter to do than 'addAll' for the shared range */
				/* We use 'insertNode' instead of 'insert' to maintain user references to nodes */
				for (NodeSized<E> node = h.root;;) {
					for (;;) {
						while (node.hasLeftChild())
							node = node.left;
						if (!node.hasRightChild())
							break;
						node = node.right;
					}
					node.size = 1;
					NodeSized<E> parent = node.parent;
					if (parent == null) {
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
						insertNode(node);
						node = parent;
					}
				}
			}

			if (hLow != null) {
				assert compare(hLow.findMax(), findMin()) < 0;
				root = meld(hLow, this);
			}
			if (hHigh != null) {
				assert compare(hHigh.findMin(), findMax()) > 0;
				root = meld(this, hHigh);
			}
		}
		h.root = null;
	}

	private static <E> NodeSized<E> meld(SplayTree<E> t1, SplayTree<E> t2) {
		/* Assume all nodes in t1 are smaller than all nodes in t2 */

		/* Splay t1 max and t2 min */
		NodeSized<E> n1 = (NodeSized<E>) t1.findMaxRef();
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
		NodeSized<E> succ = (NodeSized<E>) findGreater(e);
		if (succ == null)
			return new SplayTree<>(c);
		return split(succ);
	}

	@Override
	public SplayTree<E> split(HeapReference<E> ref) {
		SplayTree<E> newTree = new SplayTree<>(c);

		NodeSized<E> n = impl.splay((NodeSized<E>) ref);
		assert n.isRoot();

		if ((root = n.left) != null) {
			n.size -= n.left.size;
			n.left.parent = null;
			n.left = null;
		}

		newTree.root = n;
		return newTree;
	}

	@Override
	public void clear() {
		if (root == null)
			return;
		BinarySearchTrees.clear(root);
		root = null;
	}

	static class Node<E, N extends Node<E, N>> extends BinarySearchTrees.Node<E, N> implements HeapReference<E> {

		Node(E e) {
			super(e);
		}

		@Override
		public E get() {
			return data;
		}

	}

	static abstract class SplayImpl<E, N extends Node<E, N>> {

		SplayImpl() {}

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

		void beforeRotate(N n) {}

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
