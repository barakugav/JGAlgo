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
package com.jgalgo.alg.match;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MatchingWeightedDefaultImplTest extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x4d76e5f1ff8d8a7L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingAlgo.newInstance(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x9beb22df9f410340L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingAlgo.newInstance(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0xf88a16b8b051b2L;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingAlgo.newInstance(), seed);
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0xdbbb785beafedbaeL;
		MatchingUnweightedTestUtils.randGraphs(MatchingAlgo.newInstance(), seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0x17c6224d36f50c66L;
		MatchingWeightedTestUtils.randGraphsWeighted(MatchingAlgo.newInstance(), seed);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		final long seed = 0x816bc5ece852fd3L;
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingAlgo.newInstance(), seed);
	}

}
