package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelHighestFirstTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x307ba0f4e538bcdfL;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelHighestFirst(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x5c4a4099bab06fdbL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelHighestFirst(), seed);
	}
}
