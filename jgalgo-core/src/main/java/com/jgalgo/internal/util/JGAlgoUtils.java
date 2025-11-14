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
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

public class JGAlgoUtils {
	private JGAlgoUtils() {}

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

	public static void sort(int[] arr, boolean parallel) {
		sort(arr, 0, arr.length, parallel);
	}

	public static void sort(int[] arr, int from, int to, boolean parallel) {
		if (parallel) {
			IntArrays.parallelQuickSort(arr, from, to);
		} else {
			IntArrays.quickSort(arr, from, to);
		}
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

	public static int objIterSkip(Iterator<?> it, int n) {
		if (it instanceof ObjectIterator) {
			return ((ObjectIterator<?>) it).skip(n);
		} else {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			int i = n;
			while (i-- != 0 && it.hasNext())
				it.next();
			return n - i - 1;
		}
	}

	public static int objIterBack(ListIterator<?> it, int n) {
		if (it instanceof ObjectListIterator) {
			return ((ObjectListIterator<?>) it).back(n);
		} else {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			int i = n;
			while (i-- != 0 && it.hasPrevious())
				it.previous();
			return n - i - 1;
		}
	}
}
