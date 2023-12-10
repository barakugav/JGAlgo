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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class RangeTest extends TestBase {

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
			for (int x = -15; x < to + 15; x++)
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
			for (int x = from - 15; x < to + 15; x++)
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
			assertEquals(range, range);
			assertEquals(range(to), range);
			assertEquals(new IntOpenHashSet(range), range);
			assertEquals(range, new IntOpenHashSet(range));
			assertNotEquals(range(to + 1), range);
			assertNotEquals(range, range(to + 1));
			if (to != 0) {
				assertNotEquals(range(to - 1), range);
				assertNotEquals(range, range(to - 1));
			}
		}
		for (int repeat = 0; repeat < 25; repeat++) {
			final int from = rand.nextInt(100);
			final int to = from + rand.nextInt(100);
			Range range = range(from, to);
			assertEquals(range, range);
			assertEquals(range(from, to), range);
			assertEquals(new IntOpenHashSet(range), range);
			assertEquals(range, new IntOpenHashSet(range));
			assertNotEquals(range(from - 1, to), range);
			assertNotEquals(range, range(from - 1, to));
			assertNotEquals(range(from, to + 1), range);
			assertNotEquals(range, range(from, to + 1));
			if (from != to) {
				assertNotEquals(range(from + 1, to), range);
				assertNotEquals(range, range(from + 1, to));
				assertNotEquals(range(from, to - 1), range);
				assertNotEquals(range, range(from, to - 1));
			}
		}
	}

	@Test
	public void streamOperations() {
		final Random rand = new Random(0x93c026184e885dc3L);
		for (int repeat = 0; repeat < 25; repeat++) {
			final int to = rand.nextInt(100);
			Range range = range(to);
			IntList list = new IntArrayList();
			for (int x = 0; x < to; x++)
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
			for (int x = from; x < to; x++)
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

	}

}
