package com.ugav.algo.test;

import com.ugav.algo.SSSPDial1969;

public class SSSPDial1969Test {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial1969.getInstace());
	}

	@Test
	public static boolean randGraphUndirectedPositiveInt() {
		return SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial1969.getInstace());
	}

}
