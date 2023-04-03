package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Coloring;
import com.ugav.jgalgo.ColoringDSaturHeap;

public class ColoringDSaturHeapTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0x899cb1808437b167L;
		Coloring algo = new ColoringDSaturHeap();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
