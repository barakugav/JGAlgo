package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelDynamicTreesTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		MaximumFlowTestUtils.testRandGraphs(MaximumFlowPushRelabelDynamicTrees::new, seed);
	}

	@Test
	public void testRandGraphsInt() {
		final long seed = 0x00dd5c7d6b25fe3bL;
		MaximumFlowTestUtils.testRandGraphsInt(MaximumFlowPushRelabelDynamicTrees::new, seed);
	}
}
