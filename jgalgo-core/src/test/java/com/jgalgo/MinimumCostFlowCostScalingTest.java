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
import com.jgalgo.internal.util.TestBase;

public class MinimumCostFlowCostScalingTest extends TestBase {

	@Test
	public void testMinCostMaxFlowWithSourceSink() {
		final long seed = 0x9d87eacac2f9fd14L;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSink(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourceSinkLowerBound() {
		final long seed = 0x0af5c8180fcbeca2L;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSinkLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinks() {
		final long seed = 0xaa9f96849c7e279eL;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinks(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinksLowerBound() {
		final long seed = 0xbe29e6f664b78400L;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinksLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupply() {
		final long seed = 0x1f3f752a90c159ccL;
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupply(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupplyLowerBound() {
		final long seed = 0x18ce22e8f434beddL;
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupplyLowerBound(algo(), seed);
	}

	private static MinimumCostFlow algo() {
		return new MinimumCostFlowCostScaling();
	}

}
