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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

@SuppressWarnings("boxing")
public class IntAdaptersTest extends TestBase {

	@Test
	public void asIntIterator() {
		/* wrapper of a real IntIterator should not be created */
		IntIterator intIter0 = IntList.of(1, 3, 5, 9).iterator();
		assertTrue(intIter0 == IntAdapters.asIntIterator(intIter0));

		List<Integer> objList = new ArrayList<>(IntList.of(5, 7, 9, 3, 4, 86));

		/* hasNext() and next() */
		Iterator<Integer> objIter = objList.iterator();
		IntIterator intIter = IntAdapters.asIntIterator(objList.iterator());
		assertEqualsIters(objIter, intIter);

		/* remove() */
		intIter = IntAdapters.asIntIterator(objList.iterator());
		intIter.nextInt();
		intIter.nextInt();
		intIter.remove();
		intIter.nextInt();
		intIter.remove();
		assertEquals(objList, IntList.of(5, 3, 4, 86));
	}

	private static void assertEqualsIters(Iterator<Integer> objIter, IntIterator intIter) {
		while (objIter.hasNext() && intIter.hasNext())
			assertEquals(objIter.next(), intIter.nextInt());
		assertEqualsBool(objIter.hasNext(), intIter.hasNext());
	}

	@Test
	public void asIntIterable() {
		/* wrapper of a real IntIterable should not be created */
		IntIterable intIter0 = IntList.of(1, 3, 5, 9);
		assertTrue(intIter0 == IntAdapters.asIntIterable(intIter0));

		Iterable<Integer> objIterable = () -> List.of(5, 7, 9, 3, 4, 86).iterator();
		IntIterable intIterable = IntAdapters.asIntIterable(objIterable);

		/* iterator() */
		for (int i = 0; i < 5; i++)
			assertEqualsIters(objIterable.iterator(), intIterable.iterator());

		/* a wrapper of a collection should be a collection, not a plain iterable */
		assertTrue(IntAdapters.asIntIterable(List.of(0, 1, 2, 3)) instanceof List);
	}

	@Test
	public void asIntCollection() {
		/* wrapper of a real IntCollection should not be created */
		IntList intList = IntList.of(1, 3, 5, 9);
		assertTrue(intList == IntAdapters.asIntCollection(intList));

		asIntCollectionAbstract(s -> copyToRealCollection(s));

		/* a wrapper of a set/list should be a set/list, not a plain collection */
		assertTrue(IntAdapters.asIntCollection(List.of(0, 1, 2, 3)) instanceof List);
		assertTrue(IntAdapters.asIntCollection(Set.of(0, 1, 2, 3)) instanceof Set);
	}

	@Test
	public void asIntSet() {
		/* wrapper of a real IntSet should not be created */
		IntSet intSet = IntSet.of(1, 3, 5, 9);
		assertTrue(intSet == IntAdapters.asIntSet(intSet));

		asIntCollectionAbstract(s -> new HashSet<>(s));
	}

	@Test
	public void asIntList() {
		final long seed = 0xb02a1879fc1fbc04L;
		final Random rand = new Random(seed);

		/* wrapper of a real IntSet should not be created */
		IntList intList0 = IntList.of(1, 3, 5, 9);
		assertTrue(intList0 == IntAdapters.asIntList(intList0));

		asIntCollectionAbstract(s -> new ArrayList<>(s));

		List<Integer> objList = new ArrayList<>(IntSet.of(5, 7, 9, 3, 4, 86));
		objList.addAll(objList); /* duplicate so indexOf() != lastIndexOf() */
		IntList intList = IntAdapters.asIntList(objList);

		/* listIterator() */
		ListIterator<Integer> objIter = objList.listIterator();
		IntListIterator intIter = intList.listIterator();
		while (objIter.hasNext() && intIter.hasNext()) {
			assertEquals(objIter.nextIndex(), intIter.nextIndex());
			assertEquals(objIter.next().intValue(), intIter.nextInt());
		}
		assertEqualsBool(objIter.hasNext(), intIter.hasNext());
		while (objIter.hasPrevious() && intIter.hasPrevious()) {
			assertEquals(objIter.previousIndex(), intIter.previousIndex());
			assertEquals(objIter.previous().intValue(), intIter.previousInt());
		}
		assertEqualsBool(objIter.hasPrevious(), intIter.hasPrevious());

		/* get(index) */
		for (int i : range(objList.size()))
			assertEquals(objList.get(i).intValue(), intList.getInt(i));

		/* set(index) */
		int sizeBeforeSet = intList.size();
		int valBeforeSetExpected = intList.getInt(2);
		int valAfterSetExpected = valBeforeSetExpected + 1;
		int valBeforeSetActual = intList.set(2, valAfterSetExpected);
		assertEquals(valBeforeSetExpected, valBeforeSetActual);
		assertEquals(valAfterSetExpected, intList.getInt(2));
		assertEquals(sizeBeforeSet, intList.size());

		/* indexOf() */
		for (Integer x : objList)
			assertEquals(objList.indexOf(x), intList.indexOf(x.intValue()));
		for (int i = 0; i < 10; i++) {
			int nonExistingElm = rand.nextInt();
			if (objList.contains(Integer.valueOf(nonExistingElm)))
				continue;
			assertEquals(-1, intList.indexOf(nonExistingElm));
		}

		/* lastIndexOf() */
		for (Integer x : objList)
			assertEquals(objList.lastIndexOf(x), intList.lastIndexOf(x.intValue()));
		for (int i = 0; i < 10; i++) {
			int nonExistingElm = rand.nextInt();
			if (objList.contains(Integer.valueOf(nonExistingElm)))
				continue;
			assertEquals(-1, intList.lastIndexOf(nonExistingElm));
		}

		/* remove(index) */
		IntList expected = new IntArrayList(objList);
		int removedElementExpected = expected.removeInt(3);
		int removedElementActual = intList.removeInt(3);
		assertEquals(removedElementExpected, removedElementActual);
		assertEquals(expected, intList);
	}

	private static void asIntCollectionAbstract(Function<Set<Integer>, Collection<Integer>> createCollection) {
		Collection<Integer> objColl = createCollection.apply(Set.of(5, 7, 9, 3, 4, 86));
		IntCollection intColl = IntAdapters.asIntCollection(objColl);

		/* size() */
		assertEquals(objColl.size(), intColl.size());
		/* isEmpty() */
		assertEqualsBool(objColl.isEmpty(), intColl.isEmpty());

		/* iterator() */
		for (int i = 0; i < 5; i++)
			assertEqualsIters(objColl.iterator(), intColl.iterator());

		/* contains() */
		assertTrue(intColl.contains(5));
		assertTrue(intColl.contains(86));
		assertFalse(intColl.contains(-5));

		/* containsAll() */
		assertTrue(intColl.containsAll(IntList.of(3, 4, 5)));
		assertTrue(intColl.containsAll(List.of(3, 4, 5)));
		assertFalse(intColl.containsAll(List.of(3, 51, 5)));

		/* toArray() */
		assertArrayEquals(objColl.toArray(), intColl.toArray());
		assertArrayEquals(objColl.toArray(new Integer[0]), intColl.toArray(new Integer[0]));
		assertArrayEquals(objColl.toArray(new Integer[100]), intColl.toArray(new Integer[100]));

		/* add() */
		int sizeBeforeAdd = intColl.size();
		boolean isAdded = intColl.add(999);
		assertTrue(isAdded);
		assertEquals(sizeBeforeAdd + 1, intColl.size());
		assertTrue(intColl.contains(999));

		/* addAll() */
		int sizeBeforeAddAll1 = intColl.size();
		boolean isAddedAll1 = intColl.addAll(IntList.of(-1, -2));
		assertTrue(isAddedAll1);
		assertEquals(sizeBeforeAddAll1 + 2, intColl.size());
		assertTrue(intColl.containsAll(IntList.of(-1, -2)));
		int sizeBeforeAddAll2 = intColl.size();
		boolean isAddedAll2 = intColl.addAll(List.of(-3, -4));
		assertTrue(isAddedAll2);
		assertEquals(sizeBeforeAddAll2 + 2, intColl.size());
		assertTrue(intColl.containsAll(List.of(-3, -4)));

		/* remove() */
		int sizeBeforeRemove = intColl.size();
		boolean isRemoved = intColl.rem(999);
		assertTrue(isRemoved);
		assertEquals(sizeBeforeRemove - 1, intColl.size());
		assertFalse(intColl.contains(999));

		/* removeAll() */
		int sizeBeforeRemoveAll1 = intColl.size();
		boolean isRemoveAll1 = intColl.removeAll(IntList.of(-1, -2));
		assertTrue(isRemoveAll1);
		assertEquals(sizeBeforeRemoveAll1 - 2, intColl.size());
		assertFalse(intColl.contains(-1));
		assertFalse(intColl.contains(-2));
		int sizeBeforeRemoveAll2 = intColl.size();
		boolean isRemoveAll2 = intColl.removeAll(List.of(-3, -4));
		assertTrue(isRemoveAll2);
		assertEquals(sizeBeforeRemoveAll2 - 2, intColl.size());
		assertFalse(intColl.contains(-3));
		assertFalse(intColl.contains(-4));

		/* retainAll() */
		IntSet retainElements1 = new IntOpenHashSet(intColl);
		intColl.addAll(IntList.of(-5, -7, -99));
		int sizeBeforeRetainAll1 = intColl.size();
		boolean isRetain1 = intColl.retainAll(retainElements1);
		assertTrue(isRetain1);
		assertEquals(sizeBeforeRetainAll1 - 3, intColl.size());
		Set<Integer> retainElements2 = new HashSet<>(intColl);
		intColl.addAll(IntList.of(-5, -7, -99));
		int sizeBeforeRetainAll2 = intColl.size();
		boolean isRetain2 = intColl.retainAll(new HashSet<>(retainElements2));
		assertTrue(isRetain2);
		assertEquals(sizeBeforeRetainAll2 - 3, intColl.size());

		/* clear() */
		intColl.clear();
		assertTrue(intColl.isEmpty());
		assertTrue(objColl.isEmpty());
		assertEquals(0, intColl.size());
		assertFalse(intColl.iterator().hasNext());
		objColl = createCollection.apply(Set.of(5, 7, 9, 3, 4, 86));
		intColl = IntAdapters.asIntCollection(objColl);
		objColl.clear();
		assertTrue(intColl.isEmpty());
		assertTrue(objColl.isEmpty());
	}

	private static <T> Collection<T> copyToRealCollection(Collection<T> collection) {
		List<T> l = new ArrayList<>(collection);
		return new Collection<>() {

			@Override
			public int size() {
				return l.size();
			}

			@Override
			public boolean isEmpty() {
				return l.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				return l.contains(o);
			}

			@Override
			public Iterator<T> iterator() {
				return l.iterator();
			}

			@Override
			public Object[] toArray() {
				return l.toArray();
			}

			@Override
			public <T2> T2[] toArray(T2[] a) {
				return l.toArray(a);
			}

			@Override
			public boolean add(T e) {
				return l.add(e);
			}

			@Override
			public boolean remove(Object o) {
				return l.remove(o);
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return l.containsAll(c);
			}

			@Override
			public boolean addAll(Collection<? extends T> c) {
				return l.addAll(c);
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
			public String toString() {
				return l.toString();
			}
		};
	}

	@Test
	public void asIntPredicate() {
		final long seed = 0x8bfb576afeff41cbL;
		final Random rand = new Random(seed);

		it.unimi.dsi.fastutil.ints.IntPredicate intPred0 = x -> true;
		assertTrue(intPred0 == IntAdapters.asIntPredicate(intPred0));

		Predicate<Integer> objPred = x -> x.intValue() % 2 == 0;
		IntPredicate intPred = IntAdapters.asIntPredicate(objPred);

		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt();
			assertEqualsBool(objPred.test(Integer.valueOf(x)), intPred.test(x));
		}
	}

	@Test
	public void asIntUnaryOperator() {
		final long seed = 0x73077595d7e3428eL;
		final Random rand = new Random(seed);

		class UnaryOp implements ToIntFunction<Integer>, IntUnaryOperator {
			@Override
			public int applyAsInt(int operand) {
				return operand + 1;
			}

			@Override
			public int applyAsInt(Integer value) {
				return applyAsInt(value.intValue());
			}
		};
		ToIntFunction<Integer> intOp0 = new UnaryOp();
		assertTrue(intOp0 == IntAdapters.asIntUnaryOperator(intOp0));

		ToIntFunction<Integer> objOp = x -> x.intValue() + 1;
		IntUnaryOperator intOp = IntAdapters.asIntUnaryOperator(objOp);

		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt();
			assertEquals(objOp.applyAsInt(Integer.valueOf(x)), intOp.applyAsInt(x));
		}
	}

	@Test
	public void asIntSupplier() {
		final long seed = 0xe8f716168daa3d1bL;
		final Random rand = new Random(seed);

		class SupplierImpl implements Supplier<Integer>, IntSupplier {
			@Override
			public int getAsInt() {
				return 7;
			}

			@Override
			public Integer get() {
				return Integer.valueOf(getAsInt());
			}
		}
		Supplier<Integer> intSup0 = new SupplierImpl();
		assertTrue(intSup0 == IntAdapters.asIntSupplier(intSup0));

		AtomicInteger supplierVal = new AtomicInteger();
		Supplier<Integer> objSup = () -> Integer.valueOf(supplierVal.get());
		IntSupplier intSup = IntAdapters.asIntSupplier(objSup);

		for (int i = 0; i < 20; i++) {
			supplierVal.set(rand.nextInt());
			assertEquals(objSup.get().intValue(), intSup.getAsInt());
		}
	}

	@Test
	public void asIntBiOperator() {
		final long seed = 0xf0c48dbcb15e1caaL;
		final Random rand = new Random(seed);

		it.unimi.dsi.fastutil.ints.IntBinaryOperator biOp0 = (x, y) -> x + y;
		assertTrue(biOp0 == IntAdapters.asIntBiOperator(biOp0));

		BiFunction<Integer, Integer, Integer> objBiOp = (x, y) -> Integer.valueOf(x.intValue() + y.intValue());
		IntBinaryOperator intBiOp = IntAdapters.asIntBiOperator(objBiOp);

		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt();
			int y = rand.nextInt();
			assertEquals(objBiOp.apply(Integer.valueOf(x), Integer.valueOf(y)), intBiOp.applyAsInt(x, y));
		}
	}

}
