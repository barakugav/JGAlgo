package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MinimumMeanCycleDasdanGuptaTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x18400f641dec53f3L;
		MinimumMeanCycleTestUtils.testMinimumMeanCycle(new MinimumMeanCycleDasdanGupta(), seed);
	}

}
