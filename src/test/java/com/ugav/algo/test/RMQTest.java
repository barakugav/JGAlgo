package com.ugav.algo.test;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

import com.ugav.algo.RMQ;
import com.ugav.algo.RMQ.IntArrayComperator;
import com.ugav.algo.RMQLookupTable;
import com.ugav.algo.RMQPlusMinusOne;
import com.ugav.algo.RMQPowerOf2Table;

public class RMQTest extends Tests.TestTemplate {

    private static boolean testRMQ(RMQ rmq) {
	int a[] = { 35, 1, 2, 62, 10, 77, 19, 91, 5, 97, 39, 75, 68, 69, 82, 24, 76, 19, 89, 26, 22, 23, 8, 80, 57 };
	int queries[][] = { { 5, 21, 8 }, { 9, 19, 17 }, { 8, 21, 8 }, { 1, 2, 1 }, { 2, 16, 2 }, { 0, 25, 1 },
		{ 12, 23, 22 }, { 2, 4, 2 }, { 12, 13, 12 }, { 0, 10, 1 }, { 1, 16, 1 }, { 0, 21, 1 }, { 6, 10, 8 },
		{ 8, 9, 8 }, { 18, 24, 22 }, { 0, 1, 0 }, { 0, 2, 1 }, { 0, 3, 1 }, { 1, 3, 1 }, { 0, 25, 1 },
		{ 10, 25, 22 }, { 22, 25, 22 }, { 23, 25, 24 }, { 24, 25, 24 } };
	return testRMQ(rmq, a, queries);
    }

    private static boolean testRMQ(RMQ rmq, int a[], int queries[][]) {
	RMQ.Result res = rmq.preprocessRMQ(new IntArrayComperator(a), a.length);

	for (int idx = 0; idx < queries.length; idx++) {
	    int i = queries[idx][0];
	    int j = queries[idx][1];
	    int expectedIdx = queries[idx][2];
	    int expected = a[expectedIdx];
	    int actualIdx = res.query(i, j);
	    int actual = a[actualIdx];

	    if (actual != expected) {
		System.out.println(getTestPrefix() + " [" + i + "," + j + "] -> expected[" + expectedIdx + "]="
			+ expected + " actual[" + actualIdx + "]=" + actual);
		printTestFailure();
		return false;
	    }
	}

	printTestPassed();
	return true;

    }

    public static boolean test_RMQLookupTable() {
	printTestBegin();
	return testRMQ(RMQLookupTable.getInstace());
    }

    public static boolean test_RMQPowerOf2Table() {
	printTestBegin();
	return testRMQ(RMQPowerOf2Table.getInstace());
    }

    public static boolean test_RMQPlusMinusOne() {
	printTestBegin();

	int a[] = new int[128];
	int queries[][] = new int[64][];
	randRMQDataAndQueries(a, queries);
	printTestStr(printRMQDataAndQueries(a, queries));

	return testRMQ(RMQPlusMinusOne.getInstace(), a, queries);
    }

    public static boolean test_RMQPlusMinusOneOnlyInterBlock() {
	printTestBegin();

	int a[] = new int[128];
	int queries[][] = new int[64][];

	randRMQDataAndQueries(a, queries, 4);
	printTestStr(printRMQDataAndQueries(a, queries));

	return testRMQ(RMQPlusMinusOne.getInstace(), a, queries);
    }

    public static boolean runTests() {
	Collection<BooleanSupplier> tests = List.of(RMQTest::test_RMQLookupTable, RMQTest::test_RMQPowerOf2Table,
		RMQTest::test_RMQPlusMinusOne, RMQTest::test_RMQPlusMinusOneOnlyInterBlock);

	boolean passed = true;
	for (BooleanSupplier test : tests)
	    passed &= test.getAsBoolean();
	return passed;
    }

    private static void printArr(int a[]) {
	for (int i = 0; i < a.length; i++)
	    System.out.print("" + String.format("%03d", a[i]) + ", ");
	System.out.println();
	for (int i = 0; i < a.length; i++)
	    System.out.print("" + String.format("%03d", i) + ", ");
	System.out.println();
    }

    private static void randRMQDataAndQueries(int a[], int queries[][]) {
	randRMQDataAndQueries(a, queries, a.length);
    }

    private static void randRMQDataAndQueries(int a[], int queries[][], int blockSize) {
	Random rand = new Random();
	a[0] = 0;
	for (int i = 1; i < a.length; i++) {
	    a[i] = a[i - 1] + rand.nextInt(2) * 2 - 1;
	}

	for (int q = 0; q < queries.length;) {
	    int i = rand.nextInt(a.length);
	    if (i % blockSize == blockSize - 1)
		continue;
	    int blockBase = (i / blockSize) * blockSize;
	    int blockEnd = blockBase + blockSize;
	    int j = rand.nextInt(blockEnd - i) + i + 1;

	    int m = i;
	    for (int k = i; k < j; k++)
		if (a[k] < a[m])
		    m = k;
	    queries[q++] = new int[] { i, j, m };
	}
    }

    private static CharSequence printRMQDataAndQueries(int a[], int queries[][]) {
	StringBuilder s = new StringBuilder();

	s.append("{");
	for (int p : a)
	    s.append("" + p + ",");
	s.append("}\n");

	s.append("{");
	for (int q[] : queries)
	    s.append("{" + q[0] + "," + q[1] + "," + q[2] + "},");
	s.append("}\n");

	return s;
    }

}
