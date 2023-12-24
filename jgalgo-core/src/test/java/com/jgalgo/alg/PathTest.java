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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class PathTest extends TestBase {

	@Test
	public void vertices() {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int v3 = g.addVertexInt();
		int v4 = g.addVertexInt();
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v3, v4);
		int e4 = g.addEdge(v4, v1);
		int e5 = g.addEdge(v2, v4);

		assertEquals(IPath.newInstance(g, v1, v1, IntList.of(e1, e2, e3, e4)).vertices(), IntList.of(v1, v2, v3, v4));
		assertEquals(IPath.newInstance(g, v1, v2, IntList.of(e1, e5, e3, e2)).vertices(),
				IntList.of(v1, v2, v4, v3, v2));
		assertEquals(IPath.newInstance(g, v1, v2, IntList.of(e1, e5, e5)).vertices(), IntList.of(v1, v2, v4, v2));
		assertEquals(IPath.newInstance(g, v1, v3, IntList.of(e1, e5, e3)).vertices(), IntList.of(v1, v2, v4, v3));
	}

	@Test
	public void edges() {
		IndexGraph g = IndexGraph.newUndirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int e1 = g.addEdge(v1, v2);
		IntList l1 = new IntImmutableList(new int[] { e1 });
		IntList l2 = IntLists.unmodifiable(l1);
		IntList l3 = new IntArrayList(l1);
		assertTrue(IPath.newInstance(g, v1, v2, l1).edges() == l1);
		assertTrue(IPath.newInstance(g, v1, v2, l2).edges() == l2);
		assertTrue(IPath.newInstance(g, v1, v2, l3).edges() != l3);
	}

	@SuppressWarnings("boxing")
	@Test
	public void iterator() {
		final Random rand = new Random(0x6250eaa953cc030eL);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			GraphFactory<Integer, Integer> factory =
					intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
			Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();
			for (int n = 20 + rand.nextInt(20); g.vertices().size() < n;) {
				int v = rand.nextInt(1 + n * 2);
				if (!g.vertices().contains(v))
					g.addVertex(v);
			}
			for (int m = 60 + rand.nextInt(60); g.edges().size() < m;) {
				int u = Graphs.randVertex(g, rand);
				int v = Graphs.randVertex(g, rand);
				int e = rand.nextInt(1 + m * 2);
				if (!g.edges().contains(e))
					g.addEdge(u, v, e);
			}
			if (indexGraph)
				g = g.indexGraph();

			for (int i = 0; i < 100; i++) {
				IntList edges = new IntArrayList();
				IntList vertices = new IntArrayList();
				int source = Graphs.randVertex(g, rand);
				vertices.add(source);
				for (int len = rand.nextInt(10), u = source; edges.size() < len;) {
					IntList uEdges = new IntArrayList(g.outEdges(u));
					if (uEdges.isEmpty())
						break;
					int e = uEdges.getInt(rand.nextInt(uEdges.size()));
					u = g.edgeEndpoint(e, u);
					edges.add(e);
					vertices.add(u);
				}
				int target = vertices.getInt(vertices.size() - 1);
				IntList verticesFull = new IntArrayList(vertices);
				if (source == target && vertices.size() > 1)
					vertices.removeInt(vertices.size() - 1);
				Path<Integer, Integer> path = Path.newInstance(g, source, target, edges);

				assertEquals(edges, path.edges());
				assertEquals(vertices, path.vertices());
				assertEquals(source, path.source());
				assertEquals(target, path.target());
				int idx = 0;
				for (EdgeIter<Integer, Integer> it = path.edgeIter(); it.hasNext(); idx++) {
					int peek = it.peekNext();
					int e = it.next();
					int u = it.source();
					int v = it.target();
					int expectedE = edges.getInt(idx);
					int expectedU = verticesFull.getInt(idx);
					int expectedV = verticesFull.getInt(idx + 1);
					assertEquals(expectedE, e);
					assertEquals(expectedU, u);
					assertEquals(expectedV, v);
					assertEquals(peek, e);
				}

				assertEquals(edges.toString(), path.toString());
			}
		});
	}

	@Test
	public void findPath() {
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
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			findPath(g, validationAlgo, rand);
		});
	}

	private static <V, E> void findPath(Graph<V, E> g, ShortestPathSingleSource validationAlgo, Random rand) {
		V source = Graphs.randVertex(g, rand);
		V target = Graphs.randVertex(g, rand);

		Path<V, E> actual = Path.findPath(g, source, target);
		Path<V, E> expected = validationAlgo.computeShortestPaths(g, null, source).getPath(target);
		if (expected == null) {
			assertNull(actual, "found non existing path");
		} else {
			assertNotNull(actual, "failed to found a path");
			assertEquals(expected.edges().size(), actual.edges().size(), "failed to find shortest path");

			assertEquals(source, actual.source());
			assertEquals(target, actual.target());
			assertTrue(Path.isPath(g, source, target, actual.edges()));

			boolean isSimpleExpected = actual.vertices().stream().distinct().count() == actual.vertices().size();
			assertEqualsBool(isSimpleExpected, actual.isSimple());
		}
	}

	@Test
	public void isPathUndirected() {
		IntGraph g = IntGraph.newUndirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int v3 = g.addVertexInt();
		int v4 = g.addVertexInt();
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
	public void isPathDirected() {
		IntGraph g = IntGraph.newDirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int v3 = g.addVertexInt();
		int v4 = g.addVertexInt();
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
	public void isSimple() {
		foreachBoolConfig(largeGraph -> {
			IntGraph g = IntGraph.newUndirected();
			if (largeGraph)
				g.addVertices(range(1000));
			int v1 = g.addVertexInt();
			int v2 = g.addVertexInt();
			int v3 = g.addVertexInt();
			int v4 = g.addVertexInt();
			int e1 = g.addEdge(v1, v2);
			int e2 = g.addEdge(v2, v3);
			int e3 = g.addEdge(v3, v4);
			int e4 = g.addEdge(v4, v1);
			int e5 = g.addEdge(v2, v4);

			assertFalse(IPath.newInstance(g, v1, v1, IntList.of(e1, e2, e3, e4)).isSimple());
			assertFalse(IPath.newInstance(g, v1, v2, IntList.of(e1, e5, e3, e2)).isSimple());
			assertFalse(IPath.newInstance(g, v1, v2, IntList.of(e1, e5, e5)).isSimple());
			IPath path = IPath.newInstance(g, v1, v3, IntList.of(e1, e5, e3));
			assertTrue(path.isSimple());
			assertTrue(path.isSimple()); /* value should be cached */
		});
	}

	@Test
	public void reachableVertices() {
		final long seed = 0xb89cfbb5bfbc6989L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(312, 600).repeat(8);
		tester.run((n, m) -> {
			boolean directed = rand.nextBoolean();
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Integer source = Graphs.randVertex(g, rand);

			reachableVertices(g, source, rand);
		});
	}

	private static <V, E> void reachableVertices(Graph<V, E> g, V source, Random rand) {
		Set<V> reachableActual = Path.reachableVertices(g, source);
		Set<V> reachableExpected =
				g.vertices().stream().filter(v -> Path.findPath(g, source, v) != null).collect(Collectors.toSet());
		assertEquals(reachableExpected, reachableActual);
	}

}
