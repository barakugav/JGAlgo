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
 * A base interface for all index heap interfaces.
 *
 * <p>
 * Instead of using pointers to the nodes in the heap for queries and updates, we use indices in range \([0, size)\),
 * where the size is known in advance. This allows us to use arrays instead of pointers, which is more efficient in
 * terms of memory and cache locality. Sub interfaces of this interface are used to define the type of the key and the
 * comparator used to compare different elements (indices).
 *
 * @author Barak Ugav
 */
public interface IndexHeapBase {

	/**
	 * Checks if the heap is empty.
	 *
	 * @return {@code true} if the heap is empty, {@code false} otherwise.
	 */
	boolean isEmpty();

	/**
	 * Checks if the heap is not empty.
	 *
	 * @return {@code true} if the heap is not empty, {@code false} otherwise.
	 */
	boolean isNotEmpty();

	/**
	 * Checks if the element is inserted into the heap.
	 *
	 * @param  element the element to check.
	 * @return         {@code true} if the element is inserted into the heap, {@code false} otherwise.
	 */
	boolean isInserted(int element);

	/**
	 * Returns the element with the minimum key.
	 *
	 * @return the element with the minimum key
	 */
	int findMin();

	/**
	 * Removes and returns the element with the minimum key.
	 *
	 * @return the element with the minimum key
	 */
	int extractMin();

	/**
	 * Removes the element from the heap.
	 *
	 * @param element the element to remove
	 */
	void remove(int element);

	/**
	 * Clear the heap by removing all elements.
	 */
	void clear();

}
