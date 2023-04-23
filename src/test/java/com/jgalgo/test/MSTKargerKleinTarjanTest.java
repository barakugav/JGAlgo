package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTKargerKleinTarjan;

public class MSTKargerKleinTarjanTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0xe76fc4911bdb2da2L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan(seedGen.nextSeed()), seedGen.nextSeed());
	}

}
