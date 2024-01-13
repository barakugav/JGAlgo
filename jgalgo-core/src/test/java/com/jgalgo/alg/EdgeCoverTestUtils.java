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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class EdgeCoverTestUtils extends TestBase {

	static void testRandGraphs(EdgeCover algo, boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 6).repeat(64);
		tester.addPhase().withArgs(8, 12).repeat(64);
		tester.addPhase().withArgs(8, 16).repeat(32);
		tester.addPhase().withArgs(64, 256).repeat(16);
		tester.addPhase().withArgs(1024, 2048).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			/* make sure all vertices have at least one adjacent edge */
			for (Integer u : g.vertices()) {
				if (g.outEdges(u).isEmpty() && g.inEdges(u).isEmpty()) {
					Integer v = Graphs.randVertex(g, rand);
					Integer e;
					do {
						e = Integer.valueOf(rand.nextInt(g.edges().size() * 2));
					} while (g.edges().contains(e));
					g.addEdge(u, v, e);
				}
			}
			g = maybeIndexGraph(g, rand);

			WeightFunction<Integer> w = null;
			if (weighted)
				w = GraphsTestUtils.assignRandWeightsMaybeInt(g, 0, m * 16, seedGen.nextSeed());

			EdgeCoverTestUtils.testEdgeCover(g, w, algo);
		});
	}

	static <V, E> void testEdgeCover(Graph<V, E> g, WeightFunction<E> w, EdgeCover algo) {
		Set<E> ec = algo.computeMinimumEdgeCover(g, w);

		for (V v : g.vertices()) {
			boolean isCovered = g.outEdges(v).stream().anyMatch(ec::contains);
			if (g.isDirected())
				isCovered |= g.inEdges(v).stream().anyMatch(ec::contains);
			assertTrue(isCovered, "vertex is not covered: " + v);
		}

		assertTrue(EdgeCover.isCover(g, ec));

		final int m = g.edges().size();
		if (m <= 16) { /* check all covers */
			BiPredicate<Set<E>, V> isCovered;
			if (g.isDirected()) {
				isCovered = (cover, v) -> g.outEdges(v).stream().anyMatch(cover::contains)
						|| g.inEdges(v).stream().anyMatch(cover::contains);
			} else {
				isCovered = (cover, v) -> g.outEdges(v).stream().anyMatch(cover::contains);
			}
			Set<E> bestCover = SubSets
					.stream(g.edges())
					.map(ObjectOpenHashSet::new)
					.filter(cover -> g.vertices().stream().allMatch(v -> isCovered.test(cover, v)))
					.min((c1, c2) -> Double.compare(WeightFunction.weightSum(w, c1), WeightFunction.weightSum(w, c2)))
					.get();
			assertEquals(WeightFunction.weightSum(w, bestCover), WeightFunction.weightSum(w, ec));
		}
	}

	static void testNoValidCover(EdgeCover algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 12).repeat(4);
		tester.addPhase().withArgs(8, 16).repeat(4);
		tester.addPhase().withArgs(64, 256).repeat(4);
		tester.run((n, m) -> {
			boolean directed = rand.nextBoolean();
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());

			/* remove all edges of a random vertex, no edge cover will be able to cover it */
			g.removeEdgesOf(Graphs.randVertex(g, rand));

			assertThrows(IllegalArgumentException.class, () -> algo.computeMinimumEdgeCover(g, null));
		});
	}

	@Test
	public void isCoverNonExistingEdge() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 1);

		assertThrows(NoSuchEdgeException.class, () -> EdgeCover.isCover(g, IntSet.of(2)));
		assertThrows(NoSuchEdgeException.class, () -> EdgeCover.isCover(g.indexGraph(), IntSet.of(57)));
	}

	@Test
	public void isCoverDuplicatedEdge() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> EdgeCover.isCover(g, IntList.of(1, 1)));
	}

	@Test
	public void isCoverNegativeDirected() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);

		assertFalse(EdgeCover.isCover(g, IntSet.of(1)));
	}

	@Test
	public void isCoverNegativeUndirected() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(1, 2, 1);

		assertFalse(EdgeCover.isCover(g, IntSet.of(1)));
	}

	@Test
	public void defaultAlgo() {
		EdgeCover algo = EdgeCover.newInstance();

		IntGraph g = IntGraph.newUndirected();
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 1);

		assertTrue(EdgeCover.isCover(g, algo.computeMinimumEdgeCover(g, null)));
		assertTrue(EdgeCover.isCover(g, algo.computeMinimumEdgeCover(g, WeightFunction.cardinalityWeightFunction())));
		assertTrue(EdgeCover.isCover(g, algo.computeMinimumEdgeCover(g, e -> 57)));
	}

}
