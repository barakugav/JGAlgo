package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x87662c130902cf06L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabel(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x482aaa129b8af846L;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabel(), seed);
	}
}
