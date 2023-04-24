package com.jgalgo;

import org.junit.jupiter.api.Test;

public class RMQStaticPlusMinusOneTest extends TestBase {

	@Test
	public void testRegular() {
		final long seed = 0xffa0ad985dfbf2b3L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQStaticUtils.randRMQDataPlusMinusOne(a, seedGen.nextSeed());
		RMQStaticUtils.randRMQQueries(a, queries, a.length, seedGen.nextSeed());
		RMQStaticUtils.testRMQ(RMQStaticPlusMinusOne::new, a, queries);
	}

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0x263d8923b37960baL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++) {
			int[] a = new int[n];
			int[][] queries = new int[64][];
			RMQStaticUtils.randRMQDataPlusMinusOne(a, seedGen.nextSeed());
			RMQStaticUtils.randRMQQueries(a, queries, a.length, seedGen.nextSeed());

			RMQStaticUtils.testRMQ(RMQStaticPlusMinusOne::new, a, queries);
		}
	}

	@Test
	public void testOnlyInterBlock() {
		final long seed = 0xaf5fa81d79d325d9L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] a = new int[128];
		int[][] queries = new int[64][];
		RMQStaticUtils.randRMQDataPlusMinusOne(a, seedGen.nextSeed());
		RMQStaticUtils.randRMQQueries(a, queries, 4, seedGen.nextSeed());
		RMQStaticUtils.testRMQ(RMQStaticPlusMinusOne::new, a, queries);
	}

}
