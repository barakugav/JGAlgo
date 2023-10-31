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
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class IntContainers {

	private IntContainers() {}

	public static IntIterator toIntIterator(Iterator<Integer> it) {
		Objects.requireNonNull(it);
		if (it instanceof IntIterator)
			return (IntIterator) it;
		return new IntIterator() {
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public int nextInt() {
				return it.next().intValue();
			}
		};
	}

	public static IntCollection toIntCollection(Collection<Integer> c) {
		Objects.requireNonNull(c);
		if (c instanceof IntCollection)
			return (IntCollection) c;
		return new AbstractIntCollection() {
			@Override
			public int size() {
				return c.size();
			}

			@Override
			public boolean isEmpty() {
				return c.isEmpty();
			}

			@Override
			public Object[] toArray() {
				return c.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return c.toArray(a);
			}

			@Override
			public boolean containsAll(Collection<?> c2) {
				return c.containsAll(c2);
			}

			@Override
			public boolean addAll(Collection<? extends Integer> c2) {
				return c.addAll(c2);
			}

			@Override
			public boolean removeAll(Collection<?> c2) {
				return c.removeAll(c2);
			}

			@Override
			public boolean retainAll(Collection<?> c2) {
				return c.retainAll(c2);
			}

			@Override
			public void clear() {
				c.clear();
			}

			@Override
			public IntIterator iterator() {
				return toIntIterator(c.iterator());
			}

			@Override
			public boolean add(int key) {
				return c.add(Integer.valueOf(key));
			}

			@Override
			public boolean contains(int key) {
				return c.contains(Integer.valueOf(key));
			}

			@Override
			public boolean rem(int key) {
				return c.remove(Integer.valueOf(key));
			}

			@Override
			public boolean addAll(IntCollection c2) {
				return c.addAll(c2);
			}

			@Override
			public boolean containsAll(IntCollection c2) {
				return c.containsAll(c2);
			}

			@Override
			public boolean removeAll(IntCollection c2) {
				return c.removeAll(c2);
			}

			@Override
			public boolean retainAll(IntCollection c2) {
				return c.retainAll(c2);
			}
		};
	}

	public static IntUnaryOperator toIntUnaryOperator(ToIntFunction<Integer> op) {
		if (op instanceof IntUnaryOperator) {
			return (IntUnaryOperator) op;
		} else {
			return v -> op.applyAsInt(Integer.valueOf(v));
		}
	}

}
