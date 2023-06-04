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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class HeapReferenceableTestUtils extends TestUtils {

	private HeapReferenceableTestUtils() {}

	static void testRandOpsDefaultCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testRandOps(heapBuilder, null, seed);
	}

	static void testRandOpsCustomCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testRandOps(heapBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testRandOps(HeapReferenceable.Builder<Integer, Void> heapBuilder, IntComparator compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 128), phase(32, 512, 1024), phase(8, 4096, 8096),
				phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			HeapReferenceable<Integer, Void> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.Normal, compare, seedGen.nextSeed());
		});
	}

	static void testRandOpsAfterManyInserts(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n;
			HeapReferenceable<Integer, Void> heap = heapBuilder.build(compare);

			testHeap(heap, n, m, TestMode.InsertFirst, compare, seedGen.nextSeed());
		});
	}

	static void testMeldDefaultCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testMeld(heapBuilder, false, null, seed);
	}

	static void testMeldCustomCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testMeld(heapBuilder, false, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	static void testMeldWithOrderedValuesDefaultCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder,
			long seed) {
		testMeld(heapBuilder, true, null, seed);
	}

	static void testMeldWithOrderedValuesCustomCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder,
			long seed) {
		testMeld(heapBuilder, true, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testMeld(HeapReferenceable.Builder<Integer, Void> heapBuilder, boolean orderedValues,
			IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(64, 32), phase(8, 256), phase(1, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int hCount = args[0];
			testMeld(heapBuilder, orderedValues, hCount, compare, seedGen.nextSeed());
		});
	}

	private static void testMeld(HeapReferenceable.Builder<Integer, Void> heapBuilder, boolean orderedValues,
			int hCount, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Set<HeapReferenceableTracker> heaps = new ObjectOpenHashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			HeapReferenceableTracker h = new HeapReferenceableTracker(heapBuilder.build(compare),
					heapTrackerIdGen.nextId(), compare, seedGen.nextSeed());
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
			Set<HeapReferenceableTracker> heapsNext = new ObjectOpenHashSet<>();
			List<HeapReferenceableTracker> heapsSuffled = new ObjectArrayList<>(heaps);

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				HeapReferenceableTracker h1 = heapsSuffled.get(i * 2);
				HeapReferenceableTracker h2 = heapsSuffled.get(i * 2 + 1);

				h1.heap.meld(h2.heap);
				assertTrue(h2.heap.isEmpty());
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

	static void testDecreaseKeyDefaultCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, null, seed);
	}

	static void testDecreaseKeyCustomCompare(HeapReferenceable.Builder<Integer, Void> heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testDecreaseKey(HeapReferenceable.Builder<Integer, Void> heapBuilder, IntComparator compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 64), phase(64, 512), phase(16, 4096), phase(2, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			HeapReferenceable<Integer, Void> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.DecreaseKey, compare, seedGen.nextSeed());
		});
	}

	static enum TestMode {
		Normal, InsertFirst, DecreaseKey,
	}

	private static enum HeapOp {
		Insert, RemoveRef, FindMin, ExtractMin, DecreaseKey
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
	static class HeapReferenceableTracker implements Comparable<HeapReferenceableTracker> {

		private final int id;
		final HeapReferenceable<Integer, Void> heap;
		final NavigableMap<Integer, List<HeapReference<Integer, Void>>> elms;
		final Random rand;

		HeapReferenceableTracker(HeapReferenceable<Integer, Void> heap, int id, IntComparator compare, long seed) {
			this.id = id;
			this.heap = heap;
			elms = new TreeMap<>(compare);
			rand = new Random(seed);
		}

		boolean isEmpty() {
			return elms.isEmpty();
		}

		void insert(int x, HeapReference<Integer, Void> ref) {
			elms.computeIfAbsent(x, dontCare -> new ObjectArrayList<>()).add(ref);
		}

		void remove(int x, HeapReference<Integer, Void> ref) {
			List<HeapReference<Integer, Void>> l = elms.get(x);
			boolean removed = l.remove(ref);
			if (l.isEmpty())
				elms.remove(x);
			assert removed;
		}

		int findMin() {
			return elms.firstKey();
		}

		void decreaseKey(HeapReference<Integer, Void> ref, int newx) {
			remove(ref.key(), ref);
			insert(newx, ref);
		}

		void meld(HeapReferenceableTracker other) {
			for (Map.Entry<Integer, List<HeapReference<Integer, Void>>> e : other.elms.entrySet()) {
				elms.merge(e.getKey(), e.getValue(), (c1, c2) -> {
					List<HeapReference<Integer, Void>> l = new ObjectArrayList<>();
					if (c1 != null)
						l.addAll(c1);
					if (c2 != null)
						l.addAll(c2);
					return l;
				});
			}
			other.elms.clear();
		}

		void split(int x, HeapReferenceableTracker newTracker) {
			NavigableMap<Integer, List<HeapReference<Integer, Void>>> newElms = elms.tailMap(x, false);
			newTracker.elms.putAll(newElms);
			newElms.clear();
		}

		int randElement() {
			IntList elms0 = new IntArrayList();
			for (Map.Entry<Integer, List<HeapReference<Integer, Void>>> e : elms.entrySet())
				for (int i = 0; i < e.getValue().size(); i++)
					elms0.add(e.getKey().intValue());
			return elms0.getInt(rand.nextInt(elms0.size()));
		}

		HeapReference<Integer, Void> randRef() {
			List<HeapReference<Integer, Void>> elms0 = new ObjectArrayList<>();
			for (Map.Entry<Integer, List<HeapReference<Integer, Void>>> e : elms.entrySet())
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
		public int compareTo(HeapReferenceableTracker o) {
			return Integer.compare(id, o.id);
		}

	}

	static void testHeap(HeapReferenceable<Integer, Void> heap, int n, int m, TestMode mode, IntComparator compare,
			long seed) {
		testHeap(heap, n, m, mode, true, compare, seed);
	}

	static void testHeap(HeapReferenceable<Integer, Void> heap, int n, int m, TestMode mode, boolean clear,
			IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		if (clear) {
			heap.clear();
			assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}

		HeapReferenceableTracker tracker = new HeapReferenceableTracker(heap, 0, compare, seedGen.nextSeed());
		testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n)), compare, seedGen.nextSeed());

		if (clear) {
			heap.clear();
			assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}
	}

	private static void testHeap(HeapReferenceableTracker tracker, int n, int m, TestMode mode, int elementsBound,
			IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] elements = randArray(n, 0, elementsBound, seedGen.nextSeed());
		testHeap(tracker, m, mode, elements, compare, seedGen.nextSeed());
	}

	private static int compare(IntComparator c, int e1, int e2) {
		return c == null ? Integer.compare(e1, e2) : c.compare(e1, e2);
	}

	@SuppressWarnings("boxing")
	static void testHeap(HeapReferenceableTracker tracker, int m, TestMode mode, int[] values, IntComparator compare,
			long seed) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops = new ObjectArrayList<>(List.of(HeapOp.Insert, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);
		ops.add(HeapOp.RemoveRef);

		int[] elmsToInsertIds = randPermutation(values.length, seedGen.nextSeed());
		int elmsToInsertCursor = 0;

		debug.println("\t testHeap begin");

		opLoop: for (int opIdx = 0; opIdx < m;) {
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : ops.get(rand.nextInt(ops.size()));

			debug.println("\t size=" + tracker.heap.size());
			int expected, actual;
			switch (op) {
				case Insert: {
					if (elmsToInsertCursor >= elmsToInsertIds.length)
						continue;
					int x = values[elmsToInsertIds[elmsToInsertCursor++]];
					debug.println("Insert(", x, ")");

					HeapReference<Integer, Void> ref = tracker.heap.insert(x);
					tracker.insert(x, ref);
					break;
				}
				case RemoveRef: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					HeapReference<Integer, Void> ref = tracker.randRef();
					debug.println("RemoveRef(", ref, ")");

					tracker.remove(ref.key(), ref);
					HeapReferenceable<Integer, Void> heap0 = tracker.heap;
					heap0.remove(ref);
					break;
				}
				case FindMin: {
					if (tracker.isEmpty())
						continue;
					debug.println("FindMin");

					expected = tracker.findMin();
					actual = tracker.heap.findMin().key();
					assertEquals(expected, actual, "failed findMin");
					break;
				}
				case ExtractMin: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					debug.println("ExtractMin");

					HeapReference<Integer, Void> ref = tracker.heap.findMin();
					expected = tracker.findMin();
					assertEquals(expected, ref.key(), "failed findMin");

					actual = tracker.heap.extractMin().key();
					assertEquals(expected, actual, "failed extractMin");
					tracker.remove(expected, ref);

					break;
				}
				case DecreaseKey: {
					if (tracker.isEmpty())
						continue;
					HeapReference<Integer, Void> ref;
					int newVal;
					for (int retry = 20;; retry--) {
						if (retry <= 0)
							continue opLoop;
						ref = tracker.randRef();
						int x = ref.key();
						assert x >= 0;
						if (x == 0)
							continue;
						newVal = rand.nextInt(Math.max(x, x * 2));
						if (compare(compare, newVal, x) <= 0)
							break;
					}

					debug.println("DecreaseKey(" + ref.key() + ", " + newVal + ")");
					tracker.decreaseKey(ref, newVal);
					tracker.heap.decreaseKey(ref, newVal);
					break;
				}
				default:
					throw new IllegalStateException();
			}
			opIdx++;
		}

		int heapSize = tracker.heap.size();
		int countedSize = 0;
		for (@SuppressWarnings("unused")
		HeapReference<Integer, Void> ref : tracker.heap)
			countedSize++;
		assertEquals(countedSize, heapSize, "size() is different than counted size using iterator");
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
