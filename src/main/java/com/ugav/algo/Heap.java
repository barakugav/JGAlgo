package com.ugav.algo;

import java.util.Collection;

public interface Heap<E> extends Collection<E> {

	/**
	 * Insert a new element to the heap
	 *
	 * @param e new element
	 * @return handle to the new element support handle access or null if it doesn't
	 */
	public Handle<E> insert(E e);

	/**
	 * Find the minimum element in the heap
	 *
	 * @return the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E findMin();

	/**
	 * Extract the minimum element in the heap
	 *
	 * @return the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E extractMin();

	/**
	 * Meld with another heap
	 *
	 * @param h a heap to meld with. May be invalid at the end of the operation
	 */
	public void meld(Heap<? extends E> h);

	/**
	 * Check if the heap support handle objects
	 *
	 * A handle is a way to access an element in the heap directly. Used for
	 * decrease key operations and fast remove.
	 *
	 * @return true if the heap support handle objects
	 */
	public boolean isHandlesSupported();

	/**
	 * Find the handle of an element in the heap
	 *
	 * @param e an element in the heap
	 * @return the handle of the element or null if the element is not in the heap
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public Handle<E> findHanlde(E e);

	/**
	 * Find the handle of the minimal element in the heap
	 *
	 * @return handle of the minimal element
	 * @throws IllegalStateException         if the heap is empty
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public Handle<E> findMinHandle();

	/**
	 * Decrease the key of an element in the heap
	 *
	 * @param handle handle of an inserted element
	 * @param e      new key
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public void decreaseKey(Handle<E> handle, E e);

	/**
	 * Remove an element from the heap by its handle
	 *
	 * @param handle handle of an inserted element
	 * @throws UnsupportedOperationException if isHandlesSupported is false
	 */
	public void removeHandle(Handle<E> handle);

	/**
	 * Object associated with an element in a heap. Allow specific operations to
	 * perform directly on the element without searching. Not supported by all
	 * implementations.
	 */
	public static interface Handle<E> {

		/**
		 * Get the element this handle is associated with
		 *
		 * @return the element value
		 */
		public E get();

	}

}
