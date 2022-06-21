package com.ugav.algo.test;

import com.ugav.algo.SSSPGoldberg1995;

public class SSSPGoldberg1995Test extends TestUtils {

	@Test
	public static void randGraphPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg1995::new);
	}

	@Test
	public static void randGraphNegativeInt() {
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg1995::new);
	}

}
