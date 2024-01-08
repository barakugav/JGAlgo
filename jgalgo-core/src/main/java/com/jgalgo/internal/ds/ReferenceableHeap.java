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
import java.util.Objects;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.longs.LongComparator;

/**
 * A collection which maintains elements in order and support efficient retrieval of the minimum value, and expose
 * references to the underling elements.
 *
 * <p>
 * In addition to the regular Heap operations, the user can obtain a reference to each inserted element via the return
 * value of the {@code insert()} function. The reference will be valid as long as the element is still in the heap. By
 * passing the reference to the heap implementation to functions such as {@code decreaseKey()} or {@code remove()} the
 * heap implementation can perform the operations efficiently as is does not need to search for the element.
 *
 * <p>
 * Another difference from the regular Heap, is the existent of both keys and values, rather than just 'elements'. A key
 * may be changed using {@code decreaseKey()} while the <b>value</b> is the same. This matches the common use case of
 * these heaps.
 *
 * <pre> {@code
 * IntObjReferenceableHeap<Integer, String> heap = IntObjReferenceableHeap.newInstance();
 * IntObjReferenceableHeap.Ref<String> r1 = heap.insert(5, "Alice");
 * IntObjReferenceableHeap.Ref<String> r2 = heap.insert(10, "Bob");
 * IntObjReferenceableHeap.Ref<String> r3 = heap.insert(3, "Charlie");
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
 * @author Barak Ugav
 */
public interface ReferenceableHeap {

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

	static ReferenceableHeap.Builder builder() {
		return new ReferenceableHeap.Builder() {
			@Override
			public ReferenceableHeap build(Class<?> keyType, Class<?> valueType, Comparator<?> comparator) {
				Objects.requireNonNull(keyType);
				Objects.requireNonNull(valueType);
				if (keyType == int.class && valueType == int.class)
					return IntIntReferenceableHeap.newInstance((IntComparator) comparator);
				if (keyType == int.class && valueType == void.class)
					return IntReferenceableHeap.newInstance((IntComparator) comparator);
				if (keyType == long.class && valueType == int.class)
					return LongIntReferenceableHeap.newInstance((LongComparator) comparator);
				if (keyType == double.class && valueType == int.class)
					return DoubleIntReferenceableHeap.newInstance((DoubleComparator) comparator);
				if (keyType == double.class && valueType == Object.class)
					return DoubleObjReferenceableHeap.newInstance((DoubleComparator) comparator);
				if (keyType == Object.class && valueType == void.class)
					return ObjReferenceableHeap.newInstance(comparator);
				throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
			}
		};
	}

	static interface Builder {

		default ReferenceableHeap build(Class<?> keyType, Class<?> valueType) {
			return build(keyType, valueType, null);
		}

		ReferenceableHeap build(Class<?> keyType, Class<?> valueType, Comparator<?> comparator);

	}

}
