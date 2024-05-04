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

public class MatchingWeightedBipartiteSssp2Test extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x6d2c36b6f7f5d43fL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingWeightedBipartiteSssp2(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x131359e008ab11acL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MatchingWeightedBipartiteSssp2(), seed);
	}

}
