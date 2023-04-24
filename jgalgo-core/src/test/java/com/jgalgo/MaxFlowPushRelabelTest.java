package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x87662c130902cf06L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x482aaa129b8af846L;
		MaxFlowTestUtils.testRandGraphsInt(MaxFlowPushRelabel::new, seed);
	}
}
