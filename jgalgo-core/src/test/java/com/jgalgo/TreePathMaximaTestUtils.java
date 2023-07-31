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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;

public class TreePathMaximaTestUtils extends TestUtils {

	private TreePathMaximaTestUtils() {}

	private static int[] calcExpectedTPM(Graph t, WeightFunction w, TreePathMaxima.Queries queries) {
		int queriesNum = queries.size();
		int[] res = new int[queriesNum];
		for (int q = 0; q < queriesNum; q++) {
			IntIntPair query = queries.getQuery(q);
			int u = query.firstInt(), v = query.secondInt();

			Path path = Path.findPath(t, u, v);

			int maxEdge = -1;
			double maxEdgeWeight = 0;
			for (int e : path) {
				if (maxEdge == -1 || w.weight(e) > maxEdgeWeight) {
					maxEdge = e;
					maxEdgeWeight = w.weight(e);
				}
			}
			res[q] = maxEdge;
		}
		return res;
	}

	public static TreePathMaxima.Queries generateAllPossibleQueries(Graph tree) {
		TreePathMaxima.Queries queries = TreePathMaxima.Queries.newInstance();
		int[] vs = tree.vertices().toIntArray();
		for (int i = 0; i < vs.length; i++)
			for (int j = i + 1; j < vs.length; j++)
				queries.addQuery(vs[i], vs[j]);
		return queries;
	}

	private static TreePathMaxima.Queries generateRandQueries(Graph tree, int m, long seed) {
		Random rand = new Random(seed);
		TreePathMaxima.Queries queries = TreePathMaxima.Queries.newInstance();
		int[] vs = tree.vertices().toIntArray();
		for (int q = 0; q < m; q++) {
			int i, j;
			do {
				i = rand.nextInt(vs.length);
				j = rand.nextInt(vs.length);
			} while (i == j);
			queries.addQuery(vs[i], vs[j]);
		}
		return queries;
	}

	static void compareActualToExpectedResults(TreePathMaxima.Queries queries, TreePathMaxima.Result actual,
			int[] expected, WeightFunction w) {
		assertEquals(expected.length, actual.size(), "Unexpected result size");
		for (int i = 0; i < actual.size(); i++) {
			IntIntPair query = queries.getQuery(i);
			int u = query.firstInt(), v = query.secondInt();
			double aw = actual.getHeaviestEdge(i) != -1 ? w.weight(actual.getHeaviestEdge(i)) : Double.MIN_VALUE;
			double ew = expected[i] != -1 ? w.weight(expected[i]) : Double.MIN_VALUE;
			assertEquals(ew, aw, "Unexpected result for query (" + u + ", " + v + "): " + actual.getHeaviestEdge(i)
					+ " != " + expected[i]);
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
		Graph tree = GraphsTestUtils.randTree(n, seedGen.nextSeed());
		WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(tree, seedGen.nextSeed());

		TreePathMaxima.Queries queries = n <= 32 ? generateAllPossibleQueries(tree)
				: generateRandQueries(tree, Math.min(n * 16, 1000), seedGen.nextSeed());
		TreePathMaxima.Result actual = algo.computeHeaviestEdgeInTreePaths(tree, w, queries);
		int[] expected = calcExpectedTPM(tree, w, queries);
		compareActualToExpectedResults(queries, actual, expected, w);
	}

	static void verifyMSTPositive(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 16), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 2048, 4096), phase(2, 8192, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			IntCollection mstEdges = new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w).edges();

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

			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			IntCollection mstEdges =
					new IntArrayList(new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w).edges());
			Graph mst = GraphFactory.newUndirected().newGraph();
			for (int v : g.vertices())
				mst.addVertex(v);
			for (int e : mstEdges)
				mst.addEdge(g.edgeSource(e), g.edgeTarget(e), e);

			Random rand = new Random(seedGen.nextSeed());
			for (int[] edges = g.edges().toIntArray();;) {
				int badEdge = edges[rand.nextInt(edges.length)];
				if (mstEdges.contains(badEdge))
					continue;
				int badEdgeSource = g.edgeSource(badEdge);
				int badEdgeTarget = g.edgeTarget(badEdge);
				if (badEdgeSource == badEdgeTarget)
					continue;

				Path mstPath = Path.findPath(mst, badEdgeSource, badEdgeTarget);
				int goodEdge = mstPath.getInt(rand.nextInt(mstPath.size()));

				if (w.weightInt(goodEdge) < w.weightInt(badEdge)) {
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
