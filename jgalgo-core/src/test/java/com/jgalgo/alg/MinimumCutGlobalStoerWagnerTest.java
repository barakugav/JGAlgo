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
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class MinimumCutGlobalStoerWagnerTest extends TestBase {

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x34199fd52891f95aL;
		testRandGraphs(new MinimumCutGlobalStoerWagner(), seed, /* directed= */ false);
	}

	static void testRandGraphs(MinimumCutGlobal algo, long seed, boolean directed) {
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
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();

			Weights.Int w = g.addEdgesWeights("weight", int.class);
			for (int e : g.edges())
				w.set(e, rand.nextInt(16384));

			testMinCut(g, w, algo);
		});

	}

	private static void testMinCut(Graph g, WeightFunction.Int w, MinimumCutGlobal alg) {
		Cut minCut = alg.computeMinimumCut(g, w);
		int minCutWeight = (int) minCut.weight(w);

		final int n = g.vertices().size();

		if (n <= 16) {
			/* check all cuts */
			IntList vertices = new IntArrayList(g.vertices());
			final int firstVertex = g.vertices().iterator().nextInt();
			vertices.rem(firstVertex);

			IntSet cut = new IntOpenHashSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - 1); bitmap++) {
				cut.add(firstVertex);
				for (int i = 0; i < n - 1; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.getInt(i));
				if (cut.size() != n) {
					int cutWeight = 0;
					for (int u : cut) {
						for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int v = eit.target();
							if (!cut.contains(v))
								cutWeight += w.weightInt(e);
						}
					}
					assertTrue(minCutWeight <= cutWeight, "failed to find minimum cut: " + cut);
				}
				cut.clear();
			}

		} else {
			MinimumCutGlobal validationAlgo = MinimumCutSTUtils.globalMinCutFromStMinCut(new MaximumFlowEdmondsKarp());
			Cut minCutExpected = validationAlgo.computeMinimumCut(g, w);
			int minCutWeightExpected = (int) minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, "failed to find minimum cut");
		}
	}

}
