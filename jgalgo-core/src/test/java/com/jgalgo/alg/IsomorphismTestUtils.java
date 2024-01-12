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
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.Permutations;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class IsomorphismTestUtils extends TestUtils {

	static void graphsIsomorphism(IsomorphismTester algo, IsomorphismType type, boolean directed) {
		final Random rand = new Random(0xe382dc68ec73aa85L);
		PhasedTester tester = new PhasedTester();
		switch (type) {
			case Full:
				tester.addPhase().withArgs(4, 8).repeat(128);
				tester.addPhase().withArgs(8, 10).repeat(12);
				tester.addPhase().withArgs(16, 32).repeat(128);
				tester.addPhase().withArgs(16, 18).repeat(128);
				tester.addPhase().withArgs(19, 39).repeat(64);
				tester.addPhase().withArgs(23, 52).repeat(32);
				tester.addPhase().withArgs(23, 26).repeat(32);
				tester.addPhase().withArgs(64, 256).repeat(20);
				tester.addPhase().withArgs(80, 400).repeat(1);
				break;
			case InducedSubGraph:
				tester.addPhase().withArgs(4, 8).repeat(128);
				tester.addPhase().withArgs(8, 10).repeat(12);
				tester.addPhase().withArgs(16, 32).repeat(128);
				tester.addPhase().withArgs(16, 18).repeat(128);
				tester.addPhase().withArgs(19, 39).repeat(64);
				tester.addPhase().withArgs(23, 52).repeat(32);
				tester.addPhase().withArgs(23, 26).repeat(32);
				break;
			case SubGraph:
				tester.addPhase().withArgs(4, 8).repeat(128);
				tester.addPhase().withArgs(8, 10).repeat(8);
				tester.addPhase().withArgs(16, 32).repeat(32);
				tester.addPhase().withArgs(16, 20).repeat(8);
				tester.addPhase().withArgs(19, 39).repeat(4);
				if (directed) {
					tester.addPhase().withArgs(23, 52).repeat(8);
					tester.addPhase().withArgs(23, 32).repeat(2);
				}
				break;
		}
		tester.run((n, m) -> {
			Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> graphs =
					randIsomorphicGraphs(n, m, directed, type, rand.nextLong());
			Graph<Integer, Integer> g1 = graphs.left(), g2 = graphs.second();
			g1 = maybeIndexGraph(g1, rand);
			g2 = maybeIndexGraph(g2, rand);
			graphsIsomorphism(g1, g2, algo, type, rand.nextLong());
		});
	}

	@SuppressWarnings("boxing")
	private static void graphsIsomorphism(Graph<Integer, Integer> g1, Graph<Integer, Integer> g2,
			IsomorphismTester algo, IsomorphismType type, long seed) {
		final Random rand = new Random(seed);

		/* isomorphicMapping() */
		Optional<IsomorphismMapping<Integer, Integer, Integer, Integer>> mappingOptional;
		if (type == IsomorphismType.Full && rand.nextBoolean()) {
			mappingOptional = algo.isomorphicMapping(g1, g2);
		} else {
			mappingOptional = algo.isomorphicMapping(g1, g2, type);
		}
		assertTrue(mappingOptional.isPresent());
		{
			IsomorphismMapping<Integer, Integer, Integer, Integer> mapping = mappingOptional.get();
			checkMapping(mapping, type, null, null, rand);

			assertEquals(mapping.sourceGraph().vertices().stream().map(v1 -> {
				Integer v2 = mapping.mapVertex(v1);
				return "" + v1 + ":" + v2;
			}).collect(Collectors.joining(", ", "{", "}")), mapping.toString());

			assertEquals(mapping
					.sourceGraph()
					.vertices()
					.stream()
					.filter(vertex -> mapping.mapVertex(vertex) != null)
					.collect(toSet()), mapping.mappedVertices());
			assertEquals(mapping
					.sourceGraph()
					.edges()
					.stream()
					.filter(edge -> mapping.mapEdge(edge) != null)
					.collect(toSet()), mapping.mappedEdges());
			for (int i = 0; i < 5; i++) {
				Integer v = Graphs.randVertex(mapping.sourceGraph(), rand);
				assertEqualsBool(mapping.mapVertex(v) != null, mapping.mappedVertices().contains(v));
			}
			for (int i = 0; i < 5; i++) {
				Integer e = Graphs.randEdge(mapping.sourceGraph(), rand);
				assertEqualsBool(mapping.mapEdge(e) != null, mapping.mappedEdges().contains(e));
			}
			for (int i = 0; i < 5; i++) {
				int v = rand.nextInt();
				if (!mapping.sourceGraph().vertices().contains(v))
					assertFalse(mapping.mappedVertices().contains(v));
			}
			for (int i = 0; i < 5; i++) {
				int e = rand.nextInt();
				if (!mapping.sourceGraph().edges().contains(e))
					assertFalse(mapping.mappedEdges().contains(e));
			}

			assertEquals(mapping
					.targetGraph()
					.vertices()
					.stream()
					.filter(vertex -> mapping.inverse().mapVertex(vertex) != null)
					.collect(toSet()), mapping.inverse().mappedVertices());
			assertEquals(mapping
					.targetGraph()
					.edges()
					.stream()
					.filter(edge -> mapping.inverse().mapEdge(edge) != null)
					.collect(toSet()), mapping.inverse().mappedEdges());
		}

		/* isomorphicMappingsIter() */
		List<Int2IntMap> validMappings = new ArrayList<>();
		foreachBoolConfig(withFilters -> {
			Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> it;
			if (withFilters) {
				it = algo.isomorphicMappingsIter(g1, g2, type, (v1, v2) -> true, (e1, e2) -> true);
			} else if (type == IsomorphismType.Full) {
				it = algo.isomorphicMappingsIter(g1, g2);
			} else {
				it = algo.isomorphicMappingsIter(g1, g2, type);
			}
			assertTrue(it.hasNext());
			Set<Int2IntMap> mappings = collectMappingsAndCheckUnique(g1, g2, type, it, rand);

			final int n1 = g1.vertices().size();
			final int n2 = g2.vertices().size();
			if (Math.max(n1, n2) <= 8) {
				Integer[] vs1 = g1.vertices().toArray(new Integer[0]);
				Integer[] vs2 = g2.vertices().toArray(new Integer[0]);
				Stream<IntList> mappedG1VerticesStream;
				if (type == IsomorphismType.Full) {
					assert n1 == n2;
					mappedG1VerticesStream = Stream.of(range(n1).asList());
				} else {
					assert n1 >= n2;
					mappedG1VerticesStream = SubSets.stream(range(n1), n2);
				}
				Set<Int2IntMap> expectedMappings = mappedG1VerticesStream.flatMap(Permutations::stream).map(m -> {
					assert m.size() == n2;
					Int2IntMap mapping = new Int2IntOpenHashMap(n2);
					for (int i = 0; i < n2; i++) {
						int v1 = vs1[m.getInt(i)].intValue();
						int v2 = vs2[i].intValue();
						mapping.put(v1, v2);
					}
					assert mapping.size() == n2;
					return mapping;

				}).filter(mapping -> {
					Int2IntMap inv = new Int2IntOpenHashMap(n2);
					for (Int2IntMap.Entry e : mapping.int2IntEntrySet())
						inv.put(e.getIntValue(), e.getIntKey());
					assert inv.size() == n2;
					boolean g2EdgesMatch = g2.edges().stream().allMatch(e2 -> {
						Integer u2 = g2.edgeSource(e2), v2 = g2.edgeTarget(e2);
						Integer u1 = inv.get(u2.intValue()), v1 = inv.get(v2.intValue());
						return g1.containsEdge(u1, v1);
					});
					if (!g2EdgesMatch)
						return false;
					if (type != IsomorphismType.SubGraph) {
						int edgeNumInInducedG1 = (int) g1
								.edges()
								.stream()
								.filter(e1 -> mapping.containsKey(g1.edgeSource(e1).intValue())
										&& mapping.containsKey(g1.edgeTarget(e1).intValue()))
								.count();
						if (edgeNumInInducedG1 != g2.edges().size())
							return false;
					}
					return true;

				}).collect(toSet());
				assertEquals(expectedMappings, mappings);
			}

			/* save a random mapping */
			validMappings.add(new ArrayList<>(mappings).get(rand.nextInt(mappings.size())));
		});

		/* isomorphicMappingsIter() with custom vertex matcher */
		{
			Int2IntMap forcedMappingFull = validMappings.get(0);
			Int2IntMap forcedMapping = new Int2IntOpenHashMap();
			forcedMapping.defaultReturnValue(-1);
			for (int forcedNum = rand.nextInt(1 + g2.vertices().size() / 4); forcedMapping.size() < forcedNum;) {
				int v1 = Graphs.randVertex(g1, rand);
				if (!forcedMappingFull.containsKey(v1))
					continue;
				int v2 = forcedMappingFull.get(v1);
				forcedMapping.putIfAbsent(v2, v1);
			}
			BiPredicate<Integer, Integer> vertexMatcher = (v1, v2) -> {
				int v1Forced = forcedMapping.get(v2.intValue());
				return v1Forced < 0 || v1Forced == v1;
			};

			Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> it =
					algo.isomorphicMappingsIter(g1, g2, type, vertexMatcher, null);
			assertTrue(it.hasNext());

			Set<Int2IntMap> mappings = collectMappingsAndCheckUnique(g1, g2, type, it, rand);
			assertTrue(mappings.contains(forcedMappingFull));
		}

		/* isomorphicMappingsIter() with custom edge matcher */
		{
			Int2IntMap forcedMappingFull = validMappings.get(1);
			Int2ObjectMap<IntSet> forcedMapping = new Int2ObjectOpenHashMap<>();
			IntSet mappedEdges1 = new IntOpenHashSet();
			for (int forcedNum = rand.nextInt(Math.min(4, 1 + g2.edges().size() / 8)); forcedMapping
					.size() < forcedNum;) {
				int e1 = Graphs.randEdge(g1, rand);
				int u2 = forcedMappingFull.get(g1.edgeSource(e1).intValue());
				int v2 = forcedMappingFull.get(g1.edgeTarget(e1).intValue());
				if (u2 < 0 || v2 < 0)
					continue;
				Integer e2 = g2.getEdge(u2, v2);
				if (e2 == null)
					continue;
				assert e2 >= 0;
				if (forcedMapping.containsKey(e2.intValue()))
					continue;
				IntSet e1s = new IntOpenHashSet();
				e1s.add(e1);
				mappedEdges1.add(e1);
				for (int s = rand.nextInt(1 + Math.min(6, g1.edges().size() / forcedNum)); e1s.size() < s;) {
					int e1New = Graphs.randEdge(g1, rand);
					if (mappedEdges1.contains(e1New))
						continue;
					e1s.add(e1New);
					mappedEdges1.add(e1New);
				}
				forcedMapping.putIfAbsent(e2, e1s);
			}
			BiPredicate<Integer, Integer> edgeMatcher = (e1, e2) -> {
				IntSet e1Candidates = forcedMapping.get(e2.intValue());
				return e1Candidates == null || e1Candidates.contains(e1.intValue());
			};

			Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> it =
					algo.isomorphicMappingsIter(g1, g2, type, null, edgeMatcher);
			assertTrue(it.hasNext());

			Set<Int2IntMap> mappings = collectMappingsAndCheckUnique(g1, g2, type, it, rand);
			assertTrue(mappings.contains(forcedMappingFull));
		}
	}

	private static Set<Int2IntMap> collectMappingsAndCheckUnique(Graph<Integer, Integer> g1, Graph<Integer, Integer> g2,
			IsomorphismType type, Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> it, Random rand) {
		Set<Int2IntMap> mappings = new ObjectOpenHashSet<>();
		while (it.hasNext()) {
			IsomorphismMapping<Integer, Integer, Integer, Integer> m1 = it.next();
			checkMapping(m1, type, null, null, rand);

			Int2IntMap mapping = new Int2IntOpenHashMap(g1.vertices().size());
			mapping.defaultReturnValue(-1);
			for (Integer v1 : g1.vertices()) {
				Integer v2 = m1.mapVertex(v1);
				if (v2 != null)
					mapping.put(v1.intValue(), v2.intValue());
			}
			boolean added = mappings.add(mapping);
			assertTrue(added);
		}
		return mappings;
	}

	static void noVertices(IsomorphismTester algo) {
		final Random rand = new Random(0xd8b45de953dcc4eaL);
		foreachBoolConfig(directed -> {
			IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			assertTrue(algo.isomorphicMapping(g1, g2).isPresent());

			List<IsomorphismMapping<Integer, Integer, Integer, Integer>> mappings =
					new ObjectArrayList<>(algo.isomorphicMappingsIter(g1, g2));
			assertEquals(1, mappings.size());
			IsomorphismMapping<Integer, Integer, Integer, Integer> mapping = mappings.get(0);
			checkMapping(mapping, IsomorphismType.Full, null, null, rand);
		});
	}

	static void noEdges(IsomorphismTester algo) {
		final Random rand = new Random(0x9b512bfff33abb78L);
		foreachBoolConfig((directed, withFilters) -> {
			for (final int n : IntList.of(1, 2, 3, 4, 7)) {
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertices(range(n));
				g2.addVertices(range(n));

				Set<Int2IntMap> seenMappings = new ObjectOpenHashSet<>();
				Iterator<IsomorphismMapping<Integer, Integer, Integer, Integer>> mappings;
				if (!withFilters) {
					mappings = algo.isomorphicMappingsIter(g1, g2);
				} else {
					mappings = algo
							.isomorphicMappingsIter(g1, g2, IsomorphismType.Full, (v1, v2) -> true, (e1, e2) -> true);
				}
				while (mappings.hasNext()) {
					IsomorphismMapping<Integer, Integer, Integer, Integer> m1 = mappings.next();
					checkMapping(m1, IsomorphismType.Full, null, null, rand);

					/* assert the returned mappings are unique */
					Int2IntMap mapping = new Int2IntOpenHashMap(g1.vertices().size());
					for (Integer v1 : g1.vertices())
						mapping.put(v1.intValue(), m1.mapVertex(v1).intValue());
					boolean added = seenMappings.add(mapping);
					assertTrue(added);
				}

				/* we expected \(n!\) mappings, all permutations of the vertices */
				int expectedMappingsNum = 1;
				for (int i = 1; i <= n; i++)
					expectedMappingsNum *= i;
				assertEquals(expectedMappingsNum, seenMappings.size());
			}
		});
	}

	static void differentDegrees(IsomorphismTester algo) {
		final Random rand = new Random(0x94db38eea7e00f94L);
		foreachBoolConfig(directed -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(4, 8).repeat(128);
			tester.addPhase().withArgs(16, 32).repeat(128);
			tester.addPhase().withArgs(16, 18).repeat(128);
			tester.addPhase().withArgs(19, 39).repeat(64);
			tester.addPhase().withArgs(23, 52).repeat(32);
			tester.run((n, m) -> {
				Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> graphs =
						randIsomorphicGraphs(n, m, directed, IsomorphismType.Full, rand.nextLong());
				Graph<Integer, Integer> g1 = graphs.left(), g2 = graphs.second();

				Integer e = Graphs.randEdge(g2, rand);
				Integer uOld = g2.edgeSource(e), vOld = g2.edgeTarget(e);
				g2.removeEdge(e);
				Integer uNew, vNew;
				for (int repeat = 0; repeat < 100; repeat++) {
					uNew = Graphs.randVertex(g2, rand);
					vNew = Graphs.randVertex(g2, rand);
					if (g2.containsEdge(uNew, vNew))
						continue;
					if (g2.isDirected()) {
						if (g2.outEdges(uOld).size() == g2.outEdges(uNew).size())
							continue;
						if (g2.inEdges(vOld).size() == g2.inEdges(vNew).size())
							continue;
					} else {
						int uOldDeg = g2.outEdges(uOld).size();
						int vOldDeg = g2.outEdges(vOld).size();
						int uNewDeg = g2.outEdges(uNew).size();
						int vNewDeg = g2.outEdges(vNew).size();
						if (uOldDeg == uNewDeg && vOldDeg == vNewDeg)
							continue;
						if (uOldDeg == vNewDeg && vOldDeg == uNewDeg)
							continue;
					}
					break;
				}

				g1 = maybeIndexGraph(g1, rand);
				g2 = maybeIndexGraph(g2, rand);
				assertFalse(algo.isomorphicMapping(g1, g2).isPresent());
			});
		});
	}

	static void differentVerticesNum(IsomorphismTester algo) {
		foreachBoolConfig(directed -> {
			for (IsomorphismType type : IsomorphismType.values()) {
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g2.addVertexInt();
				assertFalse(algo.isomorphicMapping(g1, g2, type).isPresent());
			}
			{
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertexInt();
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				assertFalse(algo.isomorphicMapping(g1, g2).isPresent());
			}
		});
	}

	static void differentEdgesNum(IsomorphismTester algo) {
		foreachBoolConfig(directed -> {
			for (IsomorphismType type : IsomorphismType.values()) {
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertex(0);
				g1.addVertex(1);
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g2.addVertex(0);
				g2.addVertex(1);
				g2.addEdge(0, 1, 0);
				assertFalse(algo.isomorphicMapping(g1, g2, type).isPresent());
			}
			{
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertex(0);
				g1.addVertex(1);
				g1.addEdge(0, 1, 0);
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g2.addVertex(0);
				g2.addVertex(1);
				assertFalse(algo.isomorphicMapping(g1, g2).isPresent());
			}
			{
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertex(0);
				g1.addVertex(1);
				g1.addEdge(0, 1, 0);
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g2.addVertex(0);
				g2.addVertex(1);
				assertFalse(algo.isomorphicMapping(g1, g2, IsomorphismType.InducedSubGraph).isPresent());
			}
			{
				IntGraph g1 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g1.addVertex(0);
				g1.addVertex(1);
				g1.addEdge(0, 1, 0);
				IntGraph g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				g2.addVertex(0);
				g2.addVertex(1);
				assertTrue(algo.isomorphicMapping(g1, g2, IsomorphismType.SubGraph).isPresent());
			}
		});
	}

	static void differentDirectedUndirected(IsomorphismTester algo) {
		IntGraph g1 = IntGraph.newDirected();
		IntGraph g2 = IntGraph.newUndirected();
		assertThrows(IllegalArgumentException.class, () -> algo.isomorphicMapping(g1, g2).isPresent());
		assertThrows(IllegalArgumentException.class, () -> algo.isomorphicMapping(g2, g1).isPresent());
	}

	private static void checkMapping(IsomorphismMapping<Integer, Integer, Integer, Integer> m1, IsomorphismType type,
			BiPredicate<Integer, Integer> vertexMatcher, BiPredicate<Integer, Integer> edgeMatcher, Random rand) {
		IsomorphismMapping<Integer, Integer, Integer, Integer> m2 = m1.inverse();
		assertTrue(m1.inverse() == m2, "inverse matching was not cached");

		Graph<Integer, Integer> g1 = m1.sourceGraph();
		Graph<Integer, Integer> g2 = m1.targetGraph();
		assertTrue(g1.vertices().size() >= g2.vertices().size());
		assertTrue(g1.edges().size() >= g2.edges().size());

		/* assert the vertex mapping is an injective function */
		IntSet mappedG2Vertices = new IntOpenHashSet(g1.vertices().size());
		for (Integer v1 : g1.vertices()) {
			Integer v2 = m1.mapVertex(v1);
			if (v2 == null)
				continue;
			assertTrue(g2.vertices().contains(v2));
			assertEquals(v1, m2.mapVertex(v2));
			boolean modified = mappedG2Vertices.add(v2.intValue());
			assertTrue(modified);
		}
		assertEquals(g2.vertices().size(), mappedG2Vertices.size());
		assertThrows(NoSuchVertexException.class, () -> m1.mapVertex(GraphsTestUtils.nonExistingVertex(g1, rand)));
		assertThrows(NoSuchVertexException.class, () -> m2.mapVertex(GraphsTestUtils.nonExistingVertex(g2, rand)));
		if (g1.vertices().size() < g2.vertices().size()) {
			Integer unmappedG2Vertex =
					g2.vertices().stream().filter(v2 -> !mappedG2Vertices.contains(v2.intValue())).findFirst().get();
			assertNull(m2.mapVertex(unmappedG2Vertex));
			if (m2 instanceof IsomorphismIMapping) {
				IsomorphismIMapping m2Int = (IsomorphismIMapping) m2;
				assertEquals(-1, m2Int.mapVertex(unmappedG2Vertex.intValue()));
			}
		}

		/* assert the edge mapping is an injective function */
		IntSet mappedG2Edges = new IntOpenHashSet(g1.edges().size());
		for (Integer e1 : g1.edges()) {
			Integer e2 = m1.mapEdge(e1);
			if (e2 == null)
				continue;
			assertTrue(g2.edges().contains(e2));
			assertEquals(e1, m2.mapEdge(e2));
			boolean modified = mappedG2Edges.add(e2.intValue());
			assertTrue(modified);
		}
		assertEquals(g2.edges().size(), mappedG2Edges.size());
		assertThrows(NoSuchEdgeException.class, () -> m1.mapEdge(GraphsTestUtils.nonExistingEdge(g1, rand)));
		assertThrows(NoSuchEdgeException.class, () -> m2.mapEdge(GraphsTestUtils.nonExistingEdge(g2, rand)));
		if (g1.edges().size() < g2.edges().size()) {
			Integer unmappedG2Edge =
					g2.edges().stream().filter(e2 -> !mappedG2Edges.contains(e2.intValue())).findFirst().get();
			assertNull(m2.mapEdge(unmappedG2Edge));
			if (m2 instanceof IsomorphismIMapping) {
				IsomorphismIMapping m2Int = (IsomorphismIMapping) m2;
				assertEquals(-1, m2Int.mapEdge(unmappedG2Edge.intValue()));
			}
		}

		/* assert the mapping is valid */
		for (Integer e1 : g1.edges()) {
			Integer u1 = g1.edgeSource(e1), v1 = g1.edgeTarget(e1);
			Integer u2 = m1.mapVertex(u1), v2 = m1.mapVertex(v1);
			Integer e2 = m1.mapEdge(e1);
			if (e2 == null)
				continue;
			Integer u2Expected = g2.edgeSource(e2);
			Integer v2Expected = g2.edgeTarget(e2);
			if (g1.isDirected()) {
				assertEquals(u2Expected, u2);
				assertEquals(v2Expected, v2);
			} else {
				assertTrue((u2Expected.equals(u2) && v2Expected.equals(v2))
						|| (u2Expected.equals(v2) && v2Expected.equals(u2)));
			}
		}
		if (type != IsomorphismType.SubGraph) {
			g2
					.edges()
					.stream()
					.filter(e2 -> mappedG2Vertices.contains(g2.edgeSource(e2).intValue())
							&& mappedG2Vertices.contains(g2.edgeTarget(e2).intValue()))
					.forEach(e2 -> {
						Integer u2 = g2.edgeSource(e2), v2 = g2.edgeTarget(e2);
						Integer u1 = m2.mapVertex(u2), v1 = m2.mapVertex(v2);
						Integer e1Expected = g1.getEdge(m2.mapVertex(u2), m2.mapVertex(v2));
						assertNotNull(e1Expected);
						Integer e1 = m2.mapEdge(e2);
						if (g1.isDirected()) {
							assertEquals(u1, g1.edgeSource(e1));
							assertEquals(v1, g1.edgeTarget(e1));
						} else {
							assertTrue((u1.equals(g1.edgeSource(e1)) && v1.equals(g1.edgeTarget(e1)))
									|| (u1.equals(g1.edgeTarget(e1)) && v1.equals(g1.edgeSource(e1))));
						}
						assertEquals(e1Expected, e1);
					});
		}

		/* assert mapping is consistent with custom vertex and edge matchers */
		if (vertexMatcher != null) {
			for (Integer v1 : g1.vertices()) {
				Integer v2 = m1.mapVertex(v1);
				assertTrue(vertexMatcher.test(v1, v2));
			}
		}
		if (edgeMatcher != null) {
			for (Integer e1 : g1.edges()) {
				Integer e2 = m1.mapEdge(e1);
				assertTrue(edgeMatcher.test(e1, e2));
			}
		}
	}

	@SuppressWarnings("boxing")
	private static Pair<Graph<Integer, Integer>, Graph<Integer, Integer>> randIsomorphicGraphs(int n, int m,
			boolean directed, IsomorphismType type, long seed) {
		Random rand = new Random(seed);

		Graph<Integer, Integer> g1 = GraphsTestUtils.randGraph(n, m, directed, true, false, rand.nextLong());

		GraphFactory<Integer, Integer> factory;
		if (g1 instanceof IntGraph) {
			factory = IntGraphFactory.newInstance(directed);
		} else {
			factory = GraphFactory.newInstance(directed);
		}
		Graph<Integer, Integer> g2 = factory.allowSelfEdges().newGraph();

		/* add n vertices to g2 */
		final int n2 = n - (type != IsomorphismType.Full ? 1 + rand.nextInt(1 + Math.min(4, n / 8)) : 0);
		while (g2.vertices().size() < n2) {
			int v = rand.nextInt(n * 2);
			if (!g2.vertices().contains(v))
				g2.addVertex(v);
		}

		Map<Integer, Integer> vMapping = randMapping(g1.vertices(), g2.vertices(), rand.nextLong());

		/* add all edges to g2 */
		List<Integer> g1Edges = new ArrayList<>(g1.edges());
		Collections.shuffle(g1Edges, rand);
		for (Integer e1 : g1Edges) {
			Integer u1 = g1.edgeSource(e1), v1 = g1.edgeTarget(e1);
			Integer u2 = vMapping.get(u1), v2 = vMapping.get(v1);
			if (u2 == null || v2 == null)
				continue;
			Integer e2;
			do {
				e2 = rand.nextInt(m * 2);
			} while (g2.edges().contains(e2));
			g2.addEdge(u2, v2, e2);
		}

		if (type == IsomorphismType.SubGraph) {
			int edgesToRemove = Math.min(4, g2.edges().size() / 8);
			while (edgesToRemove-- > 0)
				g2.removeEdge(Graphs.randEdge(g2, rand));
		}

		return Pair.of(g1, g2);
	}

	/* assume |A|>=|B|, return a random mapping from A that cover all B */
	/* A may contain unmapped elements */
	private static <A, B> Map<A, B> randMapping(Set<A> a, Set<B> b, long seed) {
		Random rand = new Random(seed);
		List<A> aList = new ArrayList<>(a);
		List<B> bList = new ArrayList<>(b);
		assert aList.size() >= bList.size();
		Collections.shuffle(aList, rand);
		Map<A, B> map = new HashMap<>();
		for (int i : range(bList.size()))
			map.put(aList.get(i), bList.get(i));
		return map;
	}

}
