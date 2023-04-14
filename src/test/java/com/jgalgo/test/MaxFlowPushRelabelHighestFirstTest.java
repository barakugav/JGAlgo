package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaxFlowPushRelabelHighestFirst;

public class MaxFlowPushRelabelHighestFirstTest extends TestUtils {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x307ba0f4e538bcdfL;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelHighestFirst::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x5c4a4099bab06fdbL;
		MaxFlowTestUtils.testRandGraphsInt(MaxFlowPushRelabelHighestFirst::new, seed);
	}
}
