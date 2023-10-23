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
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {}

	static void randGraphsBipartiteWeighted(MatchingAlgo algo, long seed) {
		randGraphsBipartiteWeighted(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void randGraphsBipartiteWeighted(MatchingAlgo algo, Boolean2ObjectFunction<Graph> graphImpl,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8, 8).repeat(256);
		tester.addPhase().withArgs(16, 16, 64).repeat(128);
		tester.addPhase().withArgs(128, 128, 128).repeat(12);
		tester.addPhase().withArgs(256, 256, 1200).repeat(2);
		tester.run((sn, tn, m) -> {
			Graph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgo validationAlgo =
					algo instanceof MatchingWeightedBipartiteSSSP ? new MatchingWeightedBipartiteHungarianMethod()
							: new MatchingWeightedBipartiteSSSP();
			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static void randBipartiteGraphsWeightedPerfect(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8, 8).repeat(64);
		tester.addPhase().withArgs(16, 16, 64).repeat(32);
		tester.addPhase().withArgs(128, 128, 128).repeat(8);
		tester.addPhase().withArgs(128, 128, 512).repeat(4);
		tester.addPhase().withArgs(1024, 1024, 1024).repeat(1);
		tester.run((sn, tn, m) -> {
			Graph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, GraphsTestUtils.defaultGraphImpl(),
					seedGen.nextSeed());
			Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);

			MatchingAlgo cardinalityAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
			IntList unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
			unmatchedVerticesS.removeIf(v -> partition.getBool(v));
			unmatchedVerticesT.removeIf(v -> !partition.getBool(v));
			assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
			IntLists.shuffle(unmatchedVerticesS, new Random(seedGen.nextSeed()));
			IntLists.shuffle(unmatchedVerticesT, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVerticesS.size(); i++) {
				int u = unmatchedVerticesS.getInt(i);
				int v = unmatchedVerticesT.getInt(i);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgo validationUnweightedAlgo = new MatchingCardinalityBipartiteHopcroftKarp();
			MatchingAlgo validationWeightedAlgo =
					algo instanceof MatchingWeightedBipartiteHungarianMethod ? new MatchingWeightedGabow1990()
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
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MatchingAlgo validationAlgo = algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
					: new MatchingWeightedGabow1990();

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static void testGraphWeighted(MatchingAlgo algo, Graph g, WeightFunction.Int w,
			MatchingAlgo validationAlgo) {
		Matching actual = algo.computeMaximumWeightedMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		double actualWeight = actual.weight(w);

		Matching expected = validationAlgo.computeMaximumWeightedMatching(g, w);
		double expectedWeight = expected.weight(w);

		if (actualWeight > expectedWeight) {
			System.err
					.println("matching is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(MatchingAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 8).repeat(256);
		tester.addPhase().withArgs(16, 64).repeat(128);
		tester.addPhase().withArgs(128, 128).repeat(12);
		tester.addPhase().withArgs(128, 512).repeat(8);
		tester.addPhase().withArgs(1024, 1024).repeat(2);
		tester.run((n, m) -> {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			if (g.vertices().size() % 2 != 0)
				throw new IllegalArgumentException("there is no perfect matching");

			MatchingAlgo cardinalityAlgo = new MatchingCardinalityGabow1976();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVertices = new IntArrayList(cardinalityMatch.unmatchedVertices());
			assert unmatchedVertices.size() % 2 == 0;
			IntLists.shuffle(unmatchedVertices, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVertices.size() / 2; i++) {
				int u = unmatchedVertices.getInt(i * 2 + 0);
				int v = unmatchedVertices.getInt(i * 2 + 1);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MatchingAlgo validationUnweightedAlgo = new MatchingCardinalityGabow1976();
			MatchingAlgo validationWeightedAlgo =
					algo instanceof MatchingWeightedGabow1990 ? new MatchingWeightedBlossomV()
							: new MatchingWeightedGabow1990();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void testGraphWeightedPerfect(MatchingAlgo algo, Graph g, WeightFunction.Int w,
			MatchingAlgo validationUnweightedAlgo, MatchingAlgo validationWeightedAlgo) {
		Matching actual = algo.computeMaximumWeightedPerfectMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		int actualSize = actual.edges().size();
		double actualWeight = actual.weight(w);

		int expectedSize = validationUnweightedAlgo.computeMaximumCardinalityMatching(g).edges().size();
		if (actualSize > expectedSize) {
			System.err.println(
					"matching size is better than validation algo found: " + actualSize + " > " + expectedSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedSize, actualSize, "unexpected match size");

		Matching expected = validationWeightedAlgo.computeMaximumWeightedPerfectMatching(g, w);
		double expectedWeight = expected.weight(w);
		if (actualWeight > expectedWeight) {
			System.err.println(
					"matching weight is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

}
