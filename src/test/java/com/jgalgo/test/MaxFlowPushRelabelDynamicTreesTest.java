package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaxFlowPushRelabelDynamicTrees;

public class MaxFlowPushRelabelDynamicTreesTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelDynamicTrees::new, seed);
	}

	@Test
	public void testRandGraphsInt() {
		final long seed = 0x00dd5c7d6b25fe3bL;
		MaxFlowTestUtils.testRandGraphsInt(MaxFlowPushRelabelDynamicTrees::new, seed);
	}
}
