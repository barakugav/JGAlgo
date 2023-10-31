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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class MatchingBipartiteTestUtils extends TestUtils {

	private MatchingBipartiteTestUtils() {}

	static IntGraph randGraphBipartite(int sn, int tn, int m, Boolean2ObjectFunction<IntGraph> graphImpl, long seed) {
		return new RandomGraphBuilder(seed).sn(sn).tn(tn).m(m).directed(false).bipartite(true).parallelEdges(false)
				.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
	}

	static void randBipartiteGraphs(MatchingAlgo algo, long seed) {
		randBipartiteGraphs(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void randBipartiteGraphs(MatchingAlgo algo, Boolean2ObjectFunction<IntGraph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 4, 4).repeat(128);
		tester.addPhase().withArgs(16, 16, 64).repeat(64);
		tester.addPhase().withArgs(128, 128, 128).repeat(8);
		tester.addPhase().withArgs(128, 128, 512).repeat(8);
		tester.addPhase().withArgs(300, 300, 1100).repeat(1);
		tester.run((sn, tn, m) -> {
			IntGraph g = randGraphBipartite(sn, tn, m, graphImpl, seedGen.nextSeed());

			int expected = calcExpectedMaxMatching(g);
			testBipartiteAlgo(algo, g, expected);
		});
	}

	private static void testBipartiteAlgo(MatchingAlgo algo, IntGraph g, int expectedMatchSize) {
		IMatching match = (IMatching) algo.computeMaximumCardinalityMatching(g);

		MatchingUnweightedTestUtils.validateMatching(g, match);

		if (match.edges().size() > expectedMatchSize) {
			System.err.println("matching is bigger than validation algo found: " + match.edges().size() + " > "
					+ expectedMatchSize);
			throw new IllegalStateException();
		}
		assertEquals(expectedMatchSize, match.edges().size(), "unexpected match size");
	}

	private static int calcExpectedMaxMatching(IntGraph g) {
		IWeightsBool partition = g.getVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		Objects.requireNonNull(partition,
				"Bipartiteness values weren't found with weight" + BipartiteGraphs.VertexBiPartitionWeightKey);

		Int2IntMap S = new Int2IntOpenHashMap();
		Int2IntMap T = new Int2IntOpenHashMap();
		for (int u : g.vertices()) {
			if (partition.get(u)) {
				S.put(u, S.size());
			} else {
				T.put(u, T.size());
			}
		}

		boolean[][] m = new boolean[S.size()][T.size()];
		for (int u : S.keySet()) {
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
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
