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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ConnectedComponentsAlgoTest extends TestBase {

	@Test
	public void strongCCUGraph() {
		final long seed = 0xb3f19acd0e1041deL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			VertexPartition actual = ConnectedComponentsAlgo.newInstance().findConnectedComponents(g);
			validateConnectivityResult(g, actual);
			Pair<Integer, Int2IntMap> expected = calcUndirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Pair<Integer, Int2IntMap> calcUndirectedConnectivity(Graph g) {
		int n = g.vertices().size();
		int ccNum = 0;
		Int2IntMap vertexToCC = new Int2IntOpenHashMap(n);
		vertexToCC.defaultReturnValue(-1);

		for (int start : g.vertices()) {
			if (vertexToCC.get(start) != -1)
				continue;
			int ccIdx = ccNum++;
			for (BfsIter it = BfsIter.newInstance(g, start); it.hasNext();)
				vertexToCC.put(it.nextInt(), ccIdx);
		}
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
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
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			VertexPartition actual = ConnectedComponentsAlgo.newInstance().findConnectedComponents(g);
			validateConnectivityResult(g, actual);
			Pair<Integer, Int2IntMap> expected = calcDirectedConnectivity(g);
			assertConnectivityResultsEqual(g, expected, actual);
		});
	}

	private static Pair<Integer, Int2IntMap> calcDirectedConnectivity(Graph g) {
		int n = g.vertices().size();
		Int2ObjectMap<IntSet> reach = new Int2ObjectOpenHashMap<>();
		for (int start : g.vertices()) {
			IntSet vReach = new IntOpenHashSet();
			for (BfsIter it = BfsIter.newInstance(g, start); it.hasNext();)
				vReach.add(it.nextInt());
			reach.put(start, vReach);
		}

		int ccNum = 0;
		Int2IntMap vertexToCC = new Int2IntOpenHashMap(n);
		vertexToCC.defaultReturnValue(-1);

		for (int u : g.vertices()) {
			if (vertexToCC.get(u) != -1)
				continue;
			int ccIdx = ccNum++;
			vertexToCC.put(u, ccIdx);
			for (int v : reach.get(u))
				if (reach.get(v).contains(u))
					vertexToCC.put(v, ccIdx);
		}
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
	}

	@Test
	public void weakCCsDiGraph() {
		final long seed = 0x715a81d58dcf65deL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			VertexPartition actual = ConnectedComponentsAlgo.newInstance().findWeaklyConnectedComponents(g);

			/* create a undirected copy of the original directed graph */
			GraphBuilder gb = GraphBuilder.newUndirected();
			for (int u : g.vertices())
				gb.addVertex(u);
			for (int e : g.edges())
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
			VertexPartition expected = ConnectedComponentsAlgo.newInstance().findConnectedComponents(gb.build());
			Int2IntMap expectedMap = new Int2IntOpenHashMap(n);
			for (int v : g.vertices())
				expectedMap.put(v, expected.vertexBlock(v));
			Pair<Integer, Int2IntMap> expectedPair = IntObjectPair.of(expected.numberOfBlocks(), expectedMap);
			assertConnectivityResultsEqual(g, expectedPair, actual);
		});
	}

	private static void assertConnectivityResultsEqual(Graph g, Pair<Integer, Int2IntMap> r1, VertexPartition r2) {
		assertEquals(r1.first(), r2.numberOfBlocks());
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r2.numberOfBlocks());
		for (int u : g.vertices()) {
			int cc1 = r1.second().get(u);
			int cc2 = r2.vertexBlock(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	private static void validateConnectivityResult(Graph g, VertexPartition res) {
		BitSet ccs = new BitSet();
		for (int v : g.vertices())
			ccs.set(res.vertexBlock(v));
		assertEquals(ccs.cardinality(), res.numberOfBlocks());
		for (int cc = 0; cc < res.numberOfBlocks(); cc++)
			assertTrue(ccs.get(cc));
	}

}
