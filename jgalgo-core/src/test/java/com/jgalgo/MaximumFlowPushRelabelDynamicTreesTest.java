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

public class MaximumFlowPushRelabelDynamicTreesTest extends TestBase {

	@Test
	public void testRandDiGraphs() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelDynamicTrees(), seed, /*directed=*/ true);
	}

	@Test
	public void testRandDiGraphsInt() {
		final long seed = 0x00dd5c7d6b25fe3bL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelDynamicTrees(), seed, /*directed=*/ true);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x43cc9ff1b0dd495bL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelDynamicTrees());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
