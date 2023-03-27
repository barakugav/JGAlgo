package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RMQPlusMinusOneBenderFarachColton2000;

public class RMQPlusMinusOneBenderFarachColton2000Test extends TestUtils {

	@Test
	public void testRegular() {
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQTestUtils.randRMQDataPlusMinusOne(a);
		RMQTestUtils.randRMQQueries(a, queries, a.length);
		RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
	}

	@Test
	public void testRegularNRange64to256() {
		for (int n = 64; n <= 256; n++) {
			int[] a = new int[n];
			int[][] queries = new int[64][];
			RMQTestUtils.randRMQDataPlusMinusOne(a);
			RMQTestUtils.randRMQQueries(a, queries, a.length);

			RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
		}
	}

	@Test
	public void testOnlyInterBlock() {
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQTestUtils.randRMQDataPlusMinusOne(a);
		RMQTestUtils.randRMQQueries(a, queries, 4);
		RMQTestUtils.testRMQ(RMQPlusMinusOneBenderFarachColton2000::new, a, queries);
	}

}
