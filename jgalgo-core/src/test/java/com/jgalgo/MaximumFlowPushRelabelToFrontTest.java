package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelToFrontTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0x8fb191d57a090f45L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x3d296bd5e39fbefbL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x5817e5c904a5dad1L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelToFront(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc833101540b8e5f1L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelToFront());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
