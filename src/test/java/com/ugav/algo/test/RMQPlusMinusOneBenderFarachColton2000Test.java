package com.ugav.algo.test;

import com.ugav.algo.RMQPlusMinusOneBenderFarachColton2000;

public class RMQPlusMinusOneBenderFarachColton2000Test extends TestUtils {

	@Test
	public static void regular() {
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQTestUtils.randRMQDataPlusMinusOne(a);
		RMQTestUtils.randRMQQueries(a, queries, a.length);
		RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
	}

	@Test
	public static void regularNRange64to256() {
		for (int n = 64; n <= 256; n++) {
			int[] a = new int[n];
			int[][] queries = new int[64][];
			RMQTestUtils.randRMQDataPlusMinusOne(a);
			RMQTestUtils.randRMQQueries(a, queries, a.length);

			RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
		}
	}

	@Test
	public static void onlyInterBlock() {
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQTestUtils.randRMQDataPlusMinusOne(a);
		RMQTestUtils.randRMQQueries(a, queries, 4);
		RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
	}

}
