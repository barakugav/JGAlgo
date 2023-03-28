package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowPushRelabelWithDynamicTrees;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new, seed);
	}
}
