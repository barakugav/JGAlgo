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

public class MinimumCostFlowCycleCancelingTest extends TestBase {

	@Test
	public void testMinCostMaxFlowWithSourceSink() {
		final long seed = 0xd4a5d9c74a4ff3d0L;
		MinimumCostFlowTestUtils.testMinCostMaxFlowWithSourceSink(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourceSinkLowerBound() {
		final long seed = 0x1d5f848407f40c2dL;
		MinimumCostFlowTestUtils.testMinCostMaxFlowWithSourceSinkLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinks() {
		final long seed = 0x6957bbef1e9f7546L;
		MinimumCostFlowTestUtils.testMinCostMaxFlowWithSourcesSinks(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinksLowerBound() {
		final long seed = 0x66fc23c1024348aeL;
		MinimumCostFlowTestUtils.testMinCostMaxFlowWithSourcesSinksLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupply() {
		final long seed = 0x1e7f3802930a9f8bL;
		MinimumCostFlowTestUtils.testMinCostFlowWithSupply(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupplyLowerBound() {
		final long seed = 0x4942cca716edeee2L;
		MinimumCostFlowTestUtils.testMinCostFlowWithSupplyLowerBound(algo(), seed);
	}

	private static MinimumCostFlow algo() {
		return new MinimumCostFlowCycleCanceling();
	}

}
