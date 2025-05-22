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

package com.jgalgo.alg.cycle;

import static com.jgalgo.internal.util.IterTools.foreach;
import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntSupplier;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

class CyclesEnumeratorTestUtils extends TestUtils {

	static void testSimpleGraph(CyclesEnumerator cyclesFinder) {
		IndexGraph g = IndexGraph.newDirected();
		int v0 = g.addVertexInt();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int e0 = g.addEdge(v0, v1);
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v1);
		int e3 = g.addEdge(v2, v0);

		Iterator<Path<Integer, Integer>> actual = cyclesFinder.cyclesIter(g);

		IPath c1 = IPath.valueOf(g, v0, v0, IntList.of(e0, e1, e3));
		IPath c2 = IPath.valueOf(g, v1, v1, IntList.of(e1, e2));
		List<Path<Integer, Integer>> expected = List.of(c1, c2);

		assertEquals(transformCyclesToCanonical(g, expected.iterator()), transformCyclesToCanonical(g, actual));
	}

	static void testRandGraphs(CyclesEnumerator cyclesFinder, long seed) {

		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(32, 32).repeat(128);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, true, true, false, seedGen.nextSeed());
			testGraph(g, cyclesFinder);
		});
	}

	private static <V, E> void testGraph(Graph<V, E> g, CyclesEnumerator cyclesFinder) {
		CyclesEnumerator validationAlgo = cyclesFinder instanceof CyclesEnumeratorTarjan ? new CyclesEnumeratorJohnson()
				: new CyclesEnumeratorTarjan();
		Iterator<Path<V, E>> actual = cyclesFinder.cyclesIter(g);
		Iterator<Path<V, E>> expected = validationAlgo.cyclesIter(g);
		assertEquals(transformCyclesToCanonical(g, expected), transformCyclesToCanonical(g, actual));
	}

	private static <V, E> Set<List<E>> transformCyclesToCanonical(Graph<V, E> g, Iterator<Path<V, E>> cycles) {
		int expectedCount = 0;
		Set<List<E>> cycles0 = new TreeSet<>();
		for (Path<V, E> cycle : foreach(cycles)) {
			ObjectArrayList<E> cycle0 = new ObjectArrayList<>(cycle.edges());
			transformCycleToCanonical(g, cycle0);
			cycles0.add(cycle0);
			expectedCount++;
		}
		if (cycles0.size() != expectedCount)
			throw new IllegalArgumentException("cycles list contains duplications");
		return cycles0;
	}

	private static <V, E> void transformCycleToCanonical(Graph<V, E> g, ObjectArrayList<E> c) {
		final int s = c.size();
		IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
		IntSupplier findMinIdx = () -> {
			int minIdx = -1, min = Integer.MAX_VALUE;
			for (int i : range(s)) {
				E elm = c.get(i);
				int elmIdx = eiMap.idToIndex(elm);
				if (minIdx < 0 || min > elmIdx) {
					minIdx = i;
					min = elmIdx;
				}
			}
			return minIdx;
		};

		/* reverse */
		int minIdx = findMinIdx.getAsInt();
		E next = c.get((minIdx + 1) % s);
		E prev = c.get((minIdx - 1 + s) % s);
		if (eiMap.idToIndex(next) > eiMap.idToIndex(prev)) {
			ObjectArrays.reverse(c.elements(), 0, s);
			minIdx = s - minIdx - 1;
			assert minIdx == findMinIdx.getAsInt();
		}

		/* rotate */
		rotate(c, minIdx);
	}

	@SuppressWarnings("unchecked")
	private static <K> void rotate(List<K> l, int idx) {
		if (l.isEmpty() || idx == 0)
			return;
		int s = l.size();
		Object[] temp = l.toArray();
		for (int i : range(s))
			l.set(i, (K) temp[(i + idx) % s]);
	}

}
