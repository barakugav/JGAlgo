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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public class JGAlgoUtils {
	private JGAlgoUtils() {}

	private static final double LOG2 = Math.log(2);
	private static final double LOG2_INV = 1 / LOG2;

	public static double log2(double x) {
		return Math.log(x) * LOG2_INV;
	}

	public static int log2(int x) {
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

	public static int log2ceil(int x) {
		int r = log2(x);
		return (1 << r) == x ? r : r + 1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final Comparator DEFAULT_COMPARATOR = (a, b) -> ((Comparable) a).compareTo(b);

	@SuppressWarnings("unchecked")
	public static <E> Comparator<E> getDefaultComparator() {
		return DEFAULT_COMPARATOR;
	}

	@SuppressWarnings("unchecked")
	public static <E> int cmpDefault(E e1, E e2) {
		return ((Comparable<E>) e1).compareTo(e2);
	}

	public static boolean isEqual(double a, double b) {
		double mag = Math.max(Math.abs(a), Math.abs(b));
		double eps = mag * 1E-6;
		return Math.abs(a - b) <= eps;
	}

	/* syntax sugar to iterator for loops */
	public static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {
			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	/* syntax sugar to iterator for loops */
	public static IntIterable iterable(IntIterator it) {
		return new IntIterable() {
			@Override
			public IntIterator iterator() {
				return it;
			}
		};
	}

	public static interface IterPeekable<E> extends Iterator<E> {

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
					throw new NoSuchElementException(Assertions.Iters.ERR_NO_NEXT);
				}

				@Override
				public int peekNext() {
					throw new NoSuchElementException(Assertions.Iters.ERR_NO_NEXT);
				}
			};

		}

	}

	static class IterPeekableImpl<E> implements IterPeekable<E> {

		private final Iterator<? super E> it;
		private Object nextElm;
		private static final Object nextNone = JGAlgoUtils.labeledObj("None");

		IterPeekableImpl(Iterator<? super E> it) {
			this.it = Objects.requireNonNull(it);
			advance();
		}

		private void advance() {
			if (it.hasNext()) {
				nextElm = it.next();
			} else {
				nextElm = nextNone;
			}
		}

		@Override
		public boolean hasNext() {
			return nextElm != null;
		}

		@Override
		public E next() {
			Assertions.Iters.hasNext(this);
			@SuppressWarnings("unchecked")
			E ret = (E) nextElm;
			advance();
			return ret;
		}

		@Override
		@SuppressWarnings("unchecked")
		public E peekNext() {
			Assertions.Iters.hasNext(this);
			return (E) nextElm;
		}

		static class Int implements IterPeekable.Int {

			private final IntIterator it;
			private int next;
			private boolean isNextValid;

			Int(IntIterator it) {
				this.it = Objects.requireNonNull(it);
				advance();
			}

			private void advance() {
				if (isNextValid = it.hasNext())
					next = it.nextInt();
			}

			@Override
			public boolean hasNext() {
				return isNextValid;
			}

			@Override
			public int nextInt() {
				Assertions.Iters.hasNext(this);
				int ret = next;
				advance();
				return ret;
			}

			@Override
			public int peekNext() {
				Assertions.Iters.hasNext(this);
				return next;
			}

		}

	}

	private static class IterMap<A, B> implements Iterator<B> {
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

	public static <A, B> Iterator<B> iterMap(Iterator<A> it, Function<A, B> map) {
		return new IterMap<>(it, map);
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
			Assertions.Iters.hasNext(this);
			size--;
			return null;
		}

	}

	private static class NullListIterator<E> implements ListIterator<E> {

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
			Assertions.Iters.hasNext(this);
			idx++;
			return null;
		}

		@Override
		public boolean hasPrevious() {
			return idx > 0;
		}

		@Override
		public E previous() {
			Assertions.Iters.hasPrevious(this);
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

	public static <E> List<E> nullList(int size) {
		return new NullList<>(size);
	}

	private static class NullList<E> extends AbstractList<E> {

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

	@FunctionalInterface
	public static interface BiInt2IntFunc {
		int apply(int a1, int a2);
	}

	@FunctionalInterface
	public static interface BiInt2LongFunc {
		long apply(int a1, int a2);
	}

	@FunctionalInterface
	public static interface BiInt2ObjFunc<R> {
		R apply(int a1, int a2);
	}

	@FunctionalInterface
	public static interface IntDoubleConsumer {
		void accept(int a1, double a2);
	}

	public static void sort(int[] arr, int from, int to, IntComparator cmp, boolean parallel) {
		if (parallel) {
			IntArrays.parallelQuickSort(arr, from, to, cmp);
		} else {
			IntArrays.quickSort(arr, from, to, cmp);
		}
	}

	public static ForkJoinPool getPool() {
		ForkJoinPool current = ForkJoinTask.getPool();
		return current == null ? ForkJoinPool.commonPool() : current;
	}

	public static <ExecT extends Runnable & Serializable> RecursiveAction recursiveAction(ExecT exec) {
		return new RecursiveActionFromRunnable<>(exec);
	}

	public static <V, ExecT extends Supplier<? extends V> & Serializable> RecursiveTask<V> recursiveTask(ExecT exec) {
		return new RecursiveTaskFromSupplier<>(exec);
	}

	private static class RecursiveActionFromRunnable<ExecT extends Runnable & Serializable> extends RecursiveAction {
		private ExecT exec;
		private static final long serialVersionUID = 1L;

		RecursiveActionFromRunnable(ExecT exec) {
			this.exec = exec;
		}

		@Override
		protected void compute() {
			exec.run();
		}
	}

	private static class RecursiveTaskFromSupplier<V, ExecT extends Supplier<? extends V> & Serializable>
			extends RecursiveTask<V> {
		private ExecT exec;
		private static final long serialVersionUID = 1L;

		RecursiveTaskFromSupplier(ExecT exec) {
			this.exec = exec;
		}

		@Override
		protected V compute() {
			return exec.get();
		}
	}

	private static class LabeledObj {
		private final String s;

		LabeledObj(String label) {
			this.s = Objects.requireNonNull(label);
		}

		@Override
		public String toString() {
			return s;
		}
	}

	public static Object labeledObj(String label) {
		return new LabeledObj(label);
	}

	public static IWeightFunction potentialWeightFunc(IndexGraph g, IWeightFunction w, double[] potential) {
		return e -> w.weight(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	public static IWeightFunctionInt potentialWeightFunc(IndexGraph g, IWeightFunctionInt w, int[] potential) {
		return e -> w.weightInt(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	@SuppressWarnings("rawtypes")
	private static final Consumer ConsumerNoOp = x -> {
	};

	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> consumerNoOp() {
		return ConsumerNoOp;
	}

	private static class Int2IntMapEmptyWithDefVal extends AbstractInt2IntMap {

		private static final long serialVersionUID = 1L;

		private final int defVal;

		Int2IntMapEmptyWithDefVal(int defVal) {
			this.defVal = defVal;
		}

		@Override
		public int get(int key) {
			return defVal;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {}

		@Override
		public void defaultReturnValue(int rv) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int defaultReturnValue() {
			return defVal;
		}

		@Override
		public int remove(int key) {
			return defVal;
		}

		@Override
		public ObjectSet<Int2IntMap.Entry> int2IntEntrySet() {
			return ObjectSets.emptySet();
		}

		@Override
		public IntSet keySet() {
			return IntSets.emptySet();
		}

		@Override
		public IntCollection values() {
			return IntSets.emptySet();
		}

		@Override
		public boolean containsKey(int key) {
			return false;
		}

		@Override
		public boolean containsValue(int value) {
			return false;
		}
	}

	public static final Int2IntMap EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE = new Int2IntMapEmptyWithDefVal(-1);

	private static int lowerBound(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len >> 1;
			int mid = from + half;
			if (idx2key.applyAsInt(mid) < key) {
				from = mid + 1;
				len = len - half - 1;
			} else {
				len = half;
			}
		}
		return from;
	}

	private static int upperBound(int to, int from, int key, IntUnaryOperator idx2key) {
		for (int len = from - to; len > 0;) {
			int half = len >> 1;
			int mid = to + half;
			if (key < idx2key.applyAsInt(mid)) {
				len = half;
			} else {
				to = mid + 1;
				len = len - half - 1;
			}
		}
		return to;
	}

	public static IntIntPair equalRange(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len >> 1;
			int mid = from + half;
			int midKey = idx2key.applyAsInt(mid);
			if (midKey < key) {
				from = mid + 1;
				len = len - half - 1;
			} else if (key < midKey) {
				len = half;
			} else {
				int left = lowerBound(from, mid, key, idx2key);
				from += len;
				int right = upperBound(++mid, from, key, idx2key);
				return IntIntPair.of(left, right);
			}
		}
		return null;
	}

	public static class Variant {

		final Object val;

		private Variant(Object val) {
			this.val = Objects.requireNonNull(val);
		}

		public boolean contains(Class<?> type) {
			return type.isInstance(val);
		}

		@SuppressWarnings("unchecked")
		public <E> Optional<E> get(Class<E> type) {
			return contains(type) ? Optional.of((E) val) : Optional.empty();
		}

		@SuppressWarnings("unused")
		public static class Of2<A, B> extends Variant {

			private Of2(Object val) {
				super(val);
			}

			public static <A, B> Variant.Of2<A, B> withA(A val) {
				return new Variant.Of2<>(val);
			}

			public static <A, B> Variant.Of2<A, B> withB(B val) {
				return new Variant.Of2<>(val);
			}

		}

	}

	public static long longPack(int low, int high) {
		return ((high & 0xffffffffL) << 32) | ((low & 0xffffffffL) << 0);
	}

	public static int long2low(long val) {
		return (int) ((val >> 0) & 0xffffffffL);
	}

	public static int long2high(long val) {
		return (int) ((val >> 32) & 0xffffffffL);
	}

	public static <T> void clearAllUnsafe(T[] arr, IntCollection nonNullIndices) {
		/* TODO: need to benchmark when its better to clear each entry independently */
		if (nonNullIndices.size() < arr.length / 8) {
			for (int v : nonNullIndices)
				arr[v] = null;
			assert Arrays.stream(arr).allMatch(Objects::isNull);
		} else {
			Arrays.fill(arr, null);
		}
	}

}
