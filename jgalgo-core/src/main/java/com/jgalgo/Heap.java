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

package com.jgalgo;

import java.util.Collection;
import java.util.Comparator;

/**
 * A a collection which maintains elements in order and support efficient retrieval of the minimum value.
 * <p>
 * Most implementation support insertion of new elements and finding or extracting the minimum in logarithmic or
 * constant time.
 * <p>
 * Some implementation support a direct access to the element in the heap via {@link HeapReference}, which allow more
 * operations like {@code decreaseKey()}.
 *
 * <pre> {@code
 * Heap<Integer> h = Heap.newBuilder().build();
 * h.insert(5);
 * h.insert(10);
 * h.insert(1);
 * h.insert(3);
 * h.insert(9);
 *
 * assert h.size() == 5;
 * assert h.extractMin() == 1;
 * assert h.extractMin() == 3;
 *
 * assert h.size() == 3;
 * assert h.findMin() == 5;
 * assert h.size() == 3;
 * assert h.extractMin() == 5;
 * assert h.size() == 2;
 * }</pre>
 *
 * @param  <E> the elements type
 * @see        <a href= "https://en.wikipedia.org/wiki/Heap_(data_structure)">Wikipedia</a>
 * @see        HeapReferenceable
 * @author     Barak Ugav
 */
public interface Heap<E> extends Collection<E> {

	/**
	 * Insert a new element to the heap.
	 *
	 * @param  e new element
	 * @return   reference to the new element if the implementation support references to its elements or null if it
	 *           doesn't
	 */
	public HeapReference<E> insert(E e);

	/**
	 * Find the minimum element in the heap.
	 *
	 * @return                       the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E findMin();

	/**
	 * Extract the minimum element in the heap.
	 *
	 * @return                       the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	public E extractMin();

	/**
	 * Meld with another heap.
	 * <p>
	 * Melding is performed by adding all elements of the given heap to this heap, and clearing the given heap. Some
	 * implementations support efficient melding due to internal structures used to maintain the heap elements.
	 *
	 * @param h a heap to meld with. After the operation it will be empty.
	 */
	public void meld(Heap<? extends E> h);

	/**
	 * Returns the comparator used to order the elements in this heap, or {@code null} if this heap uses the
	 * {@linkplain Comparable natural ordering} of its elements.
	 *
	 * @return the comparator used to order the elements in this heap, or {@code null} if this heap uses the natural
	 *         ordering of its elements
	 */
	public Comparator<? super E> comparator();

	/**
	 * Create a new heaps builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link Heap} object.
	 *
	 * @return a new builder that can build {@link Heap} objects
	 */
	static Heap.Builder newBuilder() {
		return HeapBinary::new;
	}

	/**
	 * Builder for heaps.
	 * <p>
	 * Used to change heaps implementations which are used as black box by some algorithms.
	 *
	 * @see    Heap#newBuilder()
	 * @author Barak Ugav
	 */
	public static interface Builder {
		/**
		 * Build a new heap with the given comparator.
		 *
		 * @param  <E> the heap elements type
		 * @param  cmp the comparator that will be used to order the elements in the heap
		 * @return     the newly constructed heap
		 */
		<E> Heap<E> build(Comparator<? super E> cmp);

		/**
		 * Build a new heap with {@linkplain Comparable natural ordering}.
		 *
		 * @param  <E> the heap elements type
		 * @return     the newly constructed heap
		 */
		default <E> Heap<E> build() {
			return build(null);
		}
	}

}
