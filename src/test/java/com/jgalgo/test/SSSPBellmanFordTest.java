package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SSSPBellmanFord;

public class SSSPBellmanFordTest extends TestBase {

	@Test
	public void testRandGraphPositiveInt() {
		final long seed = 0x89d12d4775fd2b7fL;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord::new, seed);
	}

	@Test
	public void testRandGraphNegativeInt() {
		final long seed = 0x5a4758f2d75f9448L;
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPBellmanFord::new, seed);
	}

}
