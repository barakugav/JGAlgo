package com.ugav.algo.test;

import com.ugav.algo.SSSPGoldberg1995;

public class SSSPGoldberg1995Test {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPGoldberg1995.getInstace());
	}

	@Test
	public static boolean randGraphNegativeInt() {
		return SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPGoldberg1995.getInstace());
	}

}
