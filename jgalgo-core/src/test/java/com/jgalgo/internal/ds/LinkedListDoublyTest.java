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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class LinkedListDoublyTest extends TestBase {

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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			assertEquals(len, list.size());
			for (int i = 0; i < len; i++) {
				assertEquals(LinkedList.None, list.next(i));
				assertTrue(LinkedList.isNone(list.next(i)));
				assertEquals(LinkedList.None, list.prev(i));
				assertTrue(LinkedList.isNone(list.prev(i)));
			}
			assertThrows(IndexOutOfBoundsException.class, () -> list.next(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.next(len));
			assertThrows(IndexOutOfBoundsException.class, () -> list.prev(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.prev(len));
		});
	}

	@Test
	public void connect() {
		final Random rand = new Random(0x6b45faf0a934f629L);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (list.hasNext(u) || list.hasPrev(v))
					continue;
				int uPrev = list.prev(u);
				int vNext = list.next(v);
				list.connect(u, v);

				assertEquals(v, list.next(u));
				assertEquals(u, list.prev(v));

				assertEquals(uPrev, list.prev(u));
				if (!LinkedList.isNone(uPrev))
					assertEquals(u, list.next(uPrev));

				assertEquals(vNext, list.next(v));
				if (!LinkedList.isNone(vNext))
					assertEquals(v, list.prev(vNext));
			}
		});
	}

	@Test
	public void disconnect() {
		final Random rand = new Random(0x1c8dbd6a634bddacL);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (list.hasNext(u) || list.hasPrev(v))
					continue;
				list.connect(u, v);
			}
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int v = rand.nextInt(len);
				int prev = list.prev(v);
				int next = list.next(v);
				list.disconnect(v);

				assertFalse(list.hasNext(v));
				assertFalse(list.hasPrev(v));

				if (!LinkedList.isNone(prev))
					assertFalse(list.hasNext(prev));
				if (!LinkedList.isNone(next))
					assertFalse(list.hasPrev(next));
			}
		});
	}

	@Test
	public void insertAfter() {
		final Random rand = new Random(0x45e92a04f76d41f9L);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (list.hasNext(v) || list.hasPrev(v))
					continue;
				int uNext = list.next(u);
				list.insertAfter(u, v);

				assertEquals(v, list.next(u));
				assertEquals(u, list.prev(v));

				assertEquals(uNext, list.next(v));
				if (!LinkedList.isNone(uNext))
					assertEquals(v, list.prev(uNext));
			}
		});
	}

	@Test
	public void insertBefore() {
		final Random rand = new Random(0x648fed0634990e91L);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (list.hasNext(v) || list.hasPrev(v))
					continue;
				int uPrev = list.prev(u);
				list.insertBefore(u, v);

				assertEquals(v, list.prev(u));
				assertEquals(u, list.next(v));

				assertEquals(uPrev, list.prev(v));
				if (!LinkedList.isNone(uPrev))
					assertEquals(v, list.next(uPrev));
			}
		});
	}

	@Test
	public void iterator() {
		final Random rand = new Random(0x625680f90dfac910L);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			Int2IntMap nextMap = new Int2IntOpenHashMap();
			Int2IntMap prevMap = new Int2IntOpenHashMap();
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (nextMap.containsKey(u) || prevMap.containsKey(v))
					continue;
				list.connect(u, v);
				nextMap.put(u, v);
				prevMap.put(v, u);
			}
			for (int i = 0; i < len; i++) {
				final int begin = i;
				IntIterator it = list.iterator(begin);
				int expectedNext = begin;
				for (int laps = 0;;) {
					assertTrue(it.hasNext());
					int next = it.nextInt();
					assertEquals(expectedNext, next);
					assertEqualsBool(nextMap.containsKey(expectedNext), it.hasNext());
					if (!it.hasNext())
						break;
					expectedNext = nextMap.get(expectedNext);
					if (expectedNext == begin) {
						laps++;
						if (laps > 2)
							break;

					}
				}
			}
			for (int i = 0; i < len; i++) {
				final int begin = i;
				IntIterator it = list.iterRev(begin);
				int expectedNext = begin;
				for (int laps = 0;;) {
					assertTrue(it.hasNext());
					int next = it.nextInt();
					assertEquals(expectedNext, next);
					assertEqualsBool(prevMap.containsKey(expectedNext), it.hasNext());
					if (!it.hasNext())
						break;
					expectedNext = prevMap.get(expectedNext);
					if (expectedNext == begin) {
						laps++;
						if (laps > 2)
							break;

					}
				}
			}
		});
	}

	@Test
	public void removeIf() {
		final Random rand = new Random(0x8ca6b176c95959cdL);
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
			LinkedList.Doubly list = new LinkedList.Doubly(len);
			Int2IntMap v2head = new Int2IntOpenHashMap();
			Int2IntMap v2tail = new Int2IntOpenHashMap();
			for (int v : range(len)) {
				v2head.put(v, v);
				v2tail.put(v, v);
			}
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				int u = rand.nextInt(len);
				int v = rand.nextInt(len);
				if (u == v)
					continue;
				if (list.hasNext(u) || list.hasPrev(v))
					continue;
				if (u == v2tail.get(v))
					continue;
				list.connect(u, v);
				v2head.put(v, v2head.get(u));
				v2tail.put(u, v2tail.get(v));
			}
			for (int m = rand.nextInt(Math.max(10, len)); m >= 0; m--) {
				final int head;
				{
					int v = rand.nextInt(len);
					for (int vBegin = v, watchDog = 0;; watchDog++) {
						if (watchDog > len)
							throw new AssertionError();
						if (!list.hasPrev(v))
							break;
						v = list.prev(v);
						if (v == vBegin)
							throw new AssertionError("cycle");
					}
					head = v;
				}

				IntList elements = new IntArrayList(list.iterator(head));
				// final int tail = elements.getLast().intValue();
				IntSet elementsToRemove =
						new IntOpenHashSet(elements.intStream().filter(e -> rand.nextInt(4) == 0).toArray());
				int[] expectedElementsAfterRemove =
						elements.intStream().filter(e -> !elementsToRemove.contains(e)).toArray();

				var newHeadTail = list.removeIf(head, elementsToRemove::contains);
				int newHead = LinkedList.head(newHeadTail), newTail = LinkedList.tail(newHeadTail);

				for (int v : elementsToRemove) {
					assertFalse(list.hasNext(v));
					assertFalse(list.hasPrev(v));
				}
				for (int i = 0; i < expectedElementsAfterRemove.length - 1; i++) {
					int u = expectedElementsAfterRemove[i];
					int v = expectedElementsAfterRemove[i + 1];
					assertEquals(v, list.next(u));
					assertEquals(u, list.prev(v));
				}

				IntList elementsAfterRemove = new IntArrayList(list.iterMaybeNone(newHead));
				if (LinkedList.isNone(newHead)) {
					assertTrue(elementsAfterRemove.isEmpty());
				} else {
					assertEquals(newHead, elementsAfterRemove.getInt(0));
				}
				if (LinkedList.isNone(newTail)) {
					assertTrue(elementsAfterRemove.isEmpty());
				} else {
					assertEquals(newTail, elementsAfterRemove.getInt(elementsAfterRemove.size() - 1));
				}
				assertEquals(new IntArrayList(expectedElementsAfterRemove), elementsAfterRemove);
			}
		});
	}
}
