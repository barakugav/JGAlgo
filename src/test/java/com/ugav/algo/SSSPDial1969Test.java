package com.ugav.algo;

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
