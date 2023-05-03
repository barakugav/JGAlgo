/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		RMQStaticUtils.testRMQ(new RMQStaticPlusMinusOne(), a, queries);
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

			RMQStaticUtils.testRMQ(new RMQStaticPlusMinusOne(), a, queries);
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
		RMQStaticUtils.testRMQ(new RMQStaticPlusMinusOne(), a, queries);
	}

}
