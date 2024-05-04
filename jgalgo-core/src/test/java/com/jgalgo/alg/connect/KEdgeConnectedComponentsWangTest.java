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
package com.jgalgo.alg.connect;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.alg.VertexPartition;
import com.jgalgo.gen.EmptyGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class KEdgeConnectedComponentsWangTest extends TestBase {

	@Test
	public void emptyGraph() {
		final long seed = 0x3bb18bd8832291d7L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		KEdgeConnectedComponentsWang algo = new KEdgeConnectedComponentsWang();
		algo.setSeed(seedGen.nextSeed());

		Graph<Integer, Integer> g = EmptyGraphGenerator.emptyGraph(range(5));
		VertexPartition<Integer, Integer> res = algo.computeKEdgeConnectedComponents(g, 3);
		assertNotNull(res);
		assertEquals(g.vertices().size(), res.numberOfBlocks());

		g = EmptyGraphGenerator.emptyGraph(range(0));
		res = algo.computeKEdgeConnectedComponents(g, 3);
		assertNotNull(res);
		assertEquals(0, res.numberOfBlocks());
	}

	@Test
	public void randGraphUndirected() {
		final long seed = 0x76e9278ecee7ec95L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		KEdgeConnectedComponentsWang algo = new KEdgeConnectedComponentsWang();
		algo.setSeed(seedGen.nextSeed());
		randGraphs(algo, false, seedGen.nextSeed());
	}

	@Test
	public void randGraphDirected() {
		final long seed = 0x9e59724ed80b40fbL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		KEdgeConnectedComponentsWang algo = new KEdgeConnectedComponentsWang();
		algo.setSeed(seedGen.nextSeed());
		randGraphs(algo, true, seedGen.nextSeed());
	}

	static void randGraphs(KEdgeConnectedComponentsAlgo algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 2).repeat(64);
		tester.addPhase().withArgs(19, 39, 3).repeat(64);
		tester.addPhase().withArgs(23, 52, 3).repeat(16);
		tester.addPhase().withArgs(64, 256, 4).repeat(8);
		tester.addPhase().withArgs(100, 2100, 5).repeat(1);
		tester.run((n, m, k) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			validateKEdgeConnectedComponents(g, k, algo);
		});
	}

	private static <V, E> void validateKEdgeConnectedComponents(Graph<V, E> g, int k,
			KEdgeConnectedComponentsAlgo algo) {
		VertexPartition<V, E> actual = algo.computeKEdgeConnectedComponents(g, k);

		Object2IntMap<V> expected = computeExpectedKEdgeConnectedComponents(g, k);
		assertEquals(expected.values().intStream().distinct().count(), actual.numberOfBlocks());

		Int2IntMap actualToExpectedLabels = new Int2IntOpenHashMap();
		Int2IntMap expectedToActualLabels = new Int2IntOpenHashMap();
		for (V v : g.vertices()) {
			int actualLabel = actual.vertexBlock(v);
			int expectedLabel = expected.getInt(v);
			if (!actualToExpectedLabels.containsKey(actualLabel)) {
				assertTrue(!expectedToActualLabels.containsKey(expectedLabel));
				actualToExpectedLabels.put(actualLabel, expectedLabel);
				expectedToActualLabels.put(expectedLabel, actualLabel);
				actualLabel = expectedLabel;
			} else {
				assertTrue(expectedToActualLabels.containsKey(expectedLabel));
				actualLabel = actualToExpectedLabels.get(actualLabel);
			}
			assertEquals(expectedLabel, actualLabel);
		}
	}

	private static <V, E> Object2IntMap<V> computeExpectedKEdgeConnectedComponents(Graph<V, E> g, int k) {
		MinimumEdgeCutSt minCutAlgo = MinimumEdgeCutSt.newInstance();
		final int n = g.vertices().size();
		IndexIdMap<V> viMap = g.indexGraphVerticesMap();
		IndexGraphBuilder gb = IndexGraphBuilder.undirected();
		gb.addVertices(range(n));
		for (int u : range(n)) {
			for (int v : range(u + 1, n)) {
				int connectivity = minCutAlgo
						.computeMinimumCut(g, null, viMap.indexToId(u), viMap.indexToId(v))
						.crossEdges()
						.size();
				if (g.isDirected())
					connectivity = Math
							.min(connectivity,
									minCutAlgo
											.computeMinimumCut(g, null, viMap.indexToId(v), viMap.indexToId(u))
											.crossEdges()
											.size());
				if (connectivity >= k)
					gb.addEdge(u, v);
			}
		}
		IVertexPartition partition = (IVertexPartition) WeaklyConnectedComponentsAlgo
				.newInstance()
				.findWeaklyConnectedComponents(gb.build());
		Object2IntMap<V> partition0 = new Object2IntOpenHashMap<>();
		for (int v : range(n))
			partition0.put(viMap.indexToId(v), partition.vertexBlock(v));
		return partition0;
	}

}
