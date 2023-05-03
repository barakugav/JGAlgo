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

import java.util.List;

import com.jgalgo.GraphImplTestUtils.GraphImpl;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {}

	static void randGraphsBipartiteWeighted(MaximumMatchingWeighted algo, long seed) {
		randGraphsBipartiteWeighted(algo, GraphImplTestUtils.GRAPH_IMPL_DEFAULT, seed);
	}

	static void randGraphsBipartiteWeighted(MaximumMatchingWeighted algo, GraphImpl graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(2, 256, 256, 1200));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			UGraph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			MaximumMatchingWeighted validationAlgo = algo instanceof MaximumMatchingWeightedBipartiteSSSP
					? new MaximumMatchingWeightedBipartiteHungarianMethod()
					: new MaximumMatchingWeightedBipartiteSSSP();
			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static void randBipartiteGraphsWeightedPerfect(MaximumMatchingWeighted algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 8, 8, 8), phase(32, 16, 16, 64), phase(8, 128, 128, 128),
				phase(4, 128, 128, 512), phase(1, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			UGraph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, GraphImplTestUtils.GRAPH_IMPL_DEFAULT,
					seedGen.nextSeed());
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			EdgeWeightFunc.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MaximumMatching validationUnweightedAlgo = new MaximumMatchingBipartiteHopcroftKarp();
			MaximumMatchingWeighted validationWeightedAlgo =
					algo instanceof MaximumMatchingWeightedBipartiteHungarianMethodTest
							? new MaximumMatchingWeightedGabow1990()
							: new MaximumMatchingWeightedBipartiteHungarianMethod();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void randGraphsWeighted(MaximumMatchingWeighted algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(6, 128, 128, 512), phase(1, 1024, 1024, 2300));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			UGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());

			// have nothing other than MaximumMatchingWeightedGabow1990, at least shuffle
			// the graph
			MaximumMatchingWeighted validationAlgo =
					new MatchingWeightedShuffled(new MaximumMatchingWeightedGabow1990(), seedGen.nextSeed());

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static void testGraphWeighted(MaximumMatchingWeighted algo, UGraph g, EdgeWeightFunc.Int w,
			MaximumMatchingWeighted validationAlgo) {
		IntCollection actual = algo.computeMaximumMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		double actualWeight = calcMatchingWeight(actual, w);

		IntCollection expected = validationAlgo.computeMaximumMatching(g, w);
		double expectedWeight = calcMatchingWeight(expected, w);

		if (actualWeight > expectedWeight) {
			System.err
					.println("matching is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(MaximumMatchingWeighted algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(2, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			UGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			EdgeWeightFunc.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MaximumMatching validationUnweightedAlgo = new MaximumMatchingGabow1976();
			MaximumMatchingWeighted validationWeightedAlgo =
					new MatchingWeightedShuffled(new MaximumMatchingWeightedGabow1990(), seedGen.nextSeed());
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	private static void testGraphWeightedPerfect(MaximumMatchingWeighted algo, UGraph g, EdgeWeightFunc.Int w,
			MaximumMatching validationUnweightedAlgo, MaximumMatchingWeighted validationWeightedAlgo) {
		IntCollection actual = algo.computeMaximumPerfectMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		int actualSize = actual.size();
		double actualWeight = calcMatchingWeight(actual, w);

		int expectedSize = validationUnweightedAlgo.computeMaximumMatching(g).size();
		if (actualSize > expectedSize) {
			System.err.println(
					"matching size is better than validation algo found: " + actualSize + " > " + expectedSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedSize, actualSize, "unexpected match size");

		double expectedWeight = calcMatchingWeight(validationWeightedAlgo.computeMaximumPerfectMatching(g, w), w);
		if (actualWeight > expectedWeight) {
			System.err.println(
					"matching weight is better than validation algo found: " + actualWeight + " > " + expectedWeight);
			throw new IllegalStateException();
		}
		assertEquals(expectedWeight, actualWeight, "unexpected match weight");
	}

	private static double calcMatchingWeight(IntCollection matching, EdgeWeightFunc w) {
		double sum = 0;
		for (IntIterator it = matching.iterator(); it.hasNext();)
			sum += w.weight(it.nextInt());
		return sum;
	}

	private static class MatchingWeightedShuffled implements MaximumMatchingWeighted {

		private final MaximumMatchingWeighted algo;
		private final SeedGenerator seedGen;

		MatchingWeightedShuffled(MaximumMatchingWeighted algo, long seed) {
			this.algo = algo;
			seedGen = new SeedGenerator(seed);
		}

		@Override
		public IntCollection computeMaximumMatching(UGraph g, EdgeWeightFunc w) {
			return computeMaximumMatchingShuffled(g, w, false);
		}

		@Override
		public IntCollection computeMaximumPerfectMatching(UGraph g, EdgeWeightFunc w) {
			return computeMaximumMatchingShuffled(g, w, true);
		}

		private IntCollection computeMaximumMatchingShuffled(UGraph g, EdgeWeightFunc w, boolean perfect) {
			int n = g.vertices().size();
			int[] shuffle = randPermutation(n, seedGen.nextSeed());

			UGraph shuffledG = new GraphArrayUndirected(n);

			Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);
			if (partition != null) {
				/* bipartite graph */
				Weights.Bool partitionSuffled = g.addVerticesWeights(Weights.DefaultBipartiteWeightKey, boolean.class);

				int[] shuffleInv = new int[n];
				for (int v = 0; v < n; v++)
					shuffleInv[shuffle[v]] = v;

				for (int v = 0; v < n; v++)
					partitionSuffled.set(v, partition.getBool(shuffleInv[v]));
			}

			Weights.Int edgeRef = shuffledG.addEdgesWeights("edgeRef", int.class, Integer.valueOf(-1));
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int e0 = shuffledG.addEdge(shuffle[u], shuffle[v]);
				edgeRef.set(e0, e);
			}

			EdgeWeightFunc shuffledW = e -> w.weight(edgeRef.getInt(e));

			IntCollection shuffledEdges = perfect ? algo.computeMaximumPerfectMatching(shuffledG, shuffledW)
					: algo.computeMaximumMatching(shuffledG, shuffledW);

			IntList unshuffledEdges = new IntArrayList(shuffledEdges.size());
			for (IntIterator it = shuffledEdges.iterator(); it.hasNext();) {
				int e = it.nextInt();
				unshuffledEdges.add(edgeRef.getInt(e));
			}
			return unshuffledEdges;
		}

	}
}
