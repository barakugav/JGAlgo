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

package com.jgalgo.alg.color;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ColoringTestUtils extends TestUtils {

	static void testRandGraphs(ColoringAlgo algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(200, 1000).repeat(32);
		tester.addPhase().withArgs(2048, 8192).repeat(4);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, false, false, true, seedGen.nextSeed());
			VertexPartition<Integer, Integer> coloring = algo.computeColoring(g);
			validateColoring(g, coloring);
		});
	}

	static void testWithSelfLoops(ColoringAlgo algo) {
		IntGraph g = IntGraphFactory.undirected().allowSelfEdges().newGraph();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int v3 = g.addVertexInt();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		g.addEdge(v3, v3);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static void testDirectedGraph(ColoringAlgo algo) {
		IntGraph g = IntGraph.newDirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int v3 = g.addVertexInt();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static <V, E> void validateColoring(Graph<V, E> g, VertexPartition<V, E> coloring) {
		assertTrue(ColoringAlgo.isColoring(g, v -> coloring.vertexBlock(v)));

		int n = g.vertices().size();
		if (n == 0)
			return;

		IntSet seenColors = new IntOpenHashSet();
		for (V v : g.vertices())
			seenColors.add(coloring.vertexBlock(v));
		int[] seenColorsArr = seenColors.toIntArray();
		IntArrays.parallelQuickSort(seenColorsArr);
		int[] seenColorsArrExpected = new int[seenColorsArr.length];
		for (int i : range(seenColorsArrExpected.length))
			seenColorsArrExpected[i] = i;
		assertArrayEquals(seenColorsArrExpected, seenColorsArr, "colors are expected to be 0,1,2,3,...");

		assertEquals(seenColorsArr.length, coloring.numberOfBlocks(), "wrong colors num");

		for (E e : g.edges()) {
			V u = g.edgeSource(e);
			V v = g.edgeTarget(e);
			int c1 = coloring.vertexBlock(u);
			int c2 = coloring.vertexBlock(v);
			assertNotEquals(c1, c2, "neighbor vertices " + u + "," + v + " have the same color: " + c1);
		}
	}

}
