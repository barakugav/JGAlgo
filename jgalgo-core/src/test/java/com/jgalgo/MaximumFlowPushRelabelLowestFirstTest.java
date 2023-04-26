package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelLowestFirstTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0xa3401ed1fd71bd97L;
		MaximumFlowTestUtils.testRandGraphs(MaximumFlowPushRelabelLowestFirst::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x0204011e1b393aaaL;
		MaximumFlowTestUtils.testRandGraphsInt(MaximumFlowPushRelabelLowestFirst::new, seed);
	}
}
