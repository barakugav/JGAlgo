package com.ugav.algo.test;

import com.ugav.algo.MaxFlowEdmondsKarp;

public class MaxFlowEdmondsKarpTest extends TestUtils {

	@Test
	public static boolean randGraphs() {
		return MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new);
	}

}
