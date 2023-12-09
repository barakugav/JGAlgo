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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumVertexCutSTTestUtils extends TestUtils {

	static void testRandGraphs(MinimumVertexCutST algo, boolean directed, boolean weighted, long seed) {
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

			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			testMinCut(g, w, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<V> w, V source, V sink,
			MinimumVertexCutST alg) {
		Set<V> minCut = alg.computeMinimumCut(g, w, source, sink);
		if (g.getEdge(source, sink) != null) {
			assertNull(minCut);
			return;
		}
		Set<V> minCutSetCopy = new ObjectOpenHashSet<>(minCut);
		assertTrue(g.vertices().stream().filter(v -> minCutSetCopy.contains(v)).allMatch(v -> minCut.contains(v)));
		assertTrue(g.vertices().stream().filter(v -> !minCutSetCopy.contains(v)).allMatch(v -> !minCut.contains(v)));

		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();
		double minCutWeight = w.weightSum(minCut);

		final int n = g.vertices().size();
		if (n <= 16) {
			/* check all cuts */
			List<V> vertices = new ArrayList<>(g.vertices());
			vertices.remove(source);
			vertices.remove(sink);

			Set<V> remainingVertices = new ObjectOpenHashSet<>(n);
			for (int bitmap = 0; bitmap < 1 << (n - 2); bitmap++) {
				remainingVertices.clear();
				remainingVertices.add(source);
				remainingVertices.add(sink);
				for (int i = 0; i < n - 2; i++)
					if ((bitmap & (1 << i)) != 0)
						remainingVertices.add(vertices.get(i));
				if (Path.findPath(g.subGraphCopy(remainingVertices, null), source, sink) != null)
					continue; // not a cut
				double cutWeight =
						g.vertices().stream().filter(v -> !remainingVertices.contains(v)).mapToDouble(w::weight).sum();
				final double eps = 1e-4;
				assertTrue(minCutWeight <= cutWeight + eps, "failed to find minimum cut: " + remainingVertices);
			}

			// } else {
			// MinimumEdgeCutST validationAlgo = alg instanceof MaximumFlowPushRelabel ? new MaximumFlowEdmondsKarp()
			// : MaximumFlowPushRelabel.newInstanceHighestFirst();
			// VertexBiPartition<V, E> minCutExpected = validationAlgo.computeMinimumCut(g, w, source, sink);
			// double minCutWeightExpected = w.weightSum(minCutExpected.crossEdges());

			// assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

}
