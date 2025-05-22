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
package com.jgalgo.internal.ds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class LinkedListSinglyTest extends TestBase {

	@Test
	public void init() {
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(0);
		tester.addPhase().withArgs(1);
		tester.addPhase().withArgs(2);
		tester.addPhase().withArgs(3);
		tester.addPhase().withArgs(4);
		tester.addPhase().withArgs(55);
		tester.addPhase().withArgs(99);
		tester.addPhase().withArgs(64);
		tester.addPhase().withArgs(238);
		tester.run((len) -> {
			LinkedListFixedSize.Singly list = new LinkedListFixedSize.Singly(len);
			assertEquals(len, list.size());
			for (int i = 0; i < len; i++) {
				assertEquals(LinkedListFixedSize.None, list.next(i));
				assertTrue(LinkedListFixedSize.isNone(list.next(i)));
			}
			assertThrows(IndexOutOfBoundsException.class, () -> list.next(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.next(len));
		});
	}

	@Test
	public void setNext() {
		final Random rand = new Random(0x9f58457a9de3421aL);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(1);
		tester.addPhase().withArgs(2);
		tester.addPhase().withArgs(3);
		tester.addPhase().withArgs(4);
		tester.addPhase().withArgs(55);
		tester.addPhase().withArgs(99);
		tester.addPhase().withArgs(64);
		tester.addPhase().withArgs(238);
		tester.run((len) -> {
			LinkedListFixedSize.Singly list = new LinkedListFixedSize.Singly(len);
			Int2IntMap nextMap = new Int2IntOpenHashMap();
			assertEquals(len, list.size());
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				list.setNext(u, v);
				nextMap.put(u, v);
			}
			for (int i = 0; i < len; i++) {
				assertEquals(nextMap.getOrDefault(i, LinkedListFixedSize.None), list.next(i));
				assertEqualsBool(nextMap.containsKey(i), list.hasNext(i));
			}
		});
	}

	@Test
	public void clear() {
		final Random rand = new Random(0x4766dd1ac65fb399L);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(1);
		tester.addPhase().withArgs(2);
		tester.addPhase().withArgs(3);
		tester.addPhase().withArgs(4);
		tester.addPhase().withArgs(55);
		tester.addPhase().withArgs(99);
		tester.addPhase().withArgs(64);
		tester.addPhase().withArgs(238);
		tester.run((len) -> {
			LinkedListFixedSize.Singly list = new LinkedListFixedSize.Singly(len);
			assertEquals(len, list.size());
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				list.setNext(u, v);
			}
			list.clear();
			for (int i = 0; i < len; i++)
				assertEquals(LinkedListFixedSize.None, list.next(i));
		});
	}

	@Test
	public void iterator() {
		final Random rand = new Random(0x3699b2607fa12767L);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(1);
		tester.addPhase().withArgs(2);
		tester.addPhase().withArgs(3);
		tester.addPhase().withArgs(4);
		tester.addPhase().withArgs(55);
		tester.addPhase().withArgs(99);
		tester.addPhase().withArgs(64);
		tester.addPhase().withArgs(238);
		tester.run((len) -> {
			LinkedListFixedSize.Singly list = new LinkedListFixedSize.Singly(len);
			Int2IntMap nextMap = new Int2IntOpenHashMap();
			assertEquals(len, list.size());
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				list.setNext(u, v);
				nextMap.put(u, v);
			}
			for (int i = 0; i < len; i++) {
				IntIterator it = list.iterator(i);
				int expectedNext = i;
				for (int laps = 0, iterLen = 0;; iterLen++) {
					assertTrue(it.hasNext());
					int next = it.nextInt();
					assertEquals(expectedNext, next);
					assertEqualsBool(nextMap.containsKey(expectedNext), it.hasNext());
					if (!it.hasNext())
						break;
					expectedNext = nextMap.get(expectedNext);
					if (iterLen > len) {
						laps++;
						iterLen = 0;
						if (laps > 2)
							break;

					}
				}
			}
		});
	}

}
