package com.ugav.algo;

public class MaxFlowEdmondsKarpTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new);
	}

}
