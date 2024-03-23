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
package com.jgalgo.alg.distancemeasures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.path.ShortestPathSingleSource;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class DistanceMeasuresTest extends TestBase {

	@Test
	public void undirectedUnweighted() {
		final long seed = 0xa97bf8dba5505a70L;
		testMeasures(false, false, seed);
	}

	@Test
	public void undirectedWeighted() {
		final long seed = 0x1f8b2be85588113dL;
		testMeasures(false, true, seed);
	}

	@Test
	public void directedUnweighted() {
		final long seed = 0x5f40ada73f4bddfaL;
		testMeasures(true, false, seed);
	}

	@Test
	public void directedWeighted() {
		final long seed = 0x7045e12d08f1de96L;
		testMeasures(true, true, seed);
	}

	private static void testMeasures(boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(0, 0).repeat(1);
		tester.addPhase().withArgs(4, 6).repeat(16);
		tester.addPhase().withArgs(8, 16).repeat(64);
		tester.addPhase().withArgs(32, 64).repeat(32);
		tester.addPhase().withArgs(100, 2311).repeat(3);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightFunction<Integer> w = null;
			if (weighted && rand.nextBoolean()) {
				w = GraphsTestUtils.assignRandWeights(g, seedGen.nextSeed());
			} else if (weighted) {
				w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			}
			testMeasures(g, w);
		});
	}

	private static void testMeasures(Graph<Integer, Integer> g, WeightFunction<Integer> w) {
		final Random rand = new Random(0x851dbddbf1b5625fL);
		DistanceMeasures<Integer, Integer> measures = DistanceMeasures.of(g, w);

		ShortestPathSingleSource sssp = ShortestPathSingleSource.newInstance();
		Object2DoubleMap<Integer> expectedEccentricity = new Object2DoubleOpenHashMap<>();
		for (Integer v : g.vertices()) {
			ShortestPathSingleSource.Result<Integer, Integer> sp = sssp.computeShortestPaths(g, w, v);
			expectedEccentricity
					.put(v, g.vertices().stream().mapToDouble(sp::distance).max().orElse(Double.POSITIVE_INFINITY));
		}

		for (Integer v : g.vertices())
			assertEquals(expectedEccentricity.getDouble(v), measures.eccentricity(v), 1e-9);
		assertEquals(expectedEccentricity.values().doubleStream().min().orElse(Double.POSITIVE_INFINITY),
				measures.radius(), 1e-9);
		assertEquals(expectedEccentricity.values().doubleStream().max().orElse(Double.POSITIVE_INFINITY),
				measures.diameter(), 1e-9);
		assertEquals(g
				.vertices()
				.stream()
				.filter(v -> expectedEccentricity.getDouble(v) <= measures.radius() + 1e-9)
				.collect(Collectors.toSet()), measures.center());
		assertEquals(g
				.vertices()
				.stream()
				.filter(v -> measures.eccentricity(v) <= measures.radius() + 1e-9)
				.collect(Collectors.toSet()), measures.center());
		assertEquals(g
				.vertices()
				.stream()
				.filter(v -> expectedEccentricity.getDouble(v) >= measures.diameter() - 1e-9)
				.collect(Collectors.toSet()), measures.periphery());
		assertEquals(g
				.vertices()
				.stream()
				.filter(v -> measures.eccentricity(v) >= measures.diameter() - 1e-9)
				.collect(Collectors.toSet()), measures.periphery());

		for (int repeat = 0; repeat < 25; repeat++)
			assertThrows(NoSuchVertexException.class,
					() -> measures.eccentricity(GraphsTestUtils.nonExistingVertex(g, rand)));
	}

}
