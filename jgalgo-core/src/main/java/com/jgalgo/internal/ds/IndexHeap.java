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

import it.unimi.dsi.fastutil.ints.IntComparator;

/**
 * A heap of indices in pre specified range.
 *
 * <p>
 * Instead of using pointers to the nodes in the heap for queries and updates, we use indices in range \([0, size)\),
 * where the size is known in advance. This allows us to use arrays instead of pointers, which is more efficient in
 * terms of memory and cache locality. This heap always use a {@plainlink IntComparator custom comparator} to compare
 * different elements (indices). Other than that, this heap is very similar to {@link IntReferenceableHeap}.
 *
 * @author Barak Ugav
 */
public interface IndexHeap extends IndexHeapBase {

	/**
	 * Inserts the element into the heap.
	 *
	 * @param element the element to insert.
	 */
	void insert(int element);

	/**
	 * Decreases the key of the element.
	 *
	 * <p>
	 * The heap uses a custom comparator, which is used to compare the keys of the elements. The data affecting the
	 * comparator, practically 'decreasing' the key, should be updated before calling this method.
	 *
	 * @param element the element to decrease its key
	 */
	void decreaseKey(int element);

	/**
	 * Increases the key of the element.
	 *
	 * <p>
	 * The heap uses a custom comparator, which is used to compare the keys of the elements. The data affecting the
	 * comparator, practically 'increasing' the key, should be updated before calling this method.
	 *
	 * @param element the element to increase its key
	 */
	void increaseKey(int element);

	/**
	 * Returns the comparator used to compare different elements.
	 *
	 * @return the comparator used to compare different elements
	 */
	IntComparator comparator();

	/**
	 * Creates a new instance of {@link IndexHeap}.
	 *
	 * @param  size the size of the heap
	 * @param  c    the comparator used to compare different elements, must be non-null
	 * @return      a new instance of {@link IndexHeap}
	 */
	static IndexHeap newInstance(int size, IntComparator c) {
		return new IndexPairingHeap(size, c);
	}

}
