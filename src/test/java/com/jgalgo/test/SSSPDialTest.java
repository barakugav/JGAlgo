package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SSSPDial;

public class SSSPDialTest extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x1ecd0cadb4951d87L;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial::new, seed);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		final long seed = 0xadc83d79349e7784L;
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial::new, seed);
	}

}
