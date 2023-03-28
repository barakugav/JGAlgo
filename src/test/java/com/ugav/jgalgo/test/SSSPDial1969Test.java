package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPDial1969;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x1ecd0cadb4951d87L;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial1969::new, seed);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		final long seed = 0xadc83d79349e7784L;
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial1969::new, seed);
	}

}
