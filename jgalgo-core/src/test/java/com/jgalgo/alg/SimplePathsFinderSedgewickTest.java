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
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

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
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];
			int target = vs[rand.nextInt(vs.length)];

			testSimplePaths(g, source, target, algo);
		});
	}

	private static void testSimplePaths(IntGraph g, int source, int target, SimplePathsFinder algo) {
		final int limit = 20;
		Set<IntList> paths = new HashSet<>();

		Iterator<IPath> pit = algo.findAllSimplePaths(g, source, target);
		if (!pit.hasNext())
			assertNull(IPath.findPath(g, source, target));
		for (; pit.hasNext();) {
			IPath p = pit.next();
			assertEquals(source, p.sourceInt());
			assertEquals(target, p.targetInt());
			assertTrue(p.isSimple());
			assertTrue(IPath.isPath(g, source, target, p.edges()));
			assertTrue(p.vertices().intStream().distinct().count() == p.vertices().size());
			assertTrue(paths.add(p.edges()));
			if (paths.size() >= limit)
				break;
		}
	}

}
