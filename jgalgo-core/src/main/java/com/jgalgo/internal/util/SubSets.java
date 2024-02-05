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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

public class SubSets {

	private SubSets() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Iterable<List<T>> of(Collection<? extends T> set) {
		if (set instanceof IntCollection) {
			return (Iterable) SubSets.of((IntCollection) set);
		} else {
			return () -> new Iterator<>() {

				int k = 1;
				final int maxK = set.size();
				Iterator<List<T>> iter = maxK == 0 ? Collections.emptyIterator() : new SubSetsKIter(set, k);

				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public List<T> next() {
					List<T> ret = iter.next();
					if (!iter.hasNext() && k < maxK)
						iter = new SubSetsKIter(set, ++k);
					return ret;
				}
			};
		}
	}

	public static Iterable<IntList> of(IntCollection set) {
		return () -> new Iterator<>() {

			int k = 1;
			final int maxK = set.size();
			Iterator<IntList> iter = maxK == 0 ? Collections.emptyIterator() : new SubSetsKIterInt(set, k);

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public IntList next() {
				IntList ret = iter.next();
				if (!iter.hasNext() && k < maxK)
					iter = new SubSetsKIterInt(set, ++k);
				return ret;
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Iterable<List<T>> of(Collection<? extends T> set, int k) {
		if (set instanceof IntCollection) {
			return (Iterable) SubSets.of((IntCollection) set, k);
		} else {
			checkSubsetsSize(set, k);
			return () -> new SubSetsKIter<>(set, k);
		}
	}

	public static Iterable<IntList> of(IntCollection set, int k) {
		checkSubsetsSize(set, k);
		return () -> new SubSetsKIterInt(set, k);
	}

	public static <T> Stream<List<T>> stream(Collection<T> set) {
		return StreamSupport.stream(SubSets.of(set).spliterator(), false);
	}

	public static <T> Stream<List<T>> stream(Collection<T> set, int k) {
		return StreamSupport.stream(SubSets.of(set, k).spliterator(), false);
	}

	public static Stream<IntList> stream(IntCollection set) {
		return StreamSupport.stream(SubSets.of(set).spliterator(), false);
	}

	public static Stream<IntList> stream(IntCollection set, int k) {
		return StreamSupport.stream(SubSets.of(set, k).spliterator(), false);
	}

	private static void checkSubsetsSize(Collection<?> set, int k) {
		if (!(0 <= k && k <= set.size()))
			throw new IllegalArgumentException("k must be in [0, set.size()]");
	}

	private static class SubSetsKIterBase {

		final int[] subset;
		final int k;

		int nextDeviationIdx;
		boolean hasNext;

		SubSetsKIterBase(int n, int k) {
			this.subset = new int[k];
			this.k = k;

			for (int i : range(k))
				subset[i] = n - k + i;
			nextDeviationIdx = k;
			hasNext = k > 0;
		}

		public boolean hasNext() {
			return hasNext;
		}

		void advance() {
			for (int i : range(k)) {
				if (subset[i] > i) {
					nextDeviationIdx = i + 1;
					subset[i]--;
					while (i-- > 0)
						subset[i] = subset[i + 1] - 1;
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}
	}

	private static class SubSetsKIter<T> extends SubSetsKIterBase implements Iterator<List<T>> {

		private final T[] set;
		private final T[] next;
		private final List<T> nextList;

		@SuppressWarnings("unchecked")
		SubSetsKIter(Collection<? extends T> set, int k) {
			super(set.size(), k);
			this.set = (T[]) set.toArray();
			next = (T[]) new Object[k];
			nextList = new ObjectImmutableList<>(next);
		}

		@Override
		public List<T> next() {
			Assertions.hasNext(this);
			for (int i : range(nextDeviationIdx))
				next[i] = set[subset[i]];
			advance();
			return nextList;
		}
	}

	private static class SubSetsKIterInt extends SubSetsKIterBase implements Iterator<IntList> {

		private final int[] set;
		private final int[] next;
		private final IntList nextList;

		SubSetsKIterInt(IntCollection set, int k) {
			super(set.size(), k);
			this.set = set.toIntArray();
			next = new int[k];
			nextList = IntImmutableList.of(next);
		}

		@Override
		public IntList next() {
			Assertions.hasNext(this);
			for (int i : range(nextDeviationIdx))
				next[i] = set[subset[i]];
			advance();
			return nextList;
		}
	}

}
