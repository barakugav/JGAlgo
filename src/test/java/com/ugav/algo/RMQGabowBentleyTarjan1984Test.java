package com.ugav.algo;

public class RMQGabowBentleyTarjan1984Test extends TestUtils {

	@Test
	public static void regular65536() {
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, 65536, 4096);
	}

	@Test
	public static void regularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, n, 1024);
	}

	@Test
	public static void onlyInterBlock65536() {
		int[] a = randArray(65536, 0, 64, nextRandSeed());
		int[][] queries = new int[4096][];
		RMQTestUtils.randRMQQueries(a, queries, 4);
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, a, queries);
	}

}
