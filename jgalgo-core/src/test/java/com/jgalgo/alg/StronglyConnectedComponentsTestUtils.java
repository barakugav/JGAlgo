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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

class StronglyConnectedComponentsTestUtils extends TestBase {

	static void strongCcUndirected(StronglyConnectedComponentsAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			VertexPartition<Integer, Integer> actual = algo.findStronglyConnectedComponents(g);
			ConnectedComponentsTestUtils.validateConnectivityResult(g, actual);
			IntObjectPair<Object2IntMap<Integer>> expected = ConnectedComponentsTestUtils.calcUndirectedConnectivity(g);
			ConnectedComponentsTestUtils.assertConnectivityResultsEqual(g, expected, actual);

			assertEqualsBool(actual.numberOfBlocks() <= 1, algo.isStronglyConnected(g));
		});
	}

	static void strongCcDirected(StronglyConnectedComponentsAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			g = maybeIndexGraph(g, rand);

			VertexPartition<Integer, Integer> actual = algo.findStronglyConnectedComponents(g);
			ConnectedComponentsTestUtils.validateConnectivityResult(g, actual);
			IntObjectPair<Object2IntMap<Integer>> expected = ConnectedComponentsTestUtils.calcDirectedConnectivity(g);
			ConnectedComponentsTestUtils.assertConnectivityResultsEqual(g, expected, actual);

			assertEqualsBool(actual.numberOfBlocks() <= 1, algo.isStronglyConnected(g));
		});
	}

	@Test
	public void testNewInstance() {
		assertNotNull(StronglyConnectedComponentsAlgo.newInstance());
	}

	@Test
	public void testSetOption() {
		StronglyConnectedComponentsAlgo.Builder builder = StronglyConnectedComponentsAlgo.newBuilder();
		assertNotNull(builder.build());

		assertThrows(IllegalArgumentException.class, () -> builder.setOption("jdasg", "lhfj"));

		assertNotNull(builder.setOption("impl", "path-based").build());
		assertNotNull(builder.setOption("impl", "tarjan").build());
		assertThrows(IllegalArgumentException.class, () -> builder.setOption("impl", "dmksm").build());
	}

}
