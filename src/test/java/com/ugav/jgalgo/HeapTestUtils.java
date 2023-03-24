package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {
	}

	static void testRandOps(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Heap<Integer> heap = heapBuilder.get();

			testHeap(heap, n, m, TestMode.Normal);
		});
	}

	static void testRandOpsAfterManyInserts(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();

			testHeap(heap, n, m, TestMode.InsertFirst);
		});
	}

	static void testMeld(Supplier<? extends Heap<Integer>> heapBuilder) {
		testMeld(heapBuilder, false);
	}

	static void testMeldWithOrderedValues(Supplier<? extends Heap<Integer>> heapBuilder) {
		testMeld(heapBuilder, true);
	}

	private static void testMeld(Supplier<? extends Heap<Integer>> heapBuilder, boolean orderedValues) {
		List<Phase> phases = List.of(phase(64, 16), phase(64, 32), phase(8, 256), phase(1, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int hCount = args[0];
			testMeld(heapBuilder, orderedValues, hCount);
		});
	}

	private static void testMeld(Supplier<? extends Heap<Integer>> heapBuilder, boolean orderedValues, int hCount) {
		Set<HeapTracker> heaps = new HashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(nextRandSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			HeapTracker h = new HeapTracker(heapBuilder.get(), heapTrackerIdGen.nextId());
			heaps.add(h);
			if (!orderedValues) {
				testHeap(h, 16, 16, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32)));
			} else {
				int[] vals = new int[16];
				for (int j = 0; j < 16; j++)
					vals[j] = elm++;
				testHeap(h, 16, TestMode.InsertFirst, vals);
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
				testHeap(h1, opsNum, opsNum, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32)));
			}
			heaps.clear();
			heaps.addAll(heapsNext);
		}
	}

	static void testDecreaseKey(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16), phase(128, 64), phase(64, 512), phase(16, 4096), phase(2, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();
			testHeap(heap, n, m, TestMode.DecreaseKey);
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

		HeapTracker(Heap<Integer> heap, int id) {
			this.id = id;
			this.heap = heap;
			elms = new TreeMap<>();
			rand = new Random(nextRandSeed());
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
			int x = nextInt(rand, elms.firstKey(), elms.lastKey() + 1);
			if (rand.nextBoolean()) {
				Integer X = elms.floorKey(x);
				x = X != null ? X : elms.ceilingKey(x);
			} else {
				Integer X = elms.ceilingKey(x);
				x = X != null ? X : elms.floorKey(x);
			}
			return x;
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

	static void testHeap(Heap<Integer> heap, int n, int m, TestMode mode) {
		testHeap(heap, n, m, mode, true);
	}

	static void testHeap(Heap<Integer> heap, int n, int m, TestMode mode, boolean clear) {
		if (clear) {
			heap.clear();
			Assertions.assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}

		HeapTracker tracker = new HeapTracker(heap, 0);
		testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n)));

		if (clear) {
			heap.clear();
			Assertions.assertTrue(heap.size() == 0 && heap.isEmpty(), "failed clear");
		}
	}

	private static void testHeap(HeapTracker tracker, int n, int m, TestMode mode, int elementsBound) {
		int[] elements = randArray(n, 0, elementsBound, nextRandSeed());
		testHeap(tracker, m, mode, elements);
	}

	@SuppressWarnings("boxing")
	static void testHeap(HeapTracker tracker, int m, TestMode mode, int[] values) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(nextRandSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops = new ArrayList<>(List.of(HeapOp.Insert, HeapOp.Remove, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);

		int[] elmsToInsertIds = randPermutation(values.length, nextRandSeed());
		int elmsToInsertCursor = 0;

		debug.println("\t testHeap begin");

		for (int opIdx = 0; opIdx < m;) {
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
				Assertions.assertTrue(tracker.heap.remove(x), "failed to remove: " + x);
				break;

			case FindMin:
				if (tracker.isEmpty())
					continue;
				debug.println("FindMin");

				expected = tracker.findMin();
				actual = tracker.heap.findMin();
				Assertions.assertEquals(expected, actual, "failed findmin");
				break;

			case ExtractMin:
				if (tracker.isEmpty() || rand.nextInt(3) != 0)
					continue;
				debug.println("ExtractMin");

				expected = tracker.extractMin();
				actual = tracker.heap.extractMin();
				Assertions.assertEquals(expected, actual, "failed extractmin");
				break;

			case DecreaseKey:
				if (tracker.isEmpty())
					continue;
				x = tracker.randElement();
				if (x == 0)
					continue;
				int newVal = rand.nextInt(x);
				HeapDirectAccessed<Integer> heap0 = (HeapDirectAccessed<Integer>) tracker.heap;

				tracker.decreaseKey(x, newVal);
				heap0.decreaseKey(heap0.findHanlde(x), newVal);
				break;

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
		Assertions.assertEquals(expectedSize, actualSize, "size() is different than counted size using iterator");
	}

	@SuppressWarnings({ "unused" })
	private static <E> void testHeapSize(Heap<E> h) {
		int expected = h.size();
		int actual = 0;
		for (E e : h)
			actual++;
		Assertions.assertEquals(expected, actual, "unexpected size");

	}

}
