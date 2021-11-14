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
//	while (testHeap(heapBuilder, 128, 128))
//	    ;
//	return false;
    }

    static boolean testHeap(Supplier<? extends Heap<Integer>> heapBuilder, int n, int m) {
	int[] a = Utils.randArray(n, 0, 128);
	HeapOp[] ops = randHeapOps(a, m);

//	int[] a = {19, 106, 53, 122, 36, 59, 42, 14, 99, 33, 75, 79, 120, 50, 124, 55, 7, 112, 119, 42, 111, 102, 125, 72, 9, 4, 75, 68, 2, 111, 93, 104, 60, 31, 108, 23, 41, 96, 3, 86, 108, 79, 27, 24, 31, 6, 77, 92, 45, 60, 8, 56, 120, 60, 125, 22, 33, 89, 64, 29, 91, 107, 1, 14, 63, 3, 24, 107, 76, 65, 93, 124, 116, 69, 92, 121, 119, 81, 126, 89, 117, 65, 41, 11, 32, 113, 76, 55, 30, 73, 57, 12, 53, 93, 5, 127, 13, 124, 10, 120, 8, 24, 93, 104, 113, 114, 90, 105, 79, 86, 28, 32, 33, 6, 108, 95, 78, 50, 21, 44, 25, 25, 46, 116, 2, 24, 95, 19};
//	HeapOp[] ops = parseOpsStr("[I(94), FM()=94, I(19), R(19), EM()=94, I(48), FM()=48, I(127), EM()=127, I(114), R(48), I(101), EM()=101, EM()=114, I(39), EM()=39, I(15), EM()=15, I(117), FM()=117, I(32), I(76), R(117), I(111), FM()=111, FM()=111, FM()=111, EM()=111, FM()=32, I(44), I(82), I(93), FM()=44, EM()=44, EM()=82, R(76), I(36), R(32), I(8), FM()=36, R(8), FM()=36, EM()=36, FM()=93, EM()=93, I(13), I(121), I(26), I(18), FM()=121, FM()=121, I(53), I(12), I(57), FM()=121, FM()=121, FM()=121, R(121), FM()=13, FM()=13, FM()=13, FM()=13, I(11), I(105), FM()=13, FM()=13, R(57), I(14), R(13), R(14), FM()=53, I(34), I(106), R(106), EM()=53, FM()=26, FM()=26, EM()=26, I(67), FM()=11, EM()=11, I(78), FM()=67, FM()=67, FM()=67, R(18), EM()=67, I(89), EM()=89, I(62), FM()=62, FM()=62, EM()=62, R(12), R(34), EM()=105, EM()=78, I(2), R(2), I(113), FM()=113, R(113), I(23), FM()=23, R(23), I(21), EM()=21, I(124), R(124), I(90), FM()=90, R(90), I(80), R(80), I(37), I(63), I(77), EM()=63, I(109), EM()=77, FM()=109, EM()=109, FM()=37, EM()=37, I(20), EM()=20, I(6), EM()=6]");

	return testHeap(heapBuilder, a, ops);
    }

    static boolean testHeap(Supplier<? extends Heap<Integer>> heapBuilder, int[] a, HeapOp[] ops) {
	if (testHeap0(heapBuilder, a, ops))
	    return true;

	TestUtils.printTestStr(Arrays.toString(a) + "\n");
	TestUtils.printTestStr(Arrays.toString(ops) + "\n");
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
//	    System.out.println("" + heap + " " + op0);

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

}
