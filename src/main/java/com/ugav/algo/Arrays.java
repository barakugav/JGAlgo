package com.ugav.algo;

import java.util.Comparator;

public class Arrays {

	private Arrays() {
		throw new InternalError();
	}

	public static <E> E getKthElement(E[] a, int k, Comparator<? super E> c) {
		return getKthElement(a, 0, a.length, k, c, false);
	}

	/**
	 * Get the K'th element in the array if it was sorted
	 *
	 * For example, getKthElement([10, 13, 14, 11, 12], 3) = 13
	 *
	 * O(n)
	 *
	 * @param a       an array
	 * @param from    first index (inclusive)
	 * @param to      last index (exclusive)
	 * @param k       index of the desire element in the sorted array
	 * @param c       comparator
	 * @param inPlace if true, all operations will be done on the given array and at
	 *                the end it will be partitioned by the Kth element
	 * @return the Kth element
	 */
	public static <E> E getKthElement(E[] a, int from, int to, int k, Comparator<? super E> c, boolean inPlace) {
		if (from < 0 || from >= to || to > a.length || k >= to - from)
			throw new IndexOutOfBoundsException("a(" + a.length + ")[" + from + ", " + to + "][" + k + "]");
		if (!inPlace) {
			a = java.util.Arrays.copyOfRange(a, from, to);
			from = 0;
			to = a.length;
		}
		c = c != null ? c : Utils.getDefaultComparator();

		getKthElement0(a, from, to, k, c);
		return a[k];
	}

	private static <E> void getKthElement0(E[] a, int from, int to, int k, Comparator<? super E> c) {
		while (true) {
			if (from == to + 1)
				return;
			int pivotIdx = calcPivot(a, from, to, c);
			pivotIdx = pivotPartition0(a, from, to, pivotIdx, c);
			if (pivotIdx == k)
				return;
			if (pivotIdx > k)
				to = pivotIdx;
			else
				from = pivotIdx + 1;
		}
	}

	/**
	 * Partition an array by a given pivot
	 *
	 * At the end of this function, and array will be in the form:
	 *
	 * [smaller than pivot, equal to pivot, greater than pivot]
	 *
	 * O(n)
	 *
	 * @param a     an array
	 * @param from  first index (inclusive)
	 * @param to    last index (exclusive)
	 * @param pivot an element to partition the array by
	 * @param c     comparator
	 * @return the last index of element smaller or equal to the pivot (exclusive)
	 */
	public static <E> int pivotPartition(E[] a, int from, int to, E pivot, Comparator<? super E> c) {
		if (from < 0 || from >= to || to > a.length)
			throw new IndexOutOfBoundsException("a(" + a.length + ")[" + from + ", " + to + "]");
		c = c != null ? c : Utils.getDefaultComparator();

		// Find greatest element smaller than the pivot
		int pivotIdx = -1;
		for (int i = from; i < to; i++)
			if (c.compare(pivot, a[i]) >= 0 && (pivotIdx == -1 || c.compare(a[pivotIdx], a[i]) < 0))
				pivotIdx = i;
		if (pivotIdx == -1)
			return 0; // all elements are greater than the pivot

		return pivotPartition0(a, from, to, pivotIdx, c) + 1;
	}

	private static <E> int pivotPartition0(E[] a, int from, int to, int pivotIdx, Comparator<? super E> c) {
		E pivot = a[pivotIdx];
		int insertIdx = from;
		for (int i = from; i < to; i++)
			if (c.compare(a[i], pivot) < 0)
				swap(a, i, insertIdx++);
		for (int i = insertIdx; i < to; i++)
			if (c.compare(a[i], pivot) == 0)
				swap(a, i, insertIdx++);
		return insertIdx - 1;
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
			swap(a, median5, from + i);
		}

		int mid = from + blockNum / 2;
		getKthElement0(a, from, from + blockNum, mid, c);
		return mid;
	}

	private static <E> int partition5(E[] a, int from, int to, Comparator<? super E> c) {
		for (int i = from + 1; i < to; i++)
			for (int j = i; j > from && c.compare(a[j], a[j - 1]) < 0; j--)
				swap(a, j, j - 1);
		return from + (to - from) / 2;
	}

	private static <E> void swap(E a[], int i, int j) {
		E temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

	/**
	 * Partition an array to buckets where each element in bucket i is smaller than
	 * all elements in bucket i+1
	 *
	 * O(nlogk) where k is the number of buckets of the output.
	 *
	 * @param a          an array
	 * @param from       first index (inclusive)
	 * @param to         last index (exclusive)
	 * @param c          comparator
	 * @param bucketSize the size of the bucket. Last bucket may be smaller than the
	 *                   specified value.
	 */
	public static <E> void bucketPartition(E[] a, int from, int to, Comparator<? super E> c, int bucketSize) {
		if (from < 0 || from >= to || to > a.length || bucketSize <= 0)
			throw new IndexOutOfBoundsException("a(" + a.length + ")[" + from + ", " + to + "][" + bucketSize + "]");
		c = c != null ? c : Utils.getDefaultComparator();
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

}
