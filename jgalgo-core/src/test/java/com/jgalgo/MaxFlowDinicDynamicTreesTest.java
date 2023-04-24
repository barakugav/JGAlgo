package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowDinicDynamicTreesTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinicDynamicTrees::new, seed);
	}

}