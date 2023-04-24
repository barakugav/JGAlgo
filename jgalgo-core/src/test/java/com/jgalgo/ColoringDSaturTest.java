package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringDSaturTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xaf95beb0ce86b8f2L;
		Coloring algo = new ColoringDSatur();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
