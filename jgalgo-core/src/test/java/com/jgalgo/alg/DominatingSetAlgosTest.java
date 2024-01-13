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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class DominatingSetAlgosTest extends TestBase {

	@Test
	public void testIsDominatingSetDuplicateVertex() {
		Graph<Integer, Integer> g = Graph.newUndirected();
		g.addVertex(Integer.valueOf(1));
		g.addVertex(Integer.valueOf(2));
		g.addVertex(Integer.valueOf(3));

		assertThrows(IllegalArgumentException.class,
				() -> DominatingSetAlgo.isDominatingSet(g, IntList.of(1, 1, 2, 3), null));
	}

	@Test
	public void testNewInstance() {
		assertNotNull(DominatingSetAlgo.newInstance());
	}

	static void testRandGraphs(DominatingSetAlgo algo, boolean directed, EdgeDirection dominanceDirection, long seed) {
		final boolean weighted = Boolean.valueOf("false").booleanValue();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 8).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightFunction<Integer> w = null;
			if (weighted && rand.nextBoolean()) {
				WeightsInt<Integer> w0 = g.addVerticesWeights("weight", int.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextInt(551));
				w = w0;

			} else if (weighted) {
				WeightsDouble<Integer> w0 = g.addVerticesWeights("weight", double.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextDouble() * 5642);
				w = w0;
			}

			testMinDominatingSet(g, w, dominanceDirection, algo);
		});
	}

	private static <V, E> void testMinDominatingSet(Graph<V, E> g, WeightFunction<V> w,
			EdgeDirection dominanceDirection, DominatingSetAlgo algo) {
		Set<V> minDominatingSet;
		if (dominanceDirection == null) {
			minDominatingSet = algo.computeMinimumDominationSet(g, w);
		} else {
			minDominatingSet = algo.computeMinimumDominationSet(g, w, dominanceDirection);
		}
		if (dominanceDirection == null)
			dominanceDirection = EdgeDirection.All;
		// double minSetWeight = WeightFunction.weightSum(w, minDominatingSet);

		Set<V> dominated = new ObjectOpenHashSet<>(g.vertices().size());
		for (V v : minDominatingSet) {
			dominated.add(v);
			if (!g.isDirected() || dominanceDirection == EdgeDirection.Out) {
				for (EdgeIter<V, E> eit = g.outEdges(v).iterator(); eit.hasNext();) {
					eit.next();
					dominated.add(eit.target());
				}
			} else if (dominanceDirection == EdgeDirection.In) {
				for (EdgeIter<V, E> eit = g.inEdges(v).iterator(); eit.hasNext();) {
					eit.next();
					dominated.add(eit.source());
				}
			} else {
				assert dominanceDirection == EdgeDirection.All;
				for (EdgeIter<V, E> eit = g.outEdges(v).iterator(); eit.hasNext();) {
					eit.next();
					dominated.add(eit.target());
				}
				for (EdgeIter<V, E> eit = g.inEdges(v).iterator(); eit.hasNext();) {
					eit.next();
					dominated.add(eit.source());
				}
			}
		}
		assertEquals(g.vertices(), dominated);
		assertTrue(DominatingSetAlgo.isDominatingSet(g, minDominatingSet, dominanceDirection));
	}

}
