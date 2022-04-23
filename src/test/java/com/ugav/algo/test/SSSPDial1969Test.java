package com.ugav.algo.test;

import com.ugav.algo.SSSPDial1969;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPDirectedPositiveInt(new SSSPDial1969());
	}

	@Test
	public static boolean randGraphUndirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPUndirectedPositiveInt(new SSSPDial1969());
	}

}
