package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringDSaturHeapTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x899cb1808437b167L;
		Coloring algo = new ColoringDSaturHeap();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
