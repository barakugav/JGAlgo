package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelLowestFirstTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0xa3401ed1fd71bd97L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelLowestFirst::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x0204011e1b393aaaL;
		MaxFlowTestUtils.testRandGraphsInt(MaxFlowPushRelabelLowestFirst::new, seed);
	}
}
