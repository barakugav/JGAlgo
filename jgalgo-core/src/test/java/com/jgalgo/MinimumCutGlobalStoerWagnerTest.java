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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

class MinimumCutGlobalStoerWagnerTest extends TestBase {

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x34199fd52891f95aL;
		testRandGraphs(new MinimumCutGlobalStoerWagner(), seed, /* directed= */ false);
	}

	static void testRandGraphs(MinimumCutGlobal algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 6, 6), phase(16, 16, 16), phase(16, 16, 32), phase(16, 64, 64),
				phase(16, 64, 128), phase(2, 200, 800));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			Weights.Int w = g.addEdgesWeights("weight", int.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int cap = rand.nextInt(16384);
				w.set(e, cap);
			}

			testMinCut(g, w, algo);
		});
	}

	private static void testMinCut(Graph g, EdgeWeightFunc.Int w, MinimumCutGlobal alg) {
		Cut minCut = alg.computeMinimumCut(g, w);
		int minCutWeight = (int) minCut.weight(w);

		final int n = g.vertices().size();

		if (n <= 16) {
			/* check all cuts */
			IntList vertices = new IntArrayList(g.vertices());
			vertices.rem(0);

			BitSet cut = new BitSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - 1); bitmap++) {
				cut.set(0);
				for (int i = 0; i < n - 1; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.set(vertices.getInt(i));
				if (cut.cardinality() != n) {
					int cutWeight = (int) new CutImpl(g, cut).weight(w);
					assertTrue(minCutWeight <= cutWeight, "failed to find minimum cut: " + cut);
				}
				cut.clear();
			}

		} else {
			MinimumCutGlobal validationAlgo = MinimumCutSTBuilderImpl
					.globalMinCutFromStMinCut(MinimumCutST.newFromMaximumFlow(new MaximumFlowEdmondsKarp()));
			Cut minCutExpected = validationAlgo.computeMinimumCut(g, w);
			int minCutWeightExpected = (int) minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, "failed to find minimum cut");
		}
	}

}
