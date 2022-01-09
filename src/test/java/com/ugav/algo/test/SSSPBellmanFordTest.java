package com.ugav.algo.test;

import com.ugav.algo.SSSPBellmanFord;

public class SSSPBellmanFordTest {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord.getInstace());
	}

	@Test
	public static boolean randGraphNegativeInt() {
		return SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPBellmanFord.getInstace());
	}

}
