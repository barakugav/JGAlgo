package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowPushRelabelToFrontTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x8fb191d57a090f45L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabelToFront::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x3d296bd5e39fbefbL;
		MaxFlowTestUtils.testRandGraphsInt(MaxFlowPushRelabelToFront::new, seed);
	}
}
