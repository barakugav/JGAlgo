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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.function.ToDoubleFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestUtils.PhasedTester;
import com.jgalgo.internal.util.TestUtils.SeedGenerator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class EdgeCoverTestUtils {

	static void testRandGraphsCardinality(EdgeCover algo, long seed) {
		testRandGraphs(algo, seed, false);
	}

	static void testRandGraphsWeighted(EdgeCover algo, long seed) {
		testRandGraphs(algo, seed, true);
	}

	private static void testRandGraphs(EdgeCover algo, long seed, boolean weighted) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 6).repeat(64);
		tester.addPhase().withArgs(8, 12).repeat(64);
		tester.addPhase().withArgs(8, 16).repeat(32);
		tester.addPhase().withArgs(64, 256).repeat(16);
		tester.addPhase().withArgs(1024, 2048).repeat(2);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();

			RandomIntUnique rand = new RandomIntUnique(0, m * 16, seedGen.nextSeed());
			IWeightsInt weights;
			if (weighted) {
				weights = g.addEdgesWeights("weight", int.class);
				for (int e : g.edges())
					weights.set(e, rand.next());
			} else {
				weights = null;
			}

			EdgeCoverTestUtils.testEdgeCover(g, weights, algo);
		});
	}

	static void testEdgeCover(IntGraph g, IWeightFunctionInt w, EdgeCover algo) {
		EdgeCover.Result ec = algo.computeMinimumEdgeCover(g, w);

		for (int v : g.vertices()) {
			boolean isCovered = g.outEdges(v).intStream().anyMatch(ec::isInCover);
			if (g.isDirected())
				isCovered |= g.inEdges(v).intStream().anyMatch(ec::isInCover);
			assertTrue(isCovered, "vertex is not covered: " + v);
		}

		assertTrue(EdgeCover.isCover(g, ec.edges()));

		final int m = g.edges().size();
		if (m <= 16) {

			/* check all covers */
			IntSet bestCover = null;
			IntList edges = new IntArrayList(g.edges());
			IntSet cover = new IntOpenHashSet(m);
			ToDoubleFunction<IntSet> coverWeight = c -> IWeightFunction.weightSum(w, c);
			coverLoop: for (int bitmap = 0; bitmap < 1 << m; bitmap++) {
				cover.clear();
				assert cover.isEmpty();
				for (int i = 0; i < m; i++)
					if ((bitmap & (1 << i)) != 0)
						cover.add(edges.getInt(i));
				for (int v : g.vertices())
					if (!g.outEdges(v).intStream().anyMatch(cover::contains)
							&& (!g.isDirected() || !g.inEdges(v).intStream().anyMatch(cover::contains)))
						continue coverLoop; /* don't cover all vertices */
				if (bestCover == null || coverWeight.applyAsDouble(bestCover) > coverWeight.applyAsDouble(cover))
					bestCover = new IntOpenHashSet(cover);
			}

			assertNotNull(bestCover);
			assertEquals(coverWeight.applyAsDouble(bestCover), IWeightFunction.weightSum(w, ec.edges()));
		}
	}

}
