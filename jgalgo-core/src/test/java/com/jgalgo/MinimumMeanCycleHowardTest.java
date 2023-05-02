package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MinimumMeanCycleHowardTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x6968128e5b6c70dfL;
		MinimumMeanCycleTestUtils.testMinimumMeanCycle(new MinimumMeanCycleHoward(), seed);
	}

}
