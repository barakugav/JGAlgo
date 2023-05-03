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

public class MaximumFlowPushRelabelLowestFirstTest extends TestBase {

	@Test
	public void testRandGraphsDoubleFlow() {
		final long seed = 0xa3401ed1fd71bd97L;
		MaximumFlowTestUtils.testRandGraphs(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testRandGraphsIntFlow() {
		final long seed = 0x0204011e1b393aaaL;
		MaximumFlowTestUtils.testRandGraphsInt(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testMinimumCutRandGraphs() {
		final long seed = 0x9a378c8dd98b3bceL;
		MinimumCutSTTestUtils.testRandGraphs(new MaximumFlowPushRelabelLowestFirst(), seed);
	}

	@Test
	public void testMinimumCutRandGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5fc7b22045f53253L;
		MinimumCutST algo = MinimumCutST.newFromMaximumFlow(new MaximumFlowPushRelabelLowestFirst());
		MinimumCutSTTestUtils.testRandGraphs(algo, seed);
	}

}
