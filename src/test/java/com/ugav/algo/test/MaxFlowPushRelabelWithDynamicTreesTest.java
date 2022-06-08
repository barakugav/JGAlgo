package com.ugav.algo.test;

import com.ugav.algo.MaxFlowPushRelabelWithDynamicTrees;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public static boolean randGraphs() {
		return MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new);
	}
}
