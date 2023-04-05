package com.jgalgo;

public interface BST<E> extends HeapDirectAccessed<E> {

	/**
	 * Find the maximum element in the heap
	 *
	 * @return the maximum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E findMax();

	/**
	 * Extract the maximum element in the heap
	 *
	 * @return the maximum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E extractMax();

	/**
	 * Find the handle of the maximum element in the heap
	 *
	 * @return handle of the maximum element
	 * @throws IllegalStateException if the heap is empty
	 */
	public Handle<E> findMaxHandle();

	/**
	 * Search for element in the tree or the greatest element strictly smaller
	 * (predecessor) than it if it's not found
	 *
	 * @param e the search element
	 * @return handle of the searched element or it's predecessor if the element is
	 *         not found, or null if there is no predecessor
	 */
	public Handle<E> findOrSmaller(E e);

	/**
	 * Search for element in the tree or the smallest element strictly greater
	 * (successor) than it if it's not found
	 *
	 * @param e the search element
	 * @return handle of the searched element or it's successor if the element is
	 *         not found, or null if there is no successor
	 */
	public Handle<E> findOrGreater(E e);

	/**
	 * Find the greatest element strictly smaller than an element
	 *
	 * @param e element
	 * @return handle to the predecessor element with strictly smaller value or null
	 *         if no such exists
	 */
	public Handle<E> findSmaller(E e);

	/**
	 * Find the smallest element strictly greater than an element
	 *
	 * @param e element
	 * @return handle to the successor element with strictly greater value or null
	 *         if no such exists
	 */
	public Handle<E> findGreater(E e);

	/**
	 * Get the predecessor of a node in the tree
	 *
	 * The predecessor node depends on the tree structure. If there are no duplicate
	 * values, the predecessor is the greatest value strictly smaller than the given
	 * element. If there are duplicate values, it may be smaller or equal.
	 *
	 * @param handle handle of an element in the tree
	 * @return handle to the predecessor element in the tree, that is a handle with
	 *         value smaller or equal to the given handle value, or null if no such
	 *         predecessor exists
	 */
	public Handle<E> getPredecessor(Handle<E> handle);

	/**
	 * Finds the successor of an element in the tree
	 *
	 * The successor node depends on the tree structure. If there are no duplicate
	 * values, the successor is the smallest value strictly greater than the given
	 * element. If there are duplicate values, it may be greater or equal.
	 *
	 * @param handle handle of an element in the tree
	 * @return handle to the successor element in the tree, that is a handle with
	 *         value greater or equal to the given handle value, or null if no such
	 *         successor exists
	 */
	public Handle<E> getSuccessor(Handle<E> handle);

	/**
	 * Split the current BST into two different BSTs with elements strictly smaller
	 * and greater or equal than an element
	 *
	 * After this operation, all elements in this tree will be greater or equal than
	 * the given element, and the returned new tree will contain elements strictly
	 * smaller than the given element.
	 *
	 * @param e a pivot element
	 * @return new tree with elements strictly smaller than the given element
	 */
	public BST<E> splitSmaller(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller or equal
	 * and strictly greater than an element
	 *
	 * After this operation, all elements in this tree will be smaller or equal than
	 * the given element, and the returned new tree will contain elements strictly
	 * greater than the given element.
	 *
	 * @param e a pivot element
	 * @return new tree with elements strictly greater than the given element
	 */
	public BST<E> splitGreater(E e);

	/**
	 * Split the current BST into two different BSTs with elements smaller and
	 * bigger than an element
	 *
	 * After this operation, all elements in this tree will be smaller or equal to
	 * the given element, and the returned new tree will contain elements greater
	 * than the given element. If the tree contains multiple element with the given
	 * element, the new tree may contain with the same value.
	 *
	 * To split a tree accurately, use splitSmaller/splitGreater.
	 *
	 * @param handle given element in the tree
	 * @return new tree with elements greater (greater or equal if duplicate
	 *         elements of the given elements exists) than the given element
	 */
	public BST<E> split(Handle<E> handle);

}
