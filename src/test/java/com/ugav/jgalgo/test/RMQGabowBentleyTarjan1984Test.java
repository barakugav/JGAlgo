package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RMQGabowBentleyTarjan1984;

public class RMQGabowBentleyTarjan1984Test extends TestUtils {

	@Test
	public void testRegular65536() {
		final long seed = 0xcccc98185df4d891L;
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, 65536, 4096, seed);
	}

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0xf9013e7f87cc151bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testOnlyInterBlock65536() {
		final long seed = 0x0e16c7a9555ce13dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] a = randArray(65536, 0, 64, seedGen.nextSeed());
		int[][] queries = new int[4096][];
		RMQTestUtils.randRMQQueries(a, queries, 4, seedGen.nextSeed());
		RMQTestUtils.testRMQ(RMQGabowBentleyTarjan1984::new, a, queries);
	}

}
