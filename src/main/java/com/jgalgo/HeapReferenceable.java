package com.jgalgo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A a collection which maintains elements in order and support efficient
 * retrieval of the minimum value, and expose references to the underling
 * elements.
 * <p>
 * In addition to the regular {@link Heap} operations, the user can obtain a
 * reference to each inserted element via the return value of the
 * {@link #insert(Object)} function. The reference,
 */
public interface HeapReferenceable<E> extends Heap<E> {

	/**
	 * Find the element in the heap and get a reference to it.
	 *
	 * @param e an element in the heap
	 * @return a reference to the element or null if the element was not found
	 */
	default HeapReference<E> findRef(E e) {
		Comparator<? super E> c = comparator();
		if (c == null) {
			for (HeapReference<E> p : refsSet()) {
				if (Utils.cmpDefault(e, p.get()) == 0)
					return p;
			}
		} else {
			for (HeapReference<E> p : refsSet()) {
				if (c.compare(e, p.get()) == 0)
					return p;
			}
		}
		return null;
	}

	/**
	 * Find minimal element in the heap and return a reference to it.
	 *
	 * @return a reference to the minimal element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public HeapReference<E> findMinRef();

	/**
	 * Decrease the key of an element in the heap.
	 *
	 * @param ref reference to an inserted element
	 * @param e   new value
	 */
	public void decreaseKey(HeapReference<E> ref, E e);

	/**
	 * Remove an element from the heap by its reference.
	 * <p>
	 * Note that this method behavior is undefined if the reference is not valid,
	 * namely it reference to an element already removed, or to an element in
	 * another heap.
	 *
	 * @param ref reference to an inserted element
	 */
	public void removeRef(HeapReference<E> ref);

	/**
	 * Get a collection view of references to the all elements of the heap.
	 *
	 * @return view of references of the elements of the heap
	 */
	public Set<HeapReference<E>> refsSet();

	@Override
	default Iterator<E> iterator() {
		return new Iterator<>() {
			final Iterator<HeapReference<E>> it = refsSet().iterator();

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

	/**
	 * Builder for referenceable heaps.
	 * <p>
	 * Used to change heaps implementations which are used as black box by some
	 * algorithms.
	 *
	 * @author Barak Ugav
	 */
	@FunctionalInterface
	public static interface Builder extends Heap.Builder {
		@Override
		<E> HeapReferenceable<E> build(Comparator<? super E> cmp);

		@Override
		default <E> HeapReferenceable<E> build() {
			return build(null);
		}
	}

}
