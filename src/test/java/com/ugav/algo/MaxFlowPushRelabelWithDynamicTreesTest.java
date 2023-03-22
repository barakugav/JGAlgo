package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new);
	}
}
