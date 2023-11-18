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

/**
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

	static ReferenceableHeap.Builder newBuilder() {
		return new ReferenceableHeap.Builder() {
			@Override
			public ReferenceableHeap build(Class<?> keyType, Class<?> valueType, Comparator<?> comparator) {
				Objects.requireNonNull(keyType);
				Objects.requireNonNull(valueType);
				if (keyType == int.class && valueType == int.class)
					return IntIntReferenceableHeap.newInstance((IntComparator) comparator);
				if (keyType == int.class && valueType == void.class)
					return IntReferenceableHeap.newInstance((IntComparator) comparator);
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
