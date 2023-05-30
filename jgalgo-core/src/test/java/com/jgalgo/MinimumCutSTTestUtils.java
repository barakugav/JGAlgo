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
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class MinimumCutSTTestUtils extends TestUtils {

	static void testRandGraphs(MinimumCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			Weights.Int w = g.addEdgesWeights("weight", int.class);
			for (int e : g.edges())
				w.set(e, rand.nextInt(16384));

			int source, sink;
			for (;;) {
				source = rand.nextInt(g.vertices().size());
				sink = rand.nextInt(g.vertices().size());
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testMinCut(g, w, source, sink, algo);
		});
	}

	private static void testMinCut(Graph g, WeightFunction.Int w, int source, int sink, MinimumCutST alg) {
		Cut minCut = alg.computeMinimumCut(g, w, source, sink);
		int minCutWeight = (int) minCut.weight(w);

		final int n = g.vertices().size();
		if (n == 2) {
			assertEquals(minCut, IntList.of(source));
			return;
		}

		if (n <= 16) {
			/* check all cuts */
			IntList vertices = new IntArrayList(g.vertices());
			vertices.rem(source);
			vertices.rem(sink);

			BitSet cut = new BitSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - 2); bitmap++) {
				cut.set(source);
				for (int i = 0; i < n - 2; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.set(vertices.getInt(i));
				int cutWeight = (int) new CutImpl(g, cut).weight(w);
				assertTrue(minCutWeight <= cutWeight, "failed to find minimum cut: " + cut);
				cut.clear();
			}

		} else {
			MinimumCutST validationAlgo = alg instanceof MaximumFlowPushRelabelAbstract
					? MinimumCutST.newFromMaximumFlow(new MaximumFlowEdmondsKarp())
					: new MaximumFlowPushRelabelHighestFirst();
			Cut minCutExpected = validationAlgo.computeMinimumCut(g, w, source, sink);
			int minCutWeightExpected = (int) minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, "failed to find minimum cut");
		}
	}

}
