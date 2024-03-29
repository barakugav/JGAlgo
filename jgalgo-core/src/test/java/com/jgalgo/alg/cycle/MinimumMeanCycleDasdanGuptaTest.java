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

package com.jgalgo.alg.cycle;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumMeanCycleDasdanGuptaTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x18400f641dec53f3L;
		MinimumMeanCycleTestUtils.testRandGraphs(new MinimumMeanCycleDasdanGupta(), seed);
	}

	@Test
	public void testRandGraphsSimilarWeights() {
		final long seed = 0xfc10c67f9a606f41L;
		MinimumMeanCycleTestUtils.testRandGraphsSimilarWeights(new MinimumMeanCycleDasdanGupta(), seed);
	}

	@Test
	public void testRandGraphsEqualWeightCycles() {
		final long seed = 0x78eeddb6a942d422L;
		MinimumMeanCycleTestUtils.testRandGraphsEqualWeightCycles(new MinimumMeanCycleDasdanGupta(), seed);
	}

}
