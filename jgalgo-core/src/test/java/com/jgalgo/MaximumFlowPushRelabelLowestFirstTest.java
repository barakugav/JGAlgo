package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowPushRelabelLowestFirstTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0xa3401ed1fd71bd97L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x0204011e1b393aaaL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x9a378c8dd98b3bceL;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5fc7b22045f53253L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelLowestFirst());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
