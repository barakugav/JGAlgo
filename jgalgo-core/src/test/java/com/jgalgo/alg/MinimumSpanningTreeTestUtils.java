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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Comparator;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

public class MinimumSpanningTreeTestUtils extends TestUtils {

	private MinimumSpanningTreeTestUtils() {}

	static void testRandGraph(MinimumSpanningTree algo, long seed) {
		testRandGraph(algo, GraphsTestUtils.defaultGraphImpl(seed), seed);
	}

	public static void testRandGraph(MinimumSpanningTree algo,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			boolean selfEdges = graphImpl.get(false).isAllowSelfEdges();
			boolean parallelEdges = graphImpl.get(false).isAllowParallelEdges();
			Graph<Integer, Integer> g = GraphsTestUtils.withImpl(
					GraphsTestUtils.randGraph(n, m, false, selfEdges, parallelEdges, seedGen.nextSeed()), graphImpl);
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			MinimumSpanningTree.Result<Integer, Integer> mst = algo.computeMinimumSpanningTree(g, w);
			verifyMST(g, w, mst);
		});
	}

	private static class MSTEdgeComparator<V, E> implements Comparator<E> {

		private final Graph<V, E> g;
		private final WeightFunction<E> w;

		MSTEdgeComparator(Graph<V, E> g, WeightFunction<E> w) {
			this.g = g;
			this.w = w;
		}

		@Override
		public int compare(E e1, E e2) {
			IndexIdMap<V> vIdMap = g.indexGraphVerticesMap();
			int u1 = vIdMap.idToIndex(g.edgeSource(e1)), v1 = vIdMap.idToIndex(g.edgeTarget(e1));
			int u2 = vIdMap.idToIndex(g.edgeSource(e2)), v2 = vIdMap.idToIndex(g.edgeTarget(e2));
			if (v1 > u1) {
				int temp = u1;
				u1 = v1;
				v1 = temp;
			}
			if (v2 > u2) {
				int temp = u2;
				u2 = v2;
				v2 = temp;
			}
			if (u1 != u2)
				return Integer.compare(u1, u2);
			if (v1 != v2)
				return Integer.compare(v1, v2);
			return Double.compare(w.weight(e1), w.weight(e2));
		}

	}

	private static <V, E> void verifyMST(Graph<V, E> g, WeightFunction<E> w, MinimumSpanningTree.Result<V, E> mst) {
		assertTrue(MinimumSpanningTree.isSpanningForest(g, mst.edges()));
		if (WeaklyConnectedComponentsAlgo.newInstance().isWeaklyConnected(g))
			assertTrue(MinimumSpanningTree.isSpanningTree(g, mst.edges()));

		/*
		 * It's hard to verify MST, we use Kruskal algorithm to verify the others, and assume its implementation is
		 * correct
		 */
		MinimumSpanningTree.Result<V, E> expected = new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w);

		Comparator<E> c = new MSTEdgeComparator<>(g, w);
		Set<E> actualSet = new ObjectAVLTreeSet<>(c);
		actualSet.addAll(mst.edges());

		assertEquals(mst.edges().size(), actualSet.size(), "MST contains duplications");
		assertEquals(expected.edges().size(), actualSet.size(), "unexpected MST size");
		for (E e : expected.edges())
			assertTrue(actualSet.contains(e), "MST doesn't contains edge: " + e);
	}

	static void directedNotSupported(MinimumSpanningTree algo) {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 0, 0);
		g.addEdge(1, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> algo.computeMinimumSpanningTree(g, null));
	}

}
