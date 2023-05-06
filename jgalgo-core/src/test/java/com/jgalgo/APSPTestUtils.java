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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

class APSPTestUtils extends TestUtils {

	private APSPTestUtils() {}

	static void testAPSPDirectedPositiveInt(APSP algo, long seed) {
		testAPSPPositiveInt(algo, true, seed);
	}

	static void testAPSPUndirectedPositiveInt(APSP algo, long seed) {
		testAPSPPositiveInt(algo, false, seed);
	}

	private static void testAPSPPositiveInt(APSP algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 6, 20), phase(128, 16, 32), phase(64, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testAPSP(g, w, algo, new SSSPDijkstra());
		});
	}

	static void testAPSPDirectedNegativeInt(APSP algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 6, 20), phase(64, 16, 32), phase(10, 64, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, w, algo, new SSSPGoldberg());
		});
	}

	static void testAPSP(Graph g, EdgeWeightFunc w, APSP algo, SSSP validationAlgo) {
		APSP.Result result = algo.computeAllShortestPaths(g, w);

		int n = g.vertices().size();
		for (int source = 0; source < n; source++) {
			SSSP.Result expectedRes = validationAlgo.computeShortestPaths(g, w, source);

			if (result.foundNegativeCycle()) {
				Path cycle = null;
				try {
					cycle = result.getNegativeCycle();
				} catch (UnsupportedOperationException e) {
				}
				if (cycle != null) {
					double cycleWeight = cycle.weight(w);
					assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
					assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
					if (!expectedRes.foundNegativeCycle())
						throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
				} else {
					assertTrue(expectedRes.foundNegativeCycle(), "found non existing negative cycle");
				}
				return;
			}
			assertFalse(expectedRes.foundNegativeCycle(), "failed to found negative cycle");

			for (int target = 0; target < n; target++) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				Path path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = path.weight(w);
					assertEquals(pathWeight, actualDistance, "Path to vertex " + target + " doesn't match distance ("
							+ actualDistance + " != " + pathWeight + "): " + path);
				} else {
					assertEquals(Double.POSITIVE_INFINITY, actualDistance,
							"Distance to vertex " + target + " is not infinity but path is null");
				}
			}
		}
	}

}
