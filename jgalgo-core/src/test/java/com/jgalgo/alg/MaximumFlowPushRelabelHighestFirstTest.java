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

public class MaximumFlowPushRelabelHighestFirstTest extends TestBase {

	private static MaximumFlowPushRelabel algo() {
		return MaximumFlowPushRelabel.newInstanceHighestFirst();
	}

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0x307ba0f4e538bcdfL;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphs() {
		final long seed = 0xb4f642a51ed6f5a0L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0x5c4a4099bab06fdbL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0xa965568176b59253L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0xae9e5bc096153a4fL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xb93b17d4ff4ac3daL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xe548db3fba570c40L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xc9f8420fb00cdd5aL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0x254b58fa668eda9dL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0x34c1d8ba621102f6L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0xe88b8b22193549ceL;
		MinimumCutSTTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0xc4b858afa5051500L;
		MinimumCutSTTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0xe70353b8637b68d2L;
		MinimumCutSTTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0xe2d73e1680bc3e5dL;
		MinimumCutSTTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x222b311afc822531L;
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSink() {
		final long seed = 0x9bedd87c94f714edL;
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x392ec4b67c5d920aL;
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x1d9fc742a505e2c8L;
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x1d854bb800e9ea71L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5b5c45845c61f142L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x8f2b92e482d65052L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x7b5377f4bdac989cL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x6e00e9d14c395af8L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5c9584a4d2857fcbL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc6decab7f05a929cL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xfcca8353b6ee33f2L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(algo());
		MinimumCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ false);
	}

}
