package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.RMQPowerOf2Table;

public class RMQPowerOf2TableTest extends TestUtils {

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0x95ef040f1c1d0dcfL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQPowerOf2Table::new, n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testRegular65536() {
		final long seed = 0x4505769d28250811L;
		RMQTestUtils.testRMQ65536(RMQPowerOf2Table::new, seed);
	}

}
