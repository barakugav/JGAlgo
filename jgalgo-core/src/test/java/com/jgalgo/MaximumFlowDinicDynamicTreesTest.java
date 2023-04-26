package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowDinicDynamicTreesTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaximumFlowTestUtils.testRandGraphs(MaximumFlowDinicDynamicTrees::new, seed);
	}

}