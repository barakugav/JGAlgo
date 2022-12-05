package com.ugav.algo;

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
