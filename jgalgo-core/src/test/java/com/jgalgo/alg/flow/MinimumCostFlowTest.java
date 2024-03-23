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
package com.jgalgo.alg.flow;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumCostFlowTest extends TestBase {

	@Test
	public void testDefaultImplMinCostMaxFlowWithSourceSink() {
		final long seed = 0x2b0baf96366cd1b3L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSink(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourceSink(algo, seed);
	}

	@Test
	public void testDefaultImplMinCostMaxFlowWithSourceSinkLowerBound() {
		final long seed = 0xd0f3546966721780L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSinkLowerBound(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourceSinkLowerBound(algo, seed);
	}

	@Test
	public void testDefaultImplMinCostMaxFlowWithSourcesSinks() {
		final long seed = 0xd1b8df2963248f48L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinks(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourcesSinks(algo, seed);
	}

	@Test
	public void testDefaultImplMinCostMaxFlowWithSourcesSinksLowerBound() {
		final long seed = 0x6587caf0bf1a0420L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinksLowerBound(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourcesSinksLowerBound(algo, seed);
	}

	@Test
	public void testDefaultImplMinCostFlowWithSupply() {
		final long seed = 0xfed9795af7d4b127L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupply(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostFlowWithSupply(algo, seed);
	}

	@Test
	public void testDefaultImplMinCostFlowWithSupplyLowerBound() {
		final long seed = 0xba06cf69b357d357L;
		MinimumCostFlow algo = MinimumCostFlow.newInstance();
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupplyLowerBound(algo, seed);
		MinimumCostFlowTestUtilsDouble.testMinCostFlowWithSupplyLowerBound(algo, seed);
	}

	@Test
	public void testBuilderInteger() {
		MinimumCostFlow.Builder builder = MinimumCostFlow.builder();
		builder.integerCosts(true);
		builder.integerNetwork(true);
		assertNotNull(builder.build());
	}

	@Test
	public void testSetOption() {
		MinimumCostFlow.Builder builder = MinimumCostFlow.builder();
		assertNotNull(builder.build());

		assertThrows(IllegalArgumentException.class, () -> builder.setOption("jdasg", "lhfj"));

		builder.setOption("impl", "cycle-canceling");
		assertNotNull(builder.build());
		builder.setOption("impl", "cost-scaling");
		assertNotNull(builder.build());
		builder.setOption("impl", "dmksm");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

}
