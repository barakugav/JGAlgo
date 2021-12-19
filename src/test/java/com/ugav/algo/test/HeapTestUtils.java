package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
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

		Random rand = new Random();
		HeapOp[] ops = new HeapOp[m];

		int[] elmsToInsertIds = Utils.randPermutation(a.length);
		int elmsToInsertCursor = 0;

		/* init inserted elms with current heap elements */
		ArrayList<Integer> insertedElms = new ArrayList<>(heap);

		int x, idx, expected;

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
				insertedElms.add(x);
				ops[op++] = new HeapOpInsert(x);
				break;

			case REMOVE:
				if (insertedElms.isEmpty())
					continue;

				idx = rand.nextInt(insertedElms.size());
				x = insertedElms.remove(idx);
				ops[op++] = new HeapOpRemove(x);
				break;

			case FINDMIN:
				if (insertedElms.isEmpty())
					continue;

				expected = Collections.min(insertedElms);
				ops[op++] = new HeapOpFindMin(expected);
				break;

			case EXTRACTMIN:
				if (insertedElms.isEmpty())
					continue;

				expected = Collections.min(insertedElms);
				insertedElms.remove(Integer.valueOf(expected));
				ops[op++] = new HeapOpExtractMin(expected);
				break;

			case DECREASEKEY:
				if (insertedElms.isEmpty())
					continue;
				idx = rand.nextInt(insertedElms.size());
				x = insertedElms.get(idx);
				if (x == 0)
					continue;
				int newVal = rand.nextInt(x);
				insertedElms.set(idx, newVal);
				ops[op++] = new HeapOpDecreaseKey(x, newVal);
				break;
			default:
				break;
			}
		}
		return ops;
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder) {
//		Heap<Integer> heap = heapBuilder.get();
//		int[] a = { 8550, 12335, 7429, 59755, 26173, 18733, 54000, 47724, 25220, 5457, 16011, 61501, 9803, 47463, 27927,
//				28759 };
//		HeapOp[] ops = parseOpsStr(
//				"{I(59755), EM()=59755, I(27927), I(61501), FM()=27927, FM()=27927, FM()=27927, I(8550), R(27927), R(8550), R(61501), I(47724), I(16011), R(47724), EM()=16011, I(47463)}");
//		if (!testHeap(heap, a, ops, true))
//			return false;

		int[][] phases = { { 128, 16, 16 }, { 64, 64, 64 }, { 8, 512, 512 }, { 1, 4096, 4096 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];
			for (int i = 0; i < repeat; i++)
				if (!testRandOps(heapBuilder, n, m))
					return false;
		}
		return true;
	}

	static boolean testRandOps(Supplier<? extends Heap<Integer>> heapBuilder, int n, int m) {
		Heap<Integer> heap = heapBuilder.get();
		int[] a = Utils.randArray(n, 0, 65536);
		HeapOp[] ops = randHeapOps(heap, a, m);

		return testHeap(heap, a, ops, true);
	}

	static boolean testRandOpsAfterManyInserts(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 128, 16 }, { 64, 64 }, { 8, 512 }, { 1, 4096 } };

		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = n;

			for (int i = 0; i < repeat; i++) {
				Heap<Integer> heap = heapBuilder.get();
				int[] a = Utils.randArray(n, 0, 65536);

				RandHeapOpsArgs args = new RandHeapOpsArgs();
				args.heap = heap;
				args.a = a;
				args.m = m;
				args.insertFirst = m / 2;
				HeapOp[] ops = randHeapOps(args);

				if (!testHeap(heap, a, ops, true))
					return false;
			}
		}
		return true;
	}

	static boolean testMeld(Supplier<? extends Heap<Integer>> heapBuilder) {
		int hCount = 256;
		@SuppressWarnings("unchecked")
		Heap<Integer>[] hs = new Heap[hCount];
		@SuppressWarnings("unchecked")
		Heap<Integer>[] hsNext = new Heap[hCount / 2];

		for (int i = 0; i < hCount; i++) {
			Heap<Integer> h = hs[i] = heapBuilder.get();

			int[] a = Utils.randArray(16, 0, 65536);
			RandHeapOpsArgs args = new RandHeapOpsArgs();
			args.heap = h;
			args.a = a;
			args.m = 16;
			args.insertFirst = 8;
			HeapOp[] ops = randHeapOps(args);
			if (!testHeap(h, a, ops, false))
				return false;
		}

		while (hCount > 1) {
			/* meld half of the heaps */
			int[] meldOrder = Utils.randPermutation(hCount & ~1);
			for (int i = 0; i < meldOrder.length / 2; i++) {
				int h1Idx = meldOrder[i * 2], h2Idx = meldOrder[i * 2 + 1];
				Heap<Integer> h1 = hs[h1Idx], h2 = hs[h2Idx];

				h1.meld(h2);
				hs[h2Idx] = null;

				/* make some OPs on the unioned heap */
				int opsNum = 4096 / hCount;
				int[] a = Utils.randArray(opsNum, 0, 65536);
				RandHeapOpsArgs args = new RandHeapOpsArgs();
				args.heap = h1;
				args.a = a;
				args.m = opsNum;
				args.insertFirst = opsNum / 2;
				HeapOp[] ops = randHeapOps(args);
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
	}

	static boolean testDecreaseKey(Supplier<? extends Heap<Integer>> heapBuilder) {
		int[][] phases = { { 128, 16 }, { 64, 64 }, { 8, 512 }, { 1, 4096 } };

		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = n;

			for (int i = 0; i < repeat; i++) {
				Heap<Integer> heap = heapBuilder.get();
				int[] a = Utils.randArray(n, 0, 65536);

				RandHeapOpsArgs args = new RandHeapOpsArgs();
				args.heap = heap;
				args.a = a;
				args.m = m;
				args.decreaseKey = true;
				HeapOp[] ops = randHeapOps(args);

				if (!testHeap(heap, a, ops, true))
					return false;
			}
		}
		return true;

	}

	static boolean testHeap(Heap<Integer> heap, int[] a, HeapOp[] ops, boolean clear) {
		RuntimeException e = null;
		try {
			if (testHeap0(heap, a, ops, clear))
				return true;
		} catch (RuntimeException e1) {
			e = e1;
		}
		TestUtils.printTestStr(Arrays.toString(a) + "\n");
		TestUtils.printTestStr(Arrays.toString(ops) + "\n");
		if (e != null)
			throw e;
		return false;
	}

	static boolean testHeap0(Heap<Integer> heap, int[] a, HeapOp[] ops, boolean clear) {
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
