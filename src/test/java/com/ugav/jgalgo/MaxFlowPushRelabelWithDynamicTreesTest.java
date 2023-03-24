package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new);
	}
}
