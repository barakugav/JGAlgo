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

package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class ShortestPathAllPairsTestUtils extends TestBase {

	private ShortestPathAllPairsTestUtils() {}

	static void testAPSPPositive(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, allVertices, w, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPCardinality(ShortestPathAllPairs algo, boolean directed, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			testAPSP(g, verticesSubset, allVertices, null, algo, new ShortestPathSingleSourceDijkstra());
		});
	}

	static void testAPSPDirectedNegative(ShortestPathAllPairs algo, boolean allVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 20).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 256).repeat(10);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, true, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Collection<Integer> verticesSubset = verticesSubset(g, allVertices, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			testAPSP(g, verticesSubset, allVertices, w, algo, new ShortestPathSingleSourceGoldberg());
		});
	}

	private static <V, E> Collection<V> verticesSubset(Graph<V, E> g, boolean allVertices, long seed) {
		int n = g.vertices().size();
		if (allVertices || n <= 3)
			return g.vertices();
		Random rand = new Random(seed);
		Set<V> subset = new ObjectOpenHashSet<>();
		while (subset.size() < n / 2)
			subset.add(Graphs.randVertex(g, rand));
		return subset;
	}

	static void testAPSP(Graph<Integer, Integer> g, Collection<Integer> verticesSubset, boolean allVertices,
			WeightFunction<Integer> w, ShortestPathAllPairs algo, ShortestPathSingleSource validationAlgo) {
		final Random rand = new Random(0xd5106f1aa2b4b738L);

		ShortestPathAllPairs.Result<Integer, Integer> result0;
		NegativeCycleException actualNegCycle = null;
		try {
			if (allVertices) {
				result0 = algo.computeAllShortestPaths(g, w);
			} else {
				result0 = algo.computeSubsetShortestPaths(g, verticesSubset, w);
			}
		} catch (NegativeCycleException e) {
			actualNegCycle = e;
			result0 = null;
		}
		final ShortestPathAllPairs.Result<Integer, Integer> result = result0;

		for (Integer source : verticesSubset) {
			ShortestPathSingleSource.Result<Integer, Integer> expectedRes;
			NegativeCycleException expectedNegCycle = null;
			try {
				expectedRes = validationAlgo.computeShortestPaths(g, w, source);
			} catch (NegativeCycleException e) {
				expectedNegCycle = e;
				expectedRes = null;
			}
			if (expectedNegCycle != null)
				assertNotNull(actualNegCycle, "failed to found negative cycle");

			if (actualNegCycle != null) {
				Path<Integer, Integer> cycle = actualNegCycle.cycle(g);
				double cycleWeight = w.weightSum(cycle.edges());
				assertTrue(cycleWeight != Double.NaN, "Invalid cycle: " + cycle);
				assertTrue(cycleWeight < 0, "Cycle is not negative: " + cycle);
				if (expectedNegCycle == null)
					throw new IllegalStateException("validation algorithm didn't find negative cycle: " + cycle);
				return;
			}

			for (Integer target : verticesSubset) {
				double expectedDistance = expectedRes.distance(target);
				double actualDistance = result.distance(source, target);
				assertEquals(expectedDistance, actualDistance, "Distance to vertex " + target + " is wrong");
				Path<Integer, Integer> path = result.getPath(source, target);
				if (path != null) {
					double pathWeight = WeightFunction.weightSum(w, path.edges());
					assertEquals(pathWeight, actualDistance, "Path to vertex " + target + " doesn't match distance ("
							+ actualDistance + " != " + pathWeight + "): " + path);
				} else {
					assertEquals(Double.POSITIVE_INFINITY, actualDistance,
							"Distance to vertex " + target + " is not infinity but path is null");
				}
			}
		}

		Integer existingVertex = verticesSubset.iterator().next();
		Integer nonExistingVertex = GraphsTestUtils.nonExistingVertex(g, rand);
		assertThrows(NoSuchVertexException.class, () -> result.distance(existingVertex, nonExistingVertex));
		assertThrows(NoSuchVertexException.class, () -> result.distance(nonExistingVertex, existingVertex));
		assertThrows(NoSuchVertexException.class, () -> result.getPath(existingVertex, nonExistingVertex));
		assertThrows(NoSuchVertexException.class, () -> result.getPath(nonExistingVertex, existingVertex));

		if (!allVertices && !g.vertices().equals(verticesSubset)) {
			Integer vNotInSubSet = g.vertices().stream().filter(v -> !verticesSubset.contains(v)).findAny().get();
			for (var p : List.of(Pair.of(vNotInSubSet, existingVertex), Pair.of(existingVertex, vNotInSubSet))) {
				Integer source = p.first(), target = p.second();

				OptionalDouble distance;
				try {
					distance = OptionalDouble.of(result.distance(source, target));
				} catch (IllegalArgumentException e) {
					distance = OptionalDouble.empty(); /* ok, one of the vertices is not in the subset */
				}
				if (distance.isPresent()) {
					double distance0 = distance.getAsDouble();
					double expected = validationAlgo.computeShortestPaths(g, w, source).distance(target);
					assertEquals(expected, distance0);
				}

				Optional<Path<Integer, Integer>> path;
				try {
					path = Optional.ofNullable(result.getPath(source, target));
				} catch (IllegalArgumentException e) {
					path = null; /* ok, one of the vertices is not in the subset */
				}
				if (path != null) {
					Path<Integer, Integer> path0 = path.orElse(null);
					Path<Integer, Integer> expected = validationAlgo.computeShortestPaths(g, w, source).getPath(target);
					if (expected == null) {
						assertNull(path0);
					} else {
						assertNotNull(path0);
						assertEquals(WeightFunction.weightSum(w, expected.edges()),
								WeightFunction.weightSum(w, path0.edges()));
					}
				}
			}
		}
	}

	@Test
	public void testBuilderSetOption() {
		ShortestPathAllPairs.Builder builder = ShortestPathAllPairs.builder();
		assertNotNull(builder.build());

		assertThrows(IllegalArgumentException.class, () -> builder.setOption("non-existing-option", "value"));

		builder.setOption("impl", "cardinality");
		assertEquals(ShortestPathAllPairsCardinality.class, builder.build().getClass());
		builder.setOption("impl", "floyd-warshall");
		assertEquals(ShortestPathAllPairsFloydWarshall.class, builder.build().getClass());
		builder.setOption("impl", "johnson");
		assertEquals(ShortestPathAllPairsJohnson.class, builder.build().getClass());

		builder.setOption("impl", "non-existing-imp");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void testBuilderSetCardinality() {
		ShortestPathAllPairs.Builder builder = ShortestPathAllPairs.builder();
		builder.setCardinality(true);
		assertEquals(ShortestPathAllPairsCardinality.class, builder.build().getClass());
	}

	@Test
	public void testDefaultImpl() {
		ShortestPathAllPairs algo = ShortestPathAllPairs.newInstance();
		Graph<Integer, Integer> g = new CompleteGraphGenerator<Integer, Integer>()
				.vertices(range(25))
				.edges(IdBuilderInt.defaultBuilder())
				.generate();

		testAPSP(g, g.vertices(), true, null, algo, new ShortestPathSingleSourceDijkstra());
		testAPSP(g, g.vertices(), true, e -> 78 + e.intValue(), algo, new ShortestPathSingleSourceDijkstra());
		testAPSP(g, range(5), false, null, algo, new ShortestPathSingleSourceDijkstra());
		testAPSP(g, range(5), false, e -> 78 + e.intValue(), algo, new ShortestPathSingleSourceDijkstra());
	}

}
