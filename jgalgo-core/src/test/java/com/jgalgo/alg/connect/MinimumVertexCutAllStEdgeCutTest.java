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
package com.jgalgo.alg.connect;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.flow.MaximumFlowTestUtils;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MinimumVertexCutAllStEdgeCutTest extends TestBase {

	@Test
	public void testRandGraphDirectedUnweighted() {
		final long seed = 0x5f14bcbfd8d18cc2L;
		testRandGraphs(new MinimumVertexCutAllStEdgeCut(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirectedUnweighted() {
		final long seed = 0x581f01ef9a0f8a50L;
		testRandGraphs(new MinimumVertexCutAllStEdgeCut(), false, false, seed);
	}

	@Test
	public void testRandGraphDirectedWeighted() {
		final long seed = 0xc9c1095bcfd2ba56L;
		testRandGraphs(new MinimumVertexCutAllStEdgeCut(), true, true, seed);
	}

	@Test
	public void testRandGraphUndirectedWeighted() {
		final long seed = 0xabe3d570c4698993L;
		testRandGraphs(new MinimumVertexCutAllStEdgeCut(), false, true, seed);
	}

	static void testRandGraphs(MinimumVertexCutAllSt algo, boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 20).repeat(40);
		tester.addPhase().withArgs(16, 32).repeat(10);
		// tester.addPhase().withArgs(64, 128).repeat(64);
		// tester.addPhase().withArgs(128, 256).repeat(32);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightFunction<Integer> w = null;
			if (weighted && rand.nextBoolean()) {
				WeightsInt<Integer> w0 = g.addVerticesWeights("weight", int.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextInt(551));
				w = w0;

			} else if (weighted) {
				WeightsDouble<Integer> w0 = g.addVerticesWeights("weight", double.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextDouble() * 5642);
				w = w0;
			}

			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			testMinCuts(g, w, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	private static <V, E> void testMinCuts(Graph<V, E> g, WeightFunction<V> w, V source, V sink,
			MinimumVertexCutAllSt alg) {
		List<Set<V>> minCuts = alg.allMinimumCuts(g, w, source, sink);
		if (g.containsEdge(source, sink)) {
			assertNull(minCuts);
			return;
		}
		if (minCuts.isEmpty()) {
			assertNull(Path.findPath(g, source, sink));
			return;
		}
		double minCutWeight = WeightFunction.weightSum(w, minCuts.get(0));
		for (Set<V> cut : minCuts)
			assertEquals(minCutWeight, WeightFunction.weightSum(w, cut), 1e-6);

		Set<Set<V>> actual = new HashSet<>(minCuts);
		assertEquals(minCuts.size(), actual.size(), "duplicate cuts: " + minCuts);

		if (g.vertices().size() <= 16) {
			Set<Set<V>> expected = new HashSet<>(computeExpectedMinCuts(g, w, source, sink));
			assertEquals(expected, actual);
		}
	}

	private static <V, E> List<Set<V>> computeExpectedMinCuts(Graph<V, E> g, WeightFunction<V> w, V source, V sink) {
		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();

		List<V> vertices = new ArrayList<>(g.vertices());
		vertices.remove(source);
		vertices.remove(sink);

		final WeightFunction<V> w0 = w;
		List<ObjectDoublePair<Set<V>>> cuts = SubSets.stream(vertices).map(vs0 -> {
			Set<V> vs = new ObjectOpenHashSet<>(vs0);
			vs.add(source);
			vs.add(sink);
			return vs;
		}).filter(vs -> Path.findPath(g.subGraphCopy(vs, null), source, sink) == null).map(remainingVertices -> {
			Set<V> cut = g.vertices().stream().filter(v -> !remainingVertices.contains(v)).collect(Collectors.toSet());
			return ObjectDoublePair.of(cut, WeightFunction.weightSum(w0, cut));
		}).collect(toList());

		if (cuts.isEmpty())
			return List.of();
		cuts.sort((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()));
		double minCutWeight = cuts.get(0).rightDouble();
		final double eps = 1e-4;
		return cuts
				.stream()
				.filter(p -> p.secondDouble() <= minCutWeight + eps)
				.map(ObjectDoublePair::first)
				.collect(Collectors.toList());
	}

	@Test
	public void testBuilderDefaultImpl() {
		MinimumVertexCutAllSt alg = MinimumVertexCutAllSt.newInstance();
		assertEquals(MinimumVertexCutAllStEdgeCut.class, alg.getClass());
	}

}
