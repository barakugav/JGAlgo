package com.jgalgo;

import org.junit.jupiter.api.Test;

public class ColoringGreedyRandomTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xc09142094f9b1e04L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ColoringTestUtils.testRandGraphs(new ColoringGreedyRandom(seedGen.nextSeed()), seedGen.nextSeed());
	}

}
