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

import static com.jgalgo.internal.util.Range.range;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class MaximalCliquesEnumeratorTestUtils extends TestUtils {

	static void testRandGraphs(MaximalCliquesEnumerator algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(128, 256).repeat(12);
		tester.addPhase().withArgs(1024, 4096).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, false, seedGen.nextSeed());
			testAlgo(g, algo);
		});
	}

	private static <V, E> void testAlgo(Graph<V, E> g, MaximalCliquesEnumerator algo) {
		final int n = g.vertices().size();
		Collection<Set<V>> cliques = new ObjectArrayList<>(algo.maximalCliquesIter(g));

		Set<Pair<V, V>> edges = new HashSet<>();
		for (E e : g.edges()) {
			V u = g.edgeSource(e);
			V v = g.edgeTarget(e);
			edges.add(Pair.of(u, v));
			edges.add(Pair.of(v, u));
		}

		/* assert the returned cliques are actual maximal cliques */
		for (Set<V> clique : cliques)
			assertTrue(isMaximalClique(g, new ArrayList<>(clique), edges));

		if (n <= 24) {
			/* test all possible sub sets of vertices */
			Set<Set<V>> cliquesExpected = SubSets
					.stream(g.vertices())
					.filter(clique -> isMaximalClique(g, clique, edges))
					.map(ObjectOpenHashSet::new)
					.collect(toSet());

			Set<Set<V>> cliquesActual = new ObjectOpenHashSet<>(cliques);
			assertEquals(cliquesActual.size(), cliques.size());
			assertEquals(cliquesExpected, cliquesActual);
		}
	}

	private static <V> boolean isClique(List<V> clique, Set<Pair<V, V>> edges) {
		for (int i : range(clique.size())) {
			V u = clique.get(i);
			for (int j : range(i + 1, clique.size())) {
				V v = clique.get(j);
				if (!edges.contains(Pair.of(u, v)))
					return false;
			}
		}
		return true;
	};

	private static <V, E> boolean isMaximalClique(Graph<V, E> g, List<V> clique, Set<Pair<V, V>> edges) {
		if (!isClique(clique, edges))
			return false;
		Set<V> cliqueSet = new ObjectOpenHashSet<>(clique);
		cliqueAppendLoop: for (V u : g.vertices()) {
			if (cliqueSet.contains(u))
				continue;
			for (V v : clique)
				if (!edges.contains(Pair.of(u, v)))
					continue cliqueAppendLoop;
			return false;
		}
		return true;
	}

}
