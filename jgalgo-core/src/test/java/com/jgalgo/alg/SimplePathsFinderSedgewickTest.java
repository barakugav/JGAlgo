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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;

public class SimplePathsFinderSedgewickTest extends TestBase {

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0xeaab684c5d81dc5aL;
		testRandGraphs(new SimplePathsFinderSedgewick(), false, seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0x478625aebbd87022L;
		testRandGraphs(new SimplePathsFinderSedgewick(), true, seed);
	}

	private static void testRandGraphs(SimplePathsFinder algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(23, 40).repeat(128);
		tester.addPhase().withArgs(23, 55).repeat(128);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			Integer source = Graphs.randVertex(g, rand);
			Integer target = Graphs.randVertex(g, rand);

			testSimplePaths(g, source, target, algo);
		});
	}

	private static <V, E> void testSimplePaths(Graph<V, E> g, V source, V target, SimplePathsFinder algo) {
		final int limit = 20;
		Set<List<E>> paths = new HashSet<>();

		Iterator<Path<V, E>> pit = algo.findAllSimplePaths(g, source, target);
		if (!pit.hasNext())
			assertNull(Path.findPath(g, source, target));
		for (; pit.hasNext();) {
			Path<V, E> p = pit.next();
			assertEquals(source, p.source());
			assertEquals(target, p.target());
			assertTrue(p.isSimple());
			assertTrue(Path.isPath(g, source, target, p.edges()));
			assertTrue(p.vertices().stream().distinct().count() == p.vertices().size());
			assertTrue(paths.add(p.edges()));
			if (paths.size() >= limit)
				break;
		}
	}

}
