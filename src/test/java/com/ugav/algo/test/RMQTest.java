package com.ugav.algo.test;

import com.ugav.algo.RMQ;
import com.ugav.algo.RMQLookupTable;
import com.ugav.algo.RMQ.IntArrayComperator;

public class RMQTest extends Tests.TestTemplate {

    public static boolean test1(RMQ rmq) {
	int a[] = { 35, 1, 2, 62, 10, 77, 19, 91, 5, 97, 39, 75, 68, 69, 82, 24, 76, 19, 89, 26, 22, 23, 8, 80, 57 };
	int queries[][] = { { 5, 21, 8 }, { 9, 19, 17 }, { 8, 21, 8 }, { 1, 2, 1 }, { 2, 16, 2 }, { 0, 25, 1 },
		{ 12, 23, 22 }, { 2, 4, 2 }, { 12, 13, 12 }, { 0, 10, 1 }, { 1, 16, 1 }, { 0, 21, 1 }, { 6, 10, 8 },
		{ 8, 9, 8 }, { 18, 24, 22 }, { 0, 1, 0 }, { 0, 2, 1 }, { 0, 3, 1 }, { 1, 3, 1 }, { 0, 25, 1 },
		{ 10, 25, 22 }, { 22, 25, 22 }, { 23, 25, 24 }, { 24, 25, 24 } };

	RMQ.Result res = rmq.preprocessRMQ(new IntArrayComperator(a), a.length);

	for (int idx = 0; idx < queries.length; idx++) {
	    int i = queries[idx][0];
	    int j = queries[idx][1];
	    int expected = queries[idx][2];
	    int actual = res.query(i, j);
	    if (actual != expected) {
		System.out.println(getTestPrefix() + " i,j,e,a = " + i + " " + j + " " + expected + " " + actual + " ");
		printFailure();
		return false;
	    }
	}

	printPassed();
	return true;
    }

    public static boolean runTests() {
	RMQ rmq = RMQLookupTable.getInstace();

	boolean passed = true;

	passed &= test1(rmq);

	return passed;
    }

}
