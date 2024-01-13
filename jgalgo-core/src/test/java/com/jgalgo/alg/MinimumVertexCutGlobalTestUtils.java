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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumVertexCutGlobalTestUtils extends TestUtils {

	static void testRandGraphs(MinimumVertexCutGlobal algo, boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 20).repeat(40);
		tester.addPhase().withArgs(16, 32).repeat(10);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
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

			testMinCut(g, w, algo);
		});
	}

	static void testClique(MinimumVertexCutGlobal algo, boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8);
		tester.addPhase().withArgs(16);
		tester.addPhase().withArgs(64);
		tester.addPhase().withArgs(128);
		tester.run((n) -> {
			Graph<Integer, Integer> g = new CompleteGraphGenerator<>(
					rand.nextBoolean() ? GraphFactory.undirected() : IntGraphFactory.undirected())
							.vertices(range(n))
							.edges(IdBuilderInt.defaultBuilder())
							.generateMutable();

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

			testMinCut(g, w, algo);
		});
	}

	static void testEmptyGraph(MinimumVertexCutGlobal algo) {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = directed ? Graph.newDirected() : Graph.newUndirected();
			WeightFunction<Integer> w = null;
			assertThrows(IllegalArgumentException.class, () -> algo.computeMinimumCut(g, w));
		});
	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<V> w, MinimumVertexCutGlobal alg) {
		Set<V> minCut = alg.computeMinimumCut(g, w);
		Set<V> minCutSetCopy = new ObjectOpenHashSet<>(minCut);
		assertTrue(g.vertices().stream().filter(v -> minCutSetCopy.contains(v)).allMatch(v -> minCut.contains(v)));
		assertTrue(g.vertices().stream().filter(v -> !minCutSetCopy.contains(v)).allMatch(v -> !minCut.contains(v)));

		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();
		double minCutWeight = w.weightSum(minCut);
		WeaklyConnectedComponentsAlgo wcc = WeaklyConnectedComponentsAlgo.newInstance();

		final int n = g.vertices().size();
		if (n <= 16) { /* check all cuts */
			Stream<Set<V>> allCuts = SubSets
					.stream(g.vertices())
					// trivial cut
					.filter(remainingVertices -> !IntList.of(0, n).contains(remainingVertices.size()))
					// not a cut
					.filter(remainingVertices -> !wcc.isWeaklyConnected(g.subGraphCopy(remainingVertices, null)))
					.map(ObjectOpenHashSet::new);

			final WeightFunction<V> w0 = w;
			ToDoubleFunction<Set<V>> cutWeight = remainingVertices -> g
					.vertices()
					.stream()
					.filter(v -> !remainingVertices.contains(v))
					.mapToDouble(w0::weight)
					.sum();
			Set<V> bestCut = allCuts
					.min((c1, c2) -> Double.compare(cutWeight.applyAsDouble(c1), cutWeight.applyAsDouble(c2)))
					.get();

			double bestCutWeight = cutWeight.applyAsDouble(bestCut);
			final double eps = 1e-4;
			assertTrue(minCutWeight <= bestCutWeight + eps);

			// } else {
			// MinimumEdgeCutST validationAlgo = alg instanceof MaximumFlowPushRelabel ? new MaximumFlowEdmondsKarp()
			// : MaximumFlowPushRelabel.newInstanceHighestFirst();
			// VertexBiPartition<V, E> minCutExpected = validationAlgo.computeMinimumCut(g, w, source, sink);
			// double minCutWeightExpected = w.weightSum(minCutExpected.crossEdges());

			// assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

}
