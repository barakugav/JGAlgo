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

public class MaximumFlowPushRelabelFifoTest extends TestBase {

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0x87662c130902cf06L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelFifo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0x482aaa129b8af846L;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelFifo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x7c0660289a51ebd2L;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelFifo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x3ce112da3fadf191L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelFifo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0xb6c6559f24d8cc94L;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelFifo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc833101540b8e5f1L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelFifo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x0903533be6fdd4deL;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelFifo());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

}
