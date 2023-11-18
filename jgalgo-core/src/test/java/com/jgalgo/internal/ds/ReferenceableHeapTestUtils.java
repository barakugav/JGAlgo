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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ReferenceableHeapTestUtils extends TestUtils {

	private ReferenceableHeapTestUtils() {}

	static void testRandOpsDefaultCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, null, seed);
	}

	static void testRandOpsCustomCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testRandOps(heapBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testRandOps(ReferenceableHeap.Builder heapBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(32);
		tester.addPhase().withArgs(4096, 8096).repeat(8);
		tester.addPhase().withArgs(16384, 32768).repeat(4);
		tester.run((n, m) -> {
			IntReferenceableHeap heap = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, compare);
			testHeap(heap, n, m, TestMode.Normal, compare, seedGen.nextSeed());
		});
	}

	static void testRandOpsAfterManyInserts(ReferenceableHeap.Builder heapBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final IntComparator compare = null;
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(64, 128).repeat(128);
		tester.addPhase().withArgs(512, 1024).repeat(64);
		tester.addPhase().withArgs(4096, 8096).repeat(16);
		tester.addPhase().withArgs(16384, 32768).repeat(8);
		tester.run(n -> {
			int m = n;
			IntReferenceableHeap heap = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, compare);
			testHeap(heap, n, m, TestMode.InsertFirst, compare, seedGen.nextSeed());
		});
	}

	static void testMeldDefaultCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, null, seed);
	}

	static void testMeldCustomCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, false, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	static void testMeldWithOrderedValuesDefaultCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, null, seed);
	}

	static void testMeldWithOrderedValuesCustomCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testMeld(heapBuilder, true, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testMeld(ReferenceableHeap.Builder heapBuilder, boolean orderedValues, IntComparator compare,
			long seed) {
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

	private static void testMeld(ReferenceableHeap.Builder heapBuilder, boolean orderedValues, int hCount,
			IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Set<ReferenceableHeapTracker> heaps = new ObjectOpenHashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			ReferenceableHeapTracker h = new ReferenceableHeapTracker(
					(IntReferenceableHeap) heapBuilder.build(int.class, void.class, compare), heapTrackerIdGen.nextId(),
					compare, seedGen.nextSeed());
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
			Set<ReferenceableHeapTracker> heapsNext = new ObjectOpenHashSet<>();
			List<ReferenceableHeapTracker> heapsSuffled = new ObjectArrayList<>(heaps);

			for (int i = 0; i < heapsSuffled.size() / 2; i++) {
				ReferenceableHeapTracker h1 = heapsSuffled.get(i * 2);
				ReferenceableHeapTracker h2 = heapsSuffled.get(i * 2 + 1);

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

	static void testDecreaseKeyDefaultCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, null, seed);
	}

	static void testDecreaseKeyCustomCompare(ReferenceableHeap.Builder heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, (x1, x2) -> -Integer.compare(x1, x2), seed);
	}

	private static void testDecreaseKey(ReferenceableHeap.Builder heapBuilder, IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(64).repeat(128);
		tester.addPhase().withArgs(512).repeat(64);
		tester.addPhase().withArgs(4096).repeat(16);
		tester.addPhase().withArgs(16384).repeat(2);
		tester.run(n -> {
			int m = n;
			IntReferenceableHeap heap = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, compare);
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
	static class ReferenceableHeapTracker implements Comparable<ReferenceableHeapTracker> {

		private final int id;
		final IntReferenceableHeap heap;
		final NavigableMap<Integer, List<IntReferenceableHeap.Ref>> elms;
		final Random rand;

		ReferenceableHeapTracker(IntReferenceableHeap heap, int id, IntComparator compare, long seed) {
			this.id = id;
			this.heap = heap;
			elms = new TreeMap<>(compare);
			rand = new Random(seed);
		}

		boolean isEmpty() {
			return elms.isEmpty();
		}

		void insert(int x, IntReferenceableHeap.Ref ref) {
			elms.computeIfAbsent(x, dontCare -> new ObjectArrayList<>()).add(ref);
		}

		void remove(int x, IntReferenceableHeap.Ref ref) {
			List<IntReferenceableHeap.Ref> l = elms.get(x);
			boolean removed = l.remove(ref);
			if (l.isEmpty())
				elms.remove(x);
			assert removed;
		}

		int findMin() {
			return elms.firstKey();
		}

		void decreaseKey(IntReferenceableHeap.Ref ref, int newx) {
			remove(ref.key(), ref);
			insert(newx, ref);
		}

		void meld(ReferenceableHeapTracker other) {
			for (Map.Entry<Integer, List<IntReferenceableHeap.Ref>> e : other.elms.entrySet()) {
				elms.merge(e.getKey(), e.getValue(), (c1, c2) -> {
					List<IntReferenceableHeap.Ref> l = new ObjectArrayList<>();
					if (c1 != null)
						l.addAll(c1);
					if (c2 != null)
						l.addAll(c2);
					return l;
				});
			}
			other.elms.clear();
		}

		void split(int x, ReferenceableHeapTracker newTracker) {
			NavigableMap<Integer, List<IntReferenceableHeap.Ref>> newElms = elms.tailMap(x, false);
			newTracker.elms.putAll(newElms);
			newElms.clear();
		}

		int randElement() {
			IntList elms0 = new IntArrayList();
			for (Map.Entry<Integer, List<IntReferenceableHeap.Ref>> e : elms.entrySet())
				for (int i = 0; i < e.getValue().size(); i++)
					elms0.add(e.getKey().intValue());
			return TestUtils.randElement(elms0, rand);
		}

		IntReferenceableHeap.Ref randRef() {
			List<IntReferenceableHeap.Ref> elms0 = new ObjectArrayList<>();
			for (Map.Entry<Integer, List<IntReferenceableHeap.Ref>> e : elms.entrySet())
				elms0.addAll(e.getValue());
			return TestUtils.randElement(elms0, rand);
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
		public int compareTo(ReferenceableHeapTracker o) {
			return Integer.compare(id, o.id);
		}

	}

	static void testHeap(IntReferenceableHeap heap, int n, int m, TestMode mode, IntComparator compare, long seed) {
		testHeap(heap, n, m, mode, true, compare, seed);
	}

	static void testHeap(IntReferenceableHeap heap, int n, int m, TestMode mode, boolean clear, IntComparator compare,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		if (clear) {
			heap.clear();
			assertTrue(heap.isEmpty());
			assertFalse(heap.iterator().hasNext());
		}

		ReferenceableHeapTracker tracker = new ReferenceableHeapTracker(heap, 0, compare, seedGen.nextSeed());
		testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n)), compare, seedGen.nextSeed());

		if (clear) {
			heap.clear();
			assertTrue(heap.isEmpty());
			assertFalse(heap.iterator().hasNext());
		}
	}

	private static void testHeap(ReferenceableHeapTracker tracker, int n, int m, TestMode mode, int elementsBound,
			IntComparator compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] elements = randArray(n, 0, elementsBound, seedGen.nextSeed());
		testHeap(tracker, m, mode, elements, compare, seedGen.nextSeed());
	}

	private static int compare(IntComparator c, int e1, int e2) {
		return c == null ? Integer.compare(e1, e2) : c.compare(e1, e2);
	}

	@SuppressWarnings("boxing")
	static void testHeap(ReferenceableHeapTracker tracker, int m, TestMode mode, int[] values, IntComparator compare,
			long seed) {
		DebugPrinter debug = new DebugPrinter(false);
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
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : randElement(ops, rand);

			debug.printExec(() -> debug.println("\t size=" + ObjectIterables.size(tracker.heap)));
			int expected, actual;
			switch (op) {
				case Insert: {
					if (elmsToInsertCursor >= elmsToInsertIds.length)
						continue;
					int x = values[elmsToInsertIds[elmsToInsertCursor++]];
					debug.println("Insert(", x, ")");

					IntReferenceableHeap.Ref ref = tracker.heap.insert(x);
					tracker.insert(x, ref);
					break;
				}
				case RemoveRef: {
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					IntReferenceableHeap.Ref ref = tracker.randRef();
					debug.println("RemoveRef(", ref, ")");

					tracker.remove(ref.key(), ref);
					IntReferenceableHeap heap0 = tracker.heap;
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

					IntReferenceableHeap.Ref ref = tracker.heap.findMin();
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
					IntReferenceableHeap.Ref ref;
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
	static Heap.Builder<Integer> heapBuilderFromReferenceableHeapBuilder(ReferenceableHeap.Builder refHeapBuilder) {
		return new Heap.Builder<>() {

			@Override
			public Heap build(Comparator cmp) {
				return new HeapFromReferenceableHeap(
						(IntReferenceableHeap) refHeapBuilder.build(int.class, void.class, cmp));
			}

			@Override
			public Heap.Builder elementsTypeObj() {
				return this;
			}

			@Override
			public Heap.Builder elementsTypePrimitive(Class primitiveType) {
				return this;
			}
		};
	}

}
