package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.RMQLookupTable;

public class RMQLookupTableTest extends TestUtils {

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0xc7d2ec9ae1d4efd0L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQLookupTable::new, n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testRegular16384() {
		final long seed = 0xa9873a72958dd0b6L;
		RMQTestUtils.testRMQ(RMQLookupTable::new, 16384, 4096, seed);
	}
}
