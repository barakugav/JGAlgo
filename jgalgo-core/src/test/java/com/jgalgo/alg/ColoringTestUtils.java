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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
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
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(false).build();
			IVertexPartition coloring = (IVertexPartition) algo.computeColoring(g);
			validateColoring(g, coloring);
		});
	}

	static void testWithSelfLoops(ColoringAlgo algo) {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		g.addEdge(v3, v3);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static void testDirectedGraph(ColoringAlgo algo) {
		IntGraph g = IntGraph.newDirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static void validateColoring(IntGraph g, IVertexPartition coloring) {
		assertTrue(ColoringAlgo.isColoring(g, v -> coloring.vertexBlock(v.intValue())));

		int n = g.vertices().size();
		if (n == 0)
			return;

		IntSet seenColors = new IntOpenHashSet();
		for (int v : g.vertices())
			seenColors.add(coloring.vertexBlock(v));
		int[] seenColorsArr = seenColors.toIntArray();
		IntArrays.parallelQuickSort(seenColorsArr);
		int[] seenColorsArrExpected = new int[seenColorsArr.length];
		for (int i = 0; i < seenColorsArrExpected.length; i++)
			seenColorsArrExpected[i] = i;
		assertArrayEquals(seenColorsArrExpected, seenColorsArr, "colors are expected to be 0,1,2,3,...");

		assertEquals(seenColorsArr.length, coloring.numberOfBlocks(), "wrong colors num");

		for (int e : g.edges()) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int c1 = coloring.vertexBlock(u);
			int c2 = coloring.vertexBlock(v);
			assertNotEquals(c1, c2, "neighbor vertices " + u + "," + v + " have the same color: " + c1);
		}
	}

}
