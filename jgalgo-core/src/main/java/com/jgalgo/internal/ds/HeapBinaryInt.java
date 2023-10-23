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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * A binary heap implementation using an array specifically for int elements.
 * <p>
 * The binary heap is the most simple implementation of a heap. It does not use some complex pointer based data
 * structure, but rather a simple continue array, to store its elements. Its have very small memory footprint and should
 * be used as default implementation in use cases where only {@link #insert(Object)} and {@link #extractMin()}
 * operations are required, both implemented in \(O(\log n) time. If the minimum is only peeked without extraction using
 * {@link #findMin()}, constant number of operations are performed.
 * <p>
 * If fast {@code remove(...)} or {@code decreaseKey(...)} operations are required, consider using {@link HeapPairing}
 * or {@link HeapFibonacci}.
 *
 * @param  <E> the elements type
 * @see        <a href="https://en.wikipedia.org/wiki/Binary_heap">Wikipedia</a>
 * @author     Barak Ugav
 */
class HeapBinaryInt extends HeapAbstract<Integer> {

	private int[] arr;
	private int size;
	private final IntComparator intCmp;

	/**
	 * Constructs a new, empty binary heap, ordered according to the natural ordering of its elements.
	 * <p>
	 * All elements inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such
	 * elements must be <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a {@code ClassCastException}
	 * for any elements {@code e1} and {@code e2} in the heap. If the user attempts to insert an element to the heap
	 * that violates this constraint (for example, the user attempts to insert a string element to a heap whose elements
	 * are integers), the {@code insert} call will throw a {@code ClassCastException}.
	 */
	HeapBinaryInt() {
		this(null);
	}

	/**
	 * Constructs a new, empty binary heap, ordered according to the specified comparator.
	 * <p>
	 * All elements inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(e1, e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and
	 * {@code e2} in the heap. If the user attempts to insert an element to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the elements will be used.
	 */
	HeapBinaryInt(Comparator<? super Integer> comparator) {
		super(comparator);
		arr = IntArrays.EMPTY_ARRAY;
		size = 0;
		intCmp = comparator == null || comparator instanceof IntComparator ? (IntComparator) comparator
				: (k1, k2) -> comparator.compare(Integer.valueOf(k1), Integer.valueOf(k2));
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void insert(Integer e) {
		insert(e.intValue());
	}

	public void insert(int e) {
		if (arr.length == size)
			arr = Arrays.copyOf(arr, Math.max(2, arr.length * 2));

		moveUp(size, e);
		size++;
	}

	@Override
	public boolean remove(Object e) {
		int eInt = ((Integer) e).intValue();
		int s = size;
		int[] a = arr;

		int i;
		if (c == null) {
			for (i = 0; i < s; i++)
				if (Integer.compare(eInt, a[i]) == 0)
					break;
		} else {
			for (i = 0; i < s; i++)
				if (intCmp.compare(eInt, a[i]) == 0)
					break;
		}
		if (i == s)
			return false; /* not found */

		remove(i);
		return true;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public Integer findMin() {
		Assertions.Heaps.notEmpty(this);
		return Integer.valueOf(arr[0]);
	}

	private void remove(int idx) {
		int[] a = arr;

		int old = a[idx];
		int e = a[size-- - 1];

		if (compare(e, old) <= 0)
			moveUp(idx, e);
		else
			moveDown(idx, e);
	}

	@Override
	public Integer extractMin() {
		Assertions.Heaps.notEmpty(this);
		int min = arr[0];
		remove(0);
		return Integer.valueOf(min);
	}

	@Override
	public void insertAll(Collection<? extends Integer> elms) {
		int combinedSize = size + elms.size();
		if (arr.length <= combinedSize)
			arr = Arrays.copyOf(arr, Math.max(arr.length * 2, combinedSize * 3 / 2));

		int reconstructionCost = combinedSize;
		int addAllCost = elms.size() * JGAlgoUtils.log2ceil(combinedSize);
		if (reconstructionCost >= addAllCost) {
			if (elms instanceof IntCollection) {
				for (int e : (IntCollection) elms)
					insert(e);
			} else {
				for (Integer e : elms)
					insert(e.intValue());
			}
		} else {
			int[] a = arr;
			int s = size;
			if (elms instanceof IntCollection) {
				for (int e : (IntCollection) elms)
					a[s++] = e;
			} else {
				for (Integer e : elms)
					a[s++] = e.intValue();
			}
			size = s;

			if (s > 1) {
				int lastLayer = JGAlgoUtils.log2ceil(s + 1) - 1;
				int lastParent = (1 << lastLayer) - 2;
				for (int parent = lastParent; parent >= 0; parent--)
					moveDown(parent, a[parent]);
			}
		}
	}

	@Override
	public boolean addAll(Collection<? extends Integer> other) {
		if (other.isEmpty())
			return false;
		insertAll(other);
		return true;
	}

	@Override
	public IntIterator iterator() {
		return new It();
	}

	private class It implements IntIterator {

		int i;

		It() {
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < size;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			return arr[i++];
		}

	}

	private void moveUp(int i, int e) {
		int[] a = arr;

		if (c == null) {
			for (;;) {
				int p;
				if (i == 0 || Integer.compare(e, a[p = (i - 1) / 2]) >= 0) { /* reached root or parent is smaller */
					a[i] = e;
					return;
				}

				/* e is smaller than parent, continue up */
				a[i] = a[p];
				i = p;
			}
		} else {
			for (;;) {
				int p;
				if (i == 0 || intCmp.compare(e, a[p = (i - 1) / 2]) >= 0) { /* reached root or parent is smaller */
					a[i] = e;
					return;
				}

				/* e is smaller than parent, continue up */
				a[i] = a[p];
				i = p;
			}
		}
	}

	private void moveDown(int i, int e) {
		int[] a = arr;
		int s = size;

		if (c == null) {
			for (;;) {
				int c01i, c0i = i * 2 + 1, c1i = i * 2 + 2;
				if (c0i >= s)
					break;

				int c01, c0 = a[c0i], c1;
				if (c1i < s && Integer.compare(c1 = a[c1i], c0) < 0) {
					c01i = c1i;
					c01 = c1;
				} else {
					c01i = c0i;
					c01 = c0;
				}

				if (Integer.compare(e, c01) <= 0)
					break;

				/* continue down */
				a[i] = c01;
				i = c01i;
			}
		} else {
			for (;;) {
				int c01i, c0i = i * 2 + 1, c1i = i * 2 + 2;
				if (c0i >= s)
					break;

				int c01, c0 = a[c0i], c1;
				if (c1i < s && intCmp.compare(c1 = a[c1i], c0) < 0) {
					c01i = c1i;
					c01 = c1;
				} else {
					c01i = c0i;
					c01 = c0;
				}

				if (intCmp.compare(e, c01) <= 0)
					break;

				/* continue down */
				a[i] = c01;
				i = c01i;
			}
		}
		a[i] = e;
	}

	int compare(int e1, int e2) {
		return c == null ? Integer.compare(e1, e2) : intCmp.compare(e1, e2);
	}

}
