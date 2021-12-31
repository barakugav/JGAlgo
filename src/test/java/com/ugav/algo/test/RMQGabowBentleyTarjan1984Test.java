package com.ugav.algo.test;

import com.ugav.algo.RMQGabowBentleyTarjan1984;

public class RMQGabowBentleyTarjan1984Test {

	@Test
	public static boolean regular65536() {
		return RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984.getInstace(), 65536, 4096);
	}

	@Test
	public static boolean regularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			if (RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984.getInstace(), n, 1024) != true)
				return false;
		return true;
	}

	@Test
	public static boolean onlyInterBlock65536() {
		int a[] = Utils.randArray(65536, 0, 64, TestUtils.nextRandSeed());
		int queries[][] = new int[4096][];
		RMQTestUtils.randRMQQueries(a, queries, 4);

		return RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984.getInstace(), a, queries);
	}

}
