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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

public class Permutations {

	private Permutations() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Iterable<List<T>> of(Collection<? extends T> l) {
		if (l instanceof IntCollection) {
			return (Iterable) of((IntCollection) l);
		} else {
			Objects.requireNonNull(l);
			return () -> new PermutationsIter(l);
		}
	}

	public static Iterable<IntList> of(IntCollection l) {
		Objects.requireNonNull(l);
		return () -> new PermutationsIterInt(l);
	}

	public static Stream<IntList> stream(IntCollection l) {
		return StreamSupport.stream(Permutations.of(l).spliterator(), false);
	}

	private static class PermutationsIter<T> implements Iterator<List<T>> {

		private final T[] elements;
		private final int n;

		private final int[] indexes;
		private int i;

		private final List<T> next;
		private boolean nextValid;

		@SuppressWarnings("unchecked")
		PermutationsIter(Collection<T> l) {
			elements = (T[]) l.toArray();
			n = elements.length;
			indexes = new int[n];
			next = new ObjectImmutableList<>(elements);
			nextValid = n > 0;
		}

		@Override
		public boolean hasNext() {
			if (!nextValid) {
				for (; i < n; i++) {
					if (indexes[i] < i) {
						ObjectArrays.swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
						indexes[i]++;
						i = 0;
						nextValid = true;
						break;
					}
					indexes[i] = 0;
				}
			}
			return nextValid;
		}

		@Override
		public List<T> next() {
			if (!hasNext())
				throw new NoSuchElementException(Assertions.ERR_NO_NEXT);
			nextValid = false;
			return next;
		}
	}

	private static class PermutationsIterInt implements Iterator<IntList> {
		private final int[] elements;
		private final int n;

		private final int[] indexes;
		private int i;

		private final IntList next;
		private boolean nextValid;

		public PermutationsIterInt(IntCollection l) {
			elements = l.toIntArray();
			n = elements.length;
			indexes = new int[n];
			next = new IntImmutableList(elements);
			nextValid = n > 0;
		}

		@Override
		public boolean hasNext() {
			if (!nextValid) {
				for (; i < n; i++) {
					if (indexes[i] < i) {
						IntArrays.swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
						indexes[i]++;
						i = 0;
						nextValid = true;
						break;
					}
					indexes[i] = 0;
				}
			}
			return nextValid;
		}

		@Override
		public IntList next() {
			if (!hasNext())
				throw new NoSuchElementException(Assertions.ERR_NO_NEXT);
			nextValid = false;
			return next;
		}
	}

}
