package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.ugav.algo.Heap;
import com.ugav.algo.Pair;

class HeapTestUtils extends TestUtils {

	private HeapTestUtils() {
		throw new InternalError();
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16, 16 }, { 128, 64, 128 }, { 64, 512, 1024 }, { 16, 4096, 8096 },
				{ 8, 16384, 32768 } };
		return runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.Normal);
		});
	}

	static boolean testRandOpsAfterManyInserts(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16, 16 }, { 128, 64, 128 }, { 64, 512, 1024 }, { 16, 4096, 8096 },
				{ 8, 16384, 32768 } };
		return runTestMultiple(phases, args -> {
			int n = args[1];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.InsertFirst);
		});
	}

	static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 64, 16 }, { 64, 32 }, { 8, 256 }, { 1, 2048 } };
		return runTestMultiple(phases, args -> {
			int hCount = args[1];
			@SuppressWarnings("unchecked")
			Pair<Heap<Integer>, HeapTracker>[] hs = new Pair[hCount];
			@SuppressWarnings("unchecked")
			Pair<Heap<Integer>, HeapTracker>[] hsNext = new Pair[hCount / 2];

			for (int i = 0; i < hCount; i++) {
				Heap<Integer> h = heapBuilder.get();
				HeapTracker tracker = new HeapTracker();
				hs[i] = Pair.valueOf(h, tracker);
				if (!testHeap(h, tracker, 16, 16, TestMode.InsertFirst))
					return false;
			}

			while (hCount > 1) {
				/* meld half of the heaps */
				int[] meldOrder = Utils.randPermutation(hCount & ~1, nextRandSeed());
				for (int i = 0; i < meldOrder.length / 2; i++) {
					int h1Idx = meldOrder[i * 2], h2Idx = meldOrder[i * 2 + 1];
					Heap<Integer> h1 = hs[h1Idx].e1, h2 = hs[h2Idx].e1;
					HeapTracker t1 = hs[h1Idx].e2, t2 = hs[h2Idx].e2;

					h1.meld(h2);
					t1.meld(t2);
					hs[h2Idx] = null;

					/* make some OPs on the united heap */
					int opsNum = 4096 / hCount;
					if (!testHeap(h1, t1, opsNum, opsNum, TestMode.InsertFirst))
						return false;
				}

				/* contract heap array */
				int hCountNext = 0;
				for (int i = 0; i < hCount; i++)
					if (hs[i] != null)
						hsNext[hCountNext++] = hs[i];
				Pair<Heap<Integer>, HeapTracker>[] temp = hs;
				hs = hsNext;
				hsNext = temp;
				hCount = hCountNext;
			}

			return true;
		});
	}

	static boolean testDecreaseKey(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16 }, { 128, 64 }, { 64, 512 }, { 16, 4096 }, { 2, 16384 } };
		return runTestMultiple(phases, args -> {
			int n = args[1];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();

			return testHeap(heap, n, m, TestMode.DecreaseKey);
		});
	}

	private static enum TestMode {
		Normal, InsertFirst, DecreaseKey,
	}

	private static enum HeapOp {
		Insert, Remove, FindMin, ExtractMin, DecreaseKey
	}

	@SuppressWarnings("boxing")
	private static class HeapTracker {

		private final NavigableMap<Integer, Integer> insertedElms;
		private final Random rand;

		HeapTracker() {
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

	}

	private static boolean testHeap(Heap<Integer> heap, int n, int m, TestMode mode) {
		heap.clear();
		if (heap.size() != 0 || !heap.isEmpty()) {
			printTestStr("failed clear\n");
			return false;
		}

		HeapTracker tracker = new HeapTracker();
		if (!testHeap(heap, tracker, n, m, mode))
			return false;

		heap.clear();
		if (heap.size() != 0 || !heap.isEmpty()) {
			printTestStr("failed clear\n");
			return false;
		}

		return true;
	}

	@SuppressWarnings("boxing")
	private static boolean testHeap(Heap<Integer> heap, HeapTracker tracker, int n, int m, TestMode mode) {
		Random rand = new Random(nextRandSeed());
		int[] a = Utils.randArray(n, 0, 65536, nextRandSeed());
		int insertFirst = mode == TestMode.InsertFirst ? m / 2 : 0;

		List<HeapOp> ops = new ArrayList<>(List.of(HeapOp.Insert, HeapOp.Remove, HeapOp.FindMin, HeapOp.ExtractMin));
		if (mode == TestMode.DecreaseKey)
			ops.add(HeapOp.DecreaseKey);

		int[] elmsToInsertIds = Utils.randPermutation(a.length, nextRandSeed());
		int elmsToInsertCursor = 0;

		for (int opIdx = 0; opIdx < m;) {
			HeapOp op = opIdx < insertFirst ? HeapOp.Insert : ops.get(rand.nextInt(ops.size()));

			int x, expected, actual;
			switch (op) {
			case Insert:
				if (elmsToInsertCursor >= elmsToInsertIds.length)
					continue;
				x = a[elmsToInsertIds[elmsToInsertCursor++]];

				tracker.insert(x);
				heap.insert(x);
				break;

			case Remove:
				if (tracker.isEmpty())
					continue;
				x = tracker.randElement();

				tracker.remove(x);
				if (!heap.remove(x)) {
					printTestStr("failed to remove: " + x + "\n");
					return false;
				}
				break;

			case FindMin:
				if (tracker.isEmpty())
					continue;

				expected = tracker.findMin();
				actual = heap.findMin();
				if (actual != expected) {
					printTestStr("failed findmin: " + expected + " != " + actual + "\n");
					return false;
				}
				break;

			case ExtractMin:
				if (tracker.isEmpty())
					continue;

				expected = tracker.extractMin();
				actual = heap.extractMin();
				if (actual != expected) {
					printTestStr("failed extractmin: " + expected + " != " + actual + "\n");
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

				tracker.decreaseKey(x, newVal);
				heap.decreaseKey(heap.findHanlde(x), newVal);
				break;

			default:
				throw new InternalError();
			}
			opIdx++;
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
