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

package com.jgalgo.alg.shortestpath;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.tree.Trees;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

public class ShortestPathSingleSourceTestUtils extends TestBase {

	private ShortestPathSingleSourceTestUtils() {}

	public static void testSsspDirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSsspPositiveInt(algo, true, seed);
	}

	public static void testSsspUndirectedPositiveInt(ShortestPathSingleSource algo, long seed) {
		testSsspPositiveInt(algo, false, seed);
	}

	private static void testSsspPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed) {
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(1);
		testSsspPositiveInt(algo, directed, seed, tester);
	}

	static void testSsspPositiveInt(ShortestPathSingleSource algo, boolean directed, long seed, PhasedTester tester) {
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

	static void testSsspPositive(ShortestPathSingleSource algo, boolean directed, long seed) {
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

			ShortestPathSingleSource validationAlgo;
			if (!(algo instanceof ShortestPathSingleSourceDijkstra)) {
				validationAlgo = new ShortestPathSingleSourceDijkstra();
			} else {
				validationAlgo = new ShortestPathSingleSource() {
					ShortestPathSingleSource bellmanFord = new ShortestPathSingleSourceBellmanFord();

					@Override
					public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g,
							WeightFunction<E> w, V source) {
						if (g.isDirected())
							return bellmanFord.computeShortestPaths(g, w, source);

						IndexGraph ig = g.indexGraph();
						IndexIdMap<V> viMap = g.indexGraphVerticesMap();
						IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
						IndexGraphFactory g2Factory = IndexGraphFactory.directed();
						if (ig.isAllowSelfEdges())
							g2Factory.allowSelfEdges();
						if (ig.isAllowParallelEdges())
							g2Factory.allowParallelEdges();
						IndexGraph g2 = g2Factory.newGraph();
						g2.addVertices(range(g.vertices().size()));
						IWeightsDouble w2 = g2.addEdgesWeights("weight", double.class);
						for (int eIdx : range(g.edges().size())) {
							int uIdx = ig.edgeSource(eIdx);
							int vIdx = ig.edgeTarget(eIdx);
							double weight = w.weight(eiMap.indexToId(eIdx));
							int e1 = g2.addEdge(uIdx, vIdx);
							int e2 = g2.addEdge(vIdx, uIdx);
							w2.set(e1, weight);
							w2.set(e2, weight);
						}

						ShortestPathSingleSource.IResult res = (ShortestPathSingleSource.IResult) bellmanFord
								.computeShortestPaths(g2, w2, Integer.valueOf(viMap.idToIndex(source)));
						return new ShortestPathSingleSource.Result<>() {

							@Override
							public double distance(V target) {
								return res.distance(viMap.idToIndex(target));
							}

							@Override
							public Path<V, E> getPath(V target) {
								IntList edges = res.getPath(viMap.idToIndex(target)).edges();
								if (edges == null)
									return null;
								return Path.valueOf(g, source, target, IndexIdMaps.indexToIdList(edges, eiMap));
							}

							@Override
							public E backtrackEdge(V target) {
								int eIdx = res.backtrackEdge(viMap.idToIndex(target));
								return eIdx == -1 ? null : eiMap.indexToId(eIdx / 2);
							}

							@Override
							public Set<V> reachableVertices() {
								return IndexIdMaps.indexToIdSet(res.reachableVertices(), viMap);
							}

							@Override
							public V source() {
								return source;
							}

							@Override
							public Graph<V, E> graph() {
								return g;
							}
						};
					}
				};
			}
			testAlgo(g, w, source, algo, validationAlgo);
		});
	}

	static void testSsspCardinality(ShortestPathSingleSource algo, boolean directed, long seed) {
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

	static void testSsspDirectedNegativeInt(ShortestPathSingleSource algo, long seed) {
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

		Set<Integer> reachableVertices = result.reachableVertices();
		for (Integer target : g.vertices()) {
			double expectedDistance = expectedRes.distance(target);
			double actualDistance = result.distance(target);
			assertEquals(expectedDistance, actualDistance, 1e-8, "Distance to vertex " + target + " is wrong");
			Path<Integer, Integer> path = result.getPath(target);
			if (path != null) {
				double pathWeight = WeightFunction.weightSum(w, path.edges());
				assertEquals(pathWeight, actualDistance, 1e-8, () -> "Path to vertex " + target
						+ " doesn't match distance (" + actualDistance + " != " + pathWeight + "): " + path);
				if (path.edges().isEmpty()) {
					assertNull(result.backtrackEdge(target));
				} else {
					Integer lastEdge = path.edges().get(path.edges().size() - 1);
					assertEquals(lastEdge, result.backtrackEdge(target));
				}

				assertTrue(result.isReachable(target));
				assertTrue(reachableVertices.contains(target));
			} else {
				assertNull(result.backtrackEdge(target));
				assertEquals(Double.POSITIVE_INFINITY, actualDistance,
						"Distance to vertex " + target + " is not infinity but path is null");

				assertFalse(result.isReachable(target));
				assertFalse(reachableVertices.contains(target));
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
				() -> new ShortestPathSingleSourceAbstract.IndexResult(g, 0, new double[2], new int[1]));
		assertThrows(IllegalArgumentException.class,
				() -> new ShortestPathSingleSourceAbstract.IndexResult(g, 0, new double[1], new int[2]));
	}

	@Test
	public void testBuilderNegInt() {
		ShortestPathSingleSource.Builder builder = ShortestPathSingleSource.builder();
		assertNotNull(builder.build());

		builder.negativeWeights(false);
		builder.integerWeights(false);
		assertEquals(ShortestPathSingleSourceDijkstra.class, builder.build().getClass());

		builder.negativeWeights(true);
		builder.integerWeights(true);
		assertEquals(ShortestPathSingleSourceGoldberg.class, builder.build().getClass());

		builder.negativeWeights(true);
		builder.integerWeights(false);
		assertEquals(ShortestPathSingleSourceBellmanFord.class, builder.build().getClass());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testBuilderMaxDistance() {
		ShortestPathSingleSource.Builder builder = ShortestPathSingleSource.builder();
		builder.integerWeights(true);
		builder.maxDistance(10);

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
