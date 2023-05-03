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

import java.util.Comparator;
import java.util.Objects;

/**
 * Comparator used to compare elements in a sequence by {@link RMQStatic} algorithms.
 * <p>
 * When an {@link RMQStatic} perform a preprocessing, the sequence itself of elements is not passed to it, rather a
 * comparator that can compare two elements by their indices. The comparator must remain valid as long as queries are
 * still issued to the RMQ implementation.
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface RMQStaticComparator {

	/**
	 * Compare the i'th and j'th elements in the sequence.
	 *
	 * @param  i index of first element
	 * @param  j index of second element
	 * @return   value less than zero if the i'th element is smaller than the j'th element, value greater than zero if
	 *           the j'th is smaller than the i'th and zero if they are equal
	 */
	public int compare(int i, int j);

	/**
	 * Create an RMQ comparator from an object array.
	 *
	 * @param  <E> the array type
	 * @param  arr an array
	 * @return     an RMQ comparator that will compare elements in the array using their {@linkplain Comparable natural
	 *             ordering}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> RMQStaticComparator ofObjArray(E[] arr) {
		Objects.requireNonNull(arr);
		return (i, j) -> ((Comparable) arr[i]).compareTo(arr[j]);
	}

	/**
	 * Create an RMQ comparator from an object array with a custom comparator.
	 *
	 * @param  <E> the array type
	 * @param  arr an array
	 * @param  c   the comparator that will be used to compare elements in the array. If {@code null}, the
	 *                 {@linkplain Comparable natural ordering} of the elements will be used.
	 * @return     an RMQ comparator that will compare elements in the array using the given comparator
	 */
	public static <E> RMQStaticComparator ofObjArray(E[] arr, Comparator<? super E> c) {
		if (c == null) {
			return ofObjArray(arr);
		} else {
			Objects.requireNonNull(arr);
			return (i, j) -> c.compare(arr[i], arr[j]);
		}
	}

	/**
	 * Create an RMQ comparator from an {@code int} array.
	 *
	 * @param  arr an array
	 * @return     an RMQ comparator that will compare elements in the array using {@code Integer.compare()}.
	 */
	public static RMQStaticComparator ofIntArray(int[] arr) {
		Objects.requireNonNull(arr);
		return (i, j) -> Integer.compare(arr[i], arr[j]);
	}

}
