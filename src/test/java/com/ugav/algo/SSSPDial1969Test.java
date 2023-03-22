package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial1969::new);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial1969::new);
	}

}
