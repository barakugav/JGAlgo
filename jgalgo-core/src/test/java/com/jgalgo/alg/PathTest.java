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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IdBuilderInt;
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

		assertEquals(IPath.valueOf(g, v1, v1, IntList.of(e1, e2, e3, e4)).vertices(), IntList.of(v1, v2, v3, v4));
		assertEquals(IPath.valueOf(g, v1, v2, IntList.of(e1, e5, e3, e2)).vertices(), IntList.of(v1, v2, v4, v3, v2));
		assertEquals(IPath.valueOf(g, v1, v2, IntList.of(e1, e5, e5)).vertices(), IntList.of(v1, v2, v4, v2));
		assertEquals(IPath.valueOf(g, v1, v3, IntList.of(e1, e5, e3)).vertices(), IntList.of(v1, v2, v4, v3));
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
		assertTrue(IPath.valueOf(g, v1, v2, l1).edges() == l1);
		assertTrue(IPath.valueOf(g, v1, v2, l2).edges() == l2);
		assertTrue(IPath.valueOf(g, v1, v2, l3).edges() != l3);
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
			g = indexGraph ? g.indexGraph() : g;

			for (int repeat = 0; repeat < 100; repeat++) {
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
				Path<Integer, Integer> path = Path.valueOf(g, source, target, edges);

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
			}
		});
	}

	@Test
	public void isCycle() {
		final Random rand = new Random(0x8317473360e54374L);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			Graph<Integer, Integer> g0 =
					GraphsTestUtils.randGraph(20, 60, directed, true, true, intGraph, rand.nextLong());
			Graph<Integer, Integer> g = indexGraph ? g0.indexGraph() : g0;

			for (int repeat = 0; repeat < 100; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				Integer source = path.source(), target = path.target();
				assertEqualsBool(source.equals(target), path.isCycle());
			}
		});
	}

	@Test
	public void graph() {
		{
			IndexGraph g = IndexGraph.newUndirected();
			int v1 = g.addVertexInt();
			int v2 = g.addVertexInt();
			int e1 = g.addEdge(v1, v2);
			assertTrue(IPath.valueOf(g, v1, v2, IntList.of(e1)).graph() == g);
		}
		{
			IntGraph g = IntGraph.newUndirected();
			int v1 = g.addVertexInt();
			int v2 = g.addVertexInt();
			int e1 = g.addEdge(v1, v2);
			assertTrue(IPath.valueOf(g, v1, v2, IntList.of(e1)).graph() == g);
		}
		{
			Graph<Integer, Integer> g = GraphFactory
					.<Integer, Integer>undirected()
					.setVertexFactory(IdBuilderInt.defaultFactory())
					.setEdgeFactory(IdBuilderInt.defaultFactory())
					.newGraph();
			Integer v1 = g.addVertex();
			Integer v2 = g.addVertex();
			Integer e1 = g.addEdge(v1, v2);
			assertTrue(Path.valueOf(g, v1, v2, List.of(e1)).graph() == g);
		}
	}

	private static Path<Integer, Integer> randPath(Graph<Integer, Integer> g, Random rand) {
		List<Integer> edges = new IntArrayList();
		Integer source = Graphs.randVertex(g, rand);
		Integer target = source;
		final int len = rand.nextInt(10);
		for (Integer u = source; edges.size() < len;) {
			List<Integer> uEdges = new IntArrayList(g.outEdges(u));
			if (uEdges.isEmpty())
				break;
			Integer e = uEdges.get(rand.nextInt(uEdges.size()));
			u = g.edgeEndpoint(e, u);
			edges.add(e);
			target = u;
		}
		return Path.valueOf(g, source, target, edges);
	}

	@Test
	public void toStringTest() {
		final Random rand = new Random(0x9f4f3293219117e0L);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			Graph<Integer, Integer> g =
					GraphsTestUtils.randGraph(20, 60, directed, true, true, intGraph, rand.nextLong());
			g = indexGraph ? g.indexGraph() : g;

			for (int repeat = 0; repeat < 100; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				assertEquals(path.edges().toString(), path.toString());
			}
		});
	}

	@Test
	public void equalsAndHashCode() {
		final Random rand = new Random(0x3c01797d32763bcL);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			Graph<Integer, Integer> g0 =
					GraphsTestUtils.randGraph(20, 60, directed, true, true, intGraph, rand.nextLong());
			Graph<Integer, Integer> g = indexGraph ? g0.indexGraph() : g0;

			for (int repeat = 0; repeat < 100; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				Integer source = path.source(), target = path.target();

				/* equal itself */
				assertEquals(path, path);
				assertEquals(path.hashCode(), path.hashCode());
				/* wrong type */
				assertFalse(path.equals("wrong type"));

				/* equal paths */
				Path<Integer, Integer> path2 = Path.valueOf(g, source, target, path.edges());
				assertEquals(path, path2);
				assertEquals(path2, path);
				assertEquals(path.hashCode(), path2.hashCode());

				/* different graph */
				Path<Integer, Integer> path3 = Path.valueOf(g.copy(), source, target, path.edges());
				assertNotEquals(path, path3);

				/* different source */
				Optional<Integer> differentSourceEdge =
						g.inEdges(target).stream().filter(e -> !source.equals(g.edgeEndpoint(e, target))).findAny();
				if (differentSourceEdge.isPresent()) {
					Integer differentSource = g.edgeEndpoint(differentSourceEdge.get(), target);
					assertNotEquals(path, Path.valueOf(g, differentSource, target, List.of(differentSourceEdge.get())));
				}

				/* different target */
				Optional<Integer> differentTargetEdge =
						g.outEdges(source).stream().filter(e -> !target.equals(g.edgeEndpoint(e, source))).findAny();
				if (differentTargetEdge.isPresent()) {
					Integer differentTarget = g.edgeEndpoint(differentTargetEdge.get(), source);
					assertNotEquals(path, Path.valueOf(g, source, differentTarget, List.of(differentTargetEdge.get())));
				}

				/* different edges */
				if (path.edges().isEmpty())
					continue;
				Graph<Integer, Integer> g2 =
						IntGraphFactory.newInstance(g.isDirected()).allowSelfEdges().allowParallelEdges().newCopyOf(g);
				g2.removeEdge(path.edges().get(rand.nextInt(path.edges().size())));
				Path<Integer, Integer> path4 = Path.findPath(g2, source, target);
				if (path4 != null)
					assertNotEquals(path, Path.valueOf(g, source, target, path4.edges()));
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

			assertFalse(IPath.valueOf(g, v1, v1, IntList.of(e1, e2, e3, e4)).isSimple());
			assertFalse(IPath.valueOf(g, v1, v2, IntList.of(e1, e5, e3, e2)).isSimple());
			assertFalse(IPath.valueOf(g, v1, v2, IntList.of(e1, e5, e5)).isSimple());
			IPath path = IPath.valueOf(g, v1, v3, IntList.of(e1, e5, e3));
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
