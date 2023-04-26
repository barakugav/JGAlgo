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
import java.util.function.Function;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {
	}

	static void testRandOpsDefaultCompare(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		testRandOps(heapBuilder, null, seed);
	}

	static void testRandOpsCustomCompare(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		testRandOps(heapBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testRandOps(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 128), phase(32, 512, 1024),
				phase(8, 4096, 8096), phase(4, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Heap<Integer> heap = heapBuilder.apply(compare);
			testHeap(heap, n, m, TestMode.Normal, compare, seedGen.nextSeed());
		});
	}

	static void testRandOpsAfterManyInserts(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Comparator<? super Integer> compare = null;
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n;
			Heap<Integer> heap = heapBuilder.apply(compare);

			testHeap(heap, n, m, TestMode.InsertFirst, compare, seedGen.nextSeed());
		});
	}

	static void testMeldDefaultCompare(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		testMeld(heapBuilder, false, null, seed);
	}

	static void testMeldCustomCompare(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		testMeld(heapBuilder, false, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	static void testMeldWithOrderedValuesDefaultCompare(
			Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder, long seed) {
		testMeld(heapBuilder, true, null, seed);
	}

	static void testMeldWithOrderedValuesCustomCompare(
			Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder, long seed) {
		testMeld(heapBuilder, true, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testMeld(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			boolean orderedValues, Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(64, 32), phase(8, 256), phase(1, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int hCount = args[0];
			testMeld(heapBuilder, orderedValues, hCount, compare, seedGen.nextSeed());
		});
	}

	private static void testMeld(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			boolean orderedValues, int hCount, Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Set<HeapTracker> heaps = new HashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(seedGen.nextSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			HeapTracker h = new HeapTracker(heapBuilder.apply(compare), heapTrackerIdGen.nextId(), compare,
					seedGen.nextSeed());
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

	static void testDecreaseKeyDefaultCompare(
			Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder, long seed) {
		testDecreaseKey(heapBuilder, null, seed);
	}

	static void testDecreaseKeyCustomCompare(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			long seed) {
		testDecreaseKey(heapBuilder, (x1, x2) -> -Integer.compare(x1.intValue(), x2.intValue()), seed);
	}

	private static void testDecreaseKey(Function<Comparator<? super Integer>, ? extends Heap<Integer>> heapBuilder,
			Comparator<? super Integer> compare, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 64), phase(64, 512), phase(16, 4096), phase(2, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.apply(compare);
			testHeap(heap, n, m, TestMode.DecreaseKey, compare, seedGen.nextSeed());
		});
	}

	static enum TestMode {
		Normal, InsertFirst, DecreaseKey,
	}

	private static enum HeapOp {
		Insert, Remove, FindMin, ExtractMin, DecreaseKey
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

		void decreaseKey(int x, int newx) {
			remove(x);
			insert(newx);
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

		List<HeapOp> ops = new ArrayList<>(List.of(HeapOp.Insert, HeapOp.Remove, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);

		int[] elmsToInsertIds = randPermutation(values.length, seedGen.nextSeed());
		int elmsToInsertCursor = 0;

		debug.println("\t testHeap begin");

		opLoop: for (int opIdx = 0; opIdx < m;) {
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : ops.get(rand.nextInt(ops.size()));

			int x, expected, actual;
			switch (op) {
				case Insert:
					if (elmsToInsertCursor >= elmsToInsertIds.length)
						continue;
					x = values[elmsToInsertIds[elmsToInsertCursor++]];
					debug.println("Insert(", x, ")");

					tracker.insert(x);
					tracker.heap.insert(x);
					break;

				case Remove:
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					x = tracker.randElement();
					debug.println("Remove(", x, ")");

					tracker.remove(x);
					assertTrue(tracker.heap.remove(x), "failed to remove: " + x);
					break;

				case FindMin:
					if (tracker.isEmpty())
						continue;
					debug.println("FindMin");

					expected = tracker.findMin();
					actual = tracker.heap.findMin();
					assertEquals(expected, actual, "failed findMin");
					break;

				case ExtractMin:
					if (tracker.isEmpty() || rand.nextInt(3) != 0)
						continue;
					debug.println("ExtractMin");

					expected = tracker.extractMin();
					actual = tracker.heap.extractMin();
					assertEquals(expected, actual, "failed extractMin");
					break;

				case DecreaseKey: {
					if (tracker.isEmpty())
						continue;
					int newVal;
					for (int retry = 20;; retry--) {
						if (retry <= 0)
							continue opLoop;
						x = tracker.randElement();
						assert x >= 0;
						if (x == 0)
							continue;
						newVal = rand.nextInt(Math.max(x, x * 2));
						if (compare(compare, newVal, x) <= 0)
							break;
					}
					HeapReferenceable<Integer> heap0 = (HeapReferenceable<Integer>) tracker.heap;

					debug.println("DecreaseKey(" + x + ", " + newVal + ")");
					tracker.decreaseKey(x, newVal);
					heap0.decreaseKey(heap0.findRef(x), newVal);
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