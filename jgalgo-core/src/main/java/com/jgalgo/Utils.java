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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;

class Utils {
	private Utils() {}

	private static final double LOG2 = Math.log(2);
	private static final double LOG2_INV = 1 / LOG2;

	static double log2(double x) {
		return Math.log(x) * LOG2_INV;
	}

	static int log2(int x) {
		int r = 0xFFFF - x >> 31 & 0x10;
		x >>= r;
		int shift = 0xFF - x >> 31 & 0x8;
		x >>= shift;
		r |= shift;
		shift = 0xF - x >> 31 & 0x4;
		x >>= shift;
		r |= shift;
		shift = 0x3 - x >> 31 & 0x2;
		x >>= shift;
		r |= shift;
		r |= (x >> 1);
		return r;
	}

	static int log2ceil(int x) {
		int r = log2(x);
		return (1 << r) == x ? r : r + 1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final Comparator DEFAULT_COMPARATOR = (a, b) -> ((Comparable) a).compareTo(b);

	@SuppressWarnings("unchecked")
	static <E> Comparator<E> getDefaultComparator() {
		return DEFAULT_COMPARATOR;
	}

	@SuppressWarnings("unchecked")
	static <E> int cmpDefault(E e1, E e2) {
		return ((Comparable<E>) e1).compareTo(e2);
	}

	static boolean isEqual(double a, double b) {
		double mag = Math.max(Math.abs(a), Math.abs(b));
		double eps = mag * 1E-6;
		return Math.abs(a - b) <= eps;
	}

	/* syntax sugar to iterator for loops */
	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	static class RangeIter implements IntIterator {

		private int idx;
		private final int size;

		RangeIter(int size) {
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			return idx < size;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			return idx++;
		}
	}

	static interface IterPeekable<E> extends Iterator<E> {

		E peekNext();

		static interface Int extends IntIterator {

			int peekNext();

			static final IterPeekable.Int Empty = new IterPeekable.Int() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public int nextInt() {
					throw new NoSuchElementException();
				}

				@Override
				public int peekNext() {
					throw new NoSuchElementException();
				}
			};

		}

	}

	static class IterPeekableImpl<E> implements IterPeekable<E> {

		private final Iterator<? super E> it;
		private Object peek;
		private static final Object PeekNone = new Object();

		IterPeekableImpl(Iterator<? super E> it) {
			this.it = Objects.requireNonNull(it);
			peek = PeekNone;
		}

		@Override
		public boolean hasNext() {
			if (peek != PeekNone)
				return true;
			if (!it.hasNext())
				return false;
			peek = it.next();
			return true;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			@SuppressWarnings("unchecked")
			E n = (E) peek;
			peek = PeekNone;
			return n;
		}

		@Override
		@SuppressWarnings("unchecked")
		public E peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return (E) peek;
		}

		static class Int implements IterPeekable.Int {

			private final IntIterator it;
			private int peek;
			private boolean isPeekValid;

			Int(IntIterator it) {
				this.it = Objects.requireNonNull(it);
				isPeekValid = false;
			}

			@Override
			public boolean hasNext() {
				if (isPeekValid)
					return true;
				if (!it.hasNext())
					return false;
				peek = it.nextInt();
				isPeekValid = true;
				return true;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				isPeekValid = false;
				return peek;
			}

			@Override
			public int peekNext() {
				if (!hasNext())
					throw new NoSuchElementException();
				return peek;
			}

		}

	}

	static class IterMap<A, B> implements Iterator<B> {
		private final Iterator<A> it;
		private final Function<A, B> map;

		IterMap(Iterator<A> it, Function<A, B> map) {
			this.it = Objects.requireNonNull(it);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public B next() {
			return map.apply(it.next());
		}

	}

	static class QueueFixSize<E> {

		private final int idxMask;
		private final Object[] q;
		private int begin, end;

		QueueFixSize(int maxSize) {
			/* round size of next power of 2 */
			maxSize = 1 << (32 - Integer.numberOfLeadingZeros(maxSize));
			idxMask = maxSize - 1;
			q = new Object[maxSize];
			begin = end = 0;
		}

		int size() {
			return begin <= end ? end - begin : q.length - begin + end;
		}

		boolean isEmpty() {
			return begin == end;
		}

		void push(E x) {
			if (((end + 1) & idxMask) == begin)
				throw new IndexOutOfBoundsException();
			q[end] = x;
			end = (end + 1) & idxMask;
		}

		E pop() {
			if (isEmpty())
				throw new NoSuchElementException();
			@SuppressWarnings("unchecked")
			E x = (E) q[begin];
			begin = (begin + 1) & idxMask;
			return x;
		}

		void clear() {
			begin = end = 0;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append('[');

			int lastIdx = Math.min(end, q.length) - 1;
			if (begin <= lastIdx) {
				for (int i = begin;; i++) {
					b.append(q[i]);
					if (i == lastIdx) {
						if (end < begin && end != 0)
							b.append(", ");
						break;
					}
					b.append(", ");
				}
			}

			if (end < begin && end != 0) {
				for (int i = 0;; i++) {
					b.append(q[i]);
					if (i == end - 1)
						break;
					b.append(", ");
				}
			}

			return b.append(']').toString();
		}

	}

	static class NullIterator<E> implements Iterator<E> {

		private int size;

		NullIterator(int size) {
			if (size < 0)
				throw new IllegalArgumentException();
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			return size > 0;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			size--;
			return null;
		}

	}

	static class NullListIterator<E> implements ListIterator<E> {

		private final int size;
		private int idx;

		NullListIterator(int size) {
			this(size, 0);
		}

		NullListIterator(int size, int idx) {
			if (size < 0)
				throw new IllegalArgumentException();
			if (idx < 0 || idx > size)
				throw new IllegalArgumentException();
			this.size = size;
			this.idx = idx;
		}

		@Override
		public boolean hasNext() {
			return idx < size;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			idx++;
			return null;
		}

		@Override
		public boolean hasPrevious() {
			return idx > 0;
		}

		@Override
		public E previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			idx--;
			return null;
		}

		@Override
		public int nextIndex() {
			return idx;
		}

		@Override
		public int previousIndex() {
			return idx - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}

	}

	static class NullList<E> extends AbstractList<E> {

		private final int size;

		NullList(int size) {
			if (size < 0)
				throw new IllegalArgumentException();
			this.size = size;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			return o == null && size > 0;
		}

		@Override
		public Iterator<E> iterator() {
			return new NullIterator<>(size);
		}

		@Override
		public boolean add(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E get(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return null;
		}

		@Override
		public E set(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E remove(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOf(Object o) {
			return o == null && size > 0 ? 0 : -1;
		}

		@Override
		public int lastIndexOf(Object o) {
			return o == null && size > 0 ? size - 1 : -1;
		}

		@Override
		public ListIterator<E> listIterator() {
			return new NullListIterator<>(size);
		}

		@Override
		public ListIterator<E> listIterator(int index) {
			return new NullListIterator<>(size, index);
		}

		@Override
		public List<E> subList(int fromIndex, int toIndex) {
			if (fromIndex < 0 || fromIndex >= toIndex || toIndex > size)
				throw new IllegalArgumentException();
			if (fromIndex == 0 && toIndex == size)
				return this;
			return new NullList<>(toIndex - fromIndex);
		}

	}

	static IntIterator bitSetIterator(BitSet bitSet) {
		return new IntIterator() {

			int bit = bitSet.nextSetBit(0);

			@Override
			public boolean hasNext() {
				return bit != -1;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				int ret = bit;
				bit = bitSet.nextSetBit(bit + 1);
				return ret;
			}
		};
	}

	@FunctionalInterface
	static interface BiInt2IntFunction {
		int apply(int a1, int a2);
	}

	@FunctionalInterface
	static interface IntDoubleConsumer {
		void accept(int a1, double a2);
	}

	static void sort(int[] arr, int from, int to, IntComparator cmp, boolean parallel) {
		if (parallel) {
			IntArrays.parallelQuickSort(arr, from, to, cmp);
		} else {
			IntArrays.quickSort(arr, from, to, cmp);
		}
	}

	static ForkJoinPool getPool() {
		ForkJoinPool current = ForkJoinTask.getPool();
		return current == null ? ForkJoinPool.commonPool() : current;
	}

	static <Exec extends Runnable & Serializable> RecursiveAction recursiveAction(Exec exec) {
		return new RecursiveActionFromRunnable<>(exec);
	}

	static <V, Exec extends Supplier<? extends V> & Serializable> RecursiveTask<V> recursiveTask(Exec exec) {
		return new RecursiveTaskFromSupplier<>(exec);
	}

	private static class RecursiveActionFromRunnable<Exec extends Runnable & Serializable> extends RecursiveAction {
		private Exec exec;
		private static final long serialVersionUID = 1L;

		RecursiveActionFromRunnable(Exec exec) {
			this.exec = exec;
		}

		@Override
		protected void compute() {
			exec.run();
		}
	}

	private static class RecursiveTaskFromSupplier<V, Exec extends Supplier<? extends V> & Serializable>
			extends RecursiveTask<V> {
		private Exec exec;
		private static final long serialVersionUID = 1L;

		RecursiveTaskFromSupplier(Exec exec) {
			this.exec = exec;
		}

		@Override
		protected V compute() {
			return exec.get();
		}
	}

}
