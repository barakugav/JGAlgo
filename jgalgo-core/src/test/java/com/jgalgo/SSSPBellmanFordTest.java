package com.jgalgo;

import org.junit.jupiter.api.Test;

public class SSSPBellmanFordTest extends TestBase {

	@Test
	public void testRandGraphPositiveInt() {
		final long seed = 0x89d12d4775fd2b7fL;
		SSSPTestUtils.testSSSPDirectedPositiveInt(new SSSPBellmanFord(), seed);
	}

	@Test
	public void testRandGraphNegativeInt() {
		final long seed = 0x5a4758f2d75f9448L;
		SSSPTestUtils.testSSSPDirectedNegativeInt(new SSSPBellmanFord(), seed);
	}

}
