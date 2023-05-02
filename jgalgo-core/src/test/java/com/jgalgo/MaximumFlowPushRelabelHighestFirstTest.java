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

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0xe70353b8637b68d2L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelHighestFirst(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x8f2b92e482d65052L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelHighestFirst());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
