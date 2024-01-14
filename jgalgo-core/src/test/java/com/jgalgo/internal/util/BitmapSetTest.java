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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class BitmapSetTest extends TestBase {

	@Test
	public void setAndGet() {
		BitmapSet b = new BitmapSet(10);
		assertTrue(b.set(1));
		assertTrue(b.set(3));
		assertTrue(b.set(4));
		assertTrue(b.set(7));
		assertFalse(b.set(1));
		assertFalse(b.set(3));
		assertFalse(b.set(4));
		assertFalse(b.set(7));
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

		assertTrue(b.add(0));
		assertFalse(b.add(0));
		assertTrue(b.add(9));
		assertFalse(b.add(9));

		assertTrue(b.get(0));
		assertTrue(b.get(9));

		assertThrows(IndexOutOfBoundsException.class, () -> b.get(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> b.get(10));
		assertThrows(IndexOutOfBoundsException.class, () -> b.add(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> b.add(10));
	}

	@Test
	public void remove() {
		BitmapSet b = new BitmapSet(10);
		b.add(0);
		assertThrows(UnsupportedOperationException.class, () -> b.remove(0));
	}

	@Test
	public void contains() {
		Random rand = new Random(0x915bcd5948481e0cL);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			BitmapSet b = new BitmapSet(size);
			for (int n = size / 3 + rand.nextInt(1 + size / 3); oneBits.size() < n;) {
				int idx = rand.nextInt(size);
				b.add(idx);
				oneBits.add(idx);
			}

			for (int idx : range(-15, size + 15))
				assertEqualsBool(oneBits.contains(idx), b.contains(idx));
		}
	}

	@Test
	public void clear() {
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			BitmapSet b = new BitmapSet(size);
			assertEquals(size, b.capacity());
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.size());
			assertEquals(size, b.capacity());

			for (int i : range(size))
				if (i % 2 == 0)
					b.set(i);

			b.clear();
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.size());

			for (int i : range(size)) {
				if (i % 2 == 0)
					b.set(i);
				if (b.size() >= 2)
					break;
			}

			b.clear();
			for (int i : range(size))
				assertFalse(b.get(i));
			assertTrue(b.isEmpty());
			assertEquals(0, b.size());
		}
	}

	@Test
	public void iterator() {
		Random rand = new Random(0x8d9f270eefd914a2L);
		for (int size : IntList.of(0, 1, 10, 63, 64, 65, 95, 96, 97, 127, 128, 129)) {
			IntSet oneBits = new IntOpenHashSet();
			BitmapSet b = new BitmapSet(size);
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
	public void constructor() {
		assertThrows(IllegalArgumentException.class, () -> new BitmapSet(-1));
	}

}
