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

package com.jgalgo.internal.ds;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A a collection which maintains elements in order and support efficient retrieval of the minimum value.
 *
 * <p>
 * Most implementation support insertion of new elements and finding or extracting the minimum in logarithmic or
 * constant time.
 *
 * <p>
 * If {@code decreaseKey()} or fast {@code remove()} operations are required, consider using {@link HeapReferenceable}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Heap<Integer> h = Heap.newInstance();
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
public interface Heap<E> extends Iterable<E> {

	/**
	 * Insert a new element to the heap.
	 *
	 * @param elm new element
	 */
	void insert(E elm);

	/**
	 * Insert multiple elements.
	 *
	 * <p>
	 * Implementations may be more efficient if multiple elements are added to the heap using this method, rather than
	 * calling {@link #insert(Object)} repetitively.
	 *
	 * @param elms a collection containing all the elements to insert
	 */
	void insertAll(Collection<? extends E> elms);

	/**
	 * Find the minimum element in the heap.
	 *
	 * @return                       the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	E findMin();

	/**
	 * Extract the minimum element in the heap.
	 *
	 * <p>
	 * This method find and <b>remove</b> the minimum element.
	 *
	 * @return                       the minimum element in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	E extractMin();

	/**
	 * Remove an element from the heap.
	 *
	 * @param  elm element to remove
	 * @return     {@code true} if the element was removed, {@code false} otherwise
	 */
	boolean remove(E elm);

	/**
	 * Meld with another heap.
	 *
	 * <p>
	 * Melding is performed by adding all elements of the given heap to this heap, and clearing the given heap. Some
	 * implementations support efficient melding due to internal structures used to maintain the heap elements.
	 *
	 * <p>
	 * Its only possible to meld with a heap with the same implementation of this heap.
	 *
	 * <p>
	 * If the heap implementation expose references to its element (see {@link HeapReferenceable}), the references of
	 * both ({@code this} and the given {@code heap}) remain valid and its possible to use them only in this heap (they
	 * are no longer valid with respect to the given heap, which will be cleared).
	 *
	 * @param  heap                     a heap to meld with. After the operation it will be empty.
	 * @throws IllegalArgumentException if the given heap is {@code this} heap, or its of another implementation
	 */
	void meld(Heap<? extends E> heap);

	/**
	 * Check whether the heap is empty.
	 *
	 * @return {@code true} if the heap is empty, {@code false} otherwise
	 */
	boolean isEmpty();

	/**
	 * Check whether the heap is not empty.
	 *
	 * @return {@code true} if the heap is not empty, {@code false} otherwise
	 */
	boolean isNotEmpty();

	/**
	 * Removes all of the elements from this heap.
	 */
	void clear();

	/**
	 * Get a stream over the elements in this heap.
	 *
	 * @return a stream over the elements in this heap
	 */
	default Stream<E> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Returns the comparator used to order the elements in this heap, or {@code null} if this heap uses the
	 * {@linkplain Comparable natural ordering} of its elements.
	 *
	 * @return the comparator used to order the elements in this heap, or {@code null} if this heap uses the natural
	 *         ordering of its elements
	 */
	Comparator<? super E> comparator();

	/**
	 * Create a new heap.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link Heap} object. The {@link Heap.Builder} might support
	 * different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link Heap}
	 */
	static <E> Heap<E> newInstance() {
		return newBuilder().<E>elementsTypeObj().build();
	}

	/**
	 * Create a new heap with custom comparator.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link Heap} object. The {@link Heap.Builder} might support
	 * different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link Heap}
	 */
	static <E> Heap<E> newInstance(Comparator<? super E> cmp) {
		return newBuilder().<E>elementsTypeObj().build(cmp);
	}

	/**
	 * Create a new heaps builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link Heap} objects
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Heap.Builder<Object> newBuilder() {
		return new Heap.Builder<>() {

			Class<?> elmsType;

			@Override
			public Heap build(Comparator cmp) {
				if (elmsType == int.class) {
					return new HeapBinaryInt(cmp);
				} else {
					return new HeapBinary<>(cmp);
				}
			}

			@Override
			public Heap.Builder elementsTypeObj() {
				elmsType = null;
				return this;
			}

			@Override
			public Heap.Builder elementsTypePrimitive(Class primitiveType) {
				if (!primitiveType.isPrimitive())
					throw new IllegalArgumentException("type is not primitive: " + primitiveType);
				elmsType = primitiveType;
				return this;
			}
		};
	}

	/**
	 * Builder for heaps.
	 *
	 * @param  <E> the heap elements type
	 * @see        Heap#newBuilder()
	 * @author     Barak Ugav
	 */
	static interface Builder<E> {
		/**
		 * Build a new heap with the given comparator.
		 *
		 * <p>
		 * If primitive elements are in used, namely {@link #elementsTypePrimitive(Class)}, its recommended to use a
		 * primitive {@link Comparator} such as {@link it.unimi.dsi.fastutil.ints.IntComparator}, for best performance.
		 *
		 * @param  cmp the comparator that will be used to order the elements in the heap
		 * @return     the newly constructed heap
		 */
		Heap<E> build(Comparator<? super E> cmp);

		/**
		 * Build a new heap with {@linkplain Comparable natural ordering}.
		 *
		 * @return the newly constructed heap
		 */
		default Heap<E> build() {
			return build(null);
		}

		/**
		 * Change the elements type of the built heaps to a generic object type.
		 *
		 * @param  <ElementsT> object type
		 * @return             this builder
		 */
		<ElementsT> Heap.Builder<ElementsT> elementsTypeObj();

		/**
		 * Change the elements type of the built heaps to some primitive type.
		 *
		 * <p>
		 * Some specific type implementation may exists that is more efficient than the boxed general object
		 * implementation.
		 *
		 * @param  <ElementsT>              elements primitive boxed type
		 * @param  primitiveType            the class of the primitive type
		 * @return                          this builder
		 * @throws IllegalArgumentException if {@code primitiveType} is not a class of a primitive type, a.k.a
		 *                                      {@code int.class, double.class} ect.
		 */
		<ElementsT> Heap.Builder<ElementsT> elementsTypePrimitive(Class<? extends ElementsT> primitiveType);

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default Heap.Builder<E> setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
