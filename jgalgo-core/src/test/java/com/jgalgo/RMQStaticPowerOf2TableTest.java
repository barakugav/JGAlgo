package com.jgalgo;

import org.junit.jupiter.api.Test;

public class RMQStaticPowerOf2TableTest extends TestBase {

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0x95ef040f1c1d0dcfL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RMQStaticUtils.testRMQ(RMQStaticPowerOf2Table::new, n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testRegular65536() {
		final long seed = 0x4505769d28250811L;
		RMQStaticUtils.testRMQ65536(RMQStaticPowerOf2Table::new, seed);
	}

}
