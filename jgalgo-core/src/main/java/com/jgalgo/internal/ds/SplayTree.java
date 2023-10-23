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
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        <a href="https://en.wikipedia.org/wiki/Splay_tree">Wikipedia</a>
 * @author     Barak Ugav
 */
class SplayTree<K, V> extends BinarySearchTreeAbstract<K, V> {

	private SplayBSTNode<K, V> root;
	private final SplayImplWithSize<K, V> impl = new SplayImplWithSize<>();

	/**
	 * Constructs a new, empty splay tree, ordered according to the natural ordering of its keys.
	 * <p>
	 * All keys inserted into the tree must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this
	 * constraint (for example, the user attempts to insert a string element to a tree whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 */
	SplayTree() {
		this(null);
	}

	/**
	 * Constructs a new, empty splay tree, with keys ordered according to the specified comparator.
	 * <p>
	 * All keys inserted into the tree must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this tree. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the keys will be used.
	 */
	SplayTree(Comparator<? super K> comparator) {
		super(comparator);
		root = null;
	}

	@Override
	public int size() {
		return root != null ? root.size : 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<HeapReference<K, V>> iterator() {
		return (Iterator) new BinarySearchTrees.BSTIterator<>(root);
	}

	@Override
	public HeapReference<K, V> insert(K key) {
		return insertNode(new SplayBSTNode<>(key));
	}

	private SplayBSTNode<K, V> insertNode(SplayBSTNode<K, V> n) {
		assert n.parent == null;
		assert n.left == null;
		assert n.right == null;
		if (root == null)
			return root = n;

		BinarySearchTrees.insert(root, c, n);
		for (SplayBSTNode<K, V> p = n.parent; p != null; p = p.parent)
			p.size++;
		return root = impl.splay(n);
	}

	@Override
	public void remove(HeapReference<K, V> ref) {
		SplayBSTNode<K, V> n = (SplayBSTNode<K, V>) ref;

		/* If the node has two children, swap with successor */
		if (n.hasLeftChild() && n.hasRightChild())
			swap(n, BinarySearchTrees.getSuccessor(n));

		SplayBSTNode<K, V> child;
		if (!n.hasLeftChild()) {
			replace(n, child = n.right);
		} else {
			assert !n.hasRightChild();
			replace(n, child = n.left);
		}

		/* Decrease ancestors size by 1 */
		for (SplayBSTNode<K, V> p = n.parent; p != null; p = p.parent)
			p.size--;

		SplayBSTNode<K, V> parent = n.parent;
		n.clearWithoutUserData();
		root = impl.splay(child != null ? child : parent);
	}

	private void replace(SplayBSTNode<K, V> u, SplayBSTNode<K, V> v) {
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

	private void swap(SplayBSTNode<K, V> n1, SplayBSTNode<K, V> n2) {
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
	public void decreaseKey(HeapReference<K, V> ref, K newKey) {
		SplayBSTNode<K, V> n = (SplayBSTNode<K, V>) ref;
		Assertions.Heaps.decreaseKeyIsSmaller(n.key, newKey, c);
		remove(n);
		n.key = newKey;
		insertNode(n);
	}

	@Override
	public HeapReference<K, V> find(K key) {
		SplayBSTNode<K, V> n = BinarySearchTrees.find(root, c, key);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> findMin() {
		checkTreeNotEmpty();
		return root = impl.splay(BinarySearchTrees.findMin(root));
	}

	@Override
	public HeapReference<K, V> findMax() {
		checkTreeNotEmpty();
		return root = impl.splay(BinarySearchTrees.findMax(root));
	}

	@Override
	public HeapReference<K, V> findOrSmaller(K key) {
		SplayBSTNode<K, V> n = BinarySearchTrees.findOrSmaller(root, c, key);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> findOrGreater(K key) {
		SplayBSTNode<K, V> n = BinarySearchTrees.findOrGreater(root, c, key);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> findSmaller(K key) {
		SplayBSTNode<K, V> n = BinarySearchTrees.findSmaller(root, c, key);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> findGreater(K key) {
		SplayBSTNode<K, V> n = BinarySearchTrees.findGreater(root, c, key);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> getPredecessor(HeapReference<K, V> ref) {
		SplayBSTNode<K, V> n = BinarySearchTrees.getPredecessor((SplayBSTNode<K, V>) ref);
		return n == null ? null : (root = impl.splay(n));
	}

	@Override
	public HeapReference<K, V> getSuccessor(HeapReference<K, V> ref) {
		SplayBSTNode<K, V> n = BinarySearchTrees.getSuccessor((SplayBSTNode<K, V>) ref);
		return n == null ? null : (root = impl.splay(n));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void meld(HeapReferenceable<? extends K, ? extends V> heap) {
		Assertions.Heaps.noMeldWithSelf(this, heap);
		Assertions.Heaps.meldWithSameImpl(SplayTree.class, heap);
		Assertions.Heaps.equalComparatorBeforeMeld(this, heap);
		SplayTree<K, V> h = (SplayTree<K, V>) heap;
		if (h.isEmpty())
			return;
		if (isEmpty()) {
			root = h.root;
			h.root = null;
			return;
		}

		K min1, max1, min2, max2;
		if (compare(max1 = findMax().key(), min2 = h.findMin().key()) <= 0) {
			/* all elements in this tree are <= than all elements in other tree */
			root = meld(this, h);
		} else if (compare(min1 = findMin().key(), max2 = h.findMax().key()) >= 0) {
			/* all elements in this tree are >= than all elements in other tree */
			root = meld(h, this);
		} else {
			int minCmp = compare(min1, min2);
			int maxCmp = compare(max1, max2);
			SplayTree<K, V> hLow = null, hHigh = null;

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
				for (SplayBSTNode<K, V> node = h.root;;) {
					for (;;) {
						while (node.hasLeftChild())
							node = node.left;
						if (!node.hasRightChild())
							break;
						node = node.right;
					}
					node.size = 1;
					SplayBSTNode<K, V> parent = node.parent;
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
				assert compare(hLow.findMax().key(), findMin().key()) < 0;
				root = meld(hLow, this);
			}
			if (hHigh != null) {
				assert compare(hHigh.findMin().key(), findMax().key()) > 0;
				root = meld(this, hHigh);
			}
		}
		h.root = null;
	}

	private static <K, V> SplayBSTNode<K, V> meld(SplayTree<K, V> t1, SplayTree<K, V> t2) {
		/* Assume all nodes in t1 are smaller than all nodes in t2 */

		/* Splay t1 max and t2 min */
		SplayBSTNode<K, V> n1 = (SplayBSTNode<K, V>) t1.findMax();
		assert n1.isRoot();
		assert !n1.hasRightChild();

		n1.right = t2.root;
		t2.root.parent = n1;
		n1.size += t2.root.size;
		return n1;
	}

	@Override
	public SplayTree<K, V> splitSmaller(K key) {
		SplayTree<K, V> newTree = new SplayTree<>(c);
		SplayBSTNode<K, V> pred = (SplayBSTNode<K, V>) findSmaller(key);
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
	public SplayTree<K, V> splitGreater(K key) {
		SplayBSTNode<K, V> succ = (SplayBSTNode<K, V>) findGreater(key);
		if (succ == null)
			return new SplayTree<>(c);
		return split(succ);
	}

	@Override
	public SplayTree<K, V> split(HeapReference<K, V> ref) {
		SplayTree<K, V> newTree = new SplayTree<>(c);

		SplayBSTNode<K, V> n = impl.splay((SplayBSTNode<K, V>) ref);
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

	static class BaseNode<K, N extends BaseNode<K, N>> extends BinarySearchTrees.INode<K, N> {

		BaseNode(K key) {
			super(key);
		}

		public K key() {
			return key;
		}

	}

	static abstract class SplayImpl<K, N extends BaseNode<K, N>> {

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

	private static class SplayBSTNode<K, V> extends BaseNode<K, SplayBSTNode<K, V>> implements HeapReference<K, V> {

		int size;

		private V value;

		SplayBSTNode(K key) {
			super(key);
			size = 1;
		}

		@Override
		public K key() {
			return key;
		}

		@Override
		public V value() {
			return value;
		}

		@Override
		public void setValue(V val) {
			value = val;
		}

		@Override
		void clearWithoutUserData() {
			super.clearWithoutUserData();
			size = 1;
		}

	}

	private static class SplayImplWithSize<K, V> extends SplayImpl<K, SplayBSTNode<K, V>> {

		@Override
		void beforeRotate(SplayBSTNode<K, V> n) {
			super.beforeRotate(n);

			SplayBSTNode<K, V> parent = n.parent;
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
