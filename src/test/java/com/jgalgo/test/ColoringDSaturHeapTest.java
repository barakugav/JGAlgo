package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.Coloring;
import com.jgalgo.ColoringDSaturHeap;

public class ColoringDSaturHeapTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0x899cb1808437b167L;
		Coloring algo = new ColoringDSaturHeap();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
