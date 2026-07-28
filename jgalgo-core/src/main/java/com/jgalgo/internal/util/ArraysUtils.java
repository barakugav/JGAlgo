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

package com.jgalgo.internal.util;

import java.util.Arrays;
import java.util.Comparator;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

public class ArraysUtils {

	private static final int QUICKSORT_MEDIAN_OF_9 = 128;

	private ArraysUtils() {}

	/**
	 * Get the element that would be in the k-th position if the array was sorted.
	 *
	 * <p>
	 * For example, kthElement([10, 13, 14, 11, 12], 3) = 13
	 *
	 * @param  <E>     the array element type
	 * @param  a       an array
	 * @param  from    first index (inclusive)
	 * @param  to      last index (exclusive)
	 * @param  k       index of the desire element in the sorted array
	 * @param  c       comparator
	 * @param  inPlace if {@code true}, all operations will be done on the given array and at the end it will be
	 *                     partitioned by the k-th element
	 * @return         the k-th element
	 */
	static <E> E kthElement(E[] a, int from, int to, int k, Comparator<? super E> c, boolean inPlace) {
		Assertions.checkArrayFromTo(from, to, a.length);
		Assertions.checkArrayIndex(k, from, to);
		if (!inPlace) {
			k -= from;
			a = Arrays.copyOfRange(a, from, to);
			from = 0;
			to = a.length;
		}
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();

		kthElement0(a, from, to, k, c);
		return a[k];
	}

	/**
	 * Get the element that would be in the k-th position if the array was sorted.
	 *
	 * <p>
	 * For example, kthElement([10, 13, 14, 11, 12], 3) = 13
	 *
	 * @param  a       an array
	 * @param  from    first index (inclusive)
	 * @param  to      last index (exclusive)
	 * @param  k       index of the desire element in the sorted array
	 * @param  c       comparator
	 * @param  inPlace if {@code true}, all operations will be done on the given array and at the end it will be
	 *                     partitioned by the k-th element
	 * @return         the k-th element
	 */
	public static int kthElement(int[] a, int from, int to, int k, IntComparator c, boolean inPlace) {
		Assertions.checkArrayFromTo(from, to, a.length);
		Assertions.checkArrayIndex(k, from, to);
		if (!inPlace) {
			k -= from;
			a = Arrays.copyOfRange(a, from, to);
			from = 0;
			to = a.length;
		}
		c = c != null ? c : Integer::compare;

		kthElement0(a, from, to, k, c);
		return a[k];
	}

	private static <E> void kthElement0(E[] x, int from, int to, int k, Comparator<? super E> comp) {
		if (from >= to - 1)
			return;
		for (;;) {
			/* Pivot choice code from fastutil */
			final int len = to - from;
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
				int s = len / 8;
				l = med3(x, l, l + s, l + 2 * s, comp);
				m = med3(x, m - s, m, m + s, comp);
				n = med3(x, n - 2 * s, n - s, n, comp);
			}
			m = med3(x, l, m, n, comp); // Mid-size, med of 3

			int pivotIdx = m;

			long p = pivotPartition0(x, from, to, x[pivotIdx], comp);
			int lastSmaller = IntPair.first(p);
			int firstGreater = IntPair.second(p);
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

	private static void kthElement0(int[] x, int from, int to, int k, IntComparator comp) {
		if (from >= to - 1)
			return;
		for (;;) {
			/* Pivot choice code from fastutil */
			final int len = to - from;
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
				int s = len / 8;
				l = med3(x, l, l + s, l + 2 * s, comp);
				m = med3(x, m - s, m, m + s, comp);
				n = med3(x, n - 2 * s, n - s, n, comp);
			}
			m = med3(x, l, m, n, comp); // Mid-size, med of 3

			int pivotIdx = m;
			long p = pivotPartition0(x, from, to, x[pivotIdx], comp);
			int lastSmaller = IntPair.first(p);
			int firstGreater = IntPair.second(p);
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

	private static <K> int med3(final K[] x, final int a, final int b, final int c, Comparator<K> comp) {
		final int ab = comp.compare(x[a], x[b]);
		final int ac = comp.compare(x[a], x[c]);
		final int bc = comp.compare(x[b], x[c]);
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	private static int med3(final int[] x, final int a, final int b, final int c, IntComparator comp) {
		final int ab = comp.compare(x[a], x[b]);
		final int ac = comp.compare(x[a], x[c]);
		final int bc = comp.compare(x[b], x[c]);
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	/**
	 * Partition an array by a given pivot.
	 *
	 * <p>
	 * At the end of this function, and array will be in the form: [smaller than pivot, equal to pivot, greater than
	 * pivot]
	 *
	 * @param  <E>   the array element type
	 * @param  a     an array
	 * @param  from  first index (inclusive)
	 * @param  to    last index (exclusive)
	 * @param  pivot an element to partition the array by
	 * @param  c     comparator
	 * @return       the index of the first element greater than the pivot
	 */
	static <E> int pivotPartition(E[] a, int from, int to, E pivot, Comparator<? super E> c) {
		Assertions.checkArrayFromTo(from, to, a.length);
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();
		return IntPair.second(pivotPartition0(a, from, to, pivot, c));
	}

	/**
	 * Partition an array by a given pivot.
	 *
	 * <p>
	 * At the end of this function, and array will be in the form: [smaller than pivot, equal to pivot, greater than
	 * pivot]
	 *
	 * @param  a     an array
	 * @param  from  first index (inclusive)
	 * @param  to    last index (exclusive)
	 * @param  pivot an element to partition the array by
	 * @param  c     comparator
	 * @return       the index of the first element greater than the pivot
	 */
	public static int pivotPartition(int[] a, int from, int to, int pivot, IntComparator c) {
		Assertions.checkArrayFromTo(from, to, a.length);
		c = c != null ? c : Integer::compare;
		return IntPair.second(pivotPartition0(a, from, to, pivot, c));
	}

	private static <E> long pivotPartition0(E[] x, int from, int to, E pivot, Comparator<? super E> cmp) {
		/* The following code was taken from fastutil ObjectArrays.java */

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && (comparison = (cmp.compare((x[b]), pivot))) <= 0) {
				if (comparison == 0)
					ObjectArrays.swap(x, a++, b);
				b++;
			}
			while (c >= b && (comparison = (cmp.compare((x[c]), pivot))) >= 0) {
				if (comparison == 0)
					ObjectArrays.swap(x, c, d--);
				c--;
			}
			if (b > c)
				break;
			ObjectArrays.swap(x, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		ObjectArrays.swap(x, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		ObjectArrays.swap(x, b, to - s, s);

		int lastSmaller = from + (b - a) - 1;
		int firstGreater = to - (d - c);
		return IntPair.of(lastSmaller, firstGreater);
	}

	private static long pivotPartition0(int[] x, int from, int to, int pivot, IntComparator cmp) {
		/* The following code was taken from fastutil IntArrays.java */

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && (comparison = (cmp.compare((x[b]), pivot))) <= 0) {
				if (comparison == 0)
					IntArrays.swap(x, a++, b);
				b++;
			}
			while (c >= b && (comparison = (cmp.compare((x[c]), pivot))) >= 0) {
				if (comparison == 0)
					IntArrays.swap(x, c, d--);
				c--;
			}
			if (b > c)
				break;
			IntArrays.swap(x, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		IntArrays.swap(x, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		IntArrays.swap(x, b, to - s, s);

		int lastSmaller = from + (b - a) - 1;
		int firstGreater = to - (d - c);
		return IntPair.of(lastSmaller, firstGreater);
	}

	/**
	 * Partition an array to buckets where each element in bucket i is smaller than all elements in bucket i+1.
	 *
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
		Assertions.checkArrayFromTo(from, to, a.length);
		if (bucketSize <= 0)
			throw new IllegalArgumentException("invalid bucket size: " + bucketSize);
		c = c != null ? c : JGAlgoUtils.getDefaultComparator();
		bucketPartition0(a, from, to, c, bucketSize);
	}

	/**
	 * Partition an array to buckets where each element in bucket i is smaller than all elements in bucket i+1.
	 *
	 * <p>
	 * \(O(n \log k)\) where k is the number of buckets of the output.
	 *
	 * @param a          an array
	 * @param from       first index (inclusive)
	 * @param to         last index (exclusive)
	 * @param c          comparator
	 * @param bucketSize the size of the bucket. Last bucket may be smaller than the specified value.
	 */
	public static void bucketPartition(int[] a, int from, int to, IntComparator c, int bucketSize) {
		Assertions.checkArrayFromTo(from, to, a.length);
		if (bucketSize <= 0)
			throw new IllegalArgumentException("invalid bucket size: " + bucketSize);
		c = c != null ? c : Integer::compare;
		bucketPartition0(a, from, to, c, bucketSize);
	}

	private static <E> void bucketPartition0(E[] a, int from, int to, Comparator<? super E> c, int bucketSize) {
		if (to - from <= bucketSize)
			return;
		int idx = from + (((to - from) / 2 - 1) / bucketSize + 1) * bucketSize;
		kthElement0(a, from, to, idx, c);
		bucketPartition0(a, from, idx, c, bucketSize);
		bucketPartition0(a, idx, to, c, bucketSize);
	}

	private static void bucketPartition0(int[] a, int from, int to, IntComparator c, int bucketSize) {
		if (to - from <= bucketSize)
			return;
		int idx = from + (((to - from) / 2 - 1) / bucketSize + 1) * bucketSize;
		kthElement0(a, from, to, idx, c);
		bucketPartition0(a, from, idx, c, bucketSize);
		bucketPartition0(a, idx, to, c, bucketSize);
	}

}
