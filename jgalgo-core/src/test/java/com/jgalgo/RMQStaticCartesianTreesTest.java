package com.jgalgo;

import org.junit.jupiter.api.Test;

public class RMQStaticCartesianTreesTest extends TestBase {

	@Test
	public void testRegular65536() {
		final long seed = 0xcccc98185df4d891L;
		RMQStaticUtils.testRMQ(RMQStaticCartesianTrees::new, 65536, 4096, seed);
	}

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0xf9013e7f87cc151bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RMQStaticUtils.testRMQ(RMQStaticCartesianTrees::new, n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testOnlyInterBlock65536() {
		final long seed = 0x0e16c7a9555ce13dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] a = randArray(65536, 0, 64, seedGen.nextSeed());
		int[][] queries = new int[4096][];
		RMQStaticUtils.randRMQQueries(a, queries, 4, seedGen.nextSeed());
		RMQStaticUtils.testRMQ(RMQStaticCartesianTrees::new, a, queries);
	}

}
