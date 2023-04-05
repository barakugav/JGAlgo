package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.APSPFloydWarshall;

class APSPFloydWarshallTest {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x80b8af9bfbd5e5d5L;
		APSPTestUtils.testAPSPDirectedPositiveInt(APSPFloydWarshall::new, seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0x307fc7bb8684a8b5L;
		APSPTestUtils.testAPSPUndirectedPositiveInt(APSPFloydWarshall::new, seed);
	}

	@Test
	public void testRandGraphDirectedNegativeInt() {
		final long seed = 0xd3037473c85e47b3L;
		APSPTestUtils.testAPSPDirectedNegativeInt(APSPFloydWarshall::new, seed);
	}

}
