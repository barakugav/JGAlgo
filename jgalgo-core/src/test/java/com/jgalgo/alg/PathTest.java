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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

public class PathTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void testFindPath() {
		final long seed = 0x03afc698ec4c71ccL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		ShortestPathSingleSource validationAlgo = new ShortestPathSingleSourceDijkstra();
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(2048, 8192).repeat(4);
		tester.run((n, m) -> {
			boolean directed = rand.nextBoolean();
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];
			int target = vs[rand.nextInt(vs.length)];

			IPath actual = IPath.findPath(g, source, target);
			IPath expected = (IPath) validationAlgo.computeShortestPaths(g, null, source).getPath(target);
			if (expected == null) {
				assertNull(actual, "found non existing path");
			} else {
				assertNotNull(actual, "failed to found a path");
				assertEquals(expected.edges().size(), actual.edges().size(), "failed to find shortest path");

				assertEquals(source, actual.sourceInt());
				assertEquals(target, actual.targetInt());
				assertTrue(IPath.isPath(g, source, target, actual.edges()));

				boolean isSimpleExpected = actual.vertices().intStream().distinct().count() == actual.vertices().size();
				assertEquals(isSimpleExpected, actual.isSimple());
			}
		});
	}

	@Test
	public void testIsPathUndirected() {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v3, v4);
		int e4 = g.addEdge(v4, v1);
		int e5 = g.addEdge(v2, v4);

		assertTrue(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v2, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v3, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v4, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v3, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e3, e5)));
		assertFalse(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e5, e3, e4)));

		assertTrue(IPath.isPath(g, v1, v2, IntList.of(e1, e5, e3, e2)));
		assertFalse(IPath.isPath(g, v2, v2, IntList.of(e1, e5, e3, e2)));
		assertFalse(IPath.isPath(g, v4, v2, IntList.of(e1, e5, e3, e2)));
		assertFalse(IPath.isPath(g, v1, v3, IntList.of(e1, e5, e3, e2)));
		assertFalse(IPath.isPath(g, v1, v1, IntList.of(e1, e5, e3, e2)));
		assertFalse(IPath.isPath(g, v1, v2, IntList.of(e1, e5, e3, e5)));
		assertFalse(IPath.isPath(g, v1, v2, IntList.of(e1, e2, e5, e3, e2)));
	}

	@Test
	public void testIsPathDirected() {
		IntGraph g = IntGraph.newDirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v3, v4);
		int e4 = g.addEdge(v4, v1);
		int e5 = g.addEdge(v2, v4);

		assertTrue(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v2, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v3, v1, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v4, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v3, IntList.of(e1, e2, e3, e4)));
		assertFalse(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e3, e4, e1)));
		assertFalse(IPath.isPath(g, v1, v1, IntList.of(e1, e2, e5, e3, e4)));

		assertTrue(IPath.isPath(g, v1, v3, IntList.of(e1, e5, e4, e1, e2)));
		assertFalse(IPath.isPath(g, v3, v3, IntList.of(e1, e5, e4, e1, e2)));
		assertFalse(IPath.isPath(g, v2, v3, IntList.of(e1, e5, e4, e1, e2)));
		assertFalse(IPath.isPath(g, v1, v4, IntList.of(e1, e5, e4, e1, e2)));
		assertFalse(IPath.isPath(g, v1, v2, IntList.of(e1, e5, e4, e1, e2)));
		assertFalse(IPath.isPath(g, v1, v3, IntList.of(e1, e5, e4, e1, e2, e3)));
		assertFalse(IPath.isPath(g, v1, v3, IntList.of(e1, e5, e4, e1, e5, e2)));
	}

	@Test
	public void testIsSimple() {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int v4 = g.addVertex();
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v3, v4);
		int e4 = g.addEdge(v4, v1);
		int e5 = g.addEdge(v2, v4);

		assertFalse(IPath.newInstance(g, v1, v1, IntList.of(e1, e2, e3, e4)).isSimple());
		assertFalse(IPath.newInstance(g, v1, v2, IntList.of(e1, e5, e3, e2)).isSimple());
		assertTrue(IPath.newInstance(g, v1, v3, IntList.of(e1, e5, e3)).isSimple());
	}

}
