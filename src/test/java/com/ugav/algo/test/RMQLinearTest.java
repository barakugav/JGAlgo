package com.ugav.algo.test;

import com.ugav.algo.RMQLinear;

public class RMQLinearTest {

    @Test
    public static boolean regular65536() {
	return RMQTestUtils.testRMQ(RMQLinear.getInstace(), 65536, 4096);
    }

    @Test
    public static boolean regularNRange64to256() {
	for (int n = 64; n <= 256; n++)
	    if (RMQTestUtils.testRMQ(RMQLinear.getInstace(), n, 1024) != true)
		return false;
	return true;
    }

    @Test
    public static boolean onlyInterBlock65536() {
	int a[] = new int[65536];
	int queries[][] = new int[4096][];
	RMQTestUtils.randRMQData(a);
	RMQTestUtils.randRMQQueries(a, queries, 4);

	return RMQTestUtils.testRMQ(RMQLinear.getInstace(), a, queries);
    }

}
