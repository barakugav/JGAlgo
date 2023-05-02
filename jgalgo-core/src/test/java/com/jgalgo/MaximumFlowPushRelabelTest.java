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

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x3ce112da3fadf191L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabel(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc833101540b8e5f1L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabel());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
