package com.jgalgo;

import java.util.Comparator;

/**
 * Binary search tree data structure.
 * <p>
 * In addition to all {@link HeapReferenceable} operations, a binary search tree (BST) allow for an efficient search for
 * an element, not just {@link Heap#findMin()}. Every element could be found in \(O(\log n)\) time, notably
 * {@link #findMax()} in addition to {@link Heap#findMin()}. Also, given an element, the nearest (smaller or larger)
 * element in the tree can be found efficiently.
 *
 * @param  <E> the elements type
 * @author     Barak Ugav
 */
public interface BinarySearchTree<E> extends HeapReferenceable<E> {

	/**
	 * Find the maximum element in the tree.
	 *
	 * @return                       the maximum element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public E findMax();

	/**
	 * Extract the maximum element in the tree.
	 *
	 * @return                       the maximum element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public E extractMax();

	/**
	 * Find maximal element in the tree and return a reference to it.
	 *
	 * @return                       a reference to the maximal element in the tree
	 * @throws IllegalStateException if the tree is empty
	 */
	public HeapReference<E> findMaxRef();

	/**
	 * Search for an element in the tree or the greatest element strictly smaller (predecessor) than it if it's not
	 * found.
	 *
	 * @param  e the search element
	 * @return   reference to the searched element or it's predecessor if the element is not found, or null if there is
	 *           no predecessor
	 */
	public HeapReference<E> findOrSmaller(E e);

	/**
	 * Search for an element in the tree or the smallest element strictly greater (successor) than it if it's not found.
	 *
	 * @param  e the search element
	 * @return   reference to the searched element or it's successor if the element is not found, or null if there is no
	 *           successor
	 */
	public HeapReference<E> findOrGreater(E e);

	/**
	 * Find the greatest element strictly smaller than an element.
	 *
	 * @param  e an element
	 * @return   reference to the predecessor element with strictly smaller value or null if no such exists
	 */
	public HeapReference<E> findSmaller(E e);

	/**
	 * Find the smallest element strictly greater than an element.
	 *
	 * @param  e an element
	 * @return   reference to the successor element with strictly greater value or null if no such exists
	 */
	public HeapReference<E> findGreater(E e);

	/**
	 * Get the predecessor of a node in the tree.
	 *
	 * <p>
	 * The predecessor node depends on the tree structure. If there are no duplicate values, the predecessor is the
	 * greatest value strictly smaller than the given element. If there are duplicate values, it may be smaller or
	 * equal.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely it reference to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref reference to an element in the tree
	 * @return     reference to the predecessor element in the tree, that is an element smaller or equal to the given
	 *             referenced element, or null if no such predecessor exists
	 */
	public HeapReference<E> getPredecessor(HeapReference<E> ref);

	/**
	 * Finds the successor of an element in the tree.
	 *
	 * <p>
	 * The successor node depends on the tree structure. If there are no duplicate values, the successor is the smallest
	 * value strictly greater than the given element. If there are duplicate values, it may be greater or equal.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely it reference to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref reference to an element in the tree
	 * @return     reference to the successor element in the tree, that is an element greater or equal to the given
	 *             referenced element, or null if no such successor exists
	 */
	public HeapReference<E> getSuccessor(HeapReference<E> ref);

	/**
	 * Split the current BST into two different BSTs with elements strictly smaller and greater or equal than an
	 * element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be greater or equal than the given element, and the returned
	 * new tree will contain elements strictly smaller than the given element.
	 *
	 * @param  e a pivot element
	 * @return   new tree with elements strictly smaller than the given element
	 */
	public BinarySearchTree<E> splitSmaller(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller or equal and strictly greater than an
	 * element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be smaller or equal than the given element, and the returned
	 * new tree will contain elements strictly greater than the given element.
	 *
	 * @param  e a pivot element
	 * @return   new tree with elements strictly greater than the given element
	 */
	public BinarySearchTree<E> splitGreater(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller and bigger than an element.
	 *
	 * <p>
	 * After this operation, all elements in this tree will be smaller or equal to the given element, and the returned
	 * new tree will contain elements greater than the given element. If the tree contains duplications of the given
	 * element, the elements in the returned tree will be to greater or equal (rather than strictly greater). To split a
	 * tree more precisely, use {@link #splitSmaller(Object)} or {@link #splitGreater(Object)}.
	 *
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely it reference to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref given element in the tree
	 * @return     new tree with elements greater (greater or equal if duplicate elements of the given element exists)
	 *             than the given element
	 */
	public BinarySearchTree<E> split(HeapReference<E> ref);

	/**
	 * Create a new binary search tree algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link BinarySearchTree} object.
	 *
	 * @return a new builder that can build {@link BinarySearchTree} objects
	 */
	static BinarySearchTree.Builder newBuilder() {
		return new BinarySearchTree.Builder() {

			boolean splitRequired;
			boolean meldRequired;

			@Override
			public <E> BinarySearchTree<E> build(Comparator<? super E> cmp) {
				if (splitRequired || meldRequired) {
					return new SplayTree<>(cmp);
				} else {
					return new RedBlackTree<>(cmp);
				}
			}

			@Override
			public Builder setSplits(boolean enable) {
				splitRequired = enable;
				return this;
			}

			@Override
			public Builder setMelds(boolean enable) {
				meldRequired = enable;
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
	static interface Builder extends HeapReferenceable.Builder {

		@Override
		<E> BinarySearchTree<E> build(Comparator<? super E> cmp);

		@Override
		default <E> BinarySearchTree<E> build() {
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
		BinarySearchTree.Builder setSplits(boolean enable);

		/**
		 * Enable/disable efficient {@link BinarySearchTree#meld(Heap)} operations.
		 *
		 * @param  enable if {@code true} the {@link BinarySearchTree#meld(Heap)} operation will be supported
		 *                    efficiently by the trees created by this builder
		 * @return        this builder
		 */
		BinarySearchTree.Builder setMelds(boolean enable);

	}

}
