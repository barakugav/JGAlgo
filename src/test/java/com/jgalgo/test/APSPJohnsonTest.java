package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.APSPJohnson;

class APSPJohnsonTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x0a3bf9517b5923b4L;
		APSPTestUtils.testAPSPDirectedPositiveInt(APSPJohnson::new, seed);
	}

	@Test
	public void testRandGraphDirectedNegativeInt() {
		final long seed = 0xbf0dd8e7294b5cecL;
		APSPTestUtils.testAPSPDirectedNegativeInt(APSPJohnson::new, seed);
	}

}
