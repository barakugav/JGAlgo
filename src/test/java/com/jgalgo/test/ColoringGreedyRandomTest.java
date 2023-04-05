package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.ColoringGreedyRandom;
import com.jgalgo.test.TestUtils.SeedGenerator;

public class ColoringGreedyRandomTest {

	@Test
	public void testRandGraphs() {
		final long seed = 0xc09142094f9b1e04L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ColoringTestUtils.testRandGraphs(() -> new ColoringGreedyRandom(seedGen.nextSeed()), seedGen.nextSeed());
	}

}
