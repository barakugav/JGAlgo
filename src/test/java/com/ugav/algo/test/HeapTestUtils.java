package com.ugav.algo.test;

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

import com.ugav.algo.DebugPrintsManager;
import com.ugav.algo.Heap;
import com.ugav.algo.HeapDirectAccessed;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {
		throw new InternalError();
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.Normal);
		});
	}

	static boolean testRandOpsAfterManyInserts(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 64, 128), phase(64, 512, 1024),
				phase(16, 4096, 8096), phase(8, 16384, 32768));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.InsertFirst);
		});
	}

	static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder) {
		return testMeld(heapBuilder, false);
	}

	static boolean testMeldWithOrderedValues(Supplier<? extends Heap<Integer>> heapBuilder) {
		return testMeld(heapBuilder, true);
	}

	private static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder, boolean orderedValues) {
		List<Phase> phases = List.of(phase(64, 16), phase(64, 32), phase(8, 256), phase(1, 2048));
		return runTestMultiple(phases, (testIter, args) -> {
			int hCount = args[0];
			return testMeld(heapBuilder, orderedValues, hCount);
		});
	}

	private static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder, boolean orderedValues, int hCount) {
		Set<HeapTracker> heaps = new HashSet<>();
		HeapTrackerIdGenerator heapTrackerIdGen = new HeapTrackerIdGenerator(nextRandSeed());

		int elm = 0;
		for (int i = 0; i < hCount; i++) {
			HeapTracker h = new HeapTracker(heapBuilder.get(), heapTrackerIdGen.nextId());
			heaps.add(h);
			if (!orderedValues) {
				if (!testHeap(h, 16, 16, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32))))
					return false;
			} else {
				int[] vals = new int[16];
				for (int j = 0; j < 16; j++)
					vals[j] = elm++;
				if (!testHeap(h, 16, TestMode.InsertFirst, vals))
					return false;
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
				int opsNum = 4096 / heaps.size();
				if (!testHeap(h1, opsNum, opsNum, TestMode.InsertFirst, Math.max(16, (int) Math.sqrt(hCount * 32))))
					return false;
			}
			heaps.clear();
			heaps.addAll(heapsNext);
		}

		return true;
	}

	static boolean testDecreaseKey(Supplier<? extends Heap<Integer>> heapBuilder) {
		List<Phase> phases = List.of(phase(256, 16), phase(128, 64), phase(64, 512), phase(16, 4096), phase(2, 16384));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.DecreaseKey);
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
		private final NavigableMap<Integer, Integer> insertedElms;
		private final Random rand;

		HeapTracker(Heap<Integer> heap, int id) {
			this.id = id;
			this.heap = heap;
			insertedElms = new TreeMap<>();
			rand = new Random(nextRandSeed());
		}

		boolean isEmpty() {
			return insertedElms.isEmpty();
		}

		void insert(int x) {
			insertedElms.compute(x, (x0, c) -> c == null ? 1 : c + 1);
		}

		void remove(int x) {
			insertedElms.compute(x, (x0, c) -> c == 1 ? null : c - 1);
		}

		int findMin() {
			return insertedElms.firstKey();
		}

		int extractMin() {
			Integer x = insertedElms.firstKey();
			remove(x);
			return x;
		}

		void decreaseKey(int x, int newx) {
			remove(x);
			insert(newx);
		}

		void meld(HeapTracker other) {
			for (Map.Entry<Integer, Integer> e : other.insertedElms.entrySet())
				insertedElms.merge(e.getKey(), e.getValue(), (c1, c2) -> c1 != null ? c1 + c2 : c2);
			other.insertedElms.clear();
		}

		void split(int x, HeapTracker newTracker) {
			NavigableMap<Integer, Integer> newElems = insertedElms.tailMap(x, false);
			newTracker.insertedElms.putAll(newElems);
			newElems.clear();
		}

		int randElement() {
			int x = rand.nextInt(insertedElms.firstKey(), insertedElms.lastKey() + 1);
			if (rand.nextBoolean()) {
				Integer X = insertedElms.floorKey(x);
				x = X != null ? X : insertedElms.ceilingKey(x);
			} else {
				Integer X = insertedElms.ceilingKey(x);
				x = X != null ? X : insertedElms.floorKey(x);
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

	static boolean testHeap(Heap<Integer> heap, int n, int m, TestMode mode) {
		return testHeap(heap, n, m, mode, true);
	}

	static boolean testHeap(Heap<Integer> heap, int n, int m, TestMode mode, boolean clear) {
		if (clear) {
			heap.clear();
			if (heap.size() != 0 || !heap.isEmpty()) {
				printTestStr("failed clear\n");
				return false;
			}
		}

		HeapTracker tracker = new HeapTracker(heap, 0);
		if (!testHeap(tracker, n, m, mode, Math.max(16, (int) Math.sqrt(n))))
			return false;

		if (clear) {
			heap.clear();
			if (heap.size() != 0 || !heap.isEmpty()) {
				printTestStr("failed clear\n");
				return false;
			}
		}

		return true;
	}

	private static boolean testHeap(HeapTracker tracker, int n, int m, TestMode mode, int elementsBound) {
		int[] elements = Utils.randArray(n, 0, elementsBound, nextRandSeed());
		return testHeap(tracker, m, mode, elements);
	}

	@SuppressWarnings("boxing")
	static boolean testHeap(HeapTracker tracker, int m, TestMode mode, int[] values) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		Random rand = new Random(nextRandSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops = new ArrayList<>(List.of(HeapOp.Insert, HeapOp.Remove, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);

		int[] elmsToInsertIds = Utils.randPermutation(values.length, nextRandSeed());
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
				if (!tracker.heap.remove(x)) {
					printTestStr("failed to remove: ", x, "\n");
					return false;
				}
				break;

			case FindMin:
				if (tracker.isEmpty())
					continue;
				debug.println("FindMin");

				expected = tracker.findMin();
				actual = tracker.heap.findMin();
				if (actual != expected) {
					printTestStr("failed findmin: ", expected, " != ", actual, "\n");
					return false;
				}
				break;

			case ExtractMin:
				if (tracker.isEmpty() || rand.nextInt(3) != 0)
					continue;
				debug.println("ExtractMin");

				expected = tracker.extractMin();
				actual = tracker.heap.extractMin();
				if (actual != expected) {
					printTestStr("failed extractmin: ", expected, " != ", actual, "\n");
					return false;
				}
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
				throw new InternalError();
			}
			opIdx++;
		}

		int expectedSize = tracker.heap.size();
		int actualSize = 0;
		for (Iterator<Integer> it = tracker.heap.iterator(); it.hasNext();) {
			it.next();
			actualSize++;
		}
		if (expectedSize != actualSize) {
			printTestStr("size() is different than counted size using iterator: ", expectedSize, " != ", actualSize,
					"\n");
			return false;
		}

		return true;
	}

	@SuppressWarnings("unused")
	private static <E> boolean testHeapSize(Heap<E> h) {
		int expected = h.size();
		int actual = 0;
		for (E e : h)
			actual++;
		return expected == actual;

	}

}
