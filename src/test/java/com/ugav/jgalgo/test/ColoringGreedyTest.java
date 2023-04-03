package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Coloring;
import com.ugav.jgalgo.ColoringGreedy;

public class ColoringGreedyTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0xe57268894020f1d1L;
		Coloring algo = new ColoringGreedy();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
