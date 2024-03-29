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

/**
 * A heap of indices in pre specified range with object keys.
 *
 * <p>
 * Instead of using pointers to the nodes in the heap for queries and updates, we use indices in range \([0, size)\),
 * where the size is known in advance. This allows us to use arrays instead of pointers, which is more efficient in
 * terms of memory and cache locality. This heap use object keys with the default comparator to compare different
 * elements (indices). Other than that, this heap is very similar to {@link ObjReferenceableHeap}.
 *
 * @param  <K> the keys type
 * @author     Barak Ugav
 */
public interface IndexHeapObj<K extends Comparable<? super K>> extends IndexHeapBase {

	/**
	 * Inserts an element into the heap.
	 *
	 * @param element the element to insert
	 * @param key     the key of the element
	 */
	void insert(int element, K key);

	/**
	 * Decreases the key of the element.
	 *
	 * @param element the element to decrease its key
	 * @param newKey  the new key of the element
	 */
	void decreaseKey(int element, K newKey);

	/**
	 * Increases the key of the element.
	 *
	 * @param element the element to increase its key
	 * @param newKey  the new key of the element
	 */
	void increaseKey(int element, K newKey);

	/**
	 * Returns the key of the element.
	 *
	 * @param  element the element to get its key
	 * @return         the key of the element
	 */
	K key(int element);

	/**
	 * Creates a new instance of {@link IndexHeapObj}.
	 *
	 * @param  <K>  the keys type
	 * @param  size the size of the heap
	 * @return      a new instance of {@link IndexHeapObj}
	 */
	static <K extends Comparable<? super K>> IndexHeapObj<K> newInstance(int size) {
		return new IndexPairingHeapObj<>(size);
	}

}
