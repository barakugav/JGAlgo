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

package com.jgalgo.internal.ds;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class RmqStaticCartesianTreesTest extends TestBase {

	@Test
	public void testRegular65536() {
		final long seed = 0xcccc98185df4d891L;
		RmqStaticUtils.testRMQ(new RmqStaticCartesianTrees(), 65536, 4096, seed);
	}

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0xf9013e7f87cc151bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 1; n <= 256; n++)
			RmqStaticUtils.testRMQ(new RmqStaticCartesianTrees(), n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testOnlyInterBlock65536() {
		final long seed = 0x0e16c7a9555ce13dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int[] a = randArray(65536, 0, 64, seedGen.nextSeed());
		int[][] queries = new int[4096][];
		RmqStaticUtils.randRMQQueries(a, queries, 4, seedGen.nextSeed());
		RmqStaticUtils.testRMQ(new RmqStaticCartesianTrees(), a, queries);
	}

}
