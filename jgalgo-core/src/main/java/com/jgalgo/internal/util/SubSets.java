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

import java.util.Collection;
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
	public static <T> Iterable<List<T>> of(Collection<? extends T> set, int k) {
		if (set instanceof IntCollection) {
			return (Iterable) SubSets.of((IntCollection) set, k);
		} else {
			checkSubsetsSize(set, k);
			return () -> new SubSetsIter<>(set, k);
		}
	}

	public static Iterable<IntList> of(IntCollection set, int k) {
		checkSubsetsSize(set, k);
		return () -> new SubSetsIterInt(set, k);
	}

	public static Stream<IntList> stream(IntCollection set, int k) {
		return StreamSupport.stream(SubSets.of(set, k).spliterator(), false);
	}

	private static void checkSubsetsSize(Collection<?> set, int k) {
		if (!(0 <= k && k <= set.size()))
			throw new IllegalArgumentException("k must be in [0, set.size()]");
	}

	private static class SubSetsIterBase {

		final int[] subset;
		final int k;

		int nextDeviationIdx;
		boolean hasNext;

		SubSetsIterBase(int n, int k) {
			this.subset = new int[k];
			this.k = k;

			for (int i = 0; i < k; i++)
				subset[i] = n - k + i;
			nextDeviationIdx = k;
			hasNext = k > 0;
		}

		public boolean hasNext() {
			return hasNext;
		}

		void advance() {
			for (int i = 0; i < k; i++) {
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

	private static class SubSetsIter<T> extends SubSetsIterBase implements Iterator<List<T>> {

		private final T[] set;
		private final T[] next;
		private final List<T> nextList;

		@SuppressWarnings("unchecked")
		SubSetsIter(Collection<? extends T> set, int k) {
			super(set.size(), k);
			this.set = (T[]) set.toArray();
			next = (T[]) new Object[k];
			nextList = new ObjectImmutableList<>(next);
		}

		@Override
		public List<T> next() {
			Assertions.hasNext(this);
			for (int i = 0; i < nextDeviationIdx; i++)
				next[i] = set[subset[i]];
			advance();
			return nextList;
		}
	}

	private static class SubSetsIterInt extends SubSetsIterBase implements Iterator<IntList> {

		private final int[] set;
		private final int[] next;
		private final IntList nextList;

		SubSetsIterInt(IntCollection set, int k) {
			super(set.size(), k);
			this.set = set.toIntArray();
			next = new int[k];
			nextList = new IntImmutableList(next);
		}

		@Override
		public IntList next() {
			Assertions.hasNext(this);
			for (int i = 0; i < nextDeviationIdx; i++)
				next[i] = set[subset[i]];
			advance();
			return nextList;
		}
	}

}
