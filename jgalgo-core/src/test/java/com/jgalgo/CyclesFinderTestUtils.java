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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntSupplier;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;

class CyclesFinderTestUtils extends TestUtils {

	static void testSimpleGraph(CyclesFinder cyclesFinder) {
		IndexGraph g = IndexGraphFactory.newDirected().newGraph();
		int v0 = g.addVertex();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int e0 = g.addEdge(v0, v1);
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v1);
		int e3 = g.addEdge(v2, v0);

		Iterator<Path> actual = cyclesFinder.findAllCycles(g);

		Path c1 = new PathImpl(g, v0, v0, IntList.of(e0, e1, e3));
		Path c2 = new PathImpl(g, v1, v1, IntList.of(e1, e2));
		List<Path> expected = List.of(c1, c2);

		assertEquals(transformCyclesToCanonical(expected.iterator()), transformCyclesToCanonical(actual));
	}

	static void testRandGraphs(CyclesFinder cyclesFinder, long seed) {

		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(32, 32).repeat(128);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).build();
			testGraph(g, cyclesFinder);
		});
	}

	private static void testGraph(Graph g, CyclesFinder cyclesFinder) {
		CyclesFinder validationAlgo =
				cyclesFinder instanceof CyclesFinderTarjan ? new CyclesFinderJohnson() : new CyclesFinderTarjan();
		Iterator<Path> actual = cyclesFinder.findAllCycles(g);
		Iterator<Path> expected = validationAlgo.findAllCycles(g);
		assertEquals(transformCyclesToCanonical(expected), transformCyclesToCanonical(actual));
	}

	private static Set<IntList> transformCyclesToCanonical(Iterator<Path> cycles) {
		int expectedCount = 0;
		Set<IntList> cycles0 = new TreeSet<>();
		for (Path cycle : JGAlgoUtils.iterable(cycles)) {
			IntArrayList cycle0 = new IntArrayList(cycle);
			transformCycleToCanonical(cycle0);
			cycles0.add(cycle0);
			expectedCount++;
		}
		if (cycles0.size() != expectedCount)
			throw new IllegalArgumentException("cycles list contains duplications");
		return cycles0;
	}

	private static void transformCycleToCanonical(IntArrayList c) {
		final int s = c.size();
		IntSupplier findMinIdx = () -> {
			int minIdx = -1, min = Integer.MAX_VALUE;
			for (int i = 0; i < s; i++) {
				int elm = c.getInt(i);
				if (minIdx == -1 || min > elm) {
					minIdx = i;
					min = elm;
				}
			}
			return minIdx;
		};

		/* reverse */
		int minIdx = findMinIdx.getAsInt();
		int next = c.getInt((minIdx + 1) % s);
		int prev = c.getInt((minIdx - 1 + s) % s);
		if (next > prev) {
			IntArrays.reverse(c.elements(), 0, s);
			minIdx = s - minIdx - 1;
			assert minIdx == findMinIdx.getAsInt();
		}

		/* rotate */
		rotate(c, minIdx);
	}

	private static void rotate(IntList l, int idx) {
		if (l.isEmpty() || idx == 0)
			return;
		int s = l.size();
		int[] temp = l.toIntArray();
		for (int i = 0; i < s; i++)
			l.set(i, temp[(i + idx) % s]);
	}

}
