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

import static com.jgalgo.internal.util.Numbers.log2ceil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

/**
 * A binary heap implementation using an array.
 *
 * <p>
 * The binary heap is the most simple implementation of a heap. It does not use some complex pointer based data
 * structure, but rather a simple continue array, to store its elements. Its have very small memory footprint and should
 * be used as default implementation in use cases where only {@link #insert(Object)} and {@link #extractMin()}
 * operations are required, both implemented in \(O(\log n) time. If the minimum is only peeked without extraction using
 * {@link #findMin()}, constant number of operations are performed.
 *
 * <p>
 * If fast {@code remove(...)} or {@code decreaseKey(...)} operations are required, consider using {@link HeapPairing}
 * or {@link HeapFibonacci}.
 *
 * @param  <E> the elements type
 * @see        <a href="https://en.wikipedia.org/wiki/Binary_heap">Wikipedia</a>
 * @author     Barak Ugav
 */
class HeapBinary<E> extends HeapAbstract<E> {

	private E[] arr;
	private int size;

	/**
	 * Constructs a new, empty binary heap, ordered according to the natural ordering of its elements.
	 *
	 * <p>
	 * All elements inserted into the heap must implement the {@link Comparable} interface. Furthermore, all such
	 * elements must be <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a {@code ClassCastException}
	 * for any elements {@code e1} and {@code e2} in the heap. If the user attempts to insert an element to the heap
	 * that violates this constraint (for example, the user attempts to insert a string element to a heap whose elements
	 * are integers), the {@code insert} call will throw a {@code ClassCastException}.
	 */
	HeapBinary() {
		this(null);
	}

	/**
	 * Constructs a new, empty binary heap, ordered according to the specified comparator.
	 *
	 * <p>
	 * All elements inserted into the heap must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(e1, e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and
	 * {@code e2} in the heap. If the user attempts to insert an element to the heap that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 *
	 * @param comparator the comparator that will be used to order this heap. If {@code null}, the
	 *                       {@linkplain Comparable natural ordering} of the elements will be used.
	 */
	HeapBinary(Comparator<? super E> comparator) {
		super(comparator);
		@SuppressWarnings("unchecked")
		E[] arr0 = (E[]) ObjectArrays.EMPTY_ARRAY;
		arr = arr0;
		size = 0;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean isNotEmpty() {
		return size != 0;
	}

	@Override
	public void insert(E elm) {
		if (arr.length == size)
			arr = Arrays.copyOf(arr, Math.max(2, arr.length * 2));

		moveUp(size, elm);
		size++;
	}

	@Override
	public boolean remove(Object e0) {
		@SuppressWarnings("unchecked")
		E e = (E) e0;
		int s = size;
		E[] a = arr;

		int i;
		if (c == null) {
			for (i = 0; i < s; i++)
				if (JGAlgoUtils.cmpDefault(e, a[i]) == 0)
					break;
		} else {
			for (i = 0; i < s; i++)
				if (c.compare(e, a[i]) == 0)
					break;
		}
		if (i == s)
			return false; /* not found */

		remove(i);
		return true;
	}

	@Override
	public void clear() {
		Arrays.fill(arr, 0, size, null);
		size = 0;
	}

	@Override
	public E findMin() {
		Assertions.notEmpty(this);
		return arr[0];
	}

	private void remove(int idx) {
		E[] a = arr;

		E old = a[idx];
		E e = a[size-- - 1];
		a[size] = null;

		if (compare(e, old) <= 0)
			moveUp(idx, e);
		else
			moveDown(idx, e);
	}

	@Override
	public E extractMin() {
		Assertions.notEmpty(this);
		E min = arr[0];
		remove(0);
		return min;
	}

	@Override
	public void insertAll(Collection<? extends E> elms) {
		int combinedSize = size + elms.size();
		if (arr.length <= combinedSize)
			arr = Arrays.copyOf(arr, Math.max(arr.length * 2, combinedSize * 3 / 2));

		int reconstructionCost = combinedSize;
		int addAllCost = elms.size() * log2ceil(combinedSize);
		if (reconstructionCost >= addAllCost) {
			for (E e : elms)
				insert(e);
		} else {
			E[] a = arr;
			int s = size;
			for (E e : elms)
				a[s++] = e;
			size = s;

			if (s > 1) {
				int lastLayer = log2ceil(s + 1) - 1;
				int lastParent = (1 << lastLayer) - 2;
				for (int parent = lastParent; parent >= 0; parent--)
					moveDown(parent, a[parent]);
			}
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new It();
	}

	private class It implements Iterator<E> {

		int i;

		It() {
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < size;
		}

		@Override
		public E next() {
			Assertions.hasNext(this);
			return arr[i++];
		}

	}

	private void moveUp(int i, E e) {
		E[] a = arr;

		if (c == null) {
			for (;;) {
				int p;
				if (i == 0
						|| JGAlgoUtils.cmpDefault(e, a[p = (i - 1) / 2]) >= 0) { /* reached root or parent is smaller */
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
				if (i == 0 || c.compare(e, a[p = (i - 1) / 2]) >= 0) { /* reached root or parent is smaller */
					a[i] = e;
					return;
				}

				/* e is smaller than parent, continue up */
				a[i] = a[p];
				i = p;
			}
		}
	}

	private void moveDown(int i, E e) {
		E[] a = arr;
		int s = size;

		if (c == null) {
			for (;;) {
				int c01i, c0i = i * 2 + 1, c1i = i * 2 + 2;
				if (c0i >= s)
					break;

				E c01, c0 = a[c0i], c1;
				if (c1i < s && JGAlgoUtils.cmpDefault(c1 = a[c1i], c0) < 0) {
					c01i = c1i;
					c01 = c1;
				} else {
					c01i = c0i;
					c01 = c0;
				}

				if (JGAlgoUtils.cmpDefault(e, c01) <= 0)
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

				E c01, c0 = a[c0i], c1;
				if (c1i < s && c.compare(c1 = a[c1i], c0) < 0) {
					c01i = c1i;
					c01 = c1;
				} else {
					c01i = c0i;
					c01 = c0;
				}

				if (c.compare(e, c01) <= 0)
					break;

				/* continue down */
				a[i] = c01;
				i = c01i;
			}
		}
		a[i] = e;
	}

}
