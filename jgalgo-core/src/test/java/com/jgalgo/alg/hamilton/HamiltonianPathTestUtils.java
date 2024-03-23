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
package com.jgalgo.alg.hamilton;

import static com.jgalgo.internal.util.Range.range;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.path.Path;
import com.jgalgo.alg.path.SimplePathsEnumerator;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class HamiltonianPathTestUtils extends TestBase {

	static void hamiltonianPaths(HamiltonianPathAlgo algo, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(0x6df6d116b7e2e16cL);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		if (directed) {
			tester.addPhase().withArgs(3, 4).repeat(128);
			tester.addPhase().withArgs(4, 12).repeat(64);
			tester.addPhase().withArgs(8, 34).repeat(8);
			tester.addPhase().withArgs(16, 50).repeat(16);
			tester.addPhase().withArgs(23, 70).repeat(8);
		} else {
			tester.addPhase().withArgs(3, 4).repeat(64);
			tester.addPhase().withArgs(4, 12).repeat(32);
			tester.addPhase().withArgs(8, 18).repeat(8);
			tester.addPhase().withArgs(16, 32).repeat(8);
		}
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			checkHamiltonianPaths(g, algo);
		});

		IntGraph g = IntGraph.newDirected();
		assertEquals(Set.of(), collectAndCheckNoDups(algo.hamiltonianPathsIter(g)));

		Integer v = Integer.valueOf(g.addVertexInt());
		assertEquals(Set.of(Path.valueOf(g, v, v, List.of())), collectAndCheckNoDups(algo.hamiltonianPathsIter(g)));
	}

	static void hamiltonianPathsWithSourceTarget(HamiltonianPathAlgo algo, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(0xffb81c86b8450bcbL);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		if (directed) {
			tester.addPhase().withArgs(3, 4).repeat(128);
			tester.addPhase().withArgs(4, 12).repeat(64);
			tester.addPhase().withArgs(8, 36).repeat(8);
			tester.addPhase().withArgs(16, 70).repeat(16);
			tester.addPhase().withArgs(23, 90).repeat(8);
		} else {
			tester.addPhase().withArgs(3, 4).repeat(128);
			tester.addPhase().withArgs(4, 12).repeat(64);
			tester.addPhase().withArgs(8, 24).repeat(8);
			tester.addPhase().withArgs(16, 36).repeat(16);
		}
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Integer source = Graphs.randVertex(g, rand);
			Integer target = Graphs.randVertex(g, rand);
			checkHamiltonianPaths(g, source, target, algo);
		});

		IntGraph g = IntGraph.newDirected();
		Integer v = Integer.valueOf(g.addVertexInt());
		assertEquals(Set.of(Path.valueOf(g, v, v, List.of())),
				collectAndCheckNoDups(algo.hamiltonianPathsIter(g, v, v)));
	}

	static void hamiltonianCycles(HamiltonianPathAlgo algo, boolean directed) {
		final SeedGenerator seedGen = new SeedGenerator(0xc43f23f7ea546303L);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		if (directed) {
			tester.addPhase().withArgs(3, 4).repeat(128);
			tester.addPhase().withArgs(4, 12).repeat(64);
			tester.addPhase().withArgs(8, 36).repeat(8);
			tester.addPhase().withArgs(16, 60).repeat(16);
			tester.addPhase().withArgs(23, 88).repeat(8);
		} else {
			tester.addPhase().withArgs(3, 4).repeat(128);
			tester.addPhase().withArgs(4, 12).repeat(64);
			tester.addPhase().withArgs(8, 24).repeat(8);
			tester.addPhase().withArgs(16, 36).repeat(16);
		}
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			checkHamiltonianCycles(g, algo);
		});

		IntGraph g = IntGraph.newDirected();
		assertEquals(Set.of(), collectAndCheckNoDups(algo.hamiltonianCyclesIter(g)));

		Integer v = Integer.valueOf(g.addVertexInt());
		assertEquals(Set.of(Path.valueOf(g, v, v, List.of())), collectAndCheckNoDups(algo.hamiltonianCyclesIter(g)));
	}

	private static void checkHamiltonianPaths(Graph<Integer, Integer> g, HamiltonianPathAlgo algo) {
		Set<Path<Integer, Integer>> hamiltonianPaths = collectAndCheckNoDups(algo.hamiltonianPathsIter(g));

		Optional<Path<Integer, Integer>> hamiltonianPath = algo.hamiltonianPath(g);
		if (hamiltonianPaths.isEmpty()) {
			assertTrue(hamiltonianPath.isEmpty());
		} else {
			assertTrue(hamiltonianPath.isPresent());
			assertTrue(hamiltonianPaths.contains(hamiltonianPath.get()));
		}

		final int n = g.vertices().size();
		for (Path<Integer, Integer> path : hamiltonianPaths) {
			assertEquals(n, path.vertices().size());
			assertEquals(n, new IntOpenHashSet(path.vertices()).size());
			assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, path.edges()));
		}

		if (n <= 20) {
			Set<Path<Integer, Integer>> expectedPaths = expectedHamiltonianPaths(g);
			assertEquals(expectedPaths, hamiltonianPaths);
		}
	}

	private static void checkHamiltonianPaths(Graph<Integer, Integer> g, Integer source, Integer target,
			HamiltonianPathAlgo algo) {
		Set<Path<Integer, Integer>> hamiltonianPaths =
				collectAndCheckNoDups(algo.hamiltonianPathsIter(g, source, target));

		Optional<Path<Integer, Integer>> hamiltonianPath = algo.hamiltonianPath(g, source, target);
		if (hamiltonianPaths.isEmpty()) {
			assertTrue(hamiltonianPath.isEmpty());
		} else {
			assertTrue(hamiltonianPath.isPresent());
			assertTrue(hamiltonianPaths.contains(hamiltonianPath.get()));
		}

		final int n = g.vertices().size();
		for (Path<Integer, Integer> path : hamiltonianPaths) {
			assertEquals(source, path.source());
			assertEquals(target, path.target());
			assertEquals(n + (path.isCycle() ? 1 : 0), path.vertices().size());
			assertEquals(n, new IntOpenHashSet(path.vertices()).size());
			assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, path.edges()));
		}

		if (n <= 20) {
			Set<Path<Integer, Integer>> expectedPaths = expectedHamiltonianPaths(g, source, target);
			assertEquals(expectedPaths, hamiltonianPaths);
		}
	}

	private static void checkHamiltonianCycles(Graph<Integer, Integer> g, HamiltonianPathAlgo algo) {
		Set<Path<Integer, Integer>> hamiltonianCycles = collectAndCheckNoDups(algo.hamiltonianCyclesIter(g));

		Optional<Path<Integer, Integer>> hamiltonianCycle = algo.hamiltonianCycle(g);
		if (hamiltonianCycles.isEmpty()) {
			assertTrue(hamiltonianCycle.isEmpty());
		} else {
			assertTrue(hamiltonianCycle.isPresent());
			assertTrue(hamiltonianCycles.contains(hamiltonianCycle.get()));
		}

		final int n = g.vertices().size();
		for (Path<Integer, Integer> cycle : hamiltonianCycles) {
			assertTrue(cycle.isCycle());
			assertEquals(n + 1, cycle.vertices().size());
			assertEquals(n, new IntOpenHashSet(cycle.vertices()).size());
			assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, cycle.edges()));
		}

		if (n <= 20) {
			Set<Path<Integer, Integer>> expectedCycles = expectedHamiltonianCycles(g);
			assertEquals(expectedCycles, hamiltonianCycles);
		}
	}

	private static Set<Path<Integer, Integer>> expectedHamiltonianPaths(Graph<Integer, Integer> g) {
		final int n = g.vertices().size();
		if (n == 0)
			return Set.of();

		Stream<Pair<Integer, Integer>> sourcesTargets;
		if (g.isDirected()) {
			sourcesTargets = g
					.vertices()
					.stream()
					.flatMap(source -> g.vertices().stream().map(target -> Pair.of(source, target)));
		} else {
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			sourcesTargets = range(n)
					.intStream()
					.boxed()
					.flatMap(sIdx -> range(n).mapToObj(tIdx -> IntIntPair.of(sIdx.intValue(), tIdx)))
					.filter(p -> p.firstInt() < p.secondInt())
					.map(p -> Pair.of(viMap.indexToId(p.firstInt()), viMap.indexToId(p.secondInt())));
		}
		return sourcesTargets
				.filter(st -> !st.first().equals(st.second()))
				.flatMap(st -> IterTools
						.stream(SimplePathsEnumerator.newInstance().simplePathsIter(g, st.first(), st.second()))
						.filter(p -> p.edges().size() == n - 1))
				.collect(toSet());
	}

	private static Set<Path<Integer, Integer>> expectedHamiltonianPaths(Graph<Integer, Integer> g, Integer source,
			Integer target) {
		if (source.equals(target))
			return expectedHamiltonianCycles(g);

		final int n = g.vertices().size();
		return IterTools
				.stream(SimplePathsEnumerator.newInstance().simplePathsIter(g, source, target))
				.filter(p -> p.edges().size() == n - 1)
				.collect(toSet());
	}

	private static Set<Path<Integer, Integer>> expectedHamiltonianCycles(Graph<Integer, Integer> g) {
		GraphBuilder<Integer, Integer> gb = GraphBuilder.newInstance(g.isDirected());
		gb.addVertices(g.vertices());
		gb.addEdges(EdgeSet.allOf(g));
		Integer x1 = g.vertices().iterator().next();
		Integer x2;
		Integer s, t;
		final Random rand = new Random(0x2fad559665bb30faL);
		gb.addVertex(x2 = nonExistingIntNonNegative(gb.vertices(), rand));
		gb.addVertex(s = nonExistingIntNonNegative(gb.vertices(), rand));
		gb.addVertex(t = nonExistingIntNonNegative(gb.vertices(), rand));
		Map<Integer, Integer> x2ToX1 = new Int2IntOpenHashMap();
		for (Integer x1Edge : g.inEdges(x1)) {
			Integer v = g.edgeEndpoint(x1Edge, x1);
			Integer x2Edge = nonExistingIntNonNegative(gb.edges(), rand);
			gb.addEdge(v, x2, x2Edge);
			x2ToX1.put(x2Edge, x1Edge);
		}
		gb.addEdge(s, x1, nonExistingIntNonNegative(gb.edges(), rand));
		gb.addEdge(x2, t, nonExistingIntNonNegative(gb.edges(), rand));

		Graph<Integer, Integer> g2 = gb.build();
		final int n2 = g2.vertices().size();

		Stream<Path<Integer, Integer>> res = IterTools
				.stream(SimplePathsEnumerator.newInstance().simplePathsIter(g2, s, t))
				.filter(p -> p.edges().size() == n2 - 1)
				.map(p2 -> {
					List<Integer> edges2 = p2.edges();
					List<Integer> p1 = new ArrayList<>();
					p1.addAll(edges2.subList(1, edges2.size() - 2));
					p1.add(x2ToX1.get(edges2.get(edges2.size() - 2)));
					return Path.valueOf(g, x1, x1, p1);
				});
		if (!g.isDirected())
			res = res.filter(p -> {
				List<Integer> edges = p.edges();
				return edges.size() <= 2 || edges.get(0).intValue() <= edges.get(edges.size() - 1).intValue();
			});
		return res.collect(toSet());
	}

	private static <T> Set<T> collectAndCheckNoDups(Iterator<T> iter) {
		Set<T> set = new ObjectOpenHashSet<>();
		while (iter.hasNext()) {
			T next = iter.next();
			assertTrue(set.add(next));
		}
		return set;
	}

	@Test
	public void isHamiltonianPathDirected() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1, 10);
		g.addEdge(1, 2, 20);
		g.addEdge(1, 0, 25);
		g.addEdge(2, 0, 30);
		g.addEdge(2, 1, 35);

		assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20)));
		assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20, 30)));

		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 10, 20)));

		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of()));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20, 35)));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 25)));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 25, 10)));
	}

	@Test
	public void isHamiltonianPathUndirected() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1, 10);
		g.addEdge(1, 2, 20);
		g.addEdge(1, 0, 25);
		g.addEdge(2, 0, 30);
		g.addEdge(2, 1, 35);

		assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20)));
		assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20, 30)));

		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 10, 20)));

		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of()));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 20, 35)));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 25)));
		assertFalse(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of(10, 25, 10)));
	}

	@Test
	public void isHamiltonianPathEmptyGraph() {
		IntGraph g = IntGraph.newDirected();
		assertTrue(HamiltonianPathAlgo.isHamiltonianPath(g, IntList.of()));
	}

	@Test
	public void newInstance() {
		HamiltonianPathAlgo algo = HamiltonianPathAlgo.newInstance();
		assertTrue(algo instanceof HamiltonianPathRubin);
	}

}
