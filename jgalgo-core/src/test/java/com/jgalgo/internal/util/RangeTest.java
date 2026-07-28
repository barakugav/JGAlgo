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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RangeTest extends TestBase {

	@Test
	public void negativeTo() {
		assertTrue(range(-1).isEmpty());
	}

	@Test
	public void fromGreaterThanTo() {
		assertTrue(range(1, 0).isEmpty());
	}

	@Test
	public void iterator() {
		final Random rand = new Random(0xad7d4caf6c2df7daL);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			int expected = 0;
			for (int v : range) {
				assertEquals(expected, v);
				expected++;
			}
			assertEquals(to, expected);
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			int expected = from;
			for (int v : range) {
				assertEquals(expected, v);
				expected++;
			}
			assertEquals(to, expected);
		}

		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			int nextExpected = 0;
			IntList expected = new IntArrayList();
			IntList actual = new IntArrayList();
			for (IntIterator it = range.iterator(); nextExpected < to;) {
				assertTrue(it.hasNext());
				if (rand.nextBoolean()) {
					expected.add(nextExpected++);
					actual.add(it.nextInt());
				} else {
					int skipSize = rand.nextInt(8);
					int skipExpected = Math.min(skipSize, to - nextExpected);
					int skipActual = it.skip(skipSize);
					assertEquals(skipExpected, skipActual);
					nextExpected += skipExpected;
				}
			}
			assertEquals(expected, actual);
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			int prevExpected = to - 1;
			IntList expected = new IntArrayList();
			IntList actual = new IntArrayList();
			for (IntListIterator it = range.asList().listIterator(to); prevExpected >= 0;) {
				assertTrue(it.hasPrevious());
				if (rand.nextBoolean()) {
					expected.add(prevExpected--);
					actual.add(it.previousInt());
				} else {
					int skipSize = rand.nextInt(8);
					int skipExpected = Math.min(skipSize, prevExpected + 1);
					int skipActual = it.back(skipSize);
					assertEquals(skipExpected, skipActual);
					prevExpected -= skipExpected;
				}
			}
			assertEquals(expected, actual);
		}
		assertThrows(IllegalArgumentException.class, () -> range(5).iterator().skip(-1));
		assertThrows(IllegalArgumentException.class, () -> range(5).asList().listIterator().back(-1));
	}

	@Test
	public void iteratorBidirectional() {
		final Random rand = new Random(0x944d5803c3b97713L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + 1 + rand.nextInt(100);
			Range range = range(from, to);

			int fromElement = from - 10 + rand.nextInt(to - from + 20);
			IntBidirectionalIterator iter = range.iterator(fromElement);
			assertEqualsBool(fromElement <= range.lastInt(), iter.hasNext());
			assertEqualsBool(fromElement >= range.firstInt(), iter.hasPrevious());

			int expectedNext = Math.max(from, Math.min(fromElement + 1, to));
			for (int i = 0; i < 100; i++) {
				if (rand.nextBoolean()) {
					if (expectedNext < to) {
						assertTrue(iter.hasNext());
						assertEquals(expectedNext, iter.nextInt());
						expectedNext++;
					} else {
						assertFalse(iter.hasNext());
					}
				} else {
					if (expectedNext >= from + 1) {
						assertTrue(iter.hasPrevious());
						assertEquals(expectedNext - 1, iter.previousInt());
						expectedNext--;
					} else {
						assertFalse(iter.hasPrevious());
					}
				}
			}
		}
	}

	@Test
	public void size() {
		final Random rand = new Random(0xd6fc8c62633b3daL);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			assertEquals(to, range.size());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			assertEquals(to - from, range.size());
		}
	}

	@Test
	public void contains() {
		final Random rand = new Random(0x8e0003235967d7a2L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			for (int x : range(-15, to + 15))
				assertEqualsBool(0 <= x && x < to, range.contains(x));
			for (int r = 0; r < 10; r++) {
				int x = rand.nextInt();
				assertEqualsBool(0 <= x && x < to, range.contains(x));
			}
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			for (int x : range(from - 15, to + 15))
				assertEqualsBool(from <= x && x < to, range.contains(x));
			for (int r = 0; r < 10; r++) {
				int x = rand.nextInt();
				assertEqualsBool(from <= x && x < to, range.contains(x));
			}
		}
	}

	@Test
	public void hashCodeTest() {
		final Random rand = new Random(0xed8e5aac42b25beL);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			assertEquals(new IntOpenHashSet(range).hashCode(), range.hashCode());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			assertEquals(new IntOpenHashSet(range).hashCode(), range.hashCode());
		}
	}

	@Test
	public void equals() {
		final Random rand = new Random(0x46a8dbfba2ebdd8eL);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			List<Object> equal = List.of(range, range(to), new IntOpenHashSet(range), new HashSet<>(range));
			List<Object> notEqual = new ArrayList<>();
			notEqual.addAll(List.of(range(to + 1), new IntOpenHashSet(range(to + 1)), new HashSet<>(range(to + 1))));
			if (to != 0)
				notEqual
						.addAll(List
								.of(range(to - 1), new IntOpenHashSet(range(to - 1)), new HashSet<>(range(to - 1))));

			for (Object o : equal) {
				assertEquals(range, o);
				assertEquals(o, range);
			}
			for (Object o : notEqual) {
				assertNotEquals(range, o);
				assertNotEquals(o, range);
			}
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);

			List<Object> equal = List.of(range, range(from, to), new IntOpenHashSet(range), new HashSet<>(range));
			List<Object> notEqual = new ArrayList<>();
			notEqual
					.addAll(List
							.of(range(from - 1, to), new IntOpenHashSet(range(from - 1, to)),
									new HashSet<>(range(from - 1, to))));
			notEqual
					.addAll(List
							.of(range(from, to + 1), new IntOpenHashSet(range(from, to + 1)),
									new HashSet<>(range(from, to + 1))));
			if (from != to) {
				notEqual
						.addAll(List
								.of(range(from + 1, to), new IntOpenHashSet(range(from + 1, to)),
										new HashSet<>(range(from + 1, to))));
				notEqual
						.addAll(List
								.of(range(from, to - 1), new IntOpenHashSet(range(from, to - 1)),
										new HashSet<>(range(from, to - 1))));
				IntSet notEqual1 = new IntOpenHashSet();
				notEqual1.addAll(range(from, to - 1));
				notEqual1.add(to);
				notEqual.addAll(List.of(notEqual1, new HashSet<>(notEqual1)));
				IntSet notEqual2 = new IntOpenHashSet();
				notEqual2.addAll(range(from + 1, to));
				notEqual2.add(from - 1);
				notEqual.addAll(List.of(notEqual2, new HashSet<>(notEqual2)));
			}

			for (Object o : equal) {
				assertEquals(range, o);
				assertEquals(o, range);
			}
			for (Object o : notEqual) {
				assertNotEquals(range, o);
				assertNotEquals(o, range);
			}
		}
		assertEquals(range(0), range(1, 1));
	}

	@Test
	public void spliterator() {
		final Random rand = new Random(0x4c60eaab28b0b019L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);

			IntList expected = new IntArrayList();
			for (int x : range(to))
				expected.add(x);

			IntList actual = new IntArrayList();
			IntSpliterator spliterator = range.spliterator();
			while (spliterator.tryAdvance(actual::add));

			assertEquals(expected, actual);
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);

			IntList expected = new IntArrayList();
			for (int x : range(to))
				expected.add(x);

			IntList actual = new IntArrayList();
			IntSpliterator spliterator = range.spliterator();
			spliterator.forEachRemaining(actual::add);

			assertEquals(expected, actual);
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);

			IntList expected = new IntArrayList();
			for (int x : range(to))
				expected.add(x);

			List<IntSpliterator> splits = new ObjectArrayList<>();
			splits.add(range.spliterator());
			for (int r = 0; r < 10; r++) {
				int i = rand.nextInt(splits.size());
				IntSpliterator spliterator = splits.get(i);
				IntSpliterator split1 = spliterator.trySplit();
				if (split1 != null)
					splits.add(i, split1);
			}
			IntList actual = new IntArrayList();
			for (IntSpliterator spliterator : splits)
				spliterator.forEachRemaining(actual::add);

			assertEquals(expected, actual);
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			int nextExpected = 0;
			IntList expected = new IntArrayList();
			IntList actual = new IntArrayList();
			for (IntSpliterator it = range.spliterator(); nextExpected < to;) {
				if (rand.nextBoolean()) {
					expected.add(nextExpected++);
					boolean advanced = it.tryAdvance(actual::add);
					assertTrue(advanced);
				} else {
					int skipSize = rand.nextInt(8);
					int skipExpected = Math.min(skipSize, to - nextExpected);
					long skipActual = it.skip(skipSize);
					assertEquals(skipExpected, skipActual);
					nextExpected += skipExpected;
				}
			}
			assertEquals(expected, actual);
		}
		assertThrows(IllegalArgumentException.class, () -> range(5).spliterator().skip(-1));
	}

	@Test
	public void streamOperations() {
		final Random rand = new Random(0x93c026184e885dc3L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			IntList list = new IntArrayList();
			for (int x : range(to))
				list.add(x);

			for (int r = 0; r < 10; r++) {
				final int mapOpVal = rand.nextInt(100);
				IntUnaryOperator mapOp = x -> x + mapOpVal;
				assertEquals(list.intStream().map(mapOp).boxed().collect(toList()),
						range.map(mapOp).boxed().collect(toList()));

				final int mapToObjVal = rand.nextInt(100);
				IntFunction<String> mapToObj = x -> Integer.toString(x + mapToObjVal);
				assertEquals(list.intStream().mapToObj(mapToObj).collect(toList()),
						range.mapToObj(mapToObj).collect(toList()));

				final double mapToDoubleVal = rand.nextDouble() * 100;
				IntToDoubleFunction mapToDouble = x -> x + mapToDoubleVal;
				assertEquals(list.intStream().mapToDouble(mapToDouble).boxed().collect(toList()),
						range.mapToDouble(mapToDouble).boxed().collect(toList()));

				final int filterOpVal = 1 + rand.nextInt(5);
				IntPredicate filterOp = x -> x % filterOpVal == 0;
				assertEquals(list.intStream().filter(filterOp).boxed().collect(toList()),
						range.filter(filterOp).boxed().collect(toList()));

				final int allMatchOpVal = 1 + rand.nextInt(1);
				IntPredicate allMatchOp = x -> x % allMatchOpVal == 0;
				assertEqualsBool(list.intStream().allMatch(allMatchOp), range.allMatch(allMatchOp));
			}
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			IntList list = new IntArrayList();
			for (int x : range(from, to))
				list.add(x);

			for (int r = 0; r < 10; r++) {
				final int mapOpVal = rand.nextInt(100);
				IntUnaryOperator mapOp = x -> x + mapOpVal;
				assertEquals(list.intStream().map(mapOp).boxed().collect(toList()),
						range.map(mapOp).boxed().collect(toList()));

				final long mapToLongVal = rand.nextLong() * 100;
				IntToLongFunction mapToLong = x -> x + mapToLongVal;
				assertEquals(list.intStream().mapToLong(mapToLong).boxed().collect(toList()),
						range.mapToLong(mapToLong).boxed().collect(toList()));

				final int mapToObjVal = rand.nextInt(100);
				IntFunction<String> mapToObj = x -> Integer.toString(x + mapToObjVal);
				assertEquals(list.intStream().mapToObj(mapToObj).collect(toList()),
						range.mapToObj(mapToObj).collect(toList()));

				final double mapToDoubleVal = rand.nextDouble() * 100;
				IntToDoubleFunction mapToDouble = x -> x + mapToDoubleVal;
				assertEquals(list.intStream().mapToDouble(mapToDouble).boxed().collect(toList()),
						range.mapToDouble(mapToDouble).boxed().collect(toList()));

				final int filterOpVal = 1 + rand.nextInt(5);
				IntPredicate filterOp = x -> x % filterOpVal == 0;
				assertEquals(list.intStream().filter(filterOp).boxed().collect(toList()),
						range.filter(filterOp).boxed().collect(toList()));

				final int allMatchOpVal = 1 + rand.nextInt(1);
				IntPredicate allMatchOp = x -> x % allMatchOpVal == 0;
				assertEqualsBool(list.intStream().allMatch(allMatchOp), range.allMatch(allMatchOp));
			}
		}
	}

	@Test
	public void first() {
		final Random rand = new Random(0xc413697d06d8aa77L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = 1 + rand.nextInt(100);
			Range range = range(to);
			assertEquals(0, range.firstInt());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + 1 + rand.nextInt(100);
			Range range = range(from, to);
			assertEquals(from, range.firstInt());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from;
			Range range = range(from, to);
			assertThrows(NoSuchElementException.class, () -> range.firstInt());
		}
	}

	@Test
	public void last() {
		final Random rand = new Random(0x25a8eed201f004d2L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = 1 + rand.nextInt(100);
			Range range = range(to);
			assertEquals(to - 1, range.lastInt());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + 1 + rand.nextInt(100);
			Range range = range(from, to);
			assertEquals(to - 1, range.lastInt());
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from;
			Range range = range(from, to);
			assertThrows(NoSuchElementException.class, () -> range.lastInt());
		}
	}

	@Test
	public void subSet() {
		final Random rand = new Random(0x8c474ec55ead5335L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);

			int fromElement0 = from - 10 + rand.nextInt(to - from + 20);
			int toElement0 = from - 10 + rand.nextInt(to - from + 20);
			if (fromElement0 > toElement0) {
				int tmp = fromElement0;
				fromElement0 = toElement0;
				toElement0 = tmp;
			}
			int fromElement = fromElement0;
			int toElement = toElement0;
			assertEquals(range.filter(x -> fromElement <= x && x < toElement).boxed().collect(toSet()),
					range.subSet(fromElement, toElement));
		}
		assertThrows(IllegalArgumentException.class, () -> range(10).subSet(8, 5));
	}

	@Test
	public void headSet() {
		final Random rand = new Random(0x8c474ec55ead5335L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);

			int toElement = from - 10 + rand.nextInt(to - from + 20);
			assertEquals(range.filter(x -> x < toElement).boxed().collect(toSet()), range.headSet(toElement));
		}
	}

	@Test
	public void tailSet() {
		final Random rand = new Random(0x8c474ec55ead5335L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);

			int fromElement = from - 10 + rand.nextInt(to - from + 20);
			assertEquals(range.filter(x -> fromElement <= x).boxed().collect(toSet()), range.tailSet(fromElement));
		}
	}

	@Test
	public void comparator() {
		assertNull(range(10).comparator());
	}

	@Test
	public void asList() {
		Random rand = new Random(0x8ad61319542c505aL);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			IntList range = range(from, to).asList();

			/* size() */
			assertEquals(to - from, range.size());

			/* contains() */
			for (int x = -1; x <= 100; x++) {
				boolean expected = from <= x && x < to;
				assertEqualsBool(expected, range.contains(x));
			}

			/* get(index) */
			for (int i : range(range.size())) {
				int expected = from + i;
				assertEquals(expected, range.getInt(i));
			}
			assertThrows(IndexOutOfBoundsException.class, () -> range.getInt(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> range.getInt(to - from));

			/* indexOf() */
			for (int x = -1; x <= 100; x++) {
				int expected = (from <= x && x < to) ? x - from : -1;
				assertEquals(expected, range.indexOf(x));
				assertEquals(expected, range.lastIndexOf(x));
			}

			/* equals() */
			IntList expectedList = new IntArrayList();
			for (int x : range(from, to))
				expectedList.add(x);
			assertEquals(expectedList, range);
			assertEquals(range, expectedList);
			assertEquals(range, range);
			assertNotEquals(range, range(from - 1, to).asList());
			assertNotEquals(range, range(from, to + 1).asList());

			/* hashCode() */
			assertEquals(expectedList.hashCode(), range.hashCode());
			/* call hashCode() again, hash should be cached */
			assertEquals(expectedList.hashCode(), range.hashCode());

			/* subList() */
			for (int r = 0; r < 20; r++) {
				int s = to - from;
				int fromIndex = rand.nextInt(s + 1);
				int toIndex = fromIndex + rand.nextInt(s - fromIndex + 1);
				IntList subList = range.subList(fromIndex, toIndex);
				IntList expectedSubList = range(from + fromIndex, from + toIndex).asList();
				assertEquals(expectedSubList, subList);
				assertEquals(subList, expectedSubList);
			}

			/* listIterator() */
			for (int r = 0; r < 20; r++) {
				int startIdx = rand.nextInt(to - from + 1);
				IntListIterator it = range.listIterator(startIdx);
				assertEquals(startIdx, it.nextIndex());
				assertEquals(startIdx - 1, it.previousIndex());
				if (rand.nextBoolean()) {
					if (startIdx < to - from) {
						assertTrue(it.hasNext());
						assertEquals(from + startIdx, it.nextInt());
					} else {
						assertFalse(it.hasNext());
					}
				} else {
					if (startIdx > 0) {
						assertTrue(it.hasPrevious());
						assertEquals(from + startIdx - 1, it.previousInt());
					} else {
						assertFalse(it.hasPrevious());
					}
				}
			}

			/* spliterator() */
			IntList spliteratorElms = new IntArrayList();
			range.spliterator().forEachRemaining(spliteratorElms::add);
			assertEquals(expectedList, spliteratorElms);
		}
	}

}
