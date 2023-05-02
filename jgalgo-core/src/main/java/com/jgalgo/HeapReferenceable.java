package com.jgalgo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A a collection which maintains elements in order and support efficient retrieval of the minimum value, and expose
 * references to the underling elements.
 * <p>
 * In addition to the regular {@link Heap} operations, the user can obtain a {@linkplain HeapReference reference} to
 * each inserted element via the return value of the {@link #insert(Object)} function. The reference will be valid as
 * long as the element is still in the heap. By passing the reference to the heap implementation to functions such as
 * {@link #decreaseKey(HeapReference, Object)} or {@link #removeRef(HeapReference)} the heap implementation can perform
 * the operations efficiently as is does not need to search for the element.
 *
 * <pre> {@code
 * HeapReferenceable<Integer> heap = HeapReferenceable.newBuilder().build();
 * HeapReference<Integer> r1 = heap.insert(5);
 * HeapReference<Integer> r2 = heap.insert(10);
 * HeapReference<Integer> r3 = heap.insert(3);
 *
 * assert heap.findMin() == 3;
 * assert r2.get() == 10;
 * heap.decreaseKey(r2, 2);
 * assert r2.get() == 2;
 * assert heap.findMinRef() == r2;
 *
 * heap.removeRef(r1);
 * assert heap.size() == 2;
 * }</pre>
 *
 * @see HeapReference
 */
public interface HeapReferenceable<E> extends Heap<E> {

	/**
	 * Find an element in the heap and get a reference to it.
	 *
	 * @param  e an element
	 * @return   a reference to the element or null if the element was not found
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
	 * @return                       a reference to the minimal element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public HeapReference<E> findMinRef();

	/**
	 * Decrease the key of an element in the heap.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely it reference to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param ref reference to an inserted element
	 * @param e   new value
	 */
	public void decreaseKey(HeapReference<E> ref, E e);

	/**
	 * Remove an element from the heap by its reference.
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely it reference to an element already
	 * removed, or to an element in another heap.
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
	 * Create a new referenceable heaps builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link HeapReferenceable} object.
	 *
	 * @return a new builder that can build {@link HeapReferenceable} objects
	 */
	static HeapReferenceable.Builder newBuilder() {
		return HeapPairing::new;
	}

	/**
	 * Builder for referenceable heaps.
	 * <p>
	 * Used to change heaps implementations which are used as black box by some algorithms.
	 *
	 * @see    HeapReferenceable#newBuilder()
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
