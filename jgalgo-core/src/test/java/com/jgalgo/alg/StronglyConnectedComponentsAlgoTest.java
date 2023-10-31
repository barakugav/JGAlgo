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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

public class StronglyConnectedComponentsAlgoTest extends TestBase {

	@Test
	public void strongCCUGraph() {
		final long seed = 0xb3f19acd0e1041deL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			IVertexPartition actual =
					(IVertexPartition) new StronglyConnectedComponentsAlgoTarjan().findStronglyConnectedComponents(g);
			ConnectedComponentsTestUtils.validateConnectivityResult(g, actual);
			Pair<Integer, Int2IntMap> expected = ConnectedComponentsTestUtils.calcUndirectedConnectivity(g);
			ConnectedComponentsTestUtils.assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	@Test
	public void strongCCsDiGraph() {
		final long seed = 0xd21f8ca761bc1aaeL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			IVertexPartition actual =
					(IVertexPartition) new StronglyConnectedComponentsAlgoTarjan().findStronglyConnectedComponents(g);
			ConnectedComponentsTestUtils.validateConnectivityResult(g, actual);
			Pair<Integer, Int2IntMap> expected = ConnectedComponentsTestUtils.calcDirectedConnectivity(g);
			ConnectedComponentsTestUtils.assertConnectivityResultsEqual(g, expected, actual);
		});
	}

}
