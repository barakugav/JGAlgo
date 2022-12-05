package com.ugav.algo;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new);
	}
}
