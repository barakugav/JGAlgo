package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringDSaturHeapTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x899cb1808437b167L;
		ColoringTestUtils.testRandGraphs(new ColoringDSaturHeap(), seed);
	}

}
