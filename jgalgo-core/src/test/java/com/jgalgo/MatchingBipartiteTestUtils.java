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

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Objects;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

class MatchingBipartiteTestUtils extends TestUtils {

	private MatchingBipartiteTestUtils() {}

	static Graph randGraphBipartite(int sn, int tn, int m, Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		return new RandomGraphBuilder(seed).sn(sn).tn(tn).m(m).directed(false).bipartite(true).parallelEdges(false)
				.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	static void randBipartiteGraphs(MaximumMatching algo, long seed) {
		randBipartiteGraphs(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	static void randBipartiteGraphs(MaximumMatching algo, Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 4, 4, 4), phase(64, 16, 16, 64), phase(8, 128, 128, 128),
				phase(8, 128, 128, 512), phase(1, 300, 300, 1100));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];
			Graph g = randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());

			int expected = calcExpectedMaxMatching(g);
			testBipartiteAlgo(algo, g, expected);
		});
	}

	private static void testBipartiteAlgo(MaximumMatching algo, Graph g, int expectedMatchSize) {
		Matching match = algo.computeMaximumCardinalityMatching(g);

		MatchingUnweightedTestUtils.validateMatching(g, match);

		if (match.edges().size() > expectedMatchSize) {
			System.err.println("matching is bigger than validation algo found: " + match.edges().size() + " > "
					+ expectedMatchSize);
			throw new IllegalStateException();
		}
		assertTrue(match.edges().size() == expectedMatchSize, "unexpected match size");
	}

	private static int calcExpectedMaxMatching(Graph g) {
		Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight" + Weights.DefaultBipartiteWeightKey);

		Int2IntMap S = new Int2IntOpenHashMap();
		Int2IntMap T = new Int2IntOpenHashMap();
		for (int u : g.vertices()) {
			if (partition.getBool(u)) {
				S.put(u, S.size());
			} else {
				T.put(u, T.size());
			}
		}

		boolean[][] m = new boolean[S.size()][T.size()];
		for (int u : S.keySet()) {
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				m[S.get(u)][T.get(v)] = true;
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
