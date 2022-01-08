package com.ugav.algo.test;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.ugav.algo.Heap;

class HeapTestUtils {

	private HeapTestUtils() {
		throw new InternalError();
	}

	@SuppressWarnings("unused")
	private static HeapOp[] parseOpsStr(String s) {
		s = s.substring(1, s.length() - 1);
		String[] opsStr = s.split(", ");
		HeapOp[] ops = new HeapOp[opsStr.length];

		for (int i = 0; i < ops.length; i++)
			ops[i] = HeapOp.valueOf(opsStr[i]);
		return ops;
	}

	private static interface HeapOp {
		static HeapOp valueOf(String s) {
			if (s.startsWith("I"))
				return new HeapOpInsert(Integer.valueOf(s.substring(2, s.length() - 1)));
			if (s.startsWith("R"))
				return new HeapOpRemove(Integer.valueOf(s.substring(2, s.length() - 1)));
			if (s.startsWith("FM"))
				return new HeapOpFindMin(Integer.valueOf(s.substring(5, s.length())));
			if (s.startsWith("EM"))
				return new HeapOpExtractMin(Integer.valueOf(s.substring(5, s.length())));
			if (s.startsWith("DK"))
				return new HeapOpDecreaseKey(Integer.valueOf(s.substring(5, s.indexOf("->"))),
						Integer.valueOf(s.substring(s.indexOf("->") + 2, s.length())));
			throw new IllegalArgumentException(s);
		}
	};

	private static class HeapOpInsert implements HeapOp {
		final int x;

		HeapOpInsert(int x) {
			this.x = x;
		}

		@Override
		public String toString() {
			return "I(" + x + ")";
		}
	};

	private static class HeapOpRemove implements HeapOp {
		final int x;

		HeapOpRemove(int x) {
			this.x = x;
		}

		@Override
		public String toString() {
			return "R(" + x + ")";
		}
	};

	private static class HeapOpFindMin implements HeapOp {
		final int expected;

		HeapOpFindMin(int expected) {
			this.expected = expected;
		}

		@Override
		public String toString() {
			return "FM()=" + expected;
		}
	};

	private static class HeapOpExtractMin implements HeapOp {
		final int expected;

		HeapOpExtractMin(int expected) {
			this.expected = expected;
		}

		@Override
		public String toString() {
			return "EM()=" + expected;
		}
	};

	private static class HeapOpDecreaseKey implements HeapOp {
		final int key;
		final int newKey;

		HeapOpDecreaseKey(int key, int newKey) {
			this.key = key;
			this.newKey = newKey;
		}

		@Override
		public String toString() {
			return "DK()=" + key + "->" + newKey;
		}
	};

	static HeapOp[] randHeapOps(Heap<Integer> heap, int[] a, int m) {
		RandHeapOpsArgs args = new RandHeapOpsArgs();
		args.heap = heap;
		args.a = a;
		args.m = m;
		return randHeapOps(args);
	}

	private static class RandHeapOpsArgs {
		Heap<Integer> heap;
		int[] a;
		int m;
		int insertFirst;
		boolean decreaseKey;

		RandHeapOpsArgs() {
			heap = null;
			a = null;
			m = 0;
			insertFirst = 0;
			decreaseKey = false;
		}
	}

	static HeapOp[] randHeapOps(RandHeapOpsArgs args) {
		Heap<Integer> heap = args.heap;
		int[] a = args.a;
		int m = args.m;
		int insertFirst = args.insertFirst;
		boolean decreaseKey = args.decreaseKey;

		Random rand = new Random(TestUtils.nextRandSeed());
		HeapOp[] ops = new HeapOp[m];

		int[] elmsToInsertIds = Utils.randPermutation(a.length, TestUtils.nextRandSeed());
		int elmsToInsertCursor = 0;

		/* init inserted elms with current heap elements */
		NavigableMap<Integer, Integer> insertedElms = new TreeMap<>();
		for (Integer x : heap)
			insertedElms.compute(x, (x0, c) -> c == null ? 1 : c + 1);

		int x, expected;

		final int INSERT = 0;
		final int REMOVE = 1;
		final int FINDMIN = 2;
		final int EXTRACTMIN = 3;
		final int DECREASEKEY = 4;
		final int OP_COUT = decreaseKey ? 5 : 4;

		for (int op = 0; op < ops.length;) {
			int opIdx = op < insertFirst ? INSERT : rand.nextInt(OP_COUT);
			switch (opIdx) {
			case INSERT:
				if (elmsToInsertCursor >= elmsToInsertIds.length)
					continue;

				x = a[elmsToInsertIds[elmsToInsertCursor++]];
				insertedElms.compute(x, (x0, c) -> c == null ? 1 : c + 1);
				ops[op++] = new HeapOpInsert(x);
				break;

			case REMOVE:
				if (insertedElms.isEmpty())
					continue;

				x = rand.nextInt(a.length);
				if (rand.nextBoolean()) {
					Integer X = insertedElms.floorKey(x);
					x = X != null ? X : insertedElms.ceilingKey(x);
				} else {
					Integer X = insertedElms.ceilingKey(x);
					x = X != null ? X : insertedElms.floorKey(x);
				}

				insertedElms.compute(x, (x0, c) -> c == 1 ? null : c - 1);
				ops[op++] = new HeapOpRemove(x);
				break;

			case FINDMIN:
				if (insertedElms.isEmpty())
					continue;

				expected = insertedElms.firstKey();
				ops[op++] = new HeapOpFindMin(expected);
				break;

			case EXTRACTMIN:
				if (insertedElms.isEmpty())
					continue;

				expected = insertedElms.firstKey();
				insertedElms.compute(expected, (x0, c) -> c == 1 ? null : c - 1);
				ops[op++] = new HeapOpExtractMin(expected);
				break;

			case DECREASEKEY:
				if (insertedElms.isEmpty())
					continue;

				x = rand.nextInt(a.length);
				if (rand.nextBoolean()) {
					Integer X = insertedElms.floorKey(x);
					x = X != null ? X : insertedElms.ceilingKey(x);
				} else {
					Integer X = insertedElms.ceilingKey(x);
					x = X != null ? X : insertedElms.floorKey(x);
				}
				if (x == 0)
					continue;

				int newVal = rand.nextInt(x);

				insertedElms.compute(x, (x0, c) -> c == 1 ? null : c - 1);
				insertedElms.compute(newVal, (x0, c) -> c == null ? 1 : c + 1);
				ops[op++] = new HeapOpDecreaseKey(x, newVal);
				break;
			default:
				break;
			}
		}
		return ops;
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16, 16 }, { 128, 64, 128 }, { 64, 512, 1024 }, { 16, 4096, 8096 },
				{ 8, 16384, 32768 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			return testRandOps(heapBuilder, n, m);
		});
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder, int n, int m) {
		Heap<Integer> heap = heapBuilder.get();
		int[] a = Utils.randArray(n, 0, 65536, TestUtils.nextRandSeed());
		HeapOp[] ops = randHeapOps(heap, a, m);

		return testHeap(heap, a, ops, true);
	}

	static boolean testRandOpsAfterManyInserts(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16, 16 }, { 128, 64, 128 }, { 64, 512, 1024 }, { 16, 4096, 8096 },
				{ 8, 16384, 32768 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();
			int[] a = Utils.randArray(n, 0, 65536, TestUtils.nextRandSeed());

			RandHeapOpsArgs heapArgs = new RandHeapOpsArgs();
			heapArgs.heap = heap;
			heapArgs.a = a;
			heapArgs.m = m;
			heapArgs.insertFirst = m / 2;
			HeapOp[] ops = randHeapOps(heapArgs);

			return testHeap(heap, a, ops, true);
		});
	}

	static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 64, 16 }, { 64, 32 }, { 8, 256 }, { 1, 2048 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int hCount = args[1];
			@SuppressWarnings("unchecked")
			Heap<Integer>[] hs = new Heap[hCount];
			@SuppressWarnings("unchecked")
			Heap<Integer>[] hsNext = new Heap[hCount / 2];

			for (int i = 0; i < hCount; i++) {
				Heap<Integer> h = hs[i] = heapBuilder.get();

				int[] a = Utils.randArray(16, 0, 65536, TestUtils.nextRandSeed());
				RandHeapOpsArgs heapArgs = new RandHeapOpsArgs();
				heapArgs.heap = h;
				heapArgs.a = a;
				heapArgs.m = 16;
				heapArgs.insertFirst = 8;
				HeapOp[] ops = randHeapOps(heapArgs);
				if (!testHeap(h, a, ops, false))
					return false;
			}

			while (hCount > 1) {
				/* meld half of the heaps */
				int[] meldOrder = Utils.randPermutation(hCount & ~1, TestUtils.nextRandSeed());
				for (int i = 0; i < meldOrder.length / 2; i++) {
					int h1Idx = meldOrder[i * 2], h2Idx = meldOrder[i * 2 + 1];
					Heap<Integer> h1 = hs[h1Idx], h2 = hs[h2Idx];

					h1.meld(h2);
					hs[h2Idx] = null;

					/* make some OPs on the united heap */
					int opsNum = 4096 / hCount;
					int[] a = Utils.randArray(opsNum, 0, 65536, TestUtils.nextRandSeed());
					RandHeapOpsArgs heapArgs = new RandHeapOpsArgs();
					heapArgs.heap = h1;
					heapArgs.a = a;
					heapArgs.m = opsNum;
					heapArgs.insertFirst = opsNum / 2;
					HeapOp[] ops = randHeapOps(heapArgs);
					if (!testHeap(h1, a, ops, false))
						return false;
				}

				/* contract heap array */
				int hCountNext = 0;
				for (int i = 0; i < hCount; i++)
					if (hs[i] != null)
						hsNext[hCountNext++] = hs[i];
				Heap<Integer>[] temp = hs;
				hs = hsNext;
				hsNext = temp;
				hCount = hCountNext;
			}

			return true;
		});
	}

	static boolean testDecreaseKey(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 256, 16 }, { 128, 64 }, { 64, 512 }, { 16, 4096 }, { 2, 16384 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = n;
			Heap<Integer> heap = heapBuilder.get();
			int[] a = Utils.randArray(n, 0, 65536, TestUtils.nextRandSeed());

			RandHeapOpsArgs heapArgs = new RandHeapOpsArgs();
			heapArgs.heap = heap;
			heapArgs.a = a;
			heapArgs.m = m;
			heapArgs.decreaseKey = true;
			HeapOp[] ops = randHeapOps(heapArgs);

			return testHeap(heap, a, ops, true);
		});
	}

	static boolean testHeap(Heap<Integer> heap, int[] a, HeapOp[] ops, boolean clear) {
		if (clear) {
			heap.clear();
			if (heap.size() != 0 || !heap.isEmpty()) {
				TestUtils.printTestStr("failed clear\n");
				return false;
			}
		}

		for (HeapOp op0 : ops) {
			if (op0 instanceof HeapOpInsert) {
				HeapOpInsert op = (HeapOpInsert) op0;
				heap.insert(op.x);
			} else if (op0 instanceof HeapOpRemove) {
				HeapOpRemove op = (HeapOpRemove) op0;
				if (!heap.remove(Integer.valueOf(op.x))) {
					TestUtils.printTestStr("failed to remove: " + op.x + "\n");
					return false;
				}
			} else if (op0 instanceof HeapOpFindMin) {
				HeapOpFindMin op = (HeapOpFindMin) op0;
				int actual = heap.findMin();
				if (actual != op.expected) {
					TestUtils.printTestStr("failed findmin: " + op.expected + " != " + actual + "\n");
					return false;
				}
			} else if (op0 instanceof HeapOpExtractMin) {
				HeapOpExtractMin op = (HeapOpExtractMin) op0;
				int actual = heap.extractMin();
				if (actual != op.expected) {
					TestUtils.printTestStr("failed extractmin: " + op.expected + " != " + actual + "\n");
					return false;
				}
			} else if (op0 instanceof HeapOpDecreaseKey) {
				HeapOpDecreaseKey op = (HeapOpDecreaseKey) op0;
				heap.decreaseKey(heap.findHanlde(op.key), op.newKey);
			} else
				throw new IllegalArgumentException("Unknown OP: " + op0);
		}

		if (clear) {
			heap.clear();
			if (heap.size() != 0 || !heap.isEmpty()) {
				TestUtils.printTestStr("failed clear\n");
				return false;
			}
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
