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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumEdgeCutGlobalStoerWagnerTest extends TestBase {

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x34199fd52891f95aL;
		testRandGraphs(new MinimumEdgeCutGlobalStoerWagner(), seed, /* directed= */ false);
	}

	static void testRandGraphs(MinimumEdgeCutGlobal algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(16);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 64).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(200, 800).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());

			WeightFunction<Integer> w = null;
			if (rand.nextBoolean()) {
				WeightsInt<Integer> w0 = g.addEdgesWeights("weight", int.class);
				for (Integer e : g.edges())
					w0.set(e, rand.nextInt(16384));
				w = w0;

			} else if (rand.nextBoolean()) {
				WeightsDouble<Integer> w0 = g.addEdgesWeights("weight", double.class);
				for (Integer e : g.edges())
					w0.set(e, rand.nextInt(16384));
				w = w0;
			}

			testMinCut(g, w, algo);
		});

	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<E> w, MinimumEdgeCutGlobal alg) {
		VertexBiPartition<V, E> minCut = alg.computeMinimumCut(g, w);
		double minCutWeight = WeightFunction.weightSum(w, minCut.crossEdges());
		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();

		final int n = g.vertices().size();

		if (n <= 16) {
			/* check all cuts */
			List<V> vertices = new ArrayList<>(g.vertices());
			final V firstVertex = g.vertices().iterator().next();
			vertices.remove(firstVertex);

			if (w == null)
				w = WeightFunction.cardinalityWeightFunction();

			Set<V> cut = new ObjectOpenHashSet<>(n);
			for (int bitmap = 0; bitmap < 1 << (n - 1); bitmap++) {
				cut.add(firstVertex);
				for (int i = 0; i < n - 1; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.get(i));
				if (cut.size() != n) {
					double cutWeight = 0;
					for (V u : cut) {
						for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
							E e = eit.next();
							V v = eit.target();
							if (!cut.contains(v))
								cutWeight += w.weight(e);
						}
					}
					assertTrue(minCutWeight <= cutWeight, "failed to find minimum cut: " + cut);
				}
				cut.clear();
			}

		} else {
			MinimumEdgeCutGlobal validationAlgo =
					MinimumEdgeCutUtils.globalMinCutFromStMinCut(new MaximumFlowEdmondsKarp());
			VertexBiPartition<V, E> minCutExpected = validationAlgo.computeMinimumCut(g, w);
			double minCutWeightExpected = w.weightSum(minCutExpected.crossEdges());

			assertEquals(minCutWeightExpected, minCutWeight, "failed to find minimum cut");
		}
	}

}
