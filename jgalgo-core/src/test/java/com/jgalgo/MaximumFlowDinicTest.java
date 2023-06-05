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

public class MaximumFlowDinicTest extends TestBase {

	private static MaximumFlow algo() {
		return new MaximumFlowDinic();
	}

	@Test
	public void testRandDiGraphs() {
		final long seed = 0xa79b303ec46fd984L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x6be26a022c1cd652L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0x29cb2b70099bb3b0L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0x105a8227f95fe963L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0xb49154497703863bL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0xc8bfc54fab5f4cc7L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x8c5bc12568cbbae1L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x7315922188b81aeeL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

}
