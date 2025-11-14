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

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class MinimumCostFlowCycleCancelingTest extends TestBase {

	@Test
	public void testMinCostMaxFlowWithSourceSinkInt() {
		final long seed = 0xd4a5d9c74a4ff3d0L;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSink(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourceSink() {
		final long seed = 0x9297648387ebb9f9L;
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourceSink(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourceSinkLowerBoundInt() {
		final long seed = 0x1d5f848407f40c2dL;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourceSinkLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourceSinkLowerBound() {
		final long seed = 0x49ab7b38c9ac36e9L;
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourceSinkLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinksInt() {
		final long seed = 0x6957bbef1e9f7546L;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinks(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinks() {
		final long seed = 0x2cb0ede60fbfec9dL;
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourcesSinks(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinksLowerBoundInt() {
		final long seed = 0x66fc23c1024348aeL;
		MinimumCostFlowTestUtilsInt.testMinCostMaxFlowWithSourcesSinksLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostMaxFlowWithSourcesSinksLowerBound() {
		final long seed = 0xdc28e86243a4dcfbL;
		MinimumCostFlowTestUtilsDouble.testMinCostMaxFlowWithSourcesSinksLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupplyInt() {
		final long seed = 0x1e7f3802930a9f8bL;
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupply(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupply() {
		final long seed = 0xf617f6625a76c88dL;
		MinimumCostFlowTestUtilsDouble.testMinCostFlowWithSupply(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupplyLowerBoundInt() {
		final long seed = 0x4942cca716edeee2L;
		MinimumCostFlowTestUtilsInt.testMinCostFlowWithSupplyLowerBound(algo(), seed);
	}

	@Test
	public void testMinCostFlowWithSupplyLowerBound() {
		final long seed = 0x8fb29cb01468de49L;
		MinimumCostFlowTestUtilsDouble.testMinCostFlowWithSupplyLowerBound(algo(), seed);
	}

	private static MinimumCostFlow algo() {
		return new MinimumCostFlowCycleCanceling();
	}

	@Test
	public void invalidSupply() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		MinimumCostFlow algo = algo();
		IWeightsDouble supply = g.addVerticesWeights("supply", double.class);

		/* non-finite supply */
		supply.set(0, Double.POSITIVE_INFINITY);
		supply.set(1, Double.NEGATIVE_INFINITY);
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, null, null, supply));

		/* supply that does not sum to 0 */
		supply.set(0, 2);
		supply.set(1, -1);
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, null, null, supply));
	}

	@Test
	public void invalidLowerBound() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		MinimumCostFlow algo = algo();
		IWeightsDouble supply = g.addVerticesWeights("supply", double.class);
		supply.set(0, 4);
		supply.set(1, -4);

		IWeightFunction cap1 = e -> 100;
		IWeightFunctionInt cap2 = e -> 100;
		IWeightFunction lowBound1 = e -> 200;
		IWeightFunctionInt lowBound2 = e -> 200;
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, cap1, null, lowBound1, supply));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, cap1, null, lowBound2, supply));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, cap2, null, lowBound1, supply));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMinCostFlow(g, cap2, null, lowBound2, supply));
	}

}
