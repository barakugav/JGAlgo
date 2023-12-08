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

import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSingleSourceDagTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0xbaa64a2aa57cb602L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSource ssspAlgo = new ShortestPathSingleSourceDag();
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(512, 1024).repeat(16);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randDag(n, m, seedGen.nextSeed());
			WeightFunction<Integer> w =
					GraphsTestUtils.assignRandWeightsMaybeInt(g, 0, 2 + 2 * g.edges().size(), seedGen.nextSeed());
			Integer source = g.vertices().iterator().next();

			ShortestPathSingleSourceTestUtils.testAlgo(g, w, source, ssspAlgo, new ShortestPathSingleSourceDijkstra());
		});
	}

	@Test
	public void randGraphsCardinality() {
		final long seed = 0xcc9a05cd6148c76bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSource ssspAlgo = new ShortestPathSingleSourceDag();
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(512, 1024).repeat(16);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randDag(n, m, seedGen.nextSeed());
			Integer source = g.vertices().iterator().next();

			ShortestPathSingleSourceTestUtils.testAlgo(g, null, source, ssspAlgo,
					new ShortestPathSingleSourceDijkstra());
		});
	}

}
