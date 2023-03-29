package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.APSPFloydWarshall;

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

}
