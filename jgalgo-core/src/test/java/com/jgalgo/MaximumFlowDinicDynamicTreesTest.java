package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowDinicDynamicTreesTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowDinicDynamicTrees(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x57895831bc5f0b59L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowDinicDynamicTrees());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}