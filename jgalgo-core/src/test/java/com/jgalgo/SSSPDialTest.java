package com.jgalgo;

import org.junit.jupiter.api.Test;

public class SSSPDialTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x1ecd0cadb4951d87L;
		SSSPTestUtils.testSSSPDirectedPositiveInt(new SSSPDial(), seed);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		final long seed = 0xadc83d79349e7784L;
		SSSPTestUtils.testSSSPUndirectedPositiveInt(new SSSPDial(), seed);
	}

}
