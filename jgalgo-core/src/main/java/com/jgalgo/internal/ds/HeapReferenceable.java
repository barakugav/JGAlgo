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

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.jgalgo.internal.util.JGAlgoUtils;

/**
 * A a collection which maintains elements in order and support efficient retrieval of the minimum value, and expose
 * references to the underling elements.
 *
 * <p>
 * In addition to the regular {@link Heap} operations, the user can obtain a {@linkplain HeapReference reference} to
 * each inserted element via the return value of the {@link #insert(Object)} function. The reference will be valid as
 * long as the element is still in the heap. By passing the reference to the heap implementation to functions such as
 * {@link #decreaseKey(HeapReference, Object)} or {@link #remove(HeapReference)} the heap implementation can perform the
 * operations efficiently as is does not need to search for the element.
 *
 * <p>
 * Another difference from the regular {@link Heap}, is the existent of both keys and values, rather than just
 * 'elements'. A key may be changed using {@link #decreaseKey} while the <b>value</b> is the same. This matches the
 * common use case of these heaps.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * HeapReferenceable<Integer, String> heap = HeapReferenceable.newInstance();
 * HeapReference<Integer, String> r1 = heap.insert(5, "Alice");
 * HeapReference<Integer, String> r2 = heap.insert(10, "Bob");
 * HeapReference<Integer, String> r3 = heap.insert(3, "Charlie");
 *
 * assert heap.findMin() == r3;
 * assert r2.key() == 10;
 * heap.decreaseKey(r2, 2);
 * assert r2.key() == 2;
 * assert r2.value().equals("Bob");
 * assert heap.findMin() == r2;
 *
 * heap.remove(r1);
 * assert heap.size() == 2;
 * }</pre>
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @see        HeapReference
 * @author     Barak Ugav
 */
public interface HeapReferenceable<K, V> extends Iterable<HeapReference<K, V>> {

	/**
	 * Insert a new element to the heap with {@code null} value.
	 *
	 * <p>
	 * Only a key is passed to this method, and a {@code null} value will be used. To insert a new element with both a
	 * key and a value use {@link #insert(Object, Object)}.
	 *
	 * @param  key the key of the new element
	 * @return     reference to the new element
	 */
	HeapReference<K, V> insert(K key);

	/**
	 * Insert a new element to the heap with both key and value.
	 *
	 * @param  key   the key of the new element
	 * @param  value the value of the new element
	 * @return       reference to the new element
	 */
	default HeapReference<K, V> insert(K key, V value) {
		HeapReference<K, V> ref = insert(key);
		ref.setValue(value);
		return ref;
	}

	/**
	 * Find the element with the minimal key in the heap and return a reference to it.
	 *
	 * @return                       a reference to the element with the minimal key in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	HeapReference<K, V> findMin();

	/**
	 * Extract the element with the minimal key in the heap.
	 *
	 * <p>
	 * This method find and <b>remove</b> the element with the minimal key.
	 *
	 * @return                       the element with the minimal key in the heap
	 * @throws IllegalStateException if the heap is empty
	 */
	HeapReference<K, V> extractMin();

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
	 * After the melding, the references of both ({@code this} and the given {@code heap}) remain valid and its possible
	 * to use them only in this heap (they are no longer valid with respect to the given heap, which will be cleared).
	 *
	 * @param  heap                     a heap to meld with. After the operation it will be empty.
	 * @throws IllegalArgumentException if the given heap is {@code this} heap, or its of another implementation
	 */
	void meld(HeapReferenceable<? extends K, ? extends V> heap);

	/**
	 * Returns the comparator used to order the element's keys in this heap, or {@code null} if this heap uses the
	 * {@linkplain Comparable natural ordering} of its keys.
	 *
	 * @return the comparator used to order the element's keys in this heap, or {@code null} if this heap uses the
	 *         natural ordering of its keys
	 */
	Comparator<? super K> comparator();

	/**
	 * Find an element by its key in the heap and get a reference to it.
	 *
	 * <p>
	 * Note that this method uses the comparator of the heap to determine if two keys are equal, rather than
	 * {@link Object#equals}.
	 *
	 * @param  key a key
	 * @return     a reference to an element with the given key or {@code null} if such element was not found
	 */
	default HeapReference<K, V> find(K key) {
		Comparator<? super K> c = comparator();
		if (c == null) {
			for (HeapReference<K, V> p : this) {
				if (JGAlgoUtils.cmpDefault(key, p.key()) == 0)
					return p;
			}
		} else {
			for (HeapReference<K, V> p : this) {
				if (c.compare(key, p.key()) == 0)
					return p;
			}
		}
		return null;
	}

	/**
	 * Decrease the key of an element in the heap.
	 *
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely if it refer to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param  ref                      reference to an inserted element
	 * @param  newKey                   a new key
	 * @throws IllegalArgumentException if the new key is greater than the previous key
	 */
	void decreaseKey(HeapReference<K, V> ref, K newKey);

	/**
	 * Remove an element from the heap by its reference.
	 *
	 * <p>
	 * This method behavior is undefined if the reference is not valid, namely if it refer to an element already
	 * removed, or to an element in another heap.
	 *
	 * @param ref a reference to an inserted element
	 */
	void remove(HeapReference<K, V> ref);

	/**
	 * Removes all of the elements from this heap.
	 */
	void clear();

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
	 * Get a stream over the elements in this heap.
	 *
	 * @return a stream over the elements in this heap
	 */
	default Stream<HeapReference<K, V>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Create a {@link Heap} view on this referenceable heap.
	 *
	 * <p>
	 * A referenceable heap has both keys and values, regular heaps contain only keys (called plain 'elements'). A
	 * {@link Heap} can be built on this referenceable heap by using only {@code null} values and treating the keys as a
	 * regular heap elements.
	 *
	 * <p>
	 * Note that the return heap object will alter {@code this} referenceable heap.
	 *
	 * @return a {@link Heap} view of this referenceable heap
	 */
	default Heap<K> asHeap() {
		return HeapAbstract.fromHeapReferenceable(this);
	}

	/**
	 * Create a new referenceable heap.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link HeapReferenceable} object. The
	 * {@link HeapReferenceable.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link HeapReferenceable}
	 */
	static <K, V> HeapReferenceable<K, V> newInstance() {
		return newBuilder().<K>keysTypeObj().<V>valuesTypeObj().build();
	}

	/**
	 * Create a new referenceable heap with custom comparator.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link HeapReferenceable} object. The
	 * {@link HeapReferenceable.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link HeapReferenceable}
	 */
	static <K, V> HeapReferenceable<K, V> newInstance(Comparator<? super K> cmp) {
		return newBuilder().<K>keysTypeObj().<V>valuesTypeObj().build(cmp);
	}

	/**
	 * Create a new referenceable heaps builder.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link HeapReferenceable} object.
	 *
	 * @return a new builder that can build {@link HeapReferenceable} objects
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static HeapReferenceable.Builder<Object, Object> newBuilder() {
		return new HeapReferenceable.Builder<>() {

			Class<?> keysType;
			Class<?> valuesType;
			String impl;

			@Override
			public HeapReferenceable build(Comparator cmp) {
				if (impl != null) {
					switch (impl) {
						case "binomial":
							return new HeapBinomial(cmp);
						case "fibonacci":
							return new HeapFibonacci(cmp);
						case "pairing":
							return HeapPairing.newHeap(keysType, valuesType, cmp);
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return HeapPairing.newHeap(keysType, valuesType, cmp);
			}

			@Override
			public HeapReferenceable.Builder keysTypeObj() {
				keysType = null;
				return this;
			}

			@Override
			public HeapReferenceable.Builder keysTypePrimitive(Class primitiveType) {
				if (!primitiveType.isPrimitive())
					throw new IllegalArgumentException("type is not primitive: " + primitiveType);
				keysType = primitiveType;
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypeObj() {
				valuesType = null;
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypePrimitive(Class primitiveType) {
				if (!primitiveType.isPrimitive())
					throw new IllegalArgumentException("type is not primitive: " + primitiveType);
				valuesType = primitiveType;
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypeVoid() {
				valuesType = void.class;
				return this;
			}

			@Override
			public HeapReferenceable.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						HeapReferenceable.Builder.super.setOption(key, value);
				}
				return this;
			}
		};
	}

	/**
	 * Builder for referenceable heaps.
	 *
	 * @param  <K> the heap keys type
	 * @param  <V> the heap values type
	 * @see        HeapReferenceable#newBuilder()
	 * @author     Barak Ugav
	 */
	static interface Builder<K, V> {

		/**
		 * Build a new heap with the given comparator.
		 *
		 * <p>
		 * If primitive keys are in used, namely {@link #keysTypePrimitive(Class)}, its recommended to use a primitive
		 * {@link Comparator} such as {@link it.unimi.dsi.fastutil.ints.IntComparator}, for best performance.
		 *
		 * @param  cmp the comparator that will be used to order the keys in the heap
		 * @return     the newly constructed heap
		 */
		HeapReferenceable<K, V> build(Comparator<? super K> cmp);

		/**
		 * Build a new heap with {@linkplain Comparable natural ordering}.
		 *
		 * @return the newly constructed heap
		 */
		default HeapReferenceable<K, V> build() {
			return build(null);
		}

		/**
		 * Set the keys type to an object type.
		 *
		 * @param  <KeysT> the keys type
		 * @return         this builder
		 */
		<KeysT> HeapReferenceable.Builder<KeysT, V> keysTypeObj();

		/**
		 * Set the keys type to a primitive type.
		 *
		 * <p>
		 * Specific implementations may exists for some primitive keys types, which are more efficient.
		 *
		 * @param  <KeysT>                  the keys type
		 * @param  primitiveType            the primitive class, for example {@code int.class}
		 * @return                          this builder
		 * @throws IllegalArgumentException if the provided class is not primitive
		 */
		<KeysT> HeapReferenceable.Builder<KeysT, V> keysTypePrimitive(Class<? extends KeysT> primitiveType);

		/**
		 * Set the values type to an object type.
		 *
		 * @param  <ValuesT> the values type
		 * @return           this builder
		 */
		<ValuesT> HeapReferenceable.Builder<K, ValuesT> valuesTypeObj();

		/**
		 * Set the values type to a primitive type.
		 *
		 * <p>
		 * Specific implementations may exists for some primitive keys types, which are more efficient.
		 *
		 * @param  <ValuesT>                the values type
		 * @param  primitiveType            the primitive class, for example {@code int.class}
		 * @return                          this builder
		 * @throws IllegalArgumentException if the provided class is not primitive
		 */
		<ValuesT> HeapReferenceable.Builder<K, ValuesT> valuesTypePrimitive(Class<? extends ValuesT> primitiveType);

		/**
		 * Set the values type to {@code void}.
		 *
		 * <p>
		 * Specific implementations without values fields may exists, which are more efficient.
		 *
		 * @return this builder
		 */
		HeapReferenceable.Builder<K, Void> valuesTypeVoid();

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
		default HeapReferenceable.Builder<K, V> setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}