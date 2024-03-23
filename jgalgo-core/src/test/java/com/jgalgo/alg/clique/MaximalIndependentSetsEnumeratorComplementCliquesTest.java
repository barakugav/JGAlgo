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
package com.jgalgo.alg.clique;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MaximalIndependentSetsEnumeratorComplementCliquesTest extends TestBase {

	@Test
	public void randGraphsDirected() {
		MaximalIndependentSetsEnumerator algo = new MaximalIndependentSetsEnumeratorComplementCliques();
		testRandGraphs(algo, true, 0x6b447a5cf5acb46bL);
	}

	@Test
	public void randGraphsUndirected() {
		MaximalIndependentSetsEnumerator algo = new MaximalIndependentSetsEnumeratorComplementCliques();
		testRandGraphs(algo, false, 0xfbcd50632cb0fc49L);
	}

	static void testRandGraphs(MaximalIndependentSetsEnumerator algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		/* no need to test thoroughly, based on max cliques enumerator, which is tested on its own */
		tester.addPhase().withArgs(16, 100).repeat(8);
		tester.addPhase().withArgs(32, 400).repeat(3);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			testAlgo(g, algo);
		});
	}

	private static <V, E> void testAlgo(Graph<V, E> g, MaximalIndependentSetsEnumerator algo) {
		final int n = g.vertices().size();
		Collection<Set<V>> independentSets = new ObjectArrayList<>(algo.maximalIndependentSetsIter(g));

		/* assert the returned sets are actual maximal independent sets */
		for (Set<V> independentSet : independentSets)
			assertTrue(isMaximalIndependentSet(independentSet, g));

		if (n <= 24) {
			/* test all possible sub sets of vertices */
			Set<Set<V>> independentSetsExpected = SubSets
					.stream(g.vertices())
					.filter(independentSet -> isMaximalIndependentSet(new ObjectOpenHashSet<>(independentSet), g))
					.map(ObjectOpenHashSet::new)
					.collect(toSet());

			Set<Set<V>> independentSetsActual = new ObjectOpenHashSet<>(independentSets);
			assertEquals(independentSetsActual.size(), independentSets.size());
			assertEquals(independentSetsExpected, independentSetsActual);
		}
	}

	private static <V, E> boolean isIndependentSet(Set<V> independentSet, Graph<V, E> g) {
		return g
				.edges()
				.stream()
				.filter(e -> !g.edgeSource(e).equals(g.edgeTarget(e)))
				.noneMatch(e -> independentSet.contains(g.edgeSource(e)) && independentSet.contains(g.edgeTarget(e)));
	};

	private static <V, E> boolean isMaximalIndependentSet(Set<V> independentSet, Graph<V, E> g) {
		boolean directed = g.isDirected();
		return isIndependentSet(independentSet, g) && g
				.vertices()
				.stream()
				.filter(u -> !independentSet.contains(u))
				.noneMatch(u -> independentSet
						.stream()
						.noneMatch(v -> g.containsEdge(u, v) || (directed && g.containsEdge(v, u))));
	}

	@Test
	public void defaultImpl() {
		assertEquals(MaximalIndependentSetsEnumeratorComplementCliques.class,
				MaximalIndependentSetsEnumerator.newInstance().getClass());
	}

}
