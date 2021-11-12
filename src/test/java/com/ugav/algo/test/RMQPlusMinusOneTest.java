package com.ugav.algo.test;

import com.ugav.algo.RMQPlusMinusOne;

public class RMQPlusMinusOneTest {

    @Test
    public static boolean regular() {
	int a[] = new int[128];
	int queries[][] = new int[64][];
	RMQTestUtils.randRMQDataPlusMinusOne(a);
	RMQTestUtils.randRMQQueries(a, queries, a.length);

	return RMQTestUtils.testRMQ(RMQPlusMinusOne.getInstace(), a, queries);
    }

    @Test
    public static boolean regularNRange64to256() {
	for (int n = 64; n <= 256; n++) {
	    int a[] = new int[n];
	    int queries[][] = new int[64][];
	    RMQTestUtils.randRMQDataPlusMinusOne(a);
	    RMQTestUtils.randRMQQueries(a, queries, a.length);

	    if (RMQTestUtils.testRMQ(RMQPlusMinusOne.getInstace(), a, queries) != true)
		return false;
	}
	return true;
    }

    @Test
    public static boolean onlyInterBlock() {
	int a[] = new int[128];
	int queries[][] = new int[64][];
	RMQTestUtils.randRMQDataPlusMinusOne(a);
	RMQTestUtils.randRMQQueries(a, queries, 4);

	return RMQTestUtils.testRMQ(RMQPlusMinusOne.getInstace(), a, queries);
    }

}
