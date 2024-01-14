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

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.AbstractIntSortedSet;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSpliterator;

public final class Range extends AbstractIntSortedSet {

	private final int from, to;

	private Range(int from, int to) {
		if (to < from)
			to = from;
		this.from = from;
		this.to = to;
	}

	public static Range range(int to) {
		return new Range(0, to);
	}

	public static Range range(int from, int to) {
		return new Range(from, to);
	}

	@Override
	public int size() {
		return to - from;
	}

	@Override
	public boolean contains(int key) {
		return from <= key && key < to;
	}

	@Override
	public IntBidirectionalIterator iterator() {
		return new Iter(from, to);
	}

	@Override
	public IntBidirectionalIterator iterator(int fromElement) {
		int begin;
		if (fromElement < from) {
			begin = from;
		} else if (fromElement >= to) {
			begin = to;
		} else {
			begin = fromElement + 1;
		}
		return new Iter(from, to, begin);
	}

	@Override
	public Range subSet(int fromElement, int toElement) {
		if (fromElement > toElement)
			throw new IllegalArgumentException("fromElement(" + fromElement + ") > toElement(" + toElement + ")");
		return new Range(Math.max(from, fromElement), Math.min(toElement, to));
	}

	@Override
	public Range headSet(int toElement) {
		return new Range(from, Math.min(toElement, to));
	}

	@Override
	public Range tailSet(int fromElement) {
		return new Range(Math.max(from, fromElement), to);
	}

	@Override
	public IntComparator comparator() {
		return null;
	}

	@Override
	public int firstInt() {
		if (isEmpty())
			throw new NoSuchElementException();
		return from;
	}

	@Override
	public int lastInt() {
		if (isEmpty())
			throw new NoSuchElementException();
		return to - 1;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Range) {
			Range r = (Range) o;
			if (isEmpty())
				return r.isEmpty();
			return to == r.to && from == r.from;

		} else if (o instanceof IntSet) {
			IntSet s = (IntSet) o;
			int size = size();
			if (size != s.size())
				return false;
			if (size == 0)
				return true;
			int min, max;
			IntIterator it = s.iterator();
			min = max = it.nextInt();
			while (--size > 0) {
				int x = it.nextInt();
				if (max < x) {
					max = x;
				} else if (min > x) {
					min = x;
				}
			}
			return min == from && max == to - 1;

		} else {
			return super.equals(o);
		}
	}

	@Override
	public int hashCode() {
		/* hash code compatible with IntSet */
		return (from + to - 1) * (to - from) / 2;
	}

	public IntStream map(IntUnaryOperator mapper) {
		return intStream().map(mapper);
	}

	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		return intStream().mapToObj(mapper);
	}

	public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
		return intStream().mapToDouble(mapper);
	}

	public LongStream mapToLong(IntToLongFunction mapper) {
		return intStream().mapToLong(mapper);
	}

	public IntStream filter(IntPredicate predicate) {
		return intStream().filter(predicate);
	}

	public boolean allMatch(IntPredicate predicate) {
		return intStream().allMatch(predicate);
	}

	public boolean anyMatch(IntPredicate predicate) {
		return intStream().anyMatch(predicate);
	}

	@Override
	public IntSpliterator spliterator() {
		return new SplitIter(from, to);
	}

	private static class Iter implements IntBidirectionalIterator {

		final int from, to;
		int x;

		Iter(int from, int to, int startIdx) {
			assert from <= to;
			assert from <= startIdx && startIdx <= to;
			this.from = from;
			this.to = to;
			this.x = startIdx;
		}

		Iter(int from, int to) {
			this(from, to, from);
		}

		@Override
		public boolean hasNext() {
			return x < to;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			return x++;
		}

		@Override
		public boolean hasPrevious() {
			return from < x;
		}

		@Override
		public int previousInt() {
			Assertions.hasPrevious(this);
			return --x;
		}

		@Override
		public int skip(final int n) {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			if (n < to - x) {
				x += n;
				return n;
			} else {
				int begin = x;
				x = to;
				return x - begin;
			}
		}
	}

	private static class SplitIter implements IntSpliterator {

		private int x;
		private final int to;

		SplitIter(int from, int to) {
			assert from <= to;
			this.x = from;
			this.to = to;
		}

		@Override
		public int characteristics() {
			return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
					| Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.IMMUTABLE;
		}

		@Override
		public long estimateSize() {
			return to - x;
		}

		@Override
		public boolean tryAdvance(IntConsumer action) {
			if (x >= to)
				return false;
			action.accept(x++);
			return true;
		}

		@Override
		public IntSpliterator trySplit() {
			int size = to - x;
			if (size < 2)
				return null;
			int mid = x + size / 2;
			IntSpliterator prefix = new SplitIter(x, mid);
			x = mid;
			return prefix;
		}

		@Override
		public void forEachRemaining(final IntConsumer action) {
			for (; x < to; x++)
				action.accept(x);
		}

		@Override
		public long skip(long n) {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			if (n < to - x) {
				x += n;
				return n;
			} else {
				int begin = x;
				x = to;
				return x - begin;
			}
		}

		@Override
		public IntComparator getComparator() {
			return null;
		}
	}

	public IntList asList() {
		return new RangeList(from, to);
	}

	private static class RangeList extends AbstractIntList {

		private final int from, to;
		private int hash;

		RangeList(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public int size() {
			return to - from;
		}

		@Override
		public boolean contains(int key) {
			return from <= key && key < to;
		}

		@Override
		public int getInt(int index) {
			Assertions.checkArrayIndex(index, 0, size());
			return from + index;
		}

		@Override
		public IntListIterator listIterator(int index) {
			Assertions.checkArrayIndex(index, 0, size() + 1);
			return new ListIter(from, to, from + index);
		}

		@Override
		public int indexOf(int k) {
			return contains(k) ? k - from : -1;
		}

		@Override
		public int lastIndexOf(int k) {
			return indexOf(k); /* there are no duplicate elements in the list */
		}

		@Override
		public IntList subList(int from, int to) {
			Assertions.checkArrayFromTo(from, to, size());
			return new RangeList(this.from + from, this.from + to);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RangeList) {
				RangeList r = (RangeList) o;
				if (isEmpty())
					return r.isEmpty();
				return to == r.to && from == r.from;

			} else {
				return super.equals(o);
			}
		}

		@Override
		public int hashCode() {
			if (hash == 0)
				hash = super.hashCode();
			return hash;
		}

		@Override
		public IntSpliterator spliterator() {
			return new SplitIter(from, to);
		}
	}

	private static class ListIter extends Iter implements IntListIterator {

		ListIter(int from, int to, int startVal) {
			super(from, to, startVal);
		}

		@Override
		public int nextIndex() {
			return x - from;
		}

		@Override
		public int previousIndex() {
			return x - from - 1;
		}
	}

}
