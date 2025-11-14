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
package com.jgalgo.alg.cover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class VertexCoverBarYehudaTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final VertexCover algo = new VertexCoverBarYehuda();
		final double appxFactor = 2;

		final long seed = 0x3c94d9694bd37614L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 16).repeat(256);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(1024, 2048).repeat(16);
		tester.addPhase().withArgs(8096, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			RandomIntUnique wRand = new RandomIntUnique(0, 163454, seedGen.nextSeed());
			WeightsInt<Integer> weight = g.addVerticesWeights("weight", int.class);
			for (Integer v : g.vertices())
				weight.set(v, wRand.next());

			testVC(g, weight, algo, appxFactor);
		});
	}

	private static <V, E> void testVC(Graph<V, E> g, WeightFunctionInt<V> w, VertexCover algo, double appxFactor) {
		Set<V> vc = algo.computeMinimumVertexCover(g, w);

		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			assertTrue(vc.contains(u) || vc.contains(v), "edge is not covered: " + e);
		}

		assertTrue(VertexCover.isCover(g, vc));

		final int n = g.vertices().size();
		if (n < 16) { /* check all covers */
			ToDoubleFunction<Set<V>> coverWeight = c -> w.weightSum(c);
			Set<V> bestCover = SubSets
					.stream(g.vertices())
					.map(ObjectOpenHashSet::new)
					.filter(cover -> g
							.edges()
							.stream()
							.allMatch(e -> cover.contains(g.edgeSource(e)) || cover.contains(g.edgeTarget(e))))
					.min((c1, c2) -> Double.compare(coverWeight.applyAsDouble(c1), coverWeight.applyAsDouble(c2)))
					.get();

			assertNotNull(bestCover);
			assertTrue(w.weightSum(vc) / appxFactor <= coverWeight.applyAsDouble(bestCover));
		}
	}

	@Test
	public void isCoverNonExistingVertex() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 1);

		assertThrows(NoSuchVertexException.class, () -> VertexCover.isCover(g, IntSet.of(3)));
		assertThrows(NoSuchVertexException.class, () -> VertexCover.isCover(g.indexGraph(), IntSet.of(57)));
	}

	@Test
	public void isCoverDuplicatedVertex() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> VertexCover.isCover(g, IntList.of(1, 1)));
	}

	@Test
	public void isCoverNegative() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);
		g.addEdge(1, 3, 2);

		assertFalse(VertexCover.isCover(g, IntSet.of(2)));
	}

	@Test
	public void testDefaultImpl() {
		VertexCover algo = VertexCover.newInstance();
		assertEquals(VertexCoverBarYehuda.class, algo.getClass());
	}

}
