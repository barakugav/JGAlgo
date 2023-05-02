package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowDinicTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xa79b303ec46fd984L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowDinic(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0xb49154497703863bL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowDinic());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
