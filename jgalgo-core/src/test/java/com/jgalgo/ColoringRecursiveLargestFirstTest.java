package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringRecursiveLargestFirstTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xc6f079efd56fc216L;
		ColoringTestUtils.testRandGraphs(new ColoringRecursiveLargestFirst(), seed);
	}

}
