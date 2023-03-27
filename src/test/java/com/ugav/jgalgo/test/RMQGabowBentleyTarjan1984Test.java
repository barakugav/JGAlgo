package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RMQGabowBentleyTarjan1984;

public class RMQGabowBentleyTarjan1984Test extends TestUtils {

	@Test
	public void testRegular65536() {
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, 65536, 4096);
	}

	@Test
	public void testRegularNRange64to256() {
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, n, 1024);
	}

	@Test
	public void testOnlyInterBlock65536() {
		int[] a = randArray(65536, 0, 64, nextRandSeed());
		int[][] queries = new int[4096][];
		RMQTestUtils.randRMQQueries(a, queries, 4);
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, a, queries);
	}

}
