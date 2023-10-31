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
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;

public class WeaklyConnectedComponentsAlgoTest extends TestBase {

	@Test
	public void weakCCsDiGraph() {
		final long seed = 0x715a81d58dcf65deL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			IVertexPartition actual =
					(IVertexPartition) WeaklyConnectedComponentsAlgo.newInstance().findWeaklyConnectedComponents(g);

			/* create a undirected copy of the original directed graph */
			IntGraphBuilder gb = IntGraphBuilder.newUndirected();
			for (int u : g.vertices())
				gb.addVertex(u);
			for (int e : g.edges())
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
			IVertexPartition expected = (IVertexPartition) WeaklyConnectedComponentsAlgo.newInstance()
					.findWeaklyConnectedComponents(gb.build());
			Int2IntMap expectedMap = new Int2IntOpenHashMap(n);
			for (int v : g.vertices())
				expectedMap.put(v, expected.vertexBlock(v));
			Pair<Integer, Int2IntMap> expectedPair = IntObjectPair.of(expected.numberOfBlocks(), expectedMap);
			ConnectedComponentsTestUtils.assertConnectivityResultsEqual(g, expectedPair, actual);
		});
	}

}
