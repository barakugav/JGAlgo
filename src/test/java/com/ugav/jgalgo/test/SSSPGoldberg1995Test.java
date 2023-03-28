package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPGoldberg1995;

public class SSSPGoldberg1995Test extends TestUtils {

	@Test
	public void testRandGraphPositiveInt() {
		final long seed = 0x502218b82d4ab25aL;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg1995::new, seed);
	}

	@Test
	public void testRandGraphNegativeInt() {
		final long seed = 0x15f829173b4f088bL;
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg1995::new, seed);
	}

}
