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

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class TreePathMaximaTestUtils extends TestUtils {

	private TreePathMaximaTestUtils() {}

	private static int[] calcExpectedTPM(Graph t, EdgeWeightFunc w, TreePathMaxima.Queries queries) {
		int queriesNum = queries.size();
		int[] res = new int[queriesNum];
		for (int q = 0; q < queriesNum; q++) {
			IntIntPair query = queries.getQuery(q);
			int u = query.firstInt(), v = query.secondInt();

			Path path = Path.findPath(t, u, v);

			int maxEdge = -1;
			double maxEdgeWeight = 0;
			for (IntIterator it = path.iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (maxEdge == -1 || w.weight(e) > maxEdgeWeight) {
					maxEdge = e;
					maxEdgeWeight = w.weight(e);
				}
			}
			res[q] = maxEdge;
		}
		return res;
	}

	public static TreePathMaxima.Queries generateAllPossibleQueries(int n) {
		TreePathMaxima.Queries queries = new TreePathMaxima.Queries();
		for (int i = 0; i < n; i++)
			for (int j = i; j < n; j++)
				queries.addQuery(i, j);
		return queries;
	}

	public static TreePathMaxima.Queries generateRandQueries(int n, int m, long seed) {
		Random rand = new Random(seed);
		TreePathMaxima.Queries queries = new TreePathMaxima.Queries();
		for (int q = 0; q < m; q++)
			queries.addQuery(rand.nextInt(n), rand.nextInt(n));
		return queries;
	}

	static void compareActualToExpectedResults(TreePathMaxima.Queries queries, int[] actual, int[] expected,
			EdgeWeightFunc w) {
		assertEquals(expected.length, actual.length, "Unexpected result size");
		for (int i = 0; i < actual.length; i++) {
			IntIntPair query = queries.getQuery(i);
			int u = query.firstInt(), v = query.secondInt();
			double aw = actual[i] != -1 ? w.weight(actual[i]) : Double.MIN_VALUE;
			double ew = expected[i] != -1 ? w.weight(expected[i]) : Double.MIN_VALUE;
			assertEquals(ew, aw,
					"Unexpected result for query (" + u + ", " + v + "): " + actual[i] + " != " + expected[i]);
		}
	}

	static void testTPM(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16), phase(32, 32), phase(16, 64), phase(8, 128), phase(4, 256),
				phase(2, 512), phase(1, 1234));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testTPM(algo, n, seedGen.nextSeed());
		});
	}

	private static void testTPM(TreePathMaxima algo, int n, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Graph t = GraphsTestUtils.randTree(n, seedGen.nextSeed());
		EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(t, seedGen.nextSeed());

		TreePathMaxima.Queries queries = n <= 32 ? generateAllPossibleQueries(n)
				: generateRandQueries(n, Math.min(n * 16, 1000), seedGen.nextSeed());
		int[] actual = algo.computeHeaviestEdgeInTreePaths(t, w, queries);
		int[] expected = calcExpectedTPM(t, w, queries);
		compareActualToExpectedResults(queries, actual, expected, w);
	}

	static void verifyMSTPositive(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			IntCollection mstEdges = new MSTKruskal().computeMinimumSpanningTree(g, w);

			boolean isMST = TreePathMaxima.verifyMST(g, w, mstEdges, algo);
			assertTrue(isMST);
		});
	}

	static void verifyMSTNegative(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(true).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			IntCollection mstEdges = new MSTKruskal().computeMinimumSpanningTree(g, w);
			Graph mst = new GraphArrayUndirected(g.vertices().size());
			Weights.Int edgeRef = mst.addEdgesWeights("edgeRef", int.class, Integer.valueOf(-1));
			for (IntIterator it = mstEdges.iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int e0 = mst.addEdge(u, v);
				edgeRef.set(e0, e);
			}

			Random rand = new Random(seedGen.nextSeed());
			int[] edges = g.edges().toIntArray();
			for (;;) {
				int badEdge;
				do {
					badEdge = edges[rand.nextInt(edges.length)];
				} while (mstEdges.contains(badEdge));

				Path mstPath = Path.findPath(mst, g.edgeSource(badEdge), g.edgeTarget(badEdge));
				int goodEdge = mstPath.getInt(rand.nextInt(mstPath.size()));

				if (w.weightInt(edgeRef.getInt(goodEdge)) < w.weightInt(badEdge)) {
					mstEdges.rem(goodEdge);
					mstEdges.add(badEdge);
					break;
				}
			}

			boolean isMST = TreePathMaxima.verifyMST(g, w, mstEdges, algo);
			assertFalse(isMST, "MST validation failed");
		});
	}

}
