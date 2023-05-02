package com.jgalgo;

/**
 * A reference to an element in a {@link HeapReferenceable}.
 * <p>
 * When a new element is inserted to a referenceable heap using {@link HeapReferenceable#insert(Object)} a reference to
 * the element is returned. The reference object will valid as long as element is still in the heap. The reference may
 * be used to access the heap element directly using the {@link #get()} method, or for various operations such as
 * {@link HeapReferenceable#decreaseKey(HeapReference, Object)} or {@link HeapReferenceable#removeRef(HeapReference)}
 * which could be implemented efficiently given the reference, as the implementation doesn't need to search for the
 * element in the heap.
 *
 * @param  <E> the elements type
 * @see        HeapReferenceable
 * @author     Barak Ugav
 */
public interface HeapReference<E> {

	/**
	 * Get the element this reference is associated with.
	 *
	 * @return the element value
	 */
	public E get();
}
