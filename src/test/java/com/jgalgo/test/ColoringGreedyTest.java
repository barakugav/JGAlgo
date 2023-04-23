package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.Coloring;
import com.jgalgo.ColoringGreedy;

public class ColoringGreedyTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xe57268894020f1d1L;
		Coloring algo = new ColoringGreedy();
		ColoringTestUtils.testRandGraphs(() -> algo, seed);
	}

}
