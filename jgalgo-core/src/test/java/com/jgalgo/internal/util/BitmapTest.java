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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class BitmapTest extends TestBase {

	@Test
	public void getSet() {
		final long seed = 0x14c119cfbf07e27aL;
		Random rand = new Random(seed);

		Bitmap b = new Bitmap(10);
		assertTrue(b.set(1));
		assertTrue(b.set(3));
		assertFalse(b.set(3));
		assertTrue(b.set(4));
		assertFalse(b.set(4));
		assertTrue(b.set(7));
		assertFalse(b.get(0));
		assertTrue(b.get(1));
		assertFalse(b.get(2));
		assertTrue(b.get(3));
		assertTrue(b.get(4));
		assertFalse(b.get(5));
		assertFalse(b.get(6));
		assertTrue(b.get(7));
		assertFalse(b.get(8));
		assertFalse(b.get(9));
		b.set(0);
		b.set(9);
		assertTrue(b.get(0));
		assertTrue(b.get(9));
		assertTrue(b.clear(0));
		assertFalse(b.clear(0));
		assertTrue(b.clear(9));
		assertFalse(b.clear(9));
		assertFalse(b.get(0));
		assertFalse(b.get(9));

		assertThrows(IndexOutOfBoundsException.class, () -> b.get(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> b.get(10));

		IntSet expected = new IntOpenHashSet();
		for (int i : range(10)) {
			boolean bit = rand.nextBoolean();
			if (bit)
				expected.add(i);
			b.set(i, bit);
		}
		for (int i : range(10))
			assertEqualsBool(expected.contains(i), b.get(i));
	}

	@Test
	public void setClearAll() {
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			Bitmap b = new Bitmap(size);
			assertEquals(size, b.capacity());
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.cardinality());
			assertEquals(size, b.capacity());

			b.setAll();
			for (int i : range(size))
				assertTrue(b.get(i));
			assertEqualsBool(size > 0, !b.isEmpty());
			assertEquals(size, b.cardinality());

			b.clear();
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.cardinality());

			b.setAll(true);
			for (int i : range(size))
				assertTrue(b.get(i));
			assertEqualsBool(size > 0, !b.isEmpty());
			assertEquals(size, b.cardinality());

			b.setAll(false);
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.cardinality());
		}
	}

	@Test
	public void not() {
		final long seed = 0x81da52f931b50a8dL;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			Bitmap b = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); b.cardinality() < n;)
				b.set(rand.nextInt(size));

			IntSet oneBits = new IntOpenHashSet();
			for (int i : range(size))
				if (b.get(i))
					oneBits.add(i);

			b.not();
			for (int i : range(size))
				assertEqualsBool(!oneBits.contains(i), b.get(i));

			b.not();
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));
		}
	}

	@Test
	public void or() {
		final long seed = 0x4c62aec8921affabL;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			Bitmap b1 = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); b1.cardinality() < n;)
				b1.set(rand.nextInt(size));
			Bitmap b2 = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); b2.cardinality() < n;)
				b2.set(rand.nextInt(size));

			IntSet oneBits = new IntOpenHashSet();
			for (int i : range(size))
				if (b1.get(i) || b2.get(i))
					oneBits.add(i);

			b1.or(b2);
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b1.get(i));

			Bitmap b3 = new Bitmap(size + 1);
			assertThrows(IllegalArgumentException.class, () -> b1.or(b3));

			if (size > 0) {
				Bitmap b4 = new Bitmap(size - 1);
				assertThrows(IllegalArgumentException.class, () -> b1.or(b4));
			}
		}
	}

	@Test
	public void clearAllUnsafe() {
		final long seed = 0x5c0b679bb2e292fL;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			for (int n : IntList.of(size / 3 + rand.nextInt(1 + size / 3), Math.min(1, size))) {
				IntSet oneBits = new IntOpenHashSet();
				Bitmap b = new Bitmap(size);
				for (; oneBits.size() < n;) {
					int idx = rand.nextInt(size);
					b.set(idx);
					oneBits.add(idx);
				}
				assertEquals(oneBits.size(), b.cardinality());

				JGAlgoUtils.clearAllUnsafe(b, oneBits);

				for (int i : range(size))
					assertFalse(b.get(i));
				assertTrue(b.isEmpty());
				assertEquals(0, b.cardinality());
			}
		}
	}

	@Test
	public void iterator() {
		final long seed = 0x6ce40ac437e7a40bL;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			Bitmap b = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;) {
				int idx = rand.nextInt(size);
				b.set(idx);
				oneBits.add(idx);
			}

			IntSet actual = new IntOpenHashSet();
			for (int idx : b) {
				boolean modified = actual.add(idx);
				assertTrue(modified, "duplicate: " + idx);
			}
			assertEquals(oneBits, actual);
		}
	}

	@Test
	public void toArray() {
		final long seed = 0xdaa56411a403d6d5L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			Bitmap b = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); b.cardinality() < n;)
				b.set(rand.nextInt(size));

			IntList expected = new IntArrayList();
			for (int i : range(size))
				if (b.get(i))
					expected.add(i);
			IntList actual = IntList.of(b.toArray());
			assertEquals(expected, actual);
		}
	}

	@Test
	public void copy() {
		final long seed = 0x8dc38e168c54c17dL;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			Bitmap b = new Bitmap(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); b.cardinality() < n;)
				b.set(rand.nextInt(size));

			Bitmap b2 = b.copy();
			for (int i : range(size))
				assertEqualsBool(b.get(i), b2.get(i));
			assertEquals(b.capacity(), b2.capacity());
			assertEquals(b.cardinality(), b2.cardinality());
		}
	}

	@Test
	public void constructors() {
		final long seed = 0x34d91b543c87dcc6L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));

			Bitmap b = Bitmap.fromOnes(size, oneBits.toIntArray());
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));

			b = Bitmap.fromOnes(size, oneBits);
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));

			b = Bitmap.fromOnes(size, oneBits.iterator());
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));

			b = Bitmap.fromPredicate(size, oneBits::contains);
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));

			ImmutableBitmap ib = ImmutableBitmap.of(b);
			b = Bitmap.of(ib);
			for (int i : range(size))
				assertEqualsBool(oneBits.contains(i), b.get(i));

			assertThrows(IllegalArgumentException.class, () -> new Bitmap(-1));
		}
	}

	@Test
	public void nextSetBit() {
		final long seed = 0xe4de0dd7cd3563d0L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));
			Bitmap b = Bitmap.fromOnes(size, oneBits);

			for (int i : range(size)) {
				int expected;
				for (expected = i; expected < size; expected++)
					if (oneBits.contains(expected))
						break;
				if (expected == size)
					expected = -1;
				int actual = b.nextSetBit(i);
				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void nextClearBit() {
		final long seed = 0xe4de0dd7cd3563d0L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));
			Bitmap b = Bitmap.fromOnes(size, oneBits);

			for (int i : range(size)) {
				int expected;
				for (expected = i; expected < size; expected++)
					if (!oneBits.contains(expected))
						break;
				int actual = b.nextClearBit(i);
				assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void testToString() {
		final long seed = 0xe4de0dd7cd3563d0L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));
			Bitmap b = Bitmap.fromOnes(size, oneBits);

			String expected = new TreeSet<>(oneBits).toString();
			expected = expected.replace('[', '{').replace(']', '}');
			assertEquals(expected, b.toString());
		}
	}

	@Test
	public void immutableHashCode() {
		final long seed = 0xb1de2e8b9bdba6b9L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));
			ImmutableBitmap b1 = ImmutableBitmap.fromOnes(size, oneBits.iterator());
			ImmutableBitmap b2 = ImmutableBitmap.fromOnes(size, oneBits.iterator());
			assertEquals(b1.hashCode(), b2.hashCode());
		}
	}

	@Test
	public void immutableEquals() {
		final long seed = 0xb1de2e8b9bdba6b9L;
		Random rand = new Random(seed);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;)
				oneBits.add(rand.nextInt(size));
			ImmutableBitmap b1 = ImmutableBitmap.fromOnes(size, oneBits.iterator());
			ImmutableBitmap b2 = ImmutableBitmap.fromOnes(size, oneBits.iterator());
			assertEquals(b1, b2);

			ImmutableBitmap b3 = ImmutableBitmap.fromOnes(size + 1, oneBits.iterator());
			assertNotEquals(b1, b3);
			assertNotEquals(b3, b2);

			if (size > 0) {
				Bitmap b4 = Bitmap.fromOnes(size, oneBits.iterator());
				b4.not();
				ImmutableBitmap b5 = ImmutableBitmap.of(b4);
				assertNotEquals(b1, b5);
				assertNotEquals(b5, b2);
			}

			assertEquals(b1, b1);
			assertNotEquals(b1, "hello world");
		}
	}

}
