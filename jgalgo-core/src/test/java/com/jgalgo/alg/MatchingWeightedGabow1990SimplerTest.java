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

package com.jgalgo.alg;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MatchingWeightedGabow1990SimplerTest extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x66a1384df40bbdfdL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingWeightedGabow1990Simpler(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x9640378e6335de4aL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MatchingWeightedGabow1990Simpler(), seed);
	}

	// @Test
	// public void testRandBipartiteGraphsWeightedPerfect() {
	// final long seed = 0xe080d69c28c1c913L;
	// MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(new MatchingWeightedGabow1990Simpler(), seed);
	// }

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x2be96ec37c54f192L;
		MatchingUnweightedTestUtils.randGraphs(new MatchingWeightedGabow1990Simpler(), seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0x398474500317fa5bL;
		MatchingWeightedTestUtils.randGraphsWeighted(new MatchingWeightedGabow1990Simpler(), seed);
	}

	// @Test
	// public void testRandGraphsWeightedPerfect() {
	// final long seed = 0x869ba538e4884e8fL;
	// MatchingWeightedTestUtils.randGraphsWeightedPerfect(new MatchingWeightedGabow1990Simpler(), seed);
	// }

}
