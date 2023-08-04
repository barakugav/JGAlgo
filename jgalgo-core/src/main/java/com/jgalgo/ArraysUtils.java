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

import java.util.Arrays;
import java.util.Comparator;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

class ArraysUtils {

	private ArraysUtils() {}

	static <E> E getKthElement(E[] a, int k, Comparator<? super E> c) {
		return getKthElement(a, 0, a.length, k, c, false);
	}

	static int getKthElement(int[] a, int k, IntComparator c) {
		return getKthElement(a, 0, a.length, k, c, false);
	}

	/**
	 * Get the K'th element in the array if it was sorted
	 *
	 * For example, getKthElement([10, 13, 14, 11, 12], 3) = 13
	 *
	 * \(O(n)\)
	 *
	 * @param  <E>     the array element type
	 * @param  a       an array
	 * @param  from    first index (inclusive)
	 * @param  to      last index (exclusive)
	 * @param  k       index of the desire element in the sorted array
	 * @param  c       comparator
	 * @param  inPlace if true, all operations will be done on the given array and at the end it will be partitioned by
	 *                     the Kth element
	 * @return         the Kth element
	 */
	static <E> E getKthElement(E[] a, int from, int to, int k, Comparator<? super E> c, boolean inPlace) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		Assertions.Arrays.checkIndex(k, from, to);
		if (!inPlace) {
			a = Arrays.copyOfRange(a, from, to);
			from = 0;
			to = a.length;
		}
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();

		getKthElement0(a, from, to, k, c);
		return a[k];
	}

	/**
	 * Get the K'th element in the array if it was sorted
	 *
	 * For example, getKthElement([10, 13, 14, 11, 12], 3) = 13
	 *
	 * \(O(n)\)
	 *
	 * @param  a       an array
	 * @param  from    first index (inclusive)
	 * @param  to      last index (exclusive)
	 * @param  k       index of the desire element in the sorted array
	 * @param  c       comparator
	 * @param  inPlace if true, all operations will be done on the given array and at the end it will be partitioned by
	 *                     the Kth element
	 * @return         the Kth element
	 */
	static int getKthElement(int[] a, int from, int to, int k, IntComparator c, boolean inPlace) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		Assertions.Arrays.checkIndex(k, from, to);
		if (!inPlace) {
			a = Arrays.copyOfRange(a, from, to);
			from = 0;
			to = a.length;
		}
		c = c != null ? c : Integer::compare;

		getKthElement0(a, from, to, k, c);
		return a[k];
	}

	private static <E> void getKthElement0(E[] a, int from, int to, int k, Comparator<? super E> c) {
		for (;;) {
			if (from == to + 1)
				return;
			int pivotIdx = calcPivot(a, from, to, c);
			IntIntPair p = pivotPartition0(a, from, to, pivotIdx, c);
			int lastSmaller = p.firstInt();
			int firstGreater = p.secondInt();
			if (k <= lastSmaller) {
				to = lastSmaller + 1;
			} else if (k >= firstGreater) {
				from = firstGreater;
			} else {
				/* k in range (lastSmaller, firstGreater) , namely the kth element is the pivot */
				return;
			}
		}
	}

	private static void getKthElement0(int[] a, int from, int to, int k, IntComparator c) {
		for (;;) {
			if (from == to + 1)
				return;
			int pivotIdx = calcPivot(a, from, to, c);
			IntIntPair p = pivotPartition0(a, from, to, pivotIdx, c);
			int lastSmaller = p.firstInt();
			int firstGreater = p.secondInt();
			if (k <= lastSmaller) {
				to = lastSmaller + 1;
			} else if (k >= firstGreater) {
				from = firstGreater;
			} else {
				/* k in range (lastSmaller, firstGreater) , namely the kth element is the pivot */
				return;
			}
		}
	}

	/**
	 * Partition an array by a given pivot
	 *
	 * At the end of this function, and array will be in the form:
	 *
	 * [smaller than pivot, equal to pivot, greater than pivot]
	 *
	 * \(O(n)\)
	 *
	 * @param  <E>   the array element type
	 * @param  a     an array
	 * @param  from  first index (inclusive)
	 * @param  to    last index (exclusive)
	 * @param  pivot an element to partition the array by
	 * @param  c     comparator
	 * @return       the last index of element smaller or equal to the pivot (exclusive)
	 */
	static <E> int pivotPartition(E[] a, int from, int to, E pivot, Comparator<? super E> c) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();

		// Find greatest element smaller than the pivot
		int pivotIdx = -1;
		for (int i = from; i < to; i++)
			if (c.compare(pivot, a[i]) >= 0 && (pivotIdx == -1 || c.compare(a[pivotIdx], a[i]) < 0))
				pivotIdx = i;
		if (pivotIdx == -1)
			return 0; // all elements are greater than the pivot

		return pivotPartition0(a, from, to, pivotIdx, c).secondInt();
	}

	/**
	 * Partition an array by a given pivot
	 *
	 * At the end of this function, and array will be in the form:
	 *
	 * [smaller than pivot, equal to pivot, greater than pivot]
	 *
	 * \(O(n)\)
	 *
	 * @param  a     an array
	 * @param  from  first index (inclusive)
	 * @param  to    last index (exclusive)
	 * @param  pivot an element to partition the array by
	 * @param  c     comparator
	 * @return       the last index of element smaller or equal to the pivot (exclusive)
	 */
	static int pivotPartition(int[] a, int from, int to, int pivot, IntComparator c) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		c = c != null ? c : Integer::compare;

		// Find greatest element smaller than the pivot
		int pivotIdx = -1;
		for (int i = from; i < to; i++)
			if (c.compare(pivot, a[i]) >= 0 && (pivotIdx == -1 || c.compare(a[pivotIdx], a[i]) < 0))
				pivotIdx = i;
		if (pivotIdx == -1)
			return 0; // all elements are greater than the pivot

		return pivotPartition0(a, from, to, pivotIdx, c).secondInt();
	}

	private static <E> IntIntPair pivotPartition0(E[] a, int from, int to, int pivotIdx, Comparator<? super E> c) {
		E pivot = a[pivotIdx];
		int insertIdx = from;
		for (int i = from; i < to; i++)
			if (c.compare(a[i], pivot) < 0)
				ObjectArrays.swap(a, i, insertIdx++);
		final int lastSmaller = insertIdx - 1;
		for (int i = insertIdx; i < to; i++)
			if (c.compare(a[i], pivot) == 0)
				ObjectArrays.swap(a, i, insertIdx++);
		final int firstGreater = insertIdx;
		return IntIntPair.of(lastSmaller, firstGreater);
	}

	private static IntIntPair pivotPartition0(int[] a, int from, int to, int pivotIdx, IntComparator c) {
		int pivot = a[pivotIdx];
		int insertIdx = from;
		for (int i = from; i < to; i++)
			if (c.compare(a[i], pivot) < 0)
				IntArrays.swap(a, i, insertIdx++);
		final int lastSmaller = insertIdx - 1;
		for (int i = insertIdx; i < to; i++)
			if (c.compare(a[i], pivot) == 0)
				IntArrays.swap(a, i, insertIdx++);
		final int firstGreater = insertIdx;
		return IntIntPair.of(lastSmaller, firstGreater);
	}

	private static <E> int calcPivot(E[] a, int from, int to, Comparator<? super E> c) {
		if (to - from <= 5)
			return partition5(a, from, to, c);
		int blockNum = (to - from - 1) / 5 + 1;
		for (int i = 0; i < blockNum; i++) {
			int subFrom = from + i * 5;
			int subTo = subFrom + 5;
			if (subTo > to)
				subTo = to;
			int median5 = partition5(a, subFrom, subTo, c);
			ObjectArrays.swap(a, median5, from + i);
		}

		int mid = from + blockNum / 2;
		getKthElement0(a, from, from + blockNum, mid, c);
		return mid;
	}

	private static int calcPivot(int[] a, int from, int to, IntComparator c) {
		if (to - from <= 5)
			return partition5(a, from, to, c);
		int blockNum = (to - from - 1) / 5 + 1;
		for (int i = 0; i < blockNum; i++) {
			int subFrom = from + i * 5;
			int subTo = subFrom + 5;
			if (subTo > to)
				subTo = to;
			int median5 = partition5(a, subFrom, subTo, c);
			IntArrays.swap(a, median5, from + i);
		}

		int mid = from + blockNum / 2;
		getKthElement0(a, from, from + blockNum, mid, c);
		return mid;
	}

	private static <E> int partition5(E[] a, int from, int to, Comparator<? super E> c) {
		for (int i = from + 1; i < to; i++)
			for (int j = i; j > from && c.compare(a[j], a[j - 1]) < 0; j--)
				ObjectArrays.swap(a, j, j - 1);
		return from + (to - from) / 2;
	}

	private static int partition5(int[] a, int from, int to, IntComparator c) {
		for (int i = from + 1; i < to; i++)
			for (int j = i; j > from && c.compare(a[j], a[j - 1]) < 0; j--)
				IntArrays.swap(a, j, j - 1);
		return from + (to - from) / 2;
	}

	/**
	 * Partition an array to buckets where each element in bucket i is smaller than all elements in bucket i+1.
	 * <p>
	 * \(O(n \log k)\) where k is the number of buckets of the output.
	 *
	 * @param <E>        the array element type
	 * @param a          an array
	 * @param from       first index (inclusive)
	 * @param to         last index (exclusive)
	 * @param c          comparator
	 * @param bucketSize the size of the bucket. Last bucket may be smaller than the specified value.
	 */
	static <E> void bucketPartition(E[] a, int from, int to, Comparator<? super E> c, int bucketSize) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		if (bucketSize <= 0)
			throw new IllegalArgumentException("invalid bucket size: " + bucketSize);
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();
		bucketPartition0(a, from, to, c, bucketSize);
	}

	/**
	 * Partition an array to buckets where each element in bucket i is smaller than all elements in bucket i+1.
	 * <p>
	 * \(O(n \log k)\) where k is the number of buckets of the output.
	 *
	 * @param a          an array
	 * @param from       first index (inclusive)
	 * @param to         last index (exclusive)
	 * @param c          comparator
	 * @param bucketSize the size of the bucket. Last bucket may be smaller than the specified value.
	 */
	static void bucketPartition(int[] a, int from, int to, IntComparator c, int bucketSize) {
		Assertions.Arrays.checkFromTo(from, to, a.length);
		if (bucketSize <= 0)
			throw new IllegalArgumentException("invalid bucket size: " + bucketSize);
		c = c != null ? c : Integer::compare;
		bucketPartition0(a, from, to, c, bucketSize);
	}

	private static <E> void bucketPartition0(E[] a, int from, int to, Comparator<? super E> c, int bucketSize) {
		if (to - from <= bucketSize)
			return;
		int idx = from + (((to - from) / 2 - 1) / bucketSize + 1) * bucketSize;
		getKthElement0(a, from, to, idx, c);
		bucketPartition0(a, from, idx, c, bucketSize);
		bucketPartition0(a, idx, to, c, bucketSize);
	}

	private static void bucketPartition0(int[] a, int from, int to, IntComparator c, int bucketSize) {
		if (to - from <= bucketSize)
			return;
		int idx = from + (((to - from) / 2 - 1) / bucketSize + 1) * bucketSize;
		getKthElement0(a, from, to, idx, c);
		bucketPartition0(a, from, idx, c, bucketSize);
		bucketPartition0(a, idx, to, c, bucketSize);
	}

}
