package com.ugav.jgalgo;

import java.util.Collection;
import java.util.Comparator;

public interface Heap<E> extends Collection<E> {

	/**
	 * Insert a new element to the heap
	 *
	 * @param e new element
	 * @return handle to the new element support handle access or null if it doesn't
	 */
	public HeapDirectAccessed.Handle<E> insert(E e);

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

	public Comparator<? super E> comparator();

}
