package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowEdmondsKarpTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x398eea4097bc0600L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowEdmondsKarp(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0xaa7eab04a9b554cbL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowEdmondsKarp());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
