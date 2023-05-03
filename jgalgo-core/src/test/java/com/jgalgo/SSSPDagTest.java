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

import org.junit.jupiter.api.Test;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

public class SSSPDagTest extends TestBase {

	@Test
	public void testDistancesDAGUnconnected() {
		final long seed = 0xbaa64a2aa57cb602L;
		distancesDAG(false, seed);
	}

	@Test
	public void testDistancesDAGConnected() {
		final long seed = 0x21ee13eb1bee6e46L;
		distancesDAG(true, seed);
	}

	private static void distancesDAG(boolean connected, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		SSSP ssspAlgo = new SSSPDag();
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(16, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(false).cycles(false).connected(connected).build();
			EdgeWeightFunc w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = 0;

			SSSPTestUtils.testAlgo(g, w, source, ssspAlgo, new SSSPDijkstra());
		});
	}

}
