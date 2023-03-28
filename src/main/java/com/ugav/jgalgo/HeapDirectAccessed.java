package com.ugav.jgalgo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public interface HeapDirectAccessed<E> extends Heap<E> {

	/**
	 * Find the handle of an element in the heap
	 *
	 * @param e an element in the heap
	 * @return the handle of the element or null if the element is not in the heap
	 */
	default Handle<E> findHanlde(E e) {
		Comparator<? super E> c = comparator();
		for (Handle<E> p : Utils.iterable(handleIterator())) {
			if (c.compare(e, p.get()) == 0)
				return p;
		}
		return null;
	}

	/**
	 * Find the handle of the minimal element in the heap
	 *
	 * @return handle of the minimal element
	 * @throws IllegalStateException if the heap is empty
	 */
	public Handle<E> findMinHandle();

	/**
	 * Decrease the key of an element in the heap
	 *
	 * @param handle handle of an inserted element
	 * @param e      new key
	 */
	public void decreaseKey(Handle<E> handle, E e);

	/**
	 * Remove an element from the heap by its handle
	 *
	 * @param handle handle of an inserted element
	 */
	public void removeHandle(Handle<E> handle);

	/**
	 * Get an iterator that iterate over the handles of the heap
	 *
	 * @return handle iterator
	 */
	public Iterator<? extends Handle<E>> handleIterator();

	@Override
	default Iterator<E> iterator() {
		return iteratorFromHandleIter(handleIterator());
	}

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

	@FunctionalInterface
	public static interface Builder {
		<E> HeapDirectAccessed<E> build(Comparator<? super E> cmp);
	}

	public static <E> Iterator<E> iteratorFromHandleIter(Iterator<? extends Handle<E>> iter) {
		return new Iterator<>() {

			final Iterator<? extends Handle<E>> it = Objects.requireNonNull(iter);

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public E next() {
				return it.next().get();
			}
		};
	}

}
