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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ImmutableIntArraySetTest extends TestBase {

	@Test
	public void sizeAndIsEmpty() {
		final long seed = 0x48eadfe3aa182607L;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();
		for (int i = 0; i < 10; i++) {
			int from = rand.nextInt(arr.length);
			int to = rand.nextInt(arr.length + 1);
			if (from > to) {
				int tmp = from;
				from = to;
				to = tmp;
			}
			IntSet set = ImmutableIntArraySet.withNaiveContains(arr, from, to);
			assertEquals(to - from, set.size());
			assertEqualsBool(to - from == 0, set.isEmpty());
		}

		assertEquals(0, ImmutableIntArraySet.withNaiveContains(IntArrays.DEFAULT_EMPTY_ARRAY).size());
		assertTrue(ImmutableIntArraySet.withNaiveContains(IntArrays.DEFAULT_EMPTY_ARRAY).isEmpty());
		assertEquals(3, ImmutableIntArraySet.withNaiveContains(new int[] { 0, 1, 6 }).size());
		assertFalse(ImmutableIntArraySet.withNaiveContains(new int[] { 0, 1, 6 }).isEmpty());
	}

	@Test
	public void toArray() {
		final long seed = 0xb2ce46be45e5f129L;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();
		for (int i = 0; i < 10; i++) {
			int from = rand.nextInt(arr.length);
			int to = rand.nextInt(arr.length + 1);
			if (from > to) {
				int tmp = from;
				from = to;
				to = tmp;
			}
			IntSet set = ImmutableIntArraySet.withNaiveContains(arr, from, to);

			assertArrayEquals(Arrays.copyOfRange(arr, from, to), set.toIntArray());
			assertArrayEquals(Arrays.copyOfRange(arr, from, to), set.toArray(IntArrays.DEFAULT_EMPTY_ARRAY));

			int[] outputArr = new int[50];
			int[] retArr = set.toArray(outputArr);
			assertTrue(retArr == outputArr);

			assertArrayEquals(IntList.of(Arrays.copyOfRange(arr, from, to)).toArray(), set.toArray());
			assertArrayEquals(IntList.of(Arrays.copyOfRange(arr, from, to)).toArray(new Integer[0]),
					set.toArray(new Integer[0]));
			assertArrayEquals(IntList.of(Arrays.copyOfRange(arr, from, to)).toArray(new Integer[to - from]),
					set.toArray(new Integer[to - from]));
		}
	}

	@Test
	public void equalsAndHashCode() {
		final long seed = 0x6e316f1257e3baa9L;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();

		assertEquals(ImmutableIntArraySet.withNaiveContains(arr),
				ImmutableIntArraySet.withNaiveContains(arr, 0, arr.length));
		assertNotEquals(ImmutableIntArraySet.withNaiveContains(arr), null);

		for (int i = 0; i < 10; i++) {
			int from = rand.nextInt(arr.length);
			int to = rand.nextInt(arr.length + 1);
			if (from > to) {
				int tmp = from;
				from = to;
				to = tmp;
			}
			IntSet set = ImmutableIntArraySet.withNaiveContains(arr, from, to);

			assertEquals(IntSet.of(set.toIntArray()).hashCode(), set.hashCode());
			assertEquals(IntSet.of(set.toIntArray()), set);
			assertEquals(ImmutableIntArraySet.withNaiveContains(arr, from, to).hashCode(), set.hashCode());
			assertEquals(ImmutableIntArraySet.withNaiveContains(arr, from, to), set);
			assertEquals(set, set);

			if (to > from) {
				assertNotEquals(ImmutableIntArraySet.withNaiveContains(arr, from, to - 1).hashCode(), set.hashCode());
				assertNotEquals(ImmutableIntArraySet.withNaiveContains(arr, from, to - 1), set);
				assertNotEquals(set, ImmutableIntArraySet.withNaiveContains(arr, from, to - 1));
			}
		}
	}

	@Test
	public void iterator() {
		final long seed = 0xcf80bef3a3e842dfL;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();
		for (int i = 0; i < 10; i++) {
			int from = rand.nextInt(arr.length);
			int to = rand.nextInt(arr.length + 1);
			if (from > to) {
				int tmp = from;
				from = to;
				to = tmp;
			}
			IntSet set = ImmutableIntArraySet.withNaiveContains(arr, from, to);

			IntSet expected = new IntOpenHashSet();
			for (int j : range(from, to))
				expected.add(arr[j]);
			IntSet actual = new IntOpenHashSet();
			for (int elm : set) {
				boolean modified = actual.add(elm);
				assertTrue(modified, "iterator returned duplicate element");
			}
			assertEquals(expected, actual);
		}
	}

	@Test
	public void naiveContains() {
		final long seed = 0xa920c058161e3330L;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();
		IntSet set = ImmutableIntArraySet.withNaiveContains(arr);

		for (int x : arr)
			assertTrue(set.contains(x));
		for (int i = 0; i < 10; i++) {
			int x;
			do {
				x = rand.nextInt();
			} while (elms.contains(x));
			assertFalse(set.contains(x));
		}
	}

	@Test
	public void bitmapContains() {
		final long seed = 0xcc16dc60522d89abL;
		Random rand = new Random(seed);
		IntSet elms = new IntOpenHashSet();
		while (elms.size() < 10)
			elms.add(rand.nextInt(1000));
		int[] arr = elms.toIntArray();

		IntSet set = ImmutableIntArraySet.withBitmap(ImmutableIntArraySet.withNaiveContains(arr), 1000);
		for (int x : range(-10, 1010))
			assertEqualsBool(elms.contains(x), set.contains(x));

		set = ImmutableIntArraySet.withBitmap(set, 1000);
		for (int x : range(-10, 1010))
			assertEqualsBool(elms.contains(x), set.contains(x));

		set = ImmutableIntArraySet.withBitmap(set, 1001);
		for (int x : range(-10, 1010))
			assertEqualsBool(elms.contains(x), set.contains(x));

		set = ImmutableIntArraySet.withBitmap(IntList.of(arr), 1000);
		for (int x : range(-10, 1010))
			assertEqualsBool(elms.contains(x), set.contains(x));

		set = ImmutableIntArraySet.withBitmap(Bitmap.fromOnes(1000, set));
		for (int x : range(-10, 1010))
			assertEqualsBool(elms.contains(x), set.contains(x));
	}

	@Test
	public void customContains() {
		int[] arr = new int[10];
		for (int i : range(10))
			arr[i] = i * 2;
		IntSet set = ImmutableIntArraySet.newInstance(arr, key -> 0 <= key && key < 20 && key % 2 == 0);
		for (int x : range(-10, 30))
			assertEqualsBool(x % 2 == 0 && 0 <= x && x < 20, set.contains(x));
	}

	@Test
	public void outOfBoundRange() {
		int[] arr = new int[10];
		for (int i : range(10))
			arr[i] = i * 2;

		assertThrows(IndexOutOfBoundsException.class,
				() -> ImmutableIntArraySet.withNaiveContains(arr, -1, arr.length));
		assertThrows(IndexOutOfBoundsException.class,
				() -> ImmutableIntArraySet.withNaiveContains(arr, 0, arr.length + 1));
		assertThrows(IndexOutOfBoundsException.class, () -> ImmutableIntArraySet.withNaiveContains(arr, 1, 0));
	}

}
