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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class SteinerTreeMehlhornTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0;
		final SteinerTreeAlgo algo = new SteinerTreeMehlhorn();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 6, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 2).repeat(128);
		tester.addPhase().withArgs(16, 32, 5).repeat(128);
		tester.addPhase().withArgs(64, 256, 11).repeat(64);
		tester.addPhase().withArgs(512, 4096, 23).repeat(8);
		tester.addPhase().withArgs(3542, 25436, 100).repeat(1);
		tester.run((n, m, k) -> {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());

			/* choose random terminals */
			int[] vs = g.vertices().toIntArray();
			IntSet terminals = new IntOpenHashSet();
			while (terminals.size() < k)
				terminals.add(vs[rand.nextInt(vs.length)]);

			/* make sure the terminals are connected */
			connectLoop: for (WeaklyConnectedComponentsAlgo ccAlgo = WeaklyConnectedComponentsAlgo.newInstance();;) {
				VertexPartition cc = ccAlgo.findWeaklyConnectedComponents(g);
				int[] terminalsArr = terminals.toIntArray();
				IntArrays.shuffle(terminalsArr, rand);
				int t1 = terminalsArr[0];
				for (int t2Idx = 1; t2Idx < terminalsArr.length; t2Idx++) {
					int t2 = terminalsArr[t2Idx];
					if (cc.vertexBlock(t1) != cc.vertexBlock(t2)) {
						int[] t1Vs = cc.blockVertices(cc.vertexBlock(t1)).toIntArray();
						int[] t2Vs = cc.blockVertices(cc.vertexBlock(t2)).toIntArray();
						g.addEdge(t1Vs[rand.nextInt(t1Vs.length)], t2Vs[rand.nextInt(t2Vs.length)]);
						continue connectLoop;
					}
				}
				break;
			}

			WeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			double appxFactor = 2 * (1 - 1.0 / k);
			testSteinerTree(g, w, terminals, algo, appxFactor);
		});
	}

	private static void testSteinerTree(Graph g, WeightFunctionInt w, IntCollection terminals, SteinerTreeAlgo algo,
			double appxFactor) {
		SteinerTreeAlgo.Result steinerEdges = algo.computeSteinerTree(g, w, terminals);
		Graph treeRes = g.subGraphCopy(null, steinerEdges.edges());

		assertTrue(treeRes.vertices().containsAll(terminals));
		assertTrue(Trees.isTree(treeRes));
		for (int v : treeRes.vertices())
			if (treeRes.outEdges(v).size() <= 1)
				assertTrue(terminals.contains(v));

		assertTrue(SteinerTreeAlgo.isSteinerTree(g, terminals, steinerEdges.edges()));

		final int m = g.edges().size();
		if (m <= 16) { /* check all trees */
			IntSet bestTree = null;
			IntList edges = new IntArrayList(g.edges());
			IntSet tree = new IntOpenHashSet(m);
			ToDoubleFunction<IntSet> treeWeight = t -> WeightFunction.weightSum(w, t);
			treeLoop: for (int bitmap = 0; bitmap < 1 << m; bitmap++) {
				tree.clear();
				assert tree.isEmpty();
				for (int i = 0; i < m; i++)
					if ((bitmap & (1 << i)) != 0)
						tree.add(edges.getInt(i));
				Graph treeGraph = g.subGraphCopy(null, tree);
				if (!Trees.isTree(treeGraph))
					continue treeLoop; /* not a tree */
				if (!treeGraph.vertices().containsAll(terminals))
					continue treeLoop; /* doesn't cover all terminals */
				if (bestTree == null || treeWeight.applyAsDouble(bestTree) > treeWeight.applyAsDouble(tree))
					bestTree = new IntOpenHashSet(tree);
			}

			assertNotNull(bestTree);
			assertTrue(treeWeight.applyAsDouble(bestTree) / appxFactor <= WeightFunction.weightSum(w,
					steinerEdges.edges()));

		}
	}

}
