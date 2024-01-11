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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MinimumEdgeCutSTTestUtils extends TestUtils {

	static void testRandGraphs(MinimumEdgeCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightsDouble<Integer> w = g.addEdgesWeights("weight", double.class);
			for (Integer e : g.edges())
				w.set(e, rand.nextDouble() * 5642);

			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			testMinCut(g, w, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	static void testRandGraphsInt(MinimumEdgeCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightsInt<Integer> w = g.addEdgesWeights("weight", int.class);
			for (Integer e : g.edges())
				w.set(e, rand.nextInt(16384));

			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			testMinCut(g, w, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	static void testRandGraphsMultiSourceMultiSink(MinimumEdgeCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightsDouble<Integer> w = g.addEdgesWeights("weight", double.class);
			for (Integer e : g.edges())
				w.set(e, rand.nextDouble() * 5642);

			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);
			testMinCut(g, w, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	static void testRandGraphsMultiSourceMultiSinkInt(MinimumEdgeCutST algo, long seed, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 1324).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightsInt<Integer> w = g.addEdgesWeights("weight", int.class);
			for (Integer e : g.edges())
				w.set(e, rand.nextInt(16384));

			Pair<Collection<Integer>, Collection<Integer>> sourcesSinks =
					MaximumFlowTestUtils.chooseMultiSourceMultiSink(g, rand);
			testMinCut(g, w, sourcesSinks.first(), sourcesSinks.second(), algo);
		});
	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<E> w, V source, V sink, MinimumEdgeCutST alg) {
		VertexBiPartition<V, E> minCut = alg.computeMinimumCut(g, w, source, sink);
		double minCutWeight = w.weightSum(minCut.crossEdges());

		final int n = g.vertices().size();
		if (n == 2) {
			assertEquals(Set.of(source), minCut.leftVertices());
			return;
		}

		if (n <= 16) {
			/* check all cuts */
			List<V> vertices = new ArrayList<>(g.vertices());
			vertices.remove(source);
			vertices.remove(sink);

			Set<V> cut = new ObjectOpenHashSet<>(n);
			for (int bitmap = 0; bitmap < 1 << (n - 2); bitmap++) {
				cut.add(source);
				for (int i : range(n - 2))
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.get(i));
				double cutWeight = 0;
				for (V u : cut) {
					for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
						E e = eit.next();
						V v = eit.target();
						if (!cut.contains(v))
							cutWeight += w.weight(e);
					}
				}
				final double eps = 1e-4;
				assertTrue(minCutWeight <= cutWeight + eps, "failed to find minimum cut: " + cut);
				cut.clear();
			}

		} else {
			MinimumEdgeCutST validationAlgo = alg instanceof MaximumFlowPushRelabel ? new MaximumFlowEdmondsKarp()
					: MaximumFlowPushRelabel.newInstanceHighestFirst();
			VertexBiPartition<V, E> minCutExpected = validationAlgo.computeMinimumCut(g, w, source, sink);
			double minCutWeightExpected = w.weightSum(minCutExpected.crossEdges());

			assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<E> w, Collection<V> sources,
			Collection<V> sinks, MinimumEdgeCutST alg) {
		VertexBiPartition<V, E> minCut = alg.computeMinimumCut(g, w, sources, sinks);
		double minCutWeight = w.weightSum(minCut.crossEdges());

		final int terminalsNum = sources.size() + sinks.size();
		final int n = g.vertices().size();
		if (n == terminalsNum) {
			assertEquals(sources, minCut.leftEdges());
			return;
		}

		if (n <= 16) {
			/* check all cuts */
			List<V> vertices = new ArrayList<>(g.vertices());
			vertices.removeAll(sources);
			vertices.removeAll(sinks);

			Set<V> cut = new ObjectOpenHashSet<>(n);
			for (int bitmap = 0; bitmap < 1 << (n - terminalsNum); bitmap++) {
				cut.addAll(sources);
				for (int i : range(n - terminalsNum))
					if ((bitmap & (1 << i)) != 0)
						cut.add(vertices.get(i));
				double cutWeight = 0;
				for (V u : cut) {
					for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
						E e = eit.next();
						V v = eit.target();
						if (!cut.contains(v))
							cutWeight += w.weight(e);
					}
				}
				final double eps = 1e-4;
				assertTrue(minCutWeight <= cutWeight + eps, "failed to find minimum cut: " + cut);
				cut.clear();
			}

		} else {
			MinimumEdgeCutST validationAlgo = alg instanceof MaximumFlowPushRelabel ? new MaximumFlowEdmondsKarp()
					: MaximumFlowPushRelabel.newInstanceHighestFirst();
			VertexBiPartition<V, E> minCutExpected = validationAlgo.computeMinimumCut(g, w, sources, sinks);
			double minCutWeightExpected = w.weightSum(minCutExpected.crossEdges());

			assertEquals(minCutWeightExpected, minCutWeight, 0.001, "failed to find minimum cut");
		}
	}

}
