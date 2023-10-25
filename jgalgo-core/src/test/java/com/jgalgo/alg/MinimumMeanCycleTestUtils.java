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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.Random;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class MinimumMeanCycleTestUtils extends TestBase {

	static void testRandGraphs(MinimumMeanCycle algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 2).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(500, 2010).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();
			WeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			verifyMinimumMeanCycle(algo, g, w);
		});
	}

	static void testRandGraphsSimilarWeights(MinimumMeanCycle algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 2).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(500, 2010).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();
			WeightFunction w = GraphsTestUtils.assignRandWeights(g, -10, 10, seedGen.nextSeed());

			verifyMinimumMeanCycle(algo, g, w);
		});
	}

	static void testRandGraphsEqualWeightCycles(MinimumMeanCycle algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 2).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(500, 2010).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();

			WeightsDouble w = g.addEdgesWeights("weights", double.class);
			for (int e : new IntArrayList(g.edges())) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double ew = rand.nextInt(1024) - 256;
				int eTwin = g.addEdge(v, u);
				w.set(e, ew);
				w.set(eTwin, -ew);
			}

			verifyMinimumMeanCycle(algo, g, w);
		});
	}

	private static void verifyMinimumMeanCycle(MinimumMeanCycle algo, Graph g, WeightFunction w) {
		Path cycle = algo.computeMinimumMeanCycle(g, w);
		if (cycle == null) {
			Iterator<Path> cycles = new CyclesFinderTarjan().findAllCycles(g);
			Path missedCycle = cycles.hasNext() ? cycles.next() : null;
			assertNull(missedCycle, "failed to find a cycle");
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

			for (Path c : JGAlgoUtils.iterable(cycles)) {
				double cMeanWeight = getMeanWeight(c, w);
				final double EPS = 0.0001;
				assertTrue(cMeanWeight + EPS >= cycleMeanWeight, "found a cycle with smaller mean weight: " + c);
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
		return w.weightSum(cycle.edges()) / cycle.edges().size();
	}

}
