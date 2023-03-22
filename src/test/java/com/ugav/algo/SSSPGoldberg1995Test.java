package com.ugav.algo;

import org.junit.jupiter.api.Test;

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
