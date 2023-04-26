package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumFlowDinicTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xa79b303ec46fd984L;
		MaximumFlowTestUtils.testRandGraphs(MaximumFlowDinic::new, seed);
	}

}