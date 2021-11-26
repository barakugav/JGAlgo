package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.Heap;

class HeapTestUtils {

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

	static HeapOp[] randHeapOps(int[] a, int m) {
		Random rand = new Random();
		HeapOp[] ops = new HeapOp[m];

		int[] elmsToInsert = Utils.randPermutation(a.length);
		int elmsToInsertCursor = 0;

		ArrayList<Integer> insertedElms = new ArrayList<>();
		int x, expected;

		for (int op = 0; op < ops.length;) {
			switch (rand.nextInt(4)) {
			case 0:
				if (elmsToInsertCursor >= elmsToInsert.length)
					continue;

				x = elmsToInsert[elmsToInsertCursor++];
				insertedElms.add(x);
				ops[op++] = new HeapOpInsert(x);
				break;

			case 1:
				if (insertedElms.isEmpty())
					continue;

				int idx = rand.nextInt(insertedElms.size());
				x = insertedElms.remove(idx);
				ops[op++] = new HeapOpRemove(x);
				break;

			case 2:
				if (insertedElms.isEmpty())
					continue;

				expected = Collections.min(insertedElms, (i1, i2) -> Integer.compare(a[i1], a[i2]));
				ops[op++] = new HeapOpFindMin(expected);
				break;

			case 3:
				if (insertedElms.isEmpty())
					continue;

				expected = Collections.min(insertedElms, (i1, i2) -> Integer.compare(a[i1], a[i2]));
				insertedElms.remove(Integer.valueOf(expected));
				ops[op++] = new HeapOpExtractMin(expected);

			default:
				break;
			}
		}
		return ops;
	}

	static boolean testHeap(Supplier<? extends Heap<Integer>> heapBuilder) {
		return testHeap(heapBuilder, 4096, 4096);
	}

	static boolean testHeap(Supplier<? extends Heap<Integer>> heapBuilder, int n, int m) {
		int[] a = Utils.randArray(n, 0, 65536);
		HeapOp[] ops = randHeapOps(a, m);

		return testHeap(heapBuilder, a, ops);
	}

	static boolean testHeap(Supplier<? extends Heap<Integer>> heapBuilder, int[] a, HeapOp[] ops) {
		RuntimeException e = null;
		try {
			if (testHeap0(heapBuilder, a, ops))
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

	static boolean testHeap0(Supplier<? extends Heap<Integer>> heapBuilder, int[] a, HeapOp[] ops) {
		Heap<Integer> heap = heapBuilder.get();

		heap.clear();
		if (heap.size() != 0 || !heap.isEmpty()) {
			TestUtils.printTestStr("failed clear\n");
			return false;
		}

		for (HeapOp op0 : ops) {
			if (op0 instanceof HeapOpInsert) {
				HeapOpInsert op = (HeapOpInsert) op0;
				heap.insert(a[op.x]);
			} else if (op0 instanceof HeapOpRemove) {
				HeapOpRemove op = (HeapOpRemove) op0;
				if (!heap.remove(Integer.valueOf(a[op.x]))) {
					TestUtils.printTestStr("failed to remove: [" + op.x + "]=" + a[op.x] + "\n");
					return false;
				}
			} else if (op0 instanceof HeapOpFindMin) {
				HeapOpFindMin op = (HeapOpFindMin) op0;
				int actual = heap.findMin();
				if (actual != a[op.expected]) {
					TestUtils.printTestStr("failed findmin: " + a[op.expected] + " != " + actual + "\n");
					return false;
				}
			} else if (op0 instanceof HeapOpExtractMin) {
				HeapOpExtractMin op = (HeapOpExtractMin) op0;
				int actual = heap.extractMin();
				if (actual != a[op.expected]) {
					TestUtils.printTestStr("failed extractmin: " + a[op.expected] + " != " + actual + "\n");
					return false;
				}
			}
		}

		heap.clear();
		if (heap.size() != 0 || !heap.isEmpty()) {
			TestUtils.printTestStr("failed clear\n");
			return false;
		}
		return true;
	}

	private static <E> boolean testHeapSize(Heap<E> h) {
		int expected = h.size();
		int actual = 0;
		for (E e : h)
			actual++;
		return expected == actual;

	}

}
