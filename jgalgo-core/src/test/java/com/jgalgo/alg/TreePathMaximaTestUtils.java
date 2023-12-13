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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class TreePathMaximaTestUtils extends TestUtils {

	private TreePathMaximaTestUtils() {}

	private static <V, E> List<E> calcExpectedTPM(Graph<V, E> t, WeightFunction<E> w,
			TreePathMaxima.Queries<V, E> queries) {
		int queriesNum = queries.size();
		List<E> res = new ArrayList<>(queriesNum);
		for (int q = 0; q < queriesNum; q++) {
			V u = queries.getQuerySource(q), v = queries.getQueryTarget(q);

			Path<V, E> path = Path.findPath(t, u, v);

			E maxEdge = null;
			double maxEdgeWeight = 0;
			for (E e : path.edges()) {
				if (maxEdge == null || w.weight(e) > maxEdgeWeight) {
					maxEdge = e;
					maxEdgeWeight = w.weight(e);
				}
			}
			res.add(maxEdge);
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public static <V, E> TreePathMaxima.Queries<V, E> generateAllPossibleQueries(Graph<V, E> tree) {
		TreePathMaxima.Queries<V, E> queries;
		if (tree instanceof IntGraph) {
			queries = TreePathMaxima.Queries.newInstance();
		} else {
			queries = (TreePathMaxima.Queries<V, E>) TreePathMaxima.IQueries.newInstance();
		}
		List<V> vs = new ArrayList<>(tree.vertices());
		for (int i = 0; i < vs.size(); i++)
			for (int j = i + 1; j < vs.size(); j++)
				queries.addQuery(vs.get(i), vs.get(j));
		return queries;
	}

	@SuppressWarnings("unchecked")
	private static <V, E> TreePathMaxima.Queries<V, E> generateRandQueries(Graph<V, E> tree, int m, long seed) {
		Random rand = new Random(seed);
		TreePathMaxima.Queries<V, E> queries;
		if (tree instanceof IntGraph) {
			queries = TreePathMaxima.Queries.newInstance();
		} else {
			queries = (TreePathMaxima.Queries<V, E>) TreePathMaxima.IQueries.newInstance();
		}
		for (int q = 0; q < m; q++) {
			V i, j;
			do {
				i = Graphs.randVertex(tree, rand);
				j = Graphs.randVertex(tree, rand);
			} while (i.equals(j));
			queries.addQuery(i, j);
		}
		return queries;
	}

	static <V, E> void compareActualToExpectedResults(TreePathMaxima.Queries<V, E> queries,
			TreePathMaxima.Result<V, E> actual, List<E> expected, WeightFunction<E> w) {
		assertEquals(expected.size(), actual.size(), "Unexpected result size");
		for (int i = 0; i < actual.size(); i++) {
			V u = queries.getQuerySource(i), v = queries.getQueryTarget(i);
			double aw = actual.getHeaviestEdge(i) != null ? w.weight(actual.getHeaviestEdge(i)) : Double.MIN_VALUE;
			double ew = expected.get(i) != null ? w.weight(expected.get(i)) : Double.MIN_VALUE;
			assertEquals(ew, aw, "Unexpected result for query (" + u + ", " + v + "): " + actual.getHeaviestEdge(i)
					+ " != " + expected.get(i));
		}
	}

	static void testTPM(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(64);
		tester.addPhase().withArgs(32).repeat(32);
		tester.addPhase().withArgs(64).repeat(16);
		tester.addPhase().withArgs(128).repeat(8);
		tester.addPhase().withArgs(256).repeat(4);
		tester.addPhase().withArgs(512).repeat(2);
		tester.addPhase().withArgs(1234).repeat(1);
		tester.run(n -> {
			testTPM(algo, n, seedGen.nextSeed());
		});
	}

	private static void testTPM(TreePathMaxima algo, int n, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Graph<Integer, Integer> tree = GraphsTestUtils.randTree(n, seedGen.nextSeed());
		WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(tree, seedGen.nextSeed());

		TreePathMaxima.Queries<Integer, Integer> queries = n <= 32 ? generateAllPossibleQueries(tree)
				: generateRandQueries(tree, Math.min(n * 16, 1000), seedGen.nextSeed());
		TreePathMaxima.Result<Integer, Integer> actual = algo.computeHeaviestEdgeInTreePaths(tree, w, queries);
		List<Integer> expected = calcExpectedTPM(tree, w, queries);
		compareActualToExpectedResults(queries, actual, expected, w);
	}

	static void verifyMSTPositive(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 16).repeat(256);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(2048, 4096).repeat(8);
		tester.addPhase().withArgs(8192, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randConnectedGraph(n, m, false, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			Collection<Integer> mstEdges = new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w).edges();

			boolean isMST = TreePathMaxima.verifyMST(g, w, mstEdges, algo);
			assertTrue(isMST);
		});
	}

	static void verifyMSTNegative(TreePathMaxima algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 16).repeat(256);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(2048, 4096).repeat(8);
		tester.addPhase().withArgs(8192, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randConnectedGraph(n, m, false, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			Set<Integer> mstEdges =
					new IntOpenHashSet(new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w).edges());
			Graph<Integer, Integer> mst = Graph.newUndirected();
			mst.addVertices(g.vertices());
			mst.addEdges(EdgeSet.of(mstEdges, g));

			Random rand = new Random(seedGen.nextSeed());
			for (;;) {
				Integer badEdge = Graphs.randEdge(g, rand);
				if (mstEdges.contains(badEdge))
					continue;
				Integer badEdgeSource = g.edgeSource(badEdge);
				Integer badEdgeTarget = g.edgeTarget(badEdge);
				if (badEdgeSource.equals(badEdgeTarget))
					continue;

				Path<Integer, Integer> mstPath = Path.findPath(mst, badEdgeSource, badEdgeTarget);
				Integer goodEdge = randElement(mstPath.edges(), rand);

				if (w.weightInt(goodEdge) < w.weightInt(badEdge)) {
					mstEdges.remove(goodEdge);
					mstEdges.add(badEdge);
					break;
				}
			}

			boolean isMST = TreePathMaxima.verifyMST(g, w, mstEdges, algo);
			assertFalse(isMST, "MST validation failed");
		});
	}

}
