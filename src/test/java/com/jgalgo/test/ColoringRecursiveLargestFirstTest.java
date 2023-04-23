package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.Coloring;
import com.jgalgo.ColoringRecursiveLargestFirst;

public class ColoringRecursiveLargestFirstTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xc6f079efd56fc216L;
		Coloring algo = new ColoringRecursiveLargestFirst();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
