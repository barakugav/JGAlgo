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
import java.util.List;
import java.util.Random;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class MinimumCutSTTestUtils extends TestUtils {

	static void testRandGraphsInt(MinimumCutST algo, long seed, boolean directed) {
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
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testMinCutInt(g, w, source, sink, algo);
		});
	}

	private static void testMinCutInt(Graph g, WeightFunction.Int w, int source, int sink, MinimumCutST alg) {
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

			IntSet cut = new IntOpenHashSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - 2); bitmap++) {
				cut.add(source);
				for (int i = 0; i < n - 2; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.getInt(i));
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

	static void testRandGraphs(MinimumCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			Weights.Double w = g.addEdgesWeights("weight", double.class);
			for (int e : g.edges())
				w.set(e, rand.nextDouble() * 5642);

			int source, sink;
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			testMinCut(g, w, source, sink, algo);
		});
	}

	private static void testMinCut(Graph g, WeightFunction w, int source, int sink, MinimumCutST alg) {
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

			IntSet cut = new IntOpenHashSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - 2); bitmap++) {
				cut.add(source);
				for (int i = 0; i < n - 2; i++)
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.getInt(i));
				double cutWeight = 0;
				for (int u : cut) {
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						if (!cut.contains(v))
							cutWeight += w.weight(e);
					}
				}
				final double eps = 0.0001;
				assertTrue(minCutWeight <= cutWeight + eps, "failed to find minimum cut: " + cut);
				cut.clear();
			}

		} else {
			MinimumCutST validationAlgo = alg instanceof MaximumFlowPushRelabelAbstract
					? MinimumCutST.newFromMaximumFlow(new MaximumFlowEdmondsKarp())
					: new MaximumFlowPushRelabelHighestFirst();
			Cut minCutExpected = validationAlgo.computeMinimumCut(g, w, source, sink);
			int minCutWeightExpected = (int) minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

}
