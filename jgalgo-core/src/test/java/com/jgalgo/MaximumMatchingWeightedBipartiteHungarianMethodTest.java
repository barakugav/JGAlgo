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

public class MaximumMatchingWeightedBipartiteHungarianMethodTest extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x10dff70f8efc00f1L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MaximumMatchingWeightedBipartiteHungarianMethod(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x3f731f291383dd24L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MaximumMatchingWeightedBipartiteHungarianMethod(),
				seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0x3b7892e59492d431L;
		MatchingWeightedTestUtils
				.randBipartiteGraphsWeightedPerfect(new MaximumMatchingWeightedBipartiteHungarianMethod(), seed);
	}

}
