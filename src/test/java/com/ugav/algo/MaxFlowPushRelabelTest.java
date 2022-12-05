package com.ugav.algo;

public class MaxFlowPushRelabelTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new);
	}
}
