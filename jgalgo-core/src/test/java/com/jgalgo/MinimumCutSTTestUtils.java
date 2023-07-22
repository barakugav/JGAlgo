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
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class MinimumCutSTTestUtils extends TestUtils {

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

			IntIntPair sourceSink = chooseSourceSink(g, rand);
			testMinCut(g, w, sourceSink.firstInt(), sourceSink.secondInt(), algo);
		});
	}

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

			IntIntPair sourceSink = chooseSourceSink(g, rand);
			testMinCut(g, w, sourceSink.firstInt(), sourceSink.secondInt(), algo);
		});
	}

	static IntIntPair chooseSourceSink(Graph g, Random rand) {
		int source, sink;
		for (int[] vs = g.vertices().toIntArray();;) {
			source = vs[rand.nextInt(vs.length)];
			sink = vs[rand.nextInt(vs.length)];
			if (source != sink && Path.findPath(g, source, sink) != null)
				return IntIntPair.of(source, sink);
		}
	}

	static void testRandGraphsMultiSourceMultiSink(MinimumCutST algo, long seed, boolean directed) {
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

			Pair<IntCollection, IntCollection> sourcesSinks = chooseMultiSourceMultiSink(g, rand);
			testMinCut(g, w, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testRandGraphsMultiSourceMultiSinkInt(MinimumCutST algo, long seed, boolean directed) {
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

			Pair<IntCollection, IntCollection> sourcesSinks = chooseMultiSourceMultiSink(g, rand);
			testMinCut(g, w, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	private static Pair<IntCollection, IntCollection> chooseMultiSourceMultiSink(Graph g, Random rand) {
		final int n = g.vertices().size();
		final int sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
		final int sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
		IntCollection sources = new IntOpenHashSet(sourcesNum);
		IntCollection sinks = new IntOpenHashSet(sinksNum);
		for (int[] vs = g.vertices().toIntArray();;) {
			if (sources.size() < sourcesNum) {
				int source = vs[rand.nextInt(vs.length)];
				if (!sinks.contains(source))
					sources.add(source);

			} else if (sinks.size() < sinksNum) {
				int sink = vs[rand.nextInt(vs.length)];
				if (!sources.contains(sink))
					sinks.add(sink);
			} else {
				break;
			}
		}
		return Pair.of(sources, sinks);
	}

	private static void testMinCut(Graph g, WeightFunction w, int source, int sink, MinimumCutST alg) {
		Cut minCut = alg.computeMinimumCut(g, w, source, sink);
		double minCutWeight = minCut.weight(w);

		final int n = g.vertices().size();
		if (n == 2) {
			assertEquals(IntList.of(source), minCut.vertices());
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
			double minCutWeightExpected = minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

	private static void testMinCut(Graph g, WeightFunction w, IntCollection sources, IntCollection sinks,
			MinimumCutST alg) {
		Cut minCut = alg.computeMinimumCut(g, w, sources, sinks);
		double minCutWeight = minCut.weight(w);

		final int terminalsNum = sources.size() + sinks.size();
		final int n = g.vertices().size();
		if (n == terminalsNum) {
			assertEquals(sources, minCut.vertices());
			return;
		}

		if (n <= 16) {
			/* check all cuts */
			IntList vertices = new IntArrayList(g.vertices());
			vertices.removeAll(sources);
			vertices.removeAll(sinks);

			IntSet cut = new IntOpenHashSet(n);
			for (int bitmap = 0; bitmap < 1 << (n - terminalsNum); bitmap++) {
				cut.addAll(sources);
				for (int i = 0; i < n - terminalsNum; i++)
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
			Cut minCutExpected = validationAlgo.computeMinimumCut(g, w, sources, sinks);
			double minCutWeightExpected = minCutExpected.weight(w);

			assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

}
