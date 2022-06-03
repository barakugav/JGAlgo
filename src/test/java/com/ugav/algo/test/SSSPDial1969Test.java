package com.ugav.algo.test;

import com.ugav.algo.SSSPDial1969;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPDirectedPositiveInt(SSSPDial1969::new);
	}

	@Test
	public static boolean randGraphUndirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPUndirectedPositiveInt(SSSPDial1969::new);
	}

}
