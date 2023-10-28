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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class VoronoiAlgoDijkstraTest extends TestBase {

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x48f95f4d58b6aa8bL;
		testRandGraphs(new VoronoiAlgoDijkstra(), false, seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0x83283b215fbf11f8L;
		testRandGraphs(new VoronoiAlgoDijkstra(), true, seed);
	}

	private static void testRandGraphs(VoronoiAlgo algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 2).repeat(128);
		tester.addPhase().withArgs(16, 32, 5).repeat(128);
		tester.addPhase().withArgs(64, 256, 11).repeat(64);
		tester.addPhase().withArgs(512, 4096, 23).repeat(8);
		tester.addPhase().withArgs(3542, 25436, 100).repeat(1);
		tester.run((n, m, k) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			WeightFunction w = GraphsTestUtils.assignRandWeights(g, seedGen.nextSeed());

			int[] vs = g.vertices().toIntArray();
			IntSet sites = new IntOpenHashSet();
			while (sites.size() < k)
				sites.add(vs[rand.nextInt(vs.length)]);

			testAlgo(g, w, sites, algo);
		});
	}

	private static void testAlgo(Graph g, WeightFunction w, IntCollection sites, VoronoiAlgo algo) {
		VoronoiAlgo.Result cells = algo.computeVoronoiCells(g, sites, w);

		assertTrue(VertexPartition.isPartition(g, cells::vertexBlock));

		ShortestPathSingleSource sssp = new ShortestPathSingleSourceDijkstra();
		Int2ObjectMap<ShortestPathSingleSource.Result> ssspResults = new Int2ObjectOpenHashMap<>();
		for (int site : sites)
			ssspResults.put(site, sssp.computeShortestPaths(g, w, site));
		for (int v : g.vertices()) {
			double actual = cells.distance(v);
			double expected =
					sites.intStream().mapToDouble(site -> ssspResults.get(site).distance(v)).min().getAsDouble();
			assertEquals(expected, actual);
			if (expected == Double.POSITIVE_INFINITY) {
				int unreachableCell = cells.numberOfBlocks() - 1;
				assertNull(cells.getPath(v));
				assertEquals(unreachableCell, cells.vertexBlock(v));
				assertEquals(-1, cells.vertexSite(v));

			} else {
				Path path = cells.getPath(v);
				assertNotNull(path);
				assertTrue(cells.vertexBlock(v) < sites.size());
				assertEquals(cells.blockSite(cells.vertexBlock(v)), cells.vertexSite(v));
			}
		}

		int unreachableCell = cells.numberOfBlocks() > sites.size() ? cells.numberOfBlocks() - 1 : -1;
		if (unreachableCell >= 0)
			assertEquals(-1, cells.blockSite(unreachableCell));
		IntCollection unreachableVertices = unreachableCell >= 0 ? cells.blockVertices(unreachableCell) : IntList.of();
		for (int unreachable : unreachableVertices) {
			assertEquals(Double.POSITIVE_INFINITY, cells.distance(unreachable));
			assertEquals(null, cells.getPath(unreachable));
			for (int site : sites) {
				assertEquals(Double.POSITIVE_INFINITY, ssspResults.get(site).distance(unreachable));
				assertNull(ssspResults.get(site).getPath(unreachable));
			}
		}

	}

}