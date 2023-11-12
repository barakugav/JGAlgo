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
import java.util.Objects;
import java.util.Random;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class MatchingBipartiteTestUtils extends TestUtils {

	private MatchingBipartiteTestUtils() {}

	static Graph<Integer, Integer> randGraphBipartite(int sn, int tn, int m,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		return new RandomGraphBuilder(seed).sn(sn).tn(tn).m(m).directed(false).bipartite(true).parallelEdges(false)
				.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	static void randBipartiteGraphs(MatchingAlgo algo, long seed) {
		randBipartiteGraphs(algo, GraphsTestUtils.defaultGraphImpl(seed), seed);
	}

	public static void randBipartiteGraphs(MatchingAlgo algo, Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 4, 4).repeat(128);
		tester.addPhase().withArgs(16, 16, 64).repeat(64);
		tester.addPhase().withArgs(128, 128, 128).repeat(8);
		tester.addPhase().withArgs(128, 128, 512).repeat(8);
		tester.addPhase().withArgs(300, 300, 1100).repeat(1);
		tester.run((sn, tn, m) -> {
			Graph<Integer, Integer> g = randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());

			int expected = calcExpectedMaxMatching(g);
			testBipartiteAlgo(algo, g, expected, rand);
		});
	}

	private static <V, E> void testBipartiteAlgo(MatchingAlgo algo, Graph<V, E> g, int expectedMatchSize, Random rand) {
		Matching<V, E> match;
		if (rand.nextBoolean() && new MatchingCardinalityBipartiteHopcroftKarp()
				.computeMaximumCardinalityMatching(g.indexGraph()).isPerfect()) {
			if (rand.nextBoolean()) {
				/* maximum perfect matching is the same as maximum matching in unweighted graph */
				match = algo.computeMaximumPerfectMatching(g, null);

			} else {
				/* min/max has no effect as we are unweighted perfect */
				match = algo.computeMinimumPerfectMatching(g, null);
			}
		} else {
			match = algo.computeMaximumMatching(g, null);
		}

		MatchingUnweightedTestUtils.validateMatching(g, match);

		if (match.edges().size() > expectedMatchSize) {
			System.err.println("matching is bigger than validation algo found: " + match.edges().size() + " > "
					+ expectedMatchSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedMatchSize, match.edges().size(), "unexpected match size");
	}

	private static <V, E> int calcExpectedMaxMatching(Graph<V, E> g) {
		WeightsBool<V> partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight" + BipartiteGraphs.VertexBiPartitionWeightKey);

		Object2IntMap<V> S = new Object2IntOpenHashMap<>();
		Object2IntMap<V> T = new Object2IntOpenHashMap<>();
		for (V u : g.vertices()) {
			if (partition.get(u)) {
				S.put(u, S.size());
			} else {
				T.put(u, T.size());
			}
		}

		boolean[][] m = new boolean[S.size()][T.size()];
		for (V u : S.keySet()) {
			for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.next();
				V v = eit.target();
				m[S.getInt(u)][T.getInt(v)] = true;
			}
		}
		return maxBPM(m);
	}

	/*
	 * Maximum Bipartite Matching implementation of Ford-Fulkerson algorithm from the Internet
	 */
	private static int maxBPM(boolean g[][]) {
		int sn = g.length, tn = g[0].length;
		int[] matchR = new int[tn];

		for (int i = 0; i < tn; ++i)
			matchR[i] = -1;

		int result = 0;
		for (int u = 0; u < sn; u++) {
			boolean[] visited = new boolean[tn];
			for (int i = 0; i < tn; ++i)
				visited[i] = false;

			if (bpm(g, u, visited, matchR))
				result++;
		}
		return result;
	}

	private static boolean bpm(boolean g[][], int u, boolean visited[], int matchR[]) {
		int tn = g[0].length;
		for (int v = 0; v < tn; v++) {
			if (g[u][v] && !visited[v]) {
				visited[v] = true;
				if (matchR[v] < 0 || bpm(g, matchR[v], visited, matchR)) {
					matchR[v] = u;
					return true;
				}
			}
		}
		return false;
	}

}
