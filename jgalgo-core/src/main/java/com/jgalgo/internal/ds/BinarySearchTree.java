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

/**
 * Binary search tree data structure.
 * <p>
 * In addition to all {@link HeapReferenceable} operations, a binary search tree (BST) allow for an efficient search of
 * an element, not just {@link Heap#findMin()}. Every element could be found in \(O(\log n)\) time, notably
 * {@link #findMax()} in addition to {@link Heap#findMin()}. Also, given an element, the nearest (smaller or larger)
 * element in the tree can be found efficiently.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @author     Barak Ugav
 */
public interface BinarySearchTree<K, V> extends HeapReferenceable<K, V> {

	/**
	 * Find the element with the maximal key in the tree and return a reference to it.
	 *
	 * @return                       a reference to the element with the maximal key in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	HeapReference<K, V> findMax();

	/**
	 * Extract the element with the maximal key in the tree.
	 * <p>
	 * This method find and <b>remove</b> the element with the maximal key.
	 *
	 * @return                       the element with the maximal key in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	HeapReference<K, V> extractMax();

	/**
	 * Find an element in the tree by its key, or the element with the greatest strictly smaller (predecessor) key than
	 * it if it's not found.
	 *
	 * @param  key the search key
	 * @return     reference to an element with the searched key or it's predecessor if is not found, or {@code null} if
	 *             there is no predecessor
	 */
	HeapReference<K, V> findOrSmaller(K key);

	/**
	 * Find an element in the tree by its key, or the element with the smallest strictly greater (successor) key than it
	 * if it's not found.
	 *
	 * @param  key the search key
	 * @return     reference to an element with the searched key or it's successor if it is not found, or {@code null}
	 *             if there is no successor
	 */
	HeapReference<K, V> findOrGreater(K key);

	/**
	 * Find the element with the greatest strictly smaller key than a given key.
	 *
	 * @param  key a key
	 * @return     reference to the predecessor element with strictly smaller key or {@code null} if no such exists
	 */
	HeapReference<K, V> findSmaller(K key);

	/**
	 * Find the element with the smallest strictly greater key than a given key.
	 *
	 * @param  key a key
	 * @return     reference to the successor element with strictly greater key or {@code null} if no such exists
	 */
	HeapReference<K, V> findGreater(K key);

	/**
	 * Get the predecessor of a node in the tree.
	 * <p>
	 * The predecessor node depends on the tree structure. If there are no duplicate keys, the predecessor is the
	 * greatest value strictly smaller than the given element. If there are duplicate keys, it may be smaller or equal.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely if it refer to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref reference to an element in the tree
	 * @return     reference to the predecessor element in the tree, that is an element with smaller or equal key to the
	 *             given referenced element's key, or {@code null} if no such predecessor exists
	 */
	HeapReference<K, V> getPredecessor(HeapReference<K, V> ref);

	/**
	 * Finds the successor of an element in the tree.
	 * <p>
	 * The successor node depends on the tree structure. If there are no duplicate keys, the successor is the smallest
	 * value strictly greater than the given element. If there are duplicate keys, it may be greater or equal.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely if it refer to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref reference to an element in the tree
	 * @return     reference to the successor element in the tree, that is an element with greater or equal key to the
	 *             given referenced element's key, or {@code null} if no such successor exists
	 */
	HeapReference<K, V> getSuccessor(HeapReference<K, V> ref);

	/**
	 * Split the current BST into two different BSTs with keys strictly smaller and greater or equal than a key.
	 * <p>
	 * After this operation, all elements in this tree will have keys greater or equal than the given key, and the
	 * returned new tree will contain elements with keys strictly smaller than the given key.
	 *
	 * @param  key a pivot key
	 * @return     new tree with elements with keys strictly smaller than the given key
	 */
	BinarySearchTree<K, V> splitSmaller(K key);

	/**
	 * Split the current BST into two different BSTs with keys smaller or equal and strictly greater than a key.
	 * <p>
	 * After this operation, all elements in this tree will have keys be smaller or equal than the given key, and the
	 * returned new tree will contain elements with keys strictly greater than the given key.
	 *
	 * @param  key a pivot key
	 * @return     new tree with elements with keys strictly greater than the given key
	 */
	BinarySearchTree<K, V> splitGreater(K key);

	/**
	 * Split the current BST into two different BSTs with elements smaller and greater than an element's key.
	 * <p>
	 * After this operation, all elements keys in this tree will be smaller or equal to the given element's key, and the
	 * returned new tree will contain elements with keys greater than the given element's key. If the tree contains
	 * duplications of the given element's key, the elements keys in the returned tree will be greater or equal (rather
	 * than strictly greater). To split a tree more precisely, use {@link #splitSmaller(Object)} or
	 * {@link #splitGreater(Object)}.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely if it refer to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref given element in the tree
	 * @return     new tree with elements with keys greater (greater or equal if duplicate keys of the given element's
	 *             key exists) than the given key
	 */
	BinarySearchTree<K, V> split(HeapReference<K, V> ref);

	/**
	 * Create a new BST.
	 * <p>
	 * This is the recommended way to instantiate a new {@link BinarySearchTree} object. The
	 * {@link BinarySearchTree.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link BinarySearchTree}
	 */
	static <K, V> BinarySearchTree<K, V> newInstance() {
		return newBuilder().<K>keysTypeObj().<V>valuesTypeObj().build();
	}

	/**
	 * Create a new BST with custom comparator.
	 * <p>
	 * This is the recommended way to instantiate a new {@link BinarySearchTree} object. The
	 * {@link BinarySearchTree.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link BinarySearchTree}
	 */
	static <K, V> BinarySearchTree<K, V> newInstance(Comparator<? super K> cmp) {
		return newBuilder().<K>keysTypeObj().<V>valuesTypeObj().build(cmp);
	}

	/**
	 * Create a new binary search tree algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link BinarySearchTree} object.
	 *
	 * @return a new builder that can build {@link BinarySearchTree} objects
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static BinarySearchTree.Builder<Object, Object> newBuilder() {
		return new BinarySearchTree.Builder<>() {

			boolean splitRequired;
			boolean meldRequired;
			String impl;

			@Override
			public BinarySearchTree build(Comparator cmp) {
				if (impl != null) {
					switch (impl) {
						case "splay":
							return new SplayTree(cmp);
						case "red-black":
							return new RedBlackTree(cmp);
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				if (splitRequired || meldRequired) {
					return new SplayTree(cmp);
				} else {
					return new RedBlackTree(cmp);
				}
			}

			@Override
			public BinarySearchTree.Builder setSplits(boolean enable) {
				splitRequired = enable;
				return this;
			}

			@Override
			public BinarySearchTree.Builder setMelds(boolean enable) {
				meldRequired = enable;
				return this;
			}

			@Override
			public BinarySearchTree.Builder keysTypeObj() {
				return this;
			}

			@Override
			public BinarySearchTree.Builder keysTypePrimitive(Class primitiveType) {
				return this;
			}

			@Override
			public BinarySearchTree.Builder valuesTypeObj() {
				return this;
			}

			@Override
			public BinarySearchTree.Builder valuesTypePrimitive(Class primitiveType) {
				return this;
			}

			@Override
			public BinarySearchTree.Builder valuesTypeVoid() {
				return this;
			}

			@Override
			public BinarySearchTree.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link BinarySearchTree} objects.
	 *
	 * @see    BinarySearchTree#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder<K, V> extends HeapReferenceable.Builder<K, V> {

		@Override
		BinarySearchTree<K, V> build(Comparator<? super K> cmp);

		@Override
		default BinarySearchTree<K, V> build() {
			return build(null);
		}

		/**
		 * Enable/disable efficient split operations.
		 *
		 * @param  enable if {@code true} the split operations such as {@link BinarySearchTree#split(HeapReference)},
		 *                    {@link BinarySearchTree#splitSmaller(Object)} and
		 *                    {@link BinarySearchTree#splitGreater(Object)} will be supported efficiently by the trees
		 *                    created by this builder
		 * @return        this builder
		 */
		BinarySearchTree.Builder<K, V> setSplits(boolean enable);

		/**
		 * Enable/disable efficient {@link BinarySearchTree#meld} operations.
		 *
		 * @param  enable if {@code true} the {@link BinarySearchTree#meld} operation will be supported efficiently by
		 *                    the trees created by this builder
		 * @return        this builder
		 */
		BinarySearchTree.Builder<K, V> setMelds(boolean enable);

		@Override
		<Keys> BinarySearchTree.Builder<Keys, V> keysTypeObj();

		@Override
		<Keys> BinarySearchTree.Builder<Keys, V> keysTypePrimitive(Class<? extends Keys> primitiveType);

		@Override
		<Values> BinarySearchTree.Builder<K, Values> valuesTypeObj();

		@Override
		<Values> BinarySearchTree.Builder<K, Values> valuesTypePrimitive(Class<? extends Values> primitiveType);

		@Override
		BinarySearchTree.Builder<K, Void> valuesTypeVoid();

		@Override
		default BinarySearchTree.Builder<K, V> setOption(String key, Object value) {
			HeapReferenceable.Builder.super.setOption(key, value);
			return this;
		}
	}

}
