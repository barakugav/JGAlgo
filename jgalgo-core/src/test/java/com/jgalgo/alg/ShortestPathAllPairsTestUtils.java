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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ShortestPathAllPairsTestUtils extends TestUtils {

	private ShortestPathAllPairsTestUtils() {}

	static void testAPSPPositive(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			IntCollection verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, w, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPCardinality(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			IntCollection verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			testAPSP(g, verticesSubset, null, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPDirectedNegative(ShortestPathAllPairs algo, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 256).repeat(10);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			IntCollection verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, w, algo, new ShortestPathSingleSourceGoldberg());
		});
	}

	private static IntCollection verticesSubset(IntGraph g, boolean allVertices, long seed) {
		int n = g.vertices().size();
		if (allVertices || n <= 3)
			return g.vertices();
		Random rand = new Random(seed);
		IntSet subset = new IntOpenHashSet();
		for (int[] vs = g.vertices().toIntArray(); subset.size() < n / 2;)
			subset.add(vs[rand.nextInt(n)]);
		return subset;
	}

	static void testAPSP(IntGraph g, IntCollection verticesSubset, IWeightFunction w, ShortestPathAllPairs algo,
			ShortestPathSingleSource validationAlgo) {
		ShortestPathAllPairs.Result result = algo.computeAllShortestPaths(g, w);

		for (int source : verticesSubset) {
			ShortestPathSingleSource.Result expectedRes = validationAlgo.computeShortestPaths(g, w, source);

			if (result.foundNegativeCycle()) {
				IPath cycle = null;
				try {
					cycle = result.getNegativeCycle();
				} catch (UnsupportedOperationException e) {
				}
				if (cycle != null) {
					double cycleWeight = w.weightSum(cycle.edges());
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

			for (int target : verticesSubset) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				IPath path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = IWeightFunction.weightSum(w, path.edges());
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
