package com.ugav.algo.test;

import com.ugav.algo.SSSPDial1969;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public static void randGraphDirectedPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial1969::new);
	}

	@Test
	public static void randGraphUndirectedPositiveInt() {
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial1969::new);
	}

}
