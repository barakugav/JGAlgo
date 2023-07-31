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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ColoringTestUtils extends TestUtils {

	static void testRandGraphs(Coloring algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(32, 200, 1000), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(false).build();
			Coloring.Result coloring = algo.computeColoring(g);
			validateColoring(g, coloring);
		});
	}

	static void testWithSelfLoops(Coloring algo) {
		Graph g = GraphFactory.newUndirected().newGraph();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		g.addEdge(v3, v3);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static void testDirectedGraph(Coloring algo) {
		Graph g = GraphFactory.newDirected().newGraph();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v1);
		assertThrows(IllegalArgumentException.class, () -> algo.computeColoring(g));
	}

	static void validateColoring(Graph g, Coloring.Result coloring) {
		int n = g.vertices().size();
		if (n == 0)
			return;

		IntSet seenColors = new IntOpenHashSet();
		for (int v : g.vertices())
			seenColors.add(coloring.colorOf(v));
		int[] seenColorsArr = seenColors.toIntArray();
		IntArrays.parallelQuickSort(seenColorsArr);
		int[] seenColorsArrExpected = new int[seenColorsArr.length];
		for (int i = 0; i < seenColorsArrExpected.length; i++)
			seenColorsArrExpected[i] = i;
		assertArrayEquals(seenColorsArrExpected, seenColorsArr, "colors are expected to be 0,1,2,3,...");

		assertEquals(seenColorsArr.length, coloring.colorsNum(), "wrong colors num");

		for (int e : g.edges()) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int c1 = coloring.colorOf(u);
			int c2 = coloring.colorOf(v);
			assertNotEquals(c1, c2, "neighbor vertices " + u + "," + v + " have the same color: " + c1);
		}
	}

}
