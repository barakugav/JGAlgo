package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Coloring;
import com.ugav.jgalgo.ColoringRecursiveLargestFirst;

public class ColoringRecursiveLargestFirstTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0xc6f079efd56fc216L;
		Coloring algo = new ColoringRecursiveLargestFirst();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
