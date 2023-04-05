package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTKargerKleinTarjan1995;

public class MSTKargerKleinTarjan1995Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0xe76fc4911bdb2da2L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan1995(seedGen.nextSeed()), seedGen.nextSeed());
	}

}
