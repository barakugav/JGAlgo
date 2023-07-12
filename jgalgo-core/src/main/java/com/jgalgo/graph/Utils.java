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

package com.jgalgo.graph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

class Utils {
	private Utils() {}

	/* syntax sugar to iterator for loops */
	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {
			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	/* syntax sugar to iterator for loops */
	static IntIterable iterable(IntIterator it) {
		return new IntIterable() {
			@Override
			public IntIterator iterator() {
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

	static int size(IntIterable c) {
		int count = 0;
		for (IntIterator it = c.iterator(); it.hasNext(); it.nextInt())
			count++;
		return count;
	}

	static class Obj {
		private final String s;

		Obj(String s) {
			this.s = Objects.requireNonNull(s);
		}

		@Override
		public String toString() {
			return s;
		}
	}

	@SuppressWarnings("rawtypes")
	private static final Consumer ConsumerNoOp = x -> {
	};

	@SuppressWarnings("unchecked")
	static <T> Consumer<T> consumerNoOp() {
		return ConsumerNoOp;
	}

	private static class Int2IntMapEmptyWithDefVal extends AbstractInt2IntMap {

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

	static final Int2IntMap EMPTY_INT2INT_MAP_DEFVAL_NEG1 = new Int2IntMapEmptyWithDefVal(-1);

}
