package com.ugav.algo.test;

import com.ugav.algo.SSSPGoldberg1995;

public class SSSPGoldberg1995Test extends TestUtils {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg1995::new);
	}

	@Test
	public static boolean randGraphNegativeInt() {
		return SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg1995::new);
	}

}
