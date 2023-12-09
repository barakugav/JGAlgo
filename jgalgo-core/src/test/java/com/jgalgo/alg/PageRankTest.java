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
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;

class PageRankTest extends TestBase {

	@Test
	public void testRandGraphsUndirectedWeighted() {
		final long seed = 0xe573594a68dac687L;
		testRandGraphs(false, true, seed);
	}

	@Test
	public void testRandGraphsUndirectedUnweighted() {
		final long seed = 0xc665f15fa8309ef5L;
		testRandGraphs(false, false, seed);
	}

	@Test
	public void testRandGraphsDirectedWeighted() {
		final long seed = 0xa93f3d07843ad7aaL;
		testRandGraphs(true, true, seed);
	}

	@Test
	public void testRandGraphsDirectedUnweighted() {
		final long seed = 0x532700508b908e89L;
		testRandGraphs(true, false, seed);
	}

	private static void testRandGraphs(boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightFunctionInt<Integer> w = null;
			if (weighted)
				w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testPageRank(g, w);
		});
	}

	private static <V, E> void testPageRank(Graph<V, E> g, WeightFunction<E> w) {
		VertexScoring<V, E> ranks = new PageRank().computeScores(g, w);
		double sum = 0;
		for (V v : g.vertices())
			sum += ranks.vertexScore(v);
		assertEquals(1.0, sum, 0.001);
	}

}
