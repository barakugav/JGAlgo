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

package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PathTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void vertices() {
		foreachBoolConfig((intGraph, directed) -> {
			GraphFactory<Integer, Integer> factory =
					intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
			Graph<Integer, Integer> g = factory
					.setVertexBuilder(IdBuilderInt.defaultBuilder())
					.setEdgeBuilder(IdBuilderInt.defaultBuilder())
					.newGraph();
			int v1 = g.addVertex();
			int v2 = g.addVertex();
			int v3 = g.addVertex();
			int v4 = g.addVertex();
			int e1 = g.addEdge(v1, v2);
			int e2 = g.addEdge(v2, v3);
			int e3 = g.addEdge(v3, v4);
			int e4 = g.addEdge(v4, v1);
			int e5 = g.addEdge(v2, v4);

			assertEquals(IntList.of(v1, v2, v3, v4, v1),
					Path.valueOf(g, v1, v1, IntList.of(e1, e2, e3, e4)).vertices());
			assertEquals(IntList.of(v1, v2, v4, v1, v2),
					Path.valueOf(g, v1, v2, IntList.of(e1, e5, e4, e1)).vertices());
			if (!directed) {
				assertEquals(IntList.of(v1, v2, v4, v2), Path.valueOf(g, v1, v2, IntList.of(e1, e5, e5)).vertices());
				assertEquals(IntList.of(v1, v2, v4, v3), Path.valueOf(g, v1, v3, IntList.of(e1, e5, e3)).vertices());
			}

			assertEquals(IntList.of(v1, v2, v3, v4, v1),
					new ObjectArrayList<>(Path.verticesIter(g, v1, IntList.of(e1, e2, e3, e4))));
			assertEquals(IntList.of(v1, v2, v4, v1, v2),
					new ObjectArrayList<>(Path.verticesIter(g, v1, IntList.of(e1, e5, e4, e1))));
			if (!directed) {
				assertEquals(IntList.of(v1, v2, v4, v2),
						new ObjectArrayList<>(Path.verticesIter(g, v1, IntList.of(e1, e5, e5))));
				assertEquals(IntList.of(v1, v2, v4, v3),
						new ObjectArrayList<>(Path.verticesIter(g, v1, IntList.of(e1, e5, e3))));
			}

			Iterator<Integer> vIter1 = Path.verticesIter(g, v1, IntList.of(e1, e2, e3, e4));
			assertEquals(0, vIter1 instanceof IntIterator ? ((IntIterator) vIter1).skip(0)
					: JGAlgoUtils.objIterSkip(vIter1, 0));
			assertTrue(vIter1.hasNext());
			assertEquals(v1, vIter1.next());
			assertEquals(2, vIter1 instanceof IntIterator ? ((IntIterator) vIter1).skip(2)
					: JGAlgoUtils.objIterSkip(vIter1, 2));
			assertEquals(IntList.of(v4, v1), new ObjectArrayList<>(vIter1));

			Iterator<Integer> vIter2 = Path.verticesIter(g, v1, IntList.of(e1, e2, e3, e4));
			assertEquals(4, vIter2 instanceof IntIterator ? ((IntIterator) vIter2).skip(4)
					: JGAlgoUtils.objIterSkip(vIter2, 4));
			assertTrue(vIter2.hasNext());
			assertEquals(v1, vIter2.next());

			Iterator<Integer> vIter3 = Path.verticesIter(g, v1, IntList.of(e1, e2, e3, e4));
			assertEquals(5, vIter3 instanceof IntIterator ? ((IntIterator) vIter3).skip(5)
					: JGAlgoUtils.objIterSkip(vIter3, 5));
			assertFalse(vIter3.hasNext());

			Iterator<Integer> vIter4 = Path.verticesIter(g, v1, IntList.of(e1, e2, e3, e4));
			assertEquals(5, vIter3 instanceof IntIterator ? ((IntIterator) vIter4).skip(26)
					: JGAlgoUtils.objIterSkip(vIter4, 26));
			assertFalse(vIter4.hasNext());
			assertEquals(0, vIter3 instanceof IntIterator ? ((IntIterator) vIter4).skip(26)
					: JGAlgoUtils.objIterSkip(vIter4, 26));
		});
	}

	@Test
	public void edges() {
		IndexGraph g = IndexGraph.newUndirected();
		int v1 = g.addVertexInt();
		int v2 = g.addVertexInt();
		int e1 = g.addEdge(v1, v2);
		IntList l1 = Fastutil.list(e1);
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
					assertEquals(edges.getInt(idx), e);
					assertEquals(verticesFull.getInt(idx), u);
					assertEquals(verticesFull.getInt(idx + 1), v);
					assertEquals(peek, e);
				}

				/* test skip() */
				idx = 0;
				for (EdgeIter<Integer, Integer> it = path.edgeIter();;) {
					assertEqualsBool(idx < edges.size(), it.hasNext());
					if (!it.hasNext())
						break;
					if (rand.nextBoolean()) {
						int e = it.next();
						int u = it.source();
						int v = it.target();
						assertEquals(edges.getInt(idx), e);
						assertEquals(verticesFull.getInt(idx), u);
						assertEquals(verticesFull.getInt(idx + 1), v);
						idx++;

					} else {
						int skip = rand.nextInt(5);
						int expectedSkipped = Math.min(skip, edges.size() - idx);
						int skipped = it instanceof IEdgeIter ? ((IEdgeIter) it).skip(skip)
								: JGAlgoUtils.objIterSkip(it, skip);
						assertEquals(expectedSkipped, skipped);
						idx += skipped;
					}
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

	@SuppressWarnings("boxing")
	@Test
	public void equalsAndHashCode() {
		final Random rand = new Random(0x3c01797d32763bcL);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			Graph<Integer, Integer> g0 =
					GraphsTestUtils.randGraph(20, 60, directed, true, true, intGraph, rand.nextLong());
			g0 = indexGraph ? g0.indexGraph() : g0;

			Graph<Integer, Integer> g = g0.copy();
			Map<Integer, Integer> twinEdge = new Int2IntOpenHashMap();
			for (Integer e1 : g.edges()) {
				Integer e2;
				if (g instanceof IndexGraph) {
					e2 = Integer.valueOf(g.edges().size());
				} else {
					e2 = GraphsTestUtils.nonExistingEdgeNonNegative(g, rand);
				}
				g.addEdge(g.edgeSource(e1), g.edgeTarget(e1), e2);
				twinEdge.put(e1, e2);
				twinEdge.put(e2, e1);
			}

			for (int repeat = 0; repeat < 100; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				Integer source = path.source(), target = path.target();
				final boolean cycle = source.equals(target);

				BiConsumer<Path<Integer, Integer>, Path<Integer, Integer>> checkEqual = (p1, p2) -> {
					assertEquals(p1, p2);
					assertEquals(p2, p1);
					assertEquals(p1.hashCode(), p2.hashCode());
				};

				/* equal itself */
				assertEquals(path, path);
				assertEquals(path.hashCode(), path.hashCode());
				/* wrong type */
				assertFalse(path.equals(null));
				assertFalse(path.equals("wrong type"));

				/* Path with exactly same source, target and edges */
				checkEqual.accept(path, Path.valueOf(g, source, target, path.edges()));
				/* Path with same source and target, different edges */
				if (!path.edges().isEmpty()) {
					List<Integer> edges = new IntArrayList(path.edges());
					int idx = rand.nextInt(edges.size());
					edges.set(idx, twinEdge.get(edges.get(idx)));
					assertNotEquals(path, Path.valueOf(g, source, target, edges));
				}
				/* equal to reverse in undirected graph */
				if (!g.isDirected()) {
					IntArrayList edges = new IntArrayList(path.edges());
					IntArrays.reverse(edges.elements(), 0, edges.size());
					checkEqual.accept(path, Path.valueOf(g, target, source, edges));
				}
				/* reverse edges in undirected graphs, different edges */
				if (!g.isDirected() && !path.edges().isEmpty()) {
					IntArrayList edges = new IntArrayList(path.edges());
					IntArrays.reverse(edges.elements(), 0, edges.size());
					int idx = rand.nextInt(edges.size());
					edges.set(idx, twinEdge.get(edges.getInt(idx)).intValue());
					assertNotEquals(path, Path.valueOf(g, target, source, edges));
				}
				/* two cycle paths, one is a shifted version of the other */
				if (cycle && !path.edges().isEmpty()) {
					IntArrayList edges = new IntArrayList();
					int offset = rand.nextInt(path.edges().size());
					edges.addAll(path.edges().subList(offset, path.edges().size()));
					edges.addAll(path.edges().subList(0, offset));
					Integer newSource = path.vertices().get(offset);
					checkEqual.accept(path, Path.valueOf(g, newSource, newSource, edges));
				}
				/* two cycle paths, one is a shifted version of the other, one different edge */
				if (cycle && !path.edges().isEmpty()) {
					IntArrayList edges = new IntArrayList();
					int offset = rand.nextInt(path.edges().size());
					edges.addAll(path.edges().subList(offset, path.edges().size()));
					edges.addAll(path.edges().subList(0, offset));
					int idx = rand.nextInt(edges.size());
					edges.set(idx, twinEdge.get(edges.getInt(idx)).intValue());
					Integer newSource = path.vertices().get(offset);
					assertNotEquals(path, Path.valueOf(g, newSource, newSource, edges));
				}
				/* two cycle paths, one is a shifted reversed version of the other */
				if (cycle && !g.isDirected() && !path.edges().isEmpty()) {
					IntArrayList edges = new IntArrayList();
					int offset = rand.nextInt(path.edges().size());
					edges.addAll(path.edges().subList(offset, path.edges().size()));
					edges.addAll(path.edges().subList(0, offset));
					IntArrays.reverse(edges.elements(), 0, edges.size());
					Integer newSource = path.vertices().get(offset);
					checkEqual.accept(path, Path.valueOf(g, newSource, newSource, edges));
				}
				/* two cycle paths, one is a shifted reversed version of the other, one different edge */
				if (cycle && !g.isDirected() && !path.edges().isEmpty()) {
					IntArrayList edges = new IntArrayList();
					int offset = rand.nextInt(path.edges().size());
					edges.addAll(path.edges().subList(offset, path.edges().size()));
					edges.addAll(path.edges().subList(0, offset));
					IntArrays.reverse(edges.elements(), 0, edges.size());
					int idx = rand.nextInt(edges.size());
					edges.set(idx, twinEdge.get(edges.getInt(idx)).intValue());
					Integer newSource = path.vertices().get(offset);
					assertNotEquals(path, Path.valueOf(g, newSource, newSource, edges));
				}

				/* different graph */
				Path<Integer, Integer> path3 = Path.valueOf(g.copy(), source, target, path.edges());
				assertNotEquals(path, path3);

				/* different source */
				Optional<Integer> differentSourceEdge =
						g.inEdges(target).stream().filter(e -> !source.equals(g.edgeEndpoint(e, target))).findAny();
				if (differentSourceEdge.isPresent()) {
					Integer differentSource = g.edgeEndpoint(differentSourceEdge.get(), target);
					assertNotEquals(path, Path.valueOf(g, differentSource, target, List.of(differentSourceEdge.get())));
					if (!g.isDirected())
						assertNotEquals(path,
								Path.valueOf(g, target, differentSource, List.of(differentSourceEdge.get())));
				}

				/* different target */
				Optional<Integer> differentTargetEdge =
						g.outEdges(source).stream().filter(e -> !target.equals(g.edgeEndpoint(e, source))).findAny();
				if (differentTargetEdge.isPresent()) {
					Integer differentTarget = g.edgeEndpoint(differentTargetEdge.get(), source);
					assertNotEquals(path, Path.valueOf(g, source, differentTarget, List.of(differentTargetEdge.get())));
					if (!g.isDirected())
						assertNotEquals(path,
								Path.valueOf(g, differentTarget, source, List.of(differentTargetEdge.get())));
				}
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
	public void subPath() {
		final Random rand = new Random(0xd6e3f91be3f1f60bL);
		foreachBoolConfig((intGraph, indexGraph, directed) -> {
			Graph<Integer, Integer> g0 =
					GraphsTestUtils.randGraph(20, 60, directed, true, true, intGraph, rand.nextLong());
			Graph<Integer, Integer> g = indexGraph ? g0.indexGraph() : g0;

			for (int repeat = 0; repeat < 100; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);

				final boolean compareVertices = rand.nextBoolean();
				List<Integer> originalVertices = compareVertices ? path.vertices() : null;

				int size = path.edges().size();
				int from = size == 0 ? 0 : rand.nextInt(size);
				int to = from + rand.nextInt(size - from + 1);
				Path<Integer, Integer> subPath = path.subPath(from, to);

				List<Integer> expectedEdges = path.edges().subList(from, to);
				Integer expectedSource = path.vertices().get(from);
				Integer expectedTarget = path.vertices().get(to);
				Path<Integer, Integer> expectedPath = Path.valueOf(g, expectedSource, expectedTarget, expectedEdges);

				assertEquals(expectedEdges, subPath.edges());
				assertEquals(expectedSource, subPath.source());
				assertEquals(expectedTarget, subPath.target());
				assertEquals(expectedPath, subPath);

				if (compareVertices) {
					List<Integer> expectedVertices = originalVertices.subList(from, to + 1);
					assertEquals(expectedVertices, subPath.vertices());
				}
			}
			for (int repeat = 0; repeat < 3; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				int size = path.edges().size();
				int from = -1 - rand.nextInt(3);
				int to = rand.nextInt(size + 1);
				assertThrows(IndexOutOfBoundsException.class, () -> path.subPath(from, to));
			}
			for (int repeat = 0; repeat < 3; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				int size = path.edges().size();
				int from = size == 0 ? 0 : rand.nextInt(size);
				int to = size + 1 + rand.nextInt(3);
				assertThrows(IndexOutOfBoundsException.class, () -> path.subPath(from, to));
			}
			for (int repeat = 0; repeat < 3; repeat++) {
				Path<Integer, Integer> path = randPath(g, rand);
				int size = path.edges().size();
				int to = size == 0 ? 0 : rand.nextInt(size);
				int from = to + 1 + rand.nextInt(size - to + 1);
				assertThrows(RuntimeException.class, () -> path.subPath(from, to));
			}
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
