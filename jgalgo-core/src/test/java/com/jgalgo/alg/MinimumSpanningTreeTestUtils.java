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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class MinimumSpanningTreeTestUtils extends TestUtils {

	private MinimumSpanningTreeTestUtils() {}

	static void testRandGraph(MinimumSpanningTree algo, long seed) {
		testRandGraph(algo, GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void testRandGraph(MinimumSpanningTree algo, Boolean2ObjectFunction<IntGraph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			IntGraph g = GraphsTestUtils.randGraph(n, m, graphImpl, seedGen.nextSeed());
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			MinimumSpanningTree.IResult mst = (MinimumSpanningTree.IResult) algo.computeMinimumSpanningTree(g, w);
			verifyMST(g, w, mst);
		});
	}

	private static class MSTEdgeComparator implements IntComparator {

		private final IntGraph g;
		private final IWeightFunction w;

		MSTEdgeComparator(IntGraph g, IWeightFunction w) {
			this.g = g;
			this.w = w;
		}

		@Override
		public int compare(int e1, int e2) {
			int u1 = g.edgeSource(e1), v1 = g.edgeTarget(e1);
			int u2 = g.edgeSource(e2), v2 = g.edgeTarget(e2);
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

	private static void verifyMST(IntGraph g, IWeightFunction w, MinimumSpanningTree.IResult mst) {
		assertTrue(MinimumSpanningTree.isSpanningForest(g, mst.edges()));
		if (WeaklyConnectedComponentsAlgo.newInstance().isWeaklyConnected(g))
			assertTrue(MinimumSpanningTree.isSpanningTree(g, mst.edges()));

		/*
		 * It's hard to verify MST, we use Kruskal algorithm to verify the others, and assume its implementation is
		 * correct
		 */
		MinimumSpanningTree.IResult expected =
				(MinimumSpanningTree.IResult) new MinimumSpanningTreeKruskal().computeMinimumSpanningTree(g, w);

		IntComparator c = new MSTEdgeComparator(g, w);
		IntSet actualSet = new IntAVLTreeSet(c);
		actualSet.addAll(mst.edges());

		assertEquals(mst.edges().size(), actualSet.size(), "MST contains duplications");
		assertEquals(expected.edges().size(), actualSet.size(), "unexpected MST size");
		for (int e : expected.edges())
			assertTrue(actualSet.contains(e), "MST doesn't contains edge: " + e);
	}

}
