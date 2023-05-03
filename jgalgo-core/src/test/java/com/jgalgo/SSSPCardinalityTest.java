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

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

public class SSSPCardinalityTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x19a192dc9f21c8d7L;
		testRandGraph(true, seed);
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x492144d38445cac7L;
		testRandGraph(false, seed);
	}

	private static void testRandGraph(boolean directed, long seed) {
		EdgeWeightFunc w = e -> 1;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases =
				List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096), phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			int source = rand.nextInt(g.vertices().size());

			SSSPCardinality algo = new SSSPCardinality();
			SSSP.Result actualRes = algo.computeShortestPaths(g, source);

			SSSP validationAlgo = new SSSPDijkstra();
			SSSPTestUtils.validateResult(g, w, source, actualRes, validationAlgo);
		});
	}

}
