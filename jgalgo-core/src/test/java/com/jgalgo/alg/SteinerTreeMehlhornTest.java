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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SteinerTreeMehlhornTest extends TestBase {

	@Test
	public void testEmptyTerminals() {
		final SteinerTreeAlgo algo = new SteinerTreeMehlhorn();

		CompleteGraphGenerator<Integer, Integer> gen = CompleteGraphGenerator.newInstance();
		gen.setVertices(range(7));
		gen.setEdges(new AtomicInteger()::getAndIncrement);
		Graph<Integer, Integer> g = gen.generate();

		/* empty terminal set */
		assertThrows(IllegalArgumentException.class, () -> algo.computeSteinerTree(g, null, IntList.of()));

		/* non unique terminal set */
		assertThrows(IllegalArgumentException.class, () -> algo.computeSteinerTree(g, null, IntList.of(0, 0, 1)));
	}

	@Test
	public void testRandGraph() {
		final long seed = 0xf07196500e58eeaL;
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			Graph<Integer, Integer> g0 = g;
			Supplier<Integer> edgeSupplier = () -> {
				for (;;) {
					Integer e = Integer.valueOf(rand.nextInt());
					if (e.intValue() > 0 && !g0.edges().contains(e))
						return e;
				}
			};

			/* choose random terminals */
			Set<Integer> terminals = new IntOpenHashSet();
			while (terminals.size() < k)
				terminals.add(Graphs.randVertex(g, rand));

			/* make sure the terminals are connected */
			connectLoop: for (WeaklyConnectedComponentsAlgo ccAlgo = WeaklyConnectedComponentsAlgo.newInstance();;) {
				VertexPartition<Integer, Integer> cc = ccAlgo.findWeaklyConnectedComponents(g);
				List<Integer> terminalsArr = new ArrayList<>(terminals);
				Collections.shuffle(terminalsArr, rand);
				Integer t1 = terminalsArr.get(0);
				for (int t2Idx = 1; t2Idx < terminalsArr.size(); t2Idx++) {
					Integer t2 = terminalsArr.get(t2Idx);
					if (cc.vertexBlock(t1) != cc.vertexBlock(t2)) {
						List<Integer> t1Vs = new ArrayList<>(cc.blockVertices(cc.vertexBlock(t1)));
						List<Integer> t2Vs = new ArrayList<>(cc.blockVertices(cc.vertexBlock(t2)));
						g.addEdge(randElement(t1Vs, rand), randElement(t2Vs, rand), edgeSupplier.get());
						continue connectLoop;
					}
				}
				break;
			}
			if (rand.nextInt(3) == 0) {
				terminals = IndexIdMaps.idToIndexSet(terminals, g.indexGraphVerticesMap());
				g = g.indexGraph();
			}

			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			double appxFactor = 2 * (1 - 1.0 / k);
			testSteinerTree(g, w, terminals, algo, appxFactor);
		});
	}

	private static <V, E> void testSteinerTree(Graph<V, E> g, WeightFunctionInt<E> w, Collection<V> terminals,
			SteinerTreeAlgo algo, double appxFactor) {
		SteinerTreeAlgo.Result<V, E> steinerEdges = algo.computeSteinerTree(g, w, terminals);
		Graph<V, E> treeRes = g.subGraphCopy(null, steinerEdges.edges());

		assertTrue(treeRes.vertices().containsAll(terminals));
		assertTrue(Trees.isTree(treeRes));
		for (V v : treeRes.vertices())
			if (treeRes.outEdges(v).size() <= 1)
				assertTrue(terminals.contains(v));

		assertTrue(SteinerTreeAlgo.isSteinerTree(g, terminals, steinerEdges.edges()));

		final int m = g.edges().size();
		if (m <= 16) { /* check all trees */
			Set<E> bestTree = null;
			List<E> edges = new ObjectArrayList<>(g.edges());
			Set<E> tree = new ObjectOpenHashSet<>(m);
			ToDoubleFunction<Set<E>> treeWeight = t -> WeightFunction.weightSum(w, t);
			treeLoop: for (int bitmap = 0; bitmap < 1 << m; bitmap++) {
				tree.clear();
				assert tree.isEmpty();
				for (int i = 0; i < m; i++)
					if ((bitmap & (1 << i)) != 0)
						tree.add(edges.get(i));
				Graph<V, E> treeGraph = g.subGraphCopy(null, tree);
				if (!Trees.isTree(treeGraph))
					continue treeLoop; /* not a tree */
				if (!treeGraph.vertices().containsAll(terminals))
					continue treeLoop; /* doesn't cover all terminals */
				if (bestTree == null || treeWeight.applyAsDouble(bestTree) > treeWeight.applyAsDouble(tree))
					bestTree = new ObjectOpenHashSet<>(tree);
			}

			assertNotNull(bestTree);
			assertTrue(treeWeight.applyAsDouble(bestTree) / appxFactor <= WeightFunction.weightSum(w,
					steinerEdges.edges()));

		}
	}

}
