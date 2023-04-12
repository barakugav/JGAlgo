package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaxFlowDinicDynamicTrees;

public class MaxFlowDinicDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinicDynamicTrees::new, seed);
	}

}