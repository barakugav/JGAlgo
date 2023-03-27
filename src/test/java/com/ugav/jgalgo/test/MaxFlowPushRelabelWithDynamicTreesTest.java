package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowPushRelabelWithDynamicTrees;

public class MaxFlowPushRelabelWithDynamicTreesTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelWithDynamicTrees::new);
	}
}
