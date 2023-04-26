package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelToFrontTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x8fb191d57a090f45L;
		MaximumFlowTestUtils.testRandGraphs(MaximumFlowPushRelabelToFront::new, seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x3d296bd5e39fbefbL;
		MaximumFlowTestUtils.testRandGraphsInt(MaximumFlowPushRelabelToFront::new, seed);
	}
}
