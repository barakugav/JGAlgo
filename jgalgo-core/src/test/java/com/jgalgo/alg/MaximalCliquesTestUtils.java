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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class MaximalCliquesTestUtils extends TestUtils {

	static void testRandGraphs(MaximalCliques algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(128, 256).repeat(12);
		tester.addPhase().withArgs(1024, 4096).repeat(2);
		tester.run((n, m) -> {
			IntGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			testAlgo(g, algo);
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void testAlgo(IntGraph g, MaximalCliques algo) {
		final int n = g.vertices().size();
		Collection<IntSet> cliques = (Collection) algo.findAllMaximalCliques(g);

		Set<IntIntPair> edges = new HashSet<>();
		for (int e : g.edges()) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			edges.add(IntIntPair.of(u, v));
			edges.add(IntIntPair.of(v, u));
		}

		/* assert the returned cliques are actual maximal cliques */
		for (IntCollection clique : cliques)
			assertTrue(isMaximalClique(g, new IntArrayList(clique), edges));

		if (n <= 24) {
			/* test all possible sub sets of vertices */
			Set<IntSet> cliquesExpected = new HashSet<>();
			int[] vertices = g.vertices().toIntArray();
			for (int bitmap = 1; bitmap < (1 << n); bitmap++) {
				IntList tempClique = new IntArrayList();
				for (int vIdx = 0; vIdx < n; vIdx++)
					if ((bitmap & (1 << vIdx)) != 0)
						tempClique.add(vertices[vIdx]);

				if (isClique(tempClique, edges) && isMaximalClique(g, tempClique, edges))
					cliquesExpected.add(new IntOpenHashSet(tempClique));
			}

			Set<IntSet> cliquesActual = new HashSet<>();
			for (IntCollection clique : cliques)
				cliquesActual.add(new IntOpenHashSet(clique));

			assertEquals(cliquesExpected, cliquesActual);
		}
	}

	private static boolean isClique(IntList clique, Set<IntIntPair> edges) {
		for (int s = clique.size(), i = 0; i < s; i++) {
			int u = clique.getInt(i);
			for (int j = i + 1; j < s; j++) {
				int v = clique.getInt(j);
				if (!edges.contains(IntIntPair.of(u, v)))
					return false;
			}
		}
		return true;
	};

	private static boolean isMaximalClique(IntGraph g, IntList clique, Set<IntIntPair> edges) {
		if (!isClique(clique, edges))
			return false;
		IntSet cliqueSet = new IntOpenHashSet(clique);
		cliqueAppendLoop: for (int u : g.vertices()) {
			if (cliqueSet.contains(u))
				continue;
			for (int v : clique)
				if (!edges.contains(IntIntPair.of(u, v)))
					continue cliqueAppendLoop;
			return false;
		}
		return true;
	}

}
