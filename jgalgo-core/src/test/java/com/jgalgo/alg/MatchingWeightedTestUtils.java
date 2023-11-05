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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {}

	static void randGraphsBipartiteWeighted(MatchingAlgo algo, long seed) {
		randGraphsBipartiteWeighted(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void randGraphsBipartiteWeighted(MatchingAlgo algo,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8, 8).repeat(256);
		tester.addPhase().withArgs(16, 16, 64).repeat(128);
		tester.addPhase().withArgs(128, 128, 128).repeat(12);
		tester.addPhase().withArgs(256, 256, 1200).repeat(2);
		tester.run((sn, tn, m) -> {
			Graph<Integer, Integer> g =
					MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgo validationAlgo =
					algo instanceof MatchingWeightedBipartiteSSSP ? new MatchingWeightedBipartiteHungarianMethod()
							: new MatchingWeightedBipartiteSSSP();
			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static void randBipartiteGraphsWeightedPerfect(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8, 8).repeat(64);
		tester.addPhase().withArgs(16, 16, 64).repeat(32);
		tester.addPhase().withArgs(128, 128, 128).repeat(8);
		tester.addPhase().withArgs(128, 128, 512).repeat(4);
		tester.addPhase().withArgs(1024, 1024, 1024).repeat(1);
		tester.run((sn, tn, m) -> {
			Graph<Integer, Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m,
					GraphsTestUtils.defaultGraphImpl(), seedGen.nextSeed());
			WeightsBool<Integer> partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
			Supplier<Integer> edgeSupplier = () -> {
				for (;;) {
					Integer e = Integer.valueOf(rand.nextInt());
					if (e.intValue() >= 1 && !g.edges().contains(e))
						return e;
				}
			};

			MatchingAlgo cardinalityAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			Matching<Integer, Integer> cardinalityMatch = cardinalityAlgo.computeMaximumMatching(g, null);
			List<Integer> unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
			List<Integer> unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
			unmatchedVerticesS.removeIf(v -> partition.get(v));
			unmatchedVerticesT.removeIf(v -> !partition.get(v));
			assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
			Collections.shuffle(unmatchedVerticesS, rand);
			Collections.shuffle(unmatchedVerticesT, rand);
			for (int i = 0; i < unmatchedVerticesS.size(); i++) {
				Integer u = unmatchedVerticesS.get(i);
				Integer v = unmatchedVerticesT.get(i);
				g.addEdge(u, v, edgeSupplier.get());
			}
			assert cardinalityAlgo.computeMaximumMatching(g, null).isPerfect();
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunctionInt<Integer> w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgo validationUnweightedAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			MatchingAlgo validationWeightedAlgo =
					algo instanceof MatchingWeightedBipartiteHungarianMethod ? new MatchingWeightedBlossomV()
							: new MatchingWeightedBipartiteHungarianMethod();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void randGraphsWeighted(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8).repeat(256);
		tester.addPhase().withArgs(16, 64).repeat(128);
		tester.addPhase().withArgs(128, 128).repeat(12);
		tester.addPhase().withArgs(128, 512).repeat(6);
		tester.addPhase().withArgs(1024, 2300).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgo validationAlgo = algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
					: new MatchingWeightedGabow1990();

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static <V, E> void testGraphWeighted(MatchingAlgo algo, Graph<V, E> g, WeightFunctionInt<E> w,
			MatchingAlgo validationAlgo) {
		Matching<V, E> actual = algo.computeMaximumMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		double actualWeight = w.weightSum(actual.edges());

		Matching<V, E> expected = validationAlgo.computeMaximumMatching(g, w);
		double expectedWeight = w.weightSum(expected.edges());

		if (actualWeight > expectedWeight) {
			System.err
					.println("matching is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8).repeat(256);
		tester.addPhase().withArgs(16, 64).repeat(128);
		tester.addPhase().withArgs(128, 128).repeat(12);
		tester.addPhase().withArgs(128, 512).repeat(8);
		tester.addPhase().withArgs(1024, 1024).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			if (g.vertices().size() % 2 != 0)
				throw new IllegalArgumentException("there is no perfect matching");
			Supplier<Integer> edgeSupplier = () -> {
				for (;;) {
					Integer e = Integer.valueOf(rand.nextInt());
					if (e.intValue() >= 1 && !g.edges().contains(e))
						return e;
				}
			};

			MatchingAlgo cardinalityAlgo = new MatchingCardinalityGabow1976();
			Matching<Integer, Integer> cardinalityMatch = cardinalityAlgo.computeMaximumMatching(g, null);
			List<Integer> unmatchedVertices = new IntArrayList(cardinalityMatch.unmatchedVertices());
			assert unmatchedVertices.size() % 2 == 0;
			Collections.shuffle(unmatchedVertices, rand);
			for (int i = 0; i < unmatchedVertices.size() / 2; i++) {
				Integer u = unmatchedVertices.get(i * 2 + 0);
				Integer v = unmatchedVertices.get(i * 2 + 1);
				g.addEdge(u, v, edgeSupplier.get());
			}
			assert cardinalityAlgo.computeMaximumMatching(g, null).isPerfect();

			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunctionInt<Integer> w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgo validationUnweightedAlgo = new MatchingCardinalityGabow1976();
			// MatchingAlgo validationWeightedAlgo =
			// algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
			// : new MatchingWeightedGabow1990();
			MatchingAlgo validationWeightedAlgo = new MatchingWeightedBlossomV();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static <V, E> void testGraphWeightedPerfect(MatchingAlgo algo, Graph<V, E> g, WeightFunctionInt<E> w,
			MatchingAlgo validationUnweightedAlgo, MatchingAlgo validationWeightedAlgo) {
		Matching<V, E> actual = algo.computeMaximumPerfectMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		int actualSize = actual.edges().size();
		double actualWeight = w.weightSum(actual.edges());

		int expectedSize = validationUnweightedAlgo.computeMaximumMatching(g, null).edges().size();
		if (actualSize > expectedSize) {
			System.err.println(
					"matching size is better than validation algo found: " + actualSize + " > " + expectedSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedSize, actualSize, "unexpected match size");

		Matching<V, E> expected = validationWeightedAlgo.computeMaximumPerfectMatching(g, w);
		double expectedWeight = w.weightSum(expected.edges());
		if (actualWeight > expectedWeight) {
			System.err.println(
					"matching weight is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

}
