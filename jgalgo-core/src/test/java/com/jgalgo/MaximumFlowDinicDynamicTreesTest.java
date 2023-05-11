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

public class MaximumFlowDinicDynamicTreesTest extends TestBase {

	@Test
	public void testRandDiGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowDinicDynamicTrees(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x38831cada35583c6L;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowDinicDynamicTrees(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x57895831bc5f0b59L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowDinicDynamicTrees());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
