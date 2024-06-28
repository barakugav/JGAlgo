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
package com.jgalgo.alg.closure;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ClosuresEnumeratorSchrageBakerTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0xe085abe7fb5fb576L;
		randGraphs(new ClosuresEnumeratorSchrageBaker(), false, seed);
	}

	@Test
	public void randGraphsDag() {
		final long seed = 0xeb173b7c1badf134L;
		randGraphs(new ClosuresEnumeratorSchrageBaker(), true, seed);
	}

	private static void randGraphs(ClosuresEnumerator algo, boolean dag, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 2).repeat(32);
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 20).repeat(8);
		tester.addPhase().withArgs(16, 32).repeat(8);
		tester.addPhase().withArgs(32, 52).repeat(4);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g;
			if (dag) {
				g = GraphsTestUtils.randDag(n, m, seedGen.nextSeed());
			} else {
				g = GraphsTestUtils.randGraph(n, m, true, seedGen.nextSeed());
			}
			g = maybeIndexGraph(g, rand);

			testClosuresAlgo(g, algo);
		});
	}

	private static <V, E> void testClosuresAlgo(Graph<V, E> g, ClosuresEnumerator algo) {
		List<Set<V>> closures = new ObjectArrayList<>(algo.closuresIter(g));

		for (Set<V> closure : closures)
			assertTrue(ClosuresEnumerator.isClosure(g, closure));

		if (g.vertices().size() <= 16) {
			Set<Set<V>> actual = new HashSet<>(closures);
			Set<Set<V>> expected = new HashSet<>(findAllClosures(g));
			assertEquals(expected, actual);
		}
	}

	private static <V, E> List<Set<V>> findAllClosures(Graph<V, E> g) {
		return SubSets
				.stream(g.vertices())
				.filter(c -> !c.isEmpty())
				.map(ObjectOpenHashSet::new)
				.filter(c -> Path.reachableVertices(g, c.iterator()).stream().allMatch(c::contains))
				.collect(toList());
	}

	@Test
	public void testBuilderDefaultImpl() {
		ClosuresEnumerator alg = ClosuresEnumerator.newInstance();
		assertEquals(ClosuresEnumeratorSchrageBaker.class, alg.getClass());
	}

	@SuppressWarnings("boxing")
	@Test
	public void isClosure() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = intGraph ? IntGraph.newDirected() : Graph.newDirected();
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addEdge(1, 0, 0);
			g.addEdge(1, 2, 1);

			assertTrue(ClosuresEnumerator.isClosure(g, IntSet.of()));
			assertTrue(ClosuresEnumerator.isClosure(g, IntSet.of(0)));
			assertTrue(ClosuresEnumerator.isClosure(g, IntSet.of(2)));
			assertTrue(ClosuresEnumerator.isClosure(g, IntSet.of(0, 2)));
			assertFalse(ClosuresEnumerator.isClosure(g, IntSet.of(1)));
			assertFalse(ClosuresEnumerator.isClosure(g, IntSet.of(0, 1)));
			assertFalse(ClosuresEnumerator.isClosure(g, IntSet.of(1, 2)));
			assertTrue(ClosuresEnumerator.isClosure(g, IntSet.of(0, 1, 2)));
		});
	}

	@Test
	public void undirectedNotSupported() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 0, 0);
		g.addEdge(1, 2, 1);

		assertThrows(IllegalArgumentException.class,
				() -> new ObjectArrayList<>(ClosuresEnumerator.newInstance().closuresIter(g)));
	}

}
