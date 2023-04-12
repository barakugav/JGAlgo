package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaxFlowPushRelabelDynamicTrees;

public class MaxFlowPushRelabelDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelDynamicTrees::new, seed);
	}
}
