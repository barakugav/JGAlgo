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

public class MaximumFlowEdmondsKarpTest extends TestBase {

	private static MaximumFlow algo() {
		return new MaximumFlowEdmondsKarp();
	}

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0x398eea4097bc0600L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0xa180ffaa75a62d0cL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0xc65e0a6775182badL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0x2633b8fe31af8cddL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0x6bcedff3ee9079d9L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0xaa7eab04a9b554cbL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0xae19d36885e91694L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x8a373ae312238006L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x5b022255ca4a4adbL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

}
