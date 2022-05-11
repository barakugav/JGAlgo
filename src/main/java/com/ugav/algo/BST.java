package com.ugav.algo;

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
	 * Search for element in the tree or it's predecessor if the element is not
	 * found
	 * 
	 * @param e the search element
	 * @return handle of the searched element or it's predecessor if the element is
	 *         not found, or null if there is no predecessor
	 */
	public Handle<E> findOrPredecessor(E e);

	/**
	 * Search for element in the tree or it's successor if the element is not found
	 * 
	 * @param e the search element
	 * @return handle of the searched element or it's successor if the element is
	 *         not found, or null if there is no successor
	 */
	public Handle<E> findOrSuccessor(E e);

	/**
	 * Finds the predecessor of an element in the tree
	 * 
	 * @param handle handle of an element in the tree
	 * @return handle to the predecessor element in the tree or null if no such
	 *         predecessor exists
	 */
	public Handle<E> findPredecessor(Handle<E> handle);

	/**
	 * Finds the successor of an element in the tree
	 * 
	 * @param handle handle of an element in the tree
	 * @return handle to the successor element in the tree or null if no such
	 *         successor exists
	 */
	public Handle<E> findSuccessor(Handle<E> handle);

}
