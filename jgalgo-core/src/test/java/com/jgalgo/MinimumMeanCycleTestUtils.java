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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

public class MinimumMeanCycleTestUtils extends TestBase {

	static void testMinimumMeanCycle(MinimumMeanCycle algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 3, 2), phase(128, 16, 32), phase(64, 64, 128), phase(8, 500, 2010));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			verifyMinimumMeanCycle(algo, g, w);
		});
	}

	private static void verifyMinimumMeanCycle(MinimumMeanCycle algo, Graph g, WeightFunction w) {
		Path cycle = algo.computeMinimumMeanCycle(g, w);
		if (cycle == null) {
			Iterator<Path> cycles = new CyclesFinderTarjan().findAllCycles(g);
			assertFalse(cycles.hasNext(), "failed to find a cycle");
			return;
		}
		double cycleMeanWeight = getMeanWeight(cycle, w);

		if (g.vertices().size() <= 32 && g.edges().size() <= 32) {
			Iterator<Path> cycles = new CyclesFinderTarjan().findAllCycles(g);
			assertEquals(cycle.source(), cycle.target());
			int prevV = cycle.source();
			for (EdgeIter eit = cycle.edgeIter();;) {
				int e = eit.nextInt();
				assertEquals(prevV, g.edgeSource(e));
				prevV = g.edgeTarget(e);
				if (!eit.hasNext()) {
					assertEquals(cycle.target(), prevV);
					break;
				}
			}

			for (Path c : Utils.iterable(cycles)) {
				double cMeanWeight = getMeanWeight(c, w);
				assertTrue(cMeanWeight >= cycleMeanWeight, "found a cycle with smaller mean weight: " + c);
			}
		} else {
			MinimumMeanCycle validationAlgo = algo instanceof MinimumMeanCycleHoward ? new MinimumMeanCycleDasdanGupta()
					: new MinimumMeanCycleHoward();
			Path expectedCycle = validationAlgo.computeMinimumMeanCycle(g, w);
			assertNotNull(expectedCycle, "validation algo failed to find a cycle");
			double expectedWeight = getMeanWeight(expectedCycle, w);

			assertEquals(expectedWeight, cycleMeanWeight, 1E-3, "Unexpected cycle mean weight");
		}
	}

	private static double getMeanWeight(Path cycle, WeightFunction w) {
		return cycle.weight(w) / cycle.size();
	}

}
