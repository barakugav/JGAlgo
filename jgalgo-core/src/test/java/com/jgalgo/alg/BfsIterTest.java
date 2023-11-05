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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class BfsIterTest extends TestBase {

	@Test
	public void testBfsConnected() {
		final long seed = 0xa782852da2497b7fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(2048, 8192).repeat(4);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(true).build();
			testBfsConnected(g, seedGen.nextSeed());
		});
	}

	private static <V, E> void testBfsConnected(Graph<V, E> g, long seed) {
		Random rand = new Random(seed);
		final int n = g.vertices().size();
		List<V> vs = new ArrayList<>(g.vertices());
		V source = vs.get(rand.nextInt(n));

		Set<V> visited = new ObjectOpenHashSet<>(n);
		for (Bfs.Iter<V, E> it = Bfs.newInstance(g, source); it.hasNext();) {
			V v = it.next();
			E e = it.lastEdge();
			assertFalse(visited.contains(v), "already visited vertex " + v);
			if (!v.equals(source))
				assertTrue(g.edgeEndpoint(e, g.edgeEndpoint(e, v)).equals(v), "v is not an endpoint of inEdge");
			visited.add(v);
		}

		for (V v : g.vertices())
			assertTrue(visited.contains(v));

		/* run BFS again without calling .hasNext() */
		Set<V> visited2 = new ObjectOpenHashSet<>();
		Bfs.Iter<V, E> it = Bfs.newInstance(g, source);
		for (int s = visited.size(); s-- > 0;) {
			V v = it.next();
			E e = it.lastEdge();
			assertFalse(visited2.contains(v), "already visited vertex " + v);
			if (!v.equals(source))
				assertTrue(g.edgeEndpoint(e, g.edgeEndpoint(e, v)).equals(v), "v is not an endpoint of inEdge");
			visited2.add(v);
		}
		assert !it.hasNext();
	}

}
