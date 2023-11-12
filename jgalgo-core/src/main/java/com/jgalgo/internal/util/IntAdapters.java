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
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntBinaryOperator;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntAdapters {

	private IntAdapters() {}

	public static IntIterator asIntIterator(Iterator<Integer> it) {
		if (it instanceof IntIterator) {
			return (IntIterator) it;
		} else {
			return new IntIteratorWrapper(it);
		}
	}

	public static IntIterable asIntIterable(Iterable<Integer> it) {
		if (it instanceof IntIterable) {
			return (IntIterable) it;
		} else {
			return new IntIterableWrapper(it);
		}
	}

	public static IntCollection asIntCollection(Collection<Integer> c) {
		if (c instanceof IntCollection) {
			return (IntCollection) c;
		} else if (c instanceof Set) {
			return new IntSetWrapper((Set<Integer>) c);
		} else if (c instanceof List) {
			return new IntListWrapper((List<Integer>) c);
		} else {
			return new IntCollectionWrapper(c);
		}
	}

	public static IntSet asIntSet(Set<Integer> c) {
		if (c instanceof IntSet) {
			return (IntSet) c;
		} else {
			return new IntSetWrapper(c);
		}
	}

	public static IntList asIntList(List<Integer> c) {
		if (c instanceof IntList) {
			return (IntList) c;
		} else {
			return new IntListWrapper(c);
		}
	}

	private static class IntIteratorWrapper implements IntIterator {
		private final Iterator<Integer> it;

		public IntIteratorWrapper(Iterator<Integer> it) {
			this.it = Objects.requireNonNull(it);
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextInt() {
			return it.next().intValue();
		}
	}

	private static class IntListIteratorWrapper implements IntListIterator {
		private final ListIterator<Integer> it;

		public IntListIteratorWrapper(ListIterator<Integer> it) {
			this.it = Objects.requireNonNull(it);
		}

		@Override
		public int previousInt() {
			return it.previous().intValue();
		}

		@Override
		public int nextInt() {
			return it.next().intValue();
		}

		@Override
		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextIndex() {
			return it.nextIndex();
		}

		@Override
		public int previousIndex() {
			return it.previousIndex();
		}
	}

	private static class IntIterableWrapper implements IntIterable {
		private final Iterable<Integer> it;

		public IntIterableWrapper(Iterable<Integer> it) {
			this.it = Objects.requireNonNull(it);
		}

		@Override
		public IntIterator iterator() {
			return new IntIteratorWrapper(it.iterator());
		}
	}

	private static class IntCollectionWrapper extends AbstractIntCollection {
		private final Collection<Integer> c;

		public IntCollectionWrapper(Collection<Integer> c) {
			this.c = Objects.requireNonNull(c);
		}

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
			return asIntIterator(c.iterator());
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
	}

	private static class IntSetWrapper extends AbstractIntSet {

		private final Set<Integer> s;

		public IntSetWrapper(Set<Integer> s) {
			this.s = Objects.requireNonNull(s);
		}

		@Override
		public boolean add(int key) {
			return s.add(Integer.valueOf(key));
		}

		@Override
		public boolean contains(int key) {
			return s.contains(Integer.valueOf(key));
		}

		@Override
		public boolean addAll(IntCollection c) {
			return s.addAll(c);
		}

		@Override
		public boolean containsAll(IntCollection c) {
			return s.containsAll(c);
		}

		@Override
		public boolean removeAll(IntCollection c) {
			return s.removeAll(c);
		}

		@Override
		public boolean retainAll(IntCollection c) {
			return s.retainAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Integer> c) {
			return s.addAll(c);
		}

		@Override
		public int size() {
			return s.size();
		}

		@Override
		public boolean isEmpty() {
			return s.isEmpty();
		}

		@Override
		public Object[] toArray() {
			return s.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return s.toArray(a);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return s.containsAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return s.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return s.removeAll(c);
		}

		@Override
		public void clear() {
			s.clear();
		}

		@Override
		public IntIterator iterator() {
			return asIntIterator(s.iterator());
		}

		@Override
		public boolean remove(int k) {
			return s.remove(Integer.valueOf(k));
		}
	}

	private static class IntListWrapper extends AbstractIntList {

		private final List<Integer> l;

		public IntListWrapper(List<Integer> l) {
			this.l = Objects.requireNonNull(l);
		}

		@Override
		public int size() {
			return l.size();
		}

		@Override
		public boolean addAll(IntCollection c) {
			return l.addAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Integer> c) {
			return l.addAll(c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends Integer> c) {
			return l.addAll(index, c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return l.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return l.retainAll(c);
		}

		@Override
		public void clear() {
			l.clear();
		}

		@Override
		public boolean contains(int key) {
			return l.contains(Integer.valueOf(key));
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return l.containsAll(c);
		}

		@Override
		public boolean containsAll(IntCollection c) {
			return l.containsAll(c);
		}

		@Override
		public boolean rem(int key) {
			return l.remove(Integer.valueOf(key));
		}

		@Override
		public boolean removeAll(IntCollection c) {
			return l.removeAll(c);
		}

		@Override
		public boolean retainAll(IntCollection c) {
			return l.retainAll(c);
		}

		@Override
		public IntListIterator listIterator(int index) {
			return new IntListIteratorWrapper(l.listIterator(index));
		}

		@Override
		public void add(int index, int key) {
			l.add(index, Integer.valueOf(key));
		}

		@Override
		public boolean addAll(int index, IntCollection c) {
			return l.addAll(index, c);
		}

		@Override
		public int set(int index, int k) {
			return l.set(index, Integer.valueOf(k)).intValue();
		}

		@Override
		public int getInt(int index) {
			return l.get(index).intValue();
		}

		@Override
		public int indexOf(int k) {
			return l.indexOf(Integer.valueOf(k));
		}

		@Override
		public int lastIndexOf(int k) {
			return l.lastIndexOf(Integer.valueOf(k));
		}

		@Override
		public int removeInt(int index) {
			return l.remove(index).intValue();
		}

	}

	public static IntPredicate asIntPredicate(Predicate<Integer> pred) {
		if (pred instanceof IntPredicate) {
			return (IntPredicate) pred;
		} else {
			return v -> pred.test(Integer.valueOf(v));
		}
	}

	public static IntUnaryOperator asIntUnaryOperator(ToIntFunction<Integer> op) {
		if (op instanceof IntUnaryOperator) {
			return (IntUnaryOperator) op;
		} else {
			return v -> op.applyAsInt(Integer.valueOf(v));
		}
	}

	public static IntSupplier asIntSupplier(Supplier<Integer> sup) {
		if (sup instanceof IntSupplier) {
			return (IntSupplier) sup;
		} else {
			return () -> sup.get().intValue();
		}
	}

	public static IntBinaryOperator asIntBiOperator(BiFunction<Integer, Integer, Integer> func) {
		if (func instanceof IntBinaryOperator) {
			return (IntBinaryOperator) func;
		} else {
			return (a, b) -> func.apply(Integer.valueOf(a), Integer.valueOf(b)).intValue();
		}
	}

	public static IntBinaryOperator asIntBiOperator(BinaryOperator<Integer> func) {
		if (func instanceof IntBinaryOperator) {
			return (IntBinaryOperator) func;
		} else {
			return (a, b) -> func.apply(Integer.valueOf(a), Integer.valueOf(b)).intValue();
		}
	}

}
