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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import com.jgalgo.internal.util.DebugPrinter;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {}

	static void testRandOpsDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, null, seed);
	}

	static void testRandOpsCustomCompare(Heap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testRandOps(Heap.Builder heapBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(32);
		tester.addPhase().withArgs(4096, 8096).repeat(8);
		tester.addPhase().withArgs(16384, 32768).repeat(4);
		tester.run((n, m) -> {
			Heap<Integer> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.Normal, compare, seedGen.nextSeed());
		});
	}

	static void testRandOpsAfterManyInserts(Heap.Builder heapBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Comparator<? super Integer> compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(64).repeat(128);
		tester.addPhase().withArgs(512).repeat(64);
		tester.addPhase().withArgs(4096).repeat(16);
		tester.addPhase().withArgs(16384).repeat(8);
		tester.run(n -> {
			int m = n;
			Heap<Integer> heap = heapBuilder.build(compare);
			testHeap(heap, n, m, TestMode.InsertFirst, compare, seedGen.nextSeed());
		});
	}

	static void testMeldDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, null, seed);
	}

	static void testMeldCustomCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	static void testMeldWithOrderedValuesDefaultCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, null, seed);
	}

	static void testMeldWithOrderedValuesCustomCompare(Heap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testMeld(Heap.Builder heapBuilder, boolean orderedValues, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(64);
		tester.addPhase().withArgs(32).repeat(64);
		tester.addPhase().withArgs(256).repeat(8);
		tester.addPhase().withArgs(2048).repeat(1);
		tester.run(hCount -> {
			testMeld(heapBuilder, orderedValues, hCount, compare, seedGen.nextSeed());
		});
	}

	private static void testMeld(Heap.Builder heapBuilder, boolean orderedValues, int hCount,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Set<HeapTracker> heaps = new ObjectOpenHashSet<>();
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
			Set<HeapTracker> heapsNext = new ObjectOpenHashSet<>();
			List<HeapTracker> heapsSuffled = new ObjectArrayList<>(heaps);

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				HeapTracker h1 = heapsSuffled.get(i * 2);
				HeapTracker h2 = heapsSuffled.get(i * 2 + 1);

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

	static enum TestMode {
		Normal, InsertFirst,
	}

	private static enum HeapOp {
		Insert, Remove, FindMin, ExtractMin
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
		final NavigableMap<Integer, Integer> elms;
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

		void insert(int x) {
			elms.compute(x, (x0, c) -> c == null ? 1 : c + 1);
		}

		void remove(int x) {
			elms.compute(x, (x0, c) -> c == 1 ? null : c - 1);
		}

		int findMin() {
			return elms.firstKey();
		}

		int extractMin() {
			Integer x = elms.firstKey();
			remove(x);
			return x;
		}

		void meld(HeapTracker other) {
			for (Map.Entry<Integer, Integer> e : other.elms.entrySet())
				elms.merge(e.getKey(), e.getValue(), (c1, c2) -> c1 != null ? c1 + c2 : c2);
			other.elms.clear();
		}

		void split(int x, HeapTracker newTracker) {
			NavigableMap<Integer, Integer> newElems = elms.tailMap(x, false);
			newTracker.elms.putAll(newElems);
			newElems.clear();
		}

		int randElement() {
			int[] elmsArr = new int[elms.size()];
			int i = 0;
			for (Integer x : elms.keySet())
				elmsArr[i++] = x.intValue();
			return elmsArr[rand.nextInt(elmsArr.length)];
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
			assertTrue(heap.isEmpty());
			assertFalse(heap.iterator().hasNext());
		}

		HeapTracker tracker = new HeapTracker(heap, 0, compare, seedGen.nextSeed());
		testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n)), compare, seedGen.nextSeed());

		List<Integer> elms = new ObjectArrayList<>(heap.iterator());

		if (clear) {
			heap.clear();
			assertTrue(heap.isEmpty());
			assertFalse(heap.iterator().hasNext());
		}

		for (Integer x : elms)
			heap.insert(x);
		while (heap.isNotEmpty())
			assertNotNull(heap.extractMin());
		assertTrue(heap.isEmpty());
		assertFalse(heap.isNotEmpty());
		assertFalse(heap.iterator().hasNext());
	}

	private static void testHeap(HeapTracker tracker, int n, int m, TestMode mode, int elementsBound,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] elements = randArray(n, 0, elementsBound, seedGen.nextSeed());
		testHeap(tracker, m, mode, elements, compare, seedGen.nextSeed());
	}

	@SuppressWarnings("boxing")
	static void testHeap(HeapTracker tracker, int m, TestMode mode, int[] values, Comparator<? super Integer> compare,
			long seed) {
		DebugPrinter debug = new DebugPrinter(false);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops =
				new ObjectArrayList<>(List.of(HeapOp.Insert, HeapOp.Remove, HeapOp.FindMin, HeapOp.ExtractMin));

		int[] elmsToInsertIds = randPermutation(values.length, seedGen.nextSeed());
		int elmsToInsertCursor = 0;

		debug.println("\t testHeap begin");
		Heap<Integer> heap = tracker.heap;

		for (int opIdx = 0; opIdx < m;) {
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : randElement(ops, rand);

			int x, expected, actual;
			switch (op) {
				case Insert:
					if (elmsToInsertCursor >= elmsToInsertIds.length)
						continue;
					x = values[elmsToInsertIds[elmsToInsertCursor++]];
					debug.println("Insert(", x, ")");

					tracker.insert(x);
					heap.insert(x);
					break;

				case Remove:
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					x = tracker.randElement();
					debug.println("Remove(", x, ")");

					tracker.remove(x);
					assertTrue(heap.remove(x), "failed to remove: " + x);
					break;

				case FindMin:
					if (tracker.isEmpty()) {
						assertThrows(IllegalStateException.class, () -> heap.findMin());
						continue;
					}
					debug.println("FindMin");

					expected = tracker.findMin();
					actual = heap.findMin();
					assertEquals(expected, actual, "failed findMin");
					break;

				case ExtractMin:
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					debug.println("ExtractMin");

					expected = tracker.extractMin();
					actual = heap.extractMin();
					assertEquals(expected, actual, "failed extractMin");
					break;
				default:
					throw new IllegalStateException();
			}
			opIdx++;
		}
	}

	static class HeapFromReferenceableHeap implements Heap<Integer> {

		private final IntReferenceableHeap heap;

		HeapFromReferenceableHeap(IntReferenceableHeap heap) {
			this.heap = heap;
		}

		@Override
		public Iterator<Integer> iterator() {
			return IterTools.map(heap.iterator(), IntReferenceableHeap.Ref::key);
		}

		@Override
		public void insert(Integer elm) {
			heap.insert(elm.intValue());
		}

		@Override
		public void insertAll(Collection<? extends Integer> elms) {
			for (Integer elm : elms)
				heap.insert(elm.intValue());
		}

		@Override
		public Integer findMin() {
			return Integer.valueOf(heap.findMin().key());
		}

		@Override
		public Integer extractMin() {
			return Integer.valueOf(heap.extractMin().key());
		}

		@Override
		public boolean remove(Integer elm) {
			IntReferenceableHeap.Ref ref = heap.find(elm.intValue());
			if (ref == null)
				return false;
			heap.remove(ref);
			return true;
		}

		@Override
		public void meld(Heap<? extends Integer> heap) {
			for (Integer elm : heap)
				insert(elm);
			heap.clear();
		}

		@Override
		public boolean isEmpty() {
			return heap.isEmpty();
		}

		@Override
		public boolean isNotEmpty() {
			return heap.isNotEmpty();
		}

		@Override
		public void clear() {
			heap.clear();
		}

		@Override
		public Comparator<? super Integer> comparator() {
			return heap.comparator();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Heap.Builder heapBuilderFromReferenceableHeapBuilder(ReferenceableHeap.Builder refHeapBuilder) {
		return new Heap.Builder() {
			@Override
			public Heap build(Comparator cmp) {
				return new HeapFromReferenceableHeap(
						(IntReferenceableHeap) refHeapBuilder.build(int.class, void.class, cmp));
			}
		};
	}

}
