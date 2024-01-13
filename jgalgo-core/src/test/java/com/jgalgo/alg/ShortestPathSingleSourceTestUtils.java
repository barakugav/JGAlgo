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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSingleSourceTestUtils extends TestBase {

	private ShortestPathSingleSourceTestUtils() {}

	public static void testSSSPDirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, true, seed);
	}

	public static void testSSSPUndirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSSSPPositiveInt(algo, false, seed);
	}

	private static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed) {
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(1);
		testSSSPPositiveInt(algo, directed, seed, tester);
	}

	static void testSSSPPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed, PhasedTester tester) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial()
							: new ShortestPathSingleSourceDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPPositive(ShortestPathSingleSource algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 6).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(3542, 25436).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			WeightFunction<Integer> w = GraphsTestUtils.assignRandWeights(g, seedGen.nextSeed());
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo = new ShortestPathSingleSourceDijkstra();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSSSPCardinality(ShortestPathSingleSource algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			Integer source = Graphs.randVertex(g, rand);

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceDijkstra ? new ShortestPathSingleSourceDial()
							: new ShortestPathSingleSourceDijkstra();
			testAlgo(g, null, source, algo, validationAlgo);
		});
	}

	static void testSSSPDirectedNegativeInt(ShortestPathSingleSource algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 8).repeat(512);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(1024, 4096).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randConnectedGraph(n, m, true, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			Integer source = g.vertices().iterator().next();

			ShortestPathSingleSource validationAlgo =
					algo instanceof ShortestPathSingleSourceBellmanFord ? new ShortestPathSingleSourceGoldberg()
							: new ShortestPathSingleSourceBellmanFord();
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testAlgo(Graph<Integer, Integer> g, WeightFunction<Integer> w, Integer source,
			ShortestPathSingleSource algo, ShortestPathSingleSource validationAlgo) {
		ShortestPathSingleSource.Result<Integer, Integer> result;
		try {
			result = algo.computeShortestPaths(g, w, source);
		} catch (NegativeCycleException e) {
			validateResult(g, w, source, e, validationAlgo);
			return;
		}
		validateResult(g, w, source, result, validationAlgo);
	}

	static void validateResult(Graph<Integer, Integer> g, WeightFunction<Integer> w, Integer source,
			NegativeCycleException negCycleExc, ShortestPathSingleSource validationAlgo) {
		Path<Integer, Integer> cycle = negCycleExc.cycle(g);
		double cycleWeight = w.weightSum(cycle.edges());
		assertTrue(cycleWeight != Double.NaN, () -> "Invalid cycle: " + cycle);
		assertTrue(cycleWeight < 0, () -> "Cycle is not negative: " + cycle);

		/*
		 * If the cycle is not reachable from the source, some algorithm will not find the cycle, and we can't assume
		 * the validation algo will
		 */
		if (Path.reachableVertices(g, source).contains(cycle.source()))
			assertThrows(NegativeCycleException.class, () -> validationAlgo.computeShortestPaths(g, w, source),
					() -> "validation algorithm didn't find negative cycle: " + cycle);
	}

	static void validateResult(Graph<Integer, Integer> g, WeightFunction<Integer> w, Integer source,
			ShortestPathSingleSource.Result<Integer, Integer> result, ShortestPathSingleSource validationAlgo) {
		final Random rand = new Random(0x1d8e6137801437c8L);
		ShortestPathSingleSource.Result<Integer, Integer> expectedRes;
		try {
			expectedRes = validationAlgo.computeShortestPaths(g, w, source);
		} catch (NegativeCycleException e) {
			/* failed to find a negative cycle */
			/* this is a fail only if the cycle is reachable from the source */
			if (Path.reachableVertices(g, source).contains(e.cycle().source()))
				fail("failed to find negative cycle: " + e.cycle(g));
			return;
		}

		assertEquals(source, result.source());
		assertTrue(g == result.graph());

		for (Integer target : g.vertices()) {
			double expectedDistance = expectedRes.distance(target);
			double actualDistance = result.distance(target);
			assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
			Path<Integer, Integer> path = result.getPath(target);
			if (path != null) {
				double pathWeight = WeightFunction.weightSum(w, path.edges());
				assertEquals(pathWeight, actualDistance, () -> "Path to vertex " + target + " doesn't match distance ("
						+ actualDistance + " != " + pathWeight + "): " + path);
				if (path.edges().isEmpty()) {
					assertNull(result.backtrackEdge(target));
				} else {
					Integer lastEdge = path.edges().get(path.edges().size() - 1);
					assertEquals(lastEdge, result.backtrackEdge(target));
				}
			} else {
				assertNull(result.backtrackEdge(target));
				assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + target + " is not infinity but path is null");
			}
		}
		assertThrows(NoSuchVertexException.class, () -> result.distance(GraphsTestUtils.nonExistingVertex(g, rand)));
		assertThrows(NoSuchVertexException.class, () -> result.getPath(GraphsTestUtils.nonExistingVertex(g, rand)));
		assertThrows(NoSuchVertexException.class,
				() -> result.backtrackEdge(GraphsTestUtils.nonExistingVertex(g, rand)));

		{
			Graph<Integer, Integer> tree = result.shortestPathTree();
			assertTrue(Trees.isTree(tree, source));
			assertEquals(Path.reachableVertices(g, source), tree.vertices());
			assertEqualsBool(g.isDirected(), tree.isDirected());
			for (int repeat = 0; repeat < 10; repeat++) {
				Integer target = Graphs.randVertex(g, rand);
				Path<Integer, Integer> path = result.getPath(target);
				Path<Integer, Integer> treePath =
						tree.vertices().contains(target) ? Path.findPath(tree, source, target) : null;
				List<Integer> pathEdges = path == null ? null : path.edges();
				List<Integer> treePathEdges = treePath == null ? null : treePath.edges();
				if (pathEdges == null) {
					assertNull(treePathEdges);
				} else {
					assertNotNull(treePathEdges);
					assertEquals(WeightFunction.weightSum(w, pathEdges), WeightFunction.weightSum(w, treePathEdges));
				}
			}
		}
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> tree = result.shortestPathTree(directed);
			assertTrue(Trees.isTree(tree, source));
			assertEquals(Path.reachableVertices(g, source), tree.vertices());
			assertEqualsBool(directed, tree.isDirected());
			for (int repeat = 0; repeat < 10; repeat++) {
				Integer target = Graphs.randVertex(g, rand);
				Path<Integer, Integer> path = result.getPath(target);
				Path<Integer, Integer> treePath =
						tree.vertices().contains(target) ? Path.findPath(tree, source, target) : null;
				List<Integer> pathEdges = path == null ? null : path.edges();
				List<Integer> treePathEdges = treePath == null ? null : treePath.edges();
				if (pathEdges == null) {
					assertNull(treePathEdges);
				} else {
					assertNotNull(treePathEdges);
					assertEquals(WeightFunction.weightSum(w, pathEdges), WeightFunction.weightSum(w, treePathEdges));
				}
			}
		});
	}

	@Test
	public void resultObjectWithInvalidDistanceOrBacktrackArrays() {
		IndexGraph g = IndexGraph.newDirected();
		g.addVertexInt();
		assertThrows(IllegalArgumentException.class,
				() -> new ShortestPathSingleSourceUtils.IndexResult(g, 0, new double[2], new int[1]));
		assertThrows(IllegalArgumentException.class,
				() -> new ShortestPathSingleSourceUtils.IndexResult(g, 0, new double[1], new int[2]));
	}

	@Test
	public void testBuilderSetOption() {
		ShortestPathSingleSource.Builder builder = ShortestPathSingleSource.builder();
		assertNotNull(builder.build());

		assertThrows(IllegalArgumentException.class, () -> builder.setOption("non-existing-option", "value"));

		builder.setOption("impl", "cardinality");
		assertEquals(ShortestPathSingleSourceCardinality.class, builder.build().getClass());
		builder.setOption("impl", "dag");
		assertEquals(ShortestPathSingleSourceDag.class, builder.build().getClass());
		builder.setOption("impl", "dijkstra");
		assertEquals(ShortestPathSingleSourceDijkstra.class, builder.build().getClass());
		builder.setOption("impl", "dial");
		assertEquals(ShortestPathSingleSourceDial.class, builder.build().getClass());
		builder.setOption("impl", "bellman-ford");
		assertEquals(ShortestPathSingleSourceBellmanFord.class, builder.build().getClass());
		builder.setOption("impl", "goldberg");
		assertEquals(ShortestPathSingleSourceGoldberg.class, builder.build().getClass());

		builder.setOption("impl", "non-existing-imp");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void testBuilderNegInt() {
		ShortestPathSingleSource.Builder builder = ShortestPathSingleSource.builder();
		builder.setNegativeWeights(false);
		builder.setIntWeights(false);
		assertEquals(ShortestPathSingleSourceDijkstra.class, builder.build().getClass());

		builder.setNegativeWeights(true);
		builder.setIntWeights(true);
		assertEquals(ShortestPathSingleSourceGoldberg.class, builder.build().getClass());

		builder.setNegativeWeights(true);
		builder.setIntWeights(false);
		assertEquals(ShortestPathSingleSourceBellmanFord.class, builder.build().getClass());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testBuilderMaxDistance() {
		ShortestPathSingleSource.Builder builder = ShortestPathSingleSource.builder();
		builder.setIntWeights(true);
		builder.setMaxDistance(10);

		Graph<Integer, Integer> g;
		ShortestPathSingleSource.Result<Integer, Integer> res;

		g = new CompleteGraphGenerator<Integer, Integer>()
				.vertices(range(25))
				.edges(IdBuilderInt.defaultBuilder())
				.generate();
		res = builder.build().computeShortestPaths(g, null, 0);
		validateResult(g, null, 0, res, new ShortestPathSingleSourceDijkstra());

		g = new CompleteGraphGenerator<Integer, Integer>()
				.vertices(range(2))
				.edges(IdBuilderInt.defaultBuilder())
				.generate();
		res = builder.build().computeShortestPaths(g, null, 0);
		validateResult(g, null, 0, res, new ShortestPathSingleSourceDijkstra());
	}

}
