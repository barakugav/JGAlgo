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
import java.util.Random;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

public class ShortestPathSingleSourceTestUtils extends TestUtils {

	private ShortestPathSingleSourceTestUtils() {}

	public static void testSSSPDirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, true, seed);
	}

	public static void testSSSPUndirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, false, seed);
	}

	private static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed) {
		List<Phase> phases =
				List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096), phase(1, 4096, 16384));
		testSSSPPositiveInt(algo, directed, seed, phases);
	}

	static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed, List<Phase> phases) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = rand.nextInt(g.vertices().size());

			ShortestPathSingleSource validationAlgo = algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial() : new ShortestPathSingleSourceDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPCardinality(ShortestPathSingleSource algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases =
				List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			int source = rand.nextInt(g.vertices().size());

			ShortestPathSingleSource validationAlgo = algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial() : new ShortestPathSingleSourceDijkstra();
			testAlgo(g, null, source, algo, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(ShortestPathSingleSource algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(512, 4, 4), phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(2, 1024, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			int source = 0;

			ShortestPathSingleSource validationAlgo = algo instanceof ShortestPathSingleSourceBellmanFord ? new ShortestPathSingleSourceGoldberg() : new ShortestPathSingleSourceBellmanFord();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testAlgo(Graph g, WeightFunction w, int source, ShortestPathSingleSource algo, ShortestPathSingleSource validationAlgo) {
		ShortestPathSingleSource.Result result = algo.computeShortestPaths(g, w, source);
		validateResult(g, w, source, result, validationAlgo);
	}

	static void validateResult(Graph g, WeightFunction w, int source, ShortestPathSingleSource.Result result, ShortestPathSingleSource validationAlgo) {
		ShortestPathSingleSource.Result expectedRes = validationAlgo.computeShortestPaths(g, w, source);

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

		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			double expectedDistance = expectedRes.distance(v);
			double actualDistance = result.distance(v);
			assertEquals(expectedDistance, actualDistance, "Distance to vertex " + v + " is wrong");
			Path path = result.getPath(v);
			if (path != null) {
				double pathWeight = path.weight(w);
				assertEquals(pathWeight, actualDistance, "Path to vertex " + v + " doesn't match distance ("
						+ actualDistance + " != " + pathWeight + "): " + path);
			} else {
				assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + v + " is not infinity but path is null");
			}
		}
	}

}
