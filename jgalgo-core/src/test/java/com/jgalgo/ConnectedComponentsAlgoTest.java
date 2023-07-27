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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import java.util.List;
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
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			ConnectedComponentsAlgo.Result actual =
					ConnectedComponentsAlgo.newBuilder().build().computeConnectivityComponents(g);
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
			for (BFSIter it = BFSIter.newInstance(g, start); it.hasNext();)
				vertexToCC.put(it.nextInt(), ccIdx);
		}
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
	}

	@Test
	public void strongCCsDiGraph() {
		final long seed = 0xd21f8ca761bc1aaeL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			ConnectedComponentsAlgo.Result actual =
					ConnectedComponentsAlgo.newBuilder().build().computeConnectivityComponents(g);
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
			for (BFSIter it = BFSIter.newInstance(g, start); it.hasNext();)
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
		final long seed = 0;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			ConnectedComponentsAlgo.Result actual =
					ConnectedComponentsAlgo.newBuilder().build().computeWeaklyConnectivityComponents(g);

			/* create a undirected copy of the original directed graph */
			GraphBuilder gb = GraphBuilder.newUndirected();
			for (int u : g.vertices())
				gb.addVertex(u);
			for (int e : g.edges())
				gb.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
			ConnectedComponentsAlgo.Result expected =
					ConnectedComponentsAlgo.newBuilder().build().computeConnectivityComponents(gb.build());
			Int2IntMap expectedMap = new Int2IntOpenHashMap(n);
			for (int v : g.vertices())
				expectedMap.put(v, expected.getVertexCc(v));
			Pair<Integer, Int2IntMap> expectedPair = IntObjectPair.of(expected.getNumberOfCcs(), expectedMap);
			assertConnectivityResultsEqual(g, expectedPair, actual);
		});
	}

	private static void assertConnectivityResultsEqual(Graph g, Pair<Integer, Int2IntMap> r1,
			ConnectedComponentsAlgo.Result r2) {
		assertEquals(r1.first(), r2.getNumberOfCcs());
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r2.getNumberOfCcs());
		for (int u : g.vertices()) {
			int cc1 = r1.second().get(u);
			int cc2 = r2.getVertexCc(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	private static void validateConnectivityResult(Graph g, ConnectedComponentsAlgo.Result res) {
		BitSet ccs = new BitSet();
		for (int v : g.vertices())
			ccs.set(res.getVertexCc(v));
		assertEquals(ccs.cardinality(), res.getNumberOfCcs());
		for (int cc = 0; cc < res.getNumberOfCcs(); cc++)
			assertTrue(ccs.get(cc));
	}

}
