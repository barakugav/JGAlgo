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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {}

	static void testRandOpsDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, null, seed);
	}

	static void testRandOpsCustomCompare(Heap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testRandOps(Heap.Builder heapBuilder, Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Heap<Integer> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.Normal, compare, seedGen.nextSeed());
		});
	}

	static void testRandOpsAfterManyInserts(Heap.Builder heapBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Comparator<? super Integer> compare = null;
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n;
			Heap<Integer> heap = heapBuilder.build(compare);

			testHeap(heap, n, m, TestMode.InsertFirst, compare, seedGen.nextSeed());
		});
	}

	static void testMeldDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, null, seed);
	}

	static void testMeldCustomCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	static void testMeldWithOrderedValuesDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, null, seed);
	}

	static void testMeldWithOrderedValuesCustomCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testMeld(Heap.Builder heapBuilder, boolean orderedValues, Comparator<? super Integer> compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(64, 32), phase(8, 256), phase(1, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int hCount = args[0];
			testMeld(heapBuilder, orderedValues, hCount, compare, seedGen.nextSeed());
		});
	}

	private static void testMeld(Heap.Builder heapBuilder, boolean orderedValues, int hCount,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Set<HeapTracker> heaps = new HashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			HeapTracker h =
					new HeapTracker(heapBuilder.build(compare), heapTrackerIdGen.nextId(), compare, seedGen.nextSeed());
			heaps.add(h);
			if (!orderedValues) {
				testHeap(h, 16, 16, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32)), compare,
						seedGen.nextSeed());
			} else {
				int[] vals = new int[16];
				for (int j = 0; j < 16; j++)
					vals[j] = elm++;
				testHeap(h, 16, TestMode.InsertFirst, vals, compare, seedGen.nextSeed());
			}
		}

		while (heaps.size() > 1) {
			/* meld half of the heaps */
			Set<HeapTracker> heapsNext = new HashSet<>();
			List<HeapTracker> heapsSuffled = new ArrayList<>(heaps);

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				HeapTracker h1 = heapsSuffled.get(i * 2);
				HeapTracker h2 = heapsSuffled.get(i * 2 + 1);

				h1.heap.meld(h2.heap);
				h1.meld(h2);
				heapsNext.add(h1);

				/* make some OPs on the united heap */
				int opsNum = 1024 / heaps.size();
				testHeap(h1, opsNum, opsNum, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32)), compare,
						seedGen.nextSeed());
			}
			heaps.clear();
			heaps.addAll(heapsNext);
		}
	}

	static void testDecreaseKeyDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, null, seed);
	}

	static void testDecreaseKeyCustomCompare(Heap.Builder heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testDecreaseKey(Heap.Builder heapBuilder, Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 64), phase(64, 512), phase(16, 4096), phase(2, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.DecreaseKey, compare, seedGen.nextSeed());
		});
	}

	static enum TestMode {
		Normal, InsertFirst, DecreaseKey,
	}

	private static enum HeapOp {
		Insert, Remove, RemoveRef, FindMin, ExtractMin, DecreaseKey
	}

	static class HeapTrackerIdGenerator {
		private final RandomIntUnique rand;

		HeapTrackerIdGenerator(long seed) {
			rand = new RandomIntUnique(0, Integer.MAX_VALUE, seed);
		}

		int nextId() {
			return rand.next();
		}
	}

	@SuppressWarnings("boxing")
	static class HeapTracker implements Comparable<HeapTracker> {

		private final int id;
		final Heap<Integer> heap;
		final NavigableMap<Integer, List<HeapReference<Integer>>> elms;
		final Random rand;

		HeapTracker(Heap<Integer> heap, int id, Comparator<? super Integer> compare, long seed) {
			this.id = id;
			this.heap = heap;
			elms = new TreeMap<>(compare);
			rand = new Random(seed);
		}

		boolean isEmpty() {
			return elms.isEmpty();
		}

		void insert(int x, HeapReference<Integer> ref) {
			elms.computeIfAbsent(x, dontCare -> new ArrayList<>()).add(ref);
		}

		void remove(int x) {
			if (heap instanceof HeapReferenceable)
				throw new IllegalStateException();
			List<HeapReference<Integer>> l = elms.get(x);
			HeapReference<Integer> ref = l.remove(0);
			if (l.isEmpty())
				elms.remove(x);
			assert ref == null;
		}

		void remove(int x, HeapReference<Integer> ref) {
			if (!(heap instanceof HeapReferenceable))
				throw new IllegalStateException();
			List<HeapReference<Integer>> l = elms.get(x);
			boolean removed = l.remove(ref);
			if (l.isEmpty())
				elms.remove(x);
			assert removed;
		}

		int findMin() {
			return elms.firstKey();
		}

		int extractMin() {
			Integer x = elms.firstKey();
			remove(x);
			return x;
		}

		void decreaseKey(HeapReference<Integer> ref, int newx) {
			remove(ref.get(), ref);
			insert(newx, ref);
		}

		void meld(HeapTracker other) {
			for (Map.Entry<Integer, List<HeapReference<Integer>>> e : other.elms.entrySet()) {
				elms.merge(e.getKey(), e.getValue(), (c1, c2) -> {
					List<HeapReference<Integer>> l = new ArrayList<>();
					if (c1 != null)
						l.addAll(c1);
					if (c2 != null)
						l.addAll(c2);
					return l;
				});
			}
			other.elms.clear();
		}

		void split(int x, HeapTracker newTracker) {
			NavigableMap<Integer, List<HeapReference<Integer>>> newElms = elms.tailMap(x, false);
			newTracker.elms.putAll(newElms);
			newElms.clear();
		}

		int randElement() {
			IntList elms0 = new IntArrayList();
			for (Map.Entry<Integer, List<HeapReference<Integer>>> e : elms.entrySet())
				for (int i = 0; i < e.getValue().size(); i++)
					elms0.add(e.getKey().intValue());
			return elms0.getInt(rand.nextInt(elms0.size()));
		}

		HeapReference<Integer> randRef() {
			List<HeapReference<Integer>> elms0 = new ArrayList<>();
			for (Map.Entry<Integer, List<HeapReference<Integer>>> e : elms.entrySet())
				elms0.addAll(e.getValue());
			return elms0.get(rand.nextInt(elms0.size()));
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}

		@Override
		public String toString() {
			return elms.toString();
		}

		@Override
		public int compareTo(HeapTracker o) {
			return Integer.compare(id, o.id);
		}

	}

	static void testHeap(Heap<Integer> heap, int n, int m, TestMode mode, Comparator<? super Integer> compare,
			long seed) {
		testHeap(heap, n, m, mode, true, compare, seed);
	}

	static void testHeap(Heap<Integer> heap, int n, int m, TestMode mode, boolean clear,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		if (clear) {
			heap.clear();
			assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}

		HeapTracker tracker = new HeapTracker(heap, 0, compare, seedGen.nextSeed());
		testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n)), compare, seedGen.nextSeed());

		if (clear) {
			heap.clear();
			assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}
	}

	private static void testHeap(HeapTracker tracker, int n, int m, TestMode mode, int elementsBound,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] elements = randArray(n, 0, elementsBound, seedGen.nextSeed());
		testHeap(tracker, m, mode, elements, compare, seedGen.nextSeed());
	}

	@SuppressWarnings("boxing")
	private static int compare(Comparator<? super Integer> c, int e1, int e2) {
		return c == null ? Integer.compare(e1, e2) : c.compare(e1, e2);
	}

	@SuppressWarnings("boxing")
	static void testHeap(HeapTracker tracker, int m, TestMode mode, int[] values, Comparator<? super Integer> compare,
			long seed) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops = new ArrayList<>(List.of(HeapOp.Insert, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);
		if (tracker.heap instanceof HeapReferenceable<?>) {
			ops.add(HeapOp.RemoveRef);
		} else {
			ops.add(HeapOp.Remove);
		}

		int[] elmsToInsertIds = randPermutation(values.length, seedGen.nextSeed());
		int elmsToInsertCursor = 0;

		debug.println("\t testHeap begin");

		opLoop: for (int opIdx = 0; opIdx < m;) {
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : ops.get(rand.nextInt(ops.size()));

			int expected, actual;
			switch (op) {
				case Insert: {
					if (elmsToInsertCursor >= elmsToInsertIds.length)
						continue;
					int x = values[elmsToInsertIds[elmsToInsertCursor++]];
					debug.println("Insert(", x, ")");

					HeapReference<Integer> ref = tracker.heap.insert(x);
					tracker.insert(x, ref);
					break;
				}
				case Remove: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					int x = tracker.randElement();
					debug.println("Remove(", x, ")");

					tracker.remove(x);
					assertTrue(tracker.heap.remove(x), "failed to remove: " + x);
					break;
				}
				case RemoveRef: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					HeapReference<Integer> ref = tracker.randRef();
					debug.println("RemoveRef(", ref, ")");

					tracker.remove(ref.get(), ref);
					HeapReferenceable<Integer> heap0 = (HeapReferenceable<Integer>) tracker.heap;
					heap0.removeRef(ref);
					break;
				}
				case FindMin: {
					if (tracker.isEmpty())
						continue;
					debug.println("FindMin");

					expected = tracker.findMin();
					actual = tracker.heap.findMin();
					assertEquals(expected, actual, "failed findMin");
					break;
				}
				case ExtractMin: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					debug.println("ExtractMin");

					if (tracker.heap instanceof HeapReferenceable<?>) {
						HeapReference<Integer> ref = ((HeapReferenceable<Integer>) tracker.heap).findMinRef();
						expected = tracker.findMin();
						assertEquals(expected, ref.get(), "failed findMin");

						actual = tracker.heap.extractMin();
						assertEquals(expected, actual, "failed extractMin");
						tracker.remove(expected, ref);

					} else {
						expected = tracker.extractMin();
						actual = tracker.heap.extractMin();
						assertEquals(expected, actual, "failed extractMin");
					}

					break;
				}
				case DecreaseKey: {
					if (tracker.isEmpty())
						continue;
					HeapReference<Integer> ref;
					int newVal;
					for (int retry = 20;; retry--) {
						if (retry <= 0)
							continue opLoop;
						ref = tracker.randRef();
						int x = ref.get();
						assert x >= 0;
						if (x == 0)
							continue;
						newVal = rand.nextInt(Math.max(x, x * 2));
						if (compare(compare, newVal, x) <= 0)
							break;
					}
					HeapReferenceable<Integer> heap0 = (HeapReferenceable<Integer>) tracker.heap;

					debug.println("DecreaseKey(" + ref.get() + ", " + newVal + ")");
					tracker.decreaseKey(ref, newVal);
					heap0.decreaseKey(ref, newVal);
					break;
				}
				default:
					throw new IllegalStateException();
			}
			opIdx++;
		}

		int expectedSize = tracker.heap.size();
		int actualSize = 0;
		for (Iterator<Integer> it = tracker.heap.iterator(); it.hasNext();) {
			it.next();
			actualSize++;
		}
		assertEquals(expectedSize, actualSize, "size() is different than counted size using iterator");
	}

	@SuppressWarnings("unused")
	private static <E> void testHeapSize(Heap<E> h) {
		int expected = h.size();
		int actual = 0;
		for (E e : h)
			actual++;
		assertEquals(expected, actual, "unexpected size");

	}

}
