package com.jgalgo;

import org.junit.jupiter.api.Test;

class APSPJohnsonTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x0a3bf9517b5923b4L;
		APSPTestUtils.testAPSPDirectedPositiveInt(new APSPJohnson(), seed);
	}

	@Test
	public void testRandGraphDirectedNegativeInt() {
		final long seed = 0xbf0dd8e7294b5cecL;
		APSPTestUtils.testAPSPDirectedNegativeInt(new APSPJohnson(), seed);
	}

}
