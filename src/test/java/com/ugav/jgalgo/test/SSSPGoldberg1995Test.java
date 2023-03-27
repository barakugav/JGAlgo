package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPGoldberg1995;

public class SSSPGoldberg1995Test extends TestUtils {

	@Test
	public void testRandGraphPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg1995::new);
	}

	@Test
	public void testRandGraphNegativeInt() {
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg1995::new);
	}

}
