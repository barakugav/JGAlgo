package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SSSPGoldberg;

public class SSSPGoldbergTest extends TestBase {

	@Test
	public void testRandGraphPositiveInt() {
		final long seed = 0x502218b82d4ab25aL;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg::new, seed);
	}

	@Test
	public void testRandGraphNegativeInt() {
		final long seed = 0x15f829173b4f088bL;
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg::new, seed);
	}

}
