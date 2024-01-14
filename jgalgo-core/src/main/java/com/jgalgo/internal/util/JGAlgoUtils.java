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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

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
			Assertions.hasNext(this);
			size--;
			return null;
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

	@SuppressWarnings("rawtypes")
	private static final Consumer ConsumerNoOp = x -> {
	};

	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> consumerNoOp() {
		return ConsumerNoOp;
	}

	public static int lowerBound(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		return lowerBound0(from, to, key, idx2key);
	}

	private static int lowerBound0(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len / 2;
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

	public static int upperBound(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		return upperBound0(from, to, key, idx2key);
	}

	private static int upperBound0(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len >> 1;
			int mid = from + half;
			if (key < idx2key.applyAsInt(mid)) {
				len = half;
			} else {
				from = mid + 1;
				len = len - half - 1;
			}
		}
		return from;
	}

	public static IntIntPair equalRange(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		for (int len = to - from; len > 0;) {
			int half = len / 2;
			int mid = from + half;
			int midKey = idx2key.applyAsInt(mid);
			if (midKey < key) {
				from = mid + 1;
				len = len - half - 1;
			} else if (key < midKey) {
				len = half;
			} else {
				int left = lowerBound0(from, mid, key, idx2key);
				int right = upperBound0(mid + 1, from + len, key, idx2key);
				return IntIntPair.of(left, right);
			}
		}
		return null;
	}

	private static class Variant {

		final Object val;

		private Variant(Object val) {
			this.val = Objects.requireNonNull(val);
		}

		public boolean contains(Class<?> type) {
			return type.isInstance(val);
		}

		public <T> T get(Class<T> type) {
			return getOptional(type).get();
		}

		@SuppressWarnings("unchecked")
		public <T> Optional<T> getOptional(Class<T> type) {
			return contains(type) ? Optional.of((T) val) : Optional.empty();
		}
	}

	public static class Variant2<A, B> extends Variant {

		private final boolean isA;

		private Variant2(Object val, boolean isA) {
			super(val);
			this.isA = isA;
		}

		public static <A, B> Variant2<A, B> ofA(A val) {
			return new Variant2<>(val, true);
		}

		public static <A, B> Variant2<A, B> ofB(B val) {
			return new Variant2<>(val, false);
		}

		@SuppressWarnings("unchecked")
		public <R> R map(Function<A, R> a, Function<B, R> b) {
			if (isA) {
				return a.apply((A) val);
			} else {
				return b.apply((B) val);
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

	public static void clearAllUnsafe(Bitmap bitmap, IntCollection setBits) {
		/* TODO: need to benchmark when its better to clear each bit independently */
		boolean perBitClear = setBits.size() < bitmap.size / Bitmap.WordSize;
		if (perBitClear) {
			for (int idx : setBits)
				bitmap.clear(idx);
			assert bitmap.isEmpty();
		} else {
			bitmap.clear();
		}
	}

	public static <T> Iterator<T> queueIter(Collection<T> elements) {
		PriorityQueue<T> queue = new ObjectArrayFIFOQueue<>(elements.size());
		for (T elm : elements)
			queue.enqueue(elm);
		return dequeueIter(queue);
	}

	public static <T> Iterator<T> dequeueIter(PriorityQueue<T> queue) {
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public T next() {
				return queue.dequeue();
			}
		};
	}

}
