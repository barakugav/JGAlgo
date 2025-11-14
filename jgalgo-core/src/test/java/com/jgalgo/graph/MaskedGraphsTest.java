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
package com.jgalgo.graph;

import static com.jgalgo.graph.WeightsTest.weightFactoryBool;
import static com.jgalgo.graph.WeightsTest.weightFactoryByte;
import static com.jgalgo.graph.WeightsTest.weightFactoryChar;
import static com.jgalgo.graph.WeightsTest.weightFactoryDouble;
import static com.jgalgo.graph.WeightsTest.weightFactoryFloat;
import static com.jgalgo.graph.WeightsTest.weightFactoryInt;
import static com.jgalgo.graph.WeightsTest.weightFactoryLong;
import static com.jgalgo.graph.WeightsTest.weightFactoryObject;
import static com.jgalgo.graph.WeightsTest.weightFactoryShort;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.IterToolsTest;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MaskedGraphsTest extends TestBase {

	@SuppressWarnings("unchecked")
	private static <K> Set<K> randSubSet(Iterable<K> c, double ratio, Random rand) {
		if (c instanceof IntIterable)
			return (Set<K>) randSubSet((IntIterable) c, ratio, rand);
		assert 0 <= ratio && ratio <= 1;
		if (ratio == 0)
			return Collections.emptySet();
		if (ratio == 1)
			return new ObjectOpenHashSet<>(c.iterator());
		Set<K> set = new ObjectOpenHashSet<>();
		for (K elm : c)
			if (rand.nextDouble() < ratio)
				set.add(elm);
		return set;
	}

	private static IntSet randSubSet(IntIterable c, double ratio, Random rand) {
		assert 0 <= ratio && ratio <= 1;
		if (ratio == 0)
			return IntSets.emptySet();
		if (ratio == 1)
			return new IntOpenHashSet(c.iterator());
		IntSet set = new IntOpenHashSet();
		for (int elm : c)
			if (rand.nextDouble() < ratio)
				set.add(elm);
		return set;
	}

	@Test
	public void vertices() {
		final Random rand = new Random(0x8b7320eb0aa7bcfbL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			IndexGraph indexGraph = gMasked.indexGraph();

			Set<Integer> expectedVertices = new IntOpenHashSet(gOrig.vertices());
			expectedVertices.removeAll(maskedVertices);
			assertEquals(expectedVertices.size(), gMasked.vertices().size());
			assertEquals(expectedVertices, gMasked.vertices());

			assertEquals(gOrig.vertices().size() - maskedVertices.size(), indexGraph.vertices().size());
			assertEquals(range(gMasked.vertices().size()), indexGraph.vertices());

			for (Iterator<Integer> vit = gMasked.vertices().iterator();;) {
				if (!vit.hasNext()) {
					if (vit instanceof IterTools.Peek)
						assertThrows(NoSuchElementException.class, ((IterTools.Peek<Integer>) vit)::peekNext);
					assertThrows(NoSuchElementException.class, vit::next);
					break;
				}
				Integer peekNext = null;
				if (vit instanceof IterTools.Peek)
					peekNext = ((IterTools.Peek<Integer>) vit).peekNext();
				Integer v = vit.next();
				if (vit instanceof IterTools.Peek)
					assertEquals(v, peekNext);
				assertTrue(gMasked.vertices().contains(v));
			}
			for (Integer v : maskedVertices)
				assertFalse(gMasked.vertices().contains(v));
			assertFalse(gMasked.vertices().contains(GraphsTestUtils.nonExistingVertex(gMasked, rand)));
			assertFalse(gMasked.vertices().contains(GraphsTestUtils.nonExistingVertex(gOrig, rand)));

			assertTrue(gMasked.vertices().containsAll(new ArrayList<>(expectedVertices)));
			assertTrue(gMasked.vertices().containsAll(new IntArrayList(expectedVertices)));
			if (gMasked.vertices().size() != gOrig.vertices().size())
				assertFalse(gMasked.vertices().containsAll(gOrig.vertices()));
			if (!maskedVertices.isEmpty())
				assertFalse(gMasked.vertices().containsAll(maskedVertices));
			assertFalse(gMasked
					.vertices()
					.containsAll(IntList.of(GraphsTestUtils.nonExistingVertex(gMasked, rand).intValue())));
			assertFalse(gMasked
					.vertices()
					.containsAll(IntList.of(GraphsTestUtils.nonExistingVertex(gOrig, rand).intValue())));
		});
	}

	@Test
	public void edges() {
		final Random rand = new Random(0x2b820a64638ecf91L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			IndexGraph indexGraph = gMasked.indexGraph();

			Set<Integer> expectedEdges = new IntOpenHashSet(gOrig.edges());
			expectedEdges
					.removeIf(e -> maskedEdges.contains(e) || maskedVertices.contains(gOrig.edgeSource(e))
							|| maskedVertices.contains(gOrig.edgeTarget(e)));
			assertEquals(expectedEdges.size(), gMasked.edges().size());
			assertEquals(expectedEdges, gMasked.edges());

			assertEquals(expectedEdges.size(), indexGraph.edges().size());
			assertEquals(range(gMasked.edges().size()), indexGraph.edges());
		});
	}

	@Test
	public void duplicateMaskedElements() {
		final Random rand = new Random(0x59e66737e1542e33L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph, origIndex, rand);
			Integer v = g.vertices().iterator().next();
			Integer e = g.edges().iterator().next();

			assertThrows(IllegalArgumentException.class, () -> g.maskedGraphView(List.of(v, v), List.of()));
			assertThrows(IllegalArgumentException.class, () -> g.maskedGraphView(List.of(), List.of(e, e)));
		});
	}

	@Test
	public void invalidMaskedElements() {
		final Random rand = new Random(0x6d71967a7ccc672aL);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph, origIndex, rand);
			Integer v = g.vertices().iterator().next();
			Integer e = g.edges().iterator().next();
			Integer unknownV = GraphsTestUtils.nonExistingVertex(g, rand);
			Integer unknownE = GraphsTestUtils.nonExistingEdge(g, rand);

			assertThrows(IllegalArgumentException.class, () -> g.maskedGraphView(List.of(v, unknownV), List.of()));
			assertThrows(IllegalArgumentException.class, () -> g.maskedGraphView(List.of(), List.of(e, unknownE)));
		});
	}

	@Test
	public void addRemoveVertex() {
		final Random rand = new Random(0x8af57d3c51b5bdbcL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			Integer nonExistingVertex = gMasked instanceof IndexGraph ? Integer.valueOf(gMasked.vertices().size())
					: GraphsTestUtils.nonExistingVertexNonNegative(gMasked, rand);
			assertThrows(UnsupportedOperationException.class, () -> gMasked.addVertex());
			assertThrows(UnsupportedOperationException.class, () -> gMasked.addVertex(nonExistingVertex));

			Integer vertexToRemove = gMasked.vertices().iterator().next();
			assertThrows(UnsupportedOperationException.class, () -> gMasked.removeVertex(vertexToRemove));

			assertThrows(UnsupportedOperationException.class, () -> indexGraph.addVertexInt());
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeVertex(0));
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0xfe0c2e4ff409409L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertex(gMasked, rand);
			assertThrows(UnsupportedOperationException.class, () -> gMasked.addVertices(List.of(nonExistingVertex)));

			assertThrows(UnsupportedOperationException.class,
					() -> indexGraph.addVertices(List.of(Integer.valueOf(indexGraph.vertices().size()))));
		});
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0xf243f8d43d914d32L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);

			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(gMasked, rand);
			Integer vertex = gOrig.vertices().iterator().next();

			assertThrows(UnsupportedOperationException.class, () -> gMasked.renameVertex(vertex, nonExistingVertex));
		});
	}

	@Test
	public void addRemoveEdge() {
		final Random rand = new Random(0xaba41a8ddf7de9baL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			Iterator<Integer> vit = gMasked.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer nonExistingEdge = gMasked instanceof IndexGraph ? Integer.valueOf(gMasked.edges().size())
					: GraphsTestUtils.nonExistingEdgeNonNegative(gMasked, rand);
			assertThrows(UnsupportedOperationException.class, () -> gMasked.addEdge(u, v));
			assertThrows(UnsupportedOperationException.class, () -> gMasked.addEdge(u, v, nonExistingEdge));

			Integer edgeToRemove = gMasked.edges().iterator().next();
			assertThrows(UnsupportedOperationException.class, () -> gMasked.removeEdge(edgeToRemove));

			assertThrows(UnsupportedOperationException.class, () -> indexGraph.addEdge(0, 1));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeEdge(0));
		});
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0x3058d2ff90aa2adfL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			Iterator<Integer> vit = gMasked.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gMasked, rand);
			IntGraph g1 = IntGraph.newDirected();
			g1.addVertices(List.of(u, v));
			g1.addEdge(u.intValue(), v.intValue(), nonExistingEdge.intValue());
			IEdgeSet edges = IEdgeSet.allOf(g1);

			assertThrows(UnsupportedOperationException.class, () -> gMasked.addEdges(edges));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.addEdgesReassignIds(edges));
		});
	}

	@Test
	public void removeVertices() {
		final Random rand = new Random(0x2cff4ef330660f97L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			assertThrows(UnsupportedOperationException.class,
					() -> gMasked.removeVertices(List.of(gMasked.vertices().iterator().next())));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeVertices(IntList.of(0)));
		});
	}

	@Test
	public void removeEdges() {
		final Random rand = new Random(0xb1b170873ec59258L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();

			assertThrows(UnsupportedOperationException.class,
					() -> gMasked.removeEdges(List.of(gMasked.edges().iterator().next())));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeEdges(IntList.of(0)));
		});
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0x2fc42c896a0a6094L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);

			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdge(gMasked, rand);
			assertThrows(UnsupportedOperationException.class,
					() -> gMasked.renameEdge(gMasked.edges().iterator().next(), nonExistingEdge));

		});
	}

	@Test
	public void edgesOutIn() {
		final Random rand = new Random(0xdbbcbf1cf37ca6cbL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			IndexGraph indexGraph = gMasked.indexGraph();
			IndexIdMap<Integer> viMap = gMasked.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = gMasked.indexGraphEdgesMap();
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.outEdges(v).stream()).collect(toSet()));
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.inEdges(v).stream()).collect(toSet()));

			for (Integer u : gMasked.vertices()) {
				EdgeSet<Integer, Integer> edges = gMasked.outEdges(u);
				Set<Integer> expectedEdges =
						gOrig.outEdges(u).stream().filter(e -> !maskedEdges.contains(e)).collect(toSet());
				assertEquals(expectedEdges.size(), edges.size());
				assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
				assertEquals(expectedEdges, edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator();;) {
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, eit::peekNext);
						break;
					}
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(u, eit.source());
					assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
					assertEquals(gMasked.edgeEndpoint(e, u), eit.target());
					assertEquals(u, gMasked.edgeEndpoint(e, eit.target()));

					iteratedEdges.add(e);
				}
				assertEquals(expectedEdges, iteratedEdges);
				for (Integer e : gOrig.edges())
					assertEqualsBool(iteratedEdges.contains(e), edges.contains(e));
				assertFalse(edges.contains(GraphsTestUtils.nonExistingEdge(gMasked, rand)));

				assertTrue(edges.containsAll(new ArrayList<>(expectedEdges)));
				assertTrue(edges.containsAll(new IntArrayList(expectedEdges)));
				if (edges.size() != gMasked.edges().size())
					assertFalse(edges.containsAll(gMasked.edges()));
				if (!maskedEdges.isEmpty())
					assertFalse(edges.containsAll(maskedEdges));
				assertFalse(edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(gMasked, rand).intValue())));
				assertFalse(edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(gOrig, rand).intValue())));
			}
			for (int uIdx : indexGraph.vertices()) {
				Integer u = viMap.indexToId(uIdx);
				IEdgeSet edges = indexGraph.outEdges(uIdx);
				Set<Integer> expectedEdges = gOrig
						.outEdges(u)
						.stream()
						.filter(e -> !maskedEdges.contains(e))
						.mapToInt(e -> eiMap.idToIndex(e))
						.boxed()
						.collect(toSet());
				assertEquals(expectedEdges.size(), edges.size());
				assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
				assertEquals(expectedEdges, edges);

				IntSet iteratedEdges = new IntOpenHashSet();
				for (IEdgeIter eit = edges.iterator();;) {
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, eit::peekNextInt);
						break;
					}
					int peekNext = eit.peekNextInt();
					int eIdx = eit.nextInt();
					assertEquals(eIdx, peekNext);
					Integer e = eiMap.indexToId(eIdx);

					assertEquals(uIdx, eit.sourceInt());
					assertEquals(viMap.idToIndex(gOrig.edgeEndpoint(e, u)), eit.targetInt());
					assertEquals(indexGraph.edgeEndpoint(eIdx, uIdx), eit.targetInt());
					assertEquals(uIdx, indexGraph.edgeEndpoint(eIdx, eit.targetInt()));

					iteratedEdges.add(eIdx);
				}
				assertEquals(expectedEdges, iteratedEdges);
				for (int eIdx : range(gOrig.edges().size()))
					assertEqualsBool(iteratedEdges.contains(eIdx), edges.contains(eIdx));
				assertFalse(edges.contains(-1));
				assertFalse(edges.contains(indexGraph.edges().size()));

				assertTrue(edges.containsAll(new ArrayList<>(expectedEdges)));
				assertTrue(edges.containsAll(new IntArrayList(expectedEdges)));
				if (edges.size() != indexGraph.edges().size())
					assertFalse(edges.containsAll(indexGraph.edges()));
				assertFalse(
						edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(indexGraph, rand).intValue())));
			}
			for (Integer v : gMasked.vertices()) {
				EdgeSet<Integer, Integer> edges = gMasked.inEdges(v);
				Set<Integer> expectedEdges =
						gOrig.inEdges(v).stream().filter(e -> !maskedEdges.contains(e)).collect(toSet());
				assertEquals(expectedEdges.size(), edges.size());
				assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
				assertEquals(expectedEdges, edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator();;) {
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, eit::peekNext);
						break;
					}
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(v, eit.target());
					assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
					assertEquals(gMasked.edgeEndpoint(e, v), eit.source());
					assertEquals(v, gMasked.edgeEndpoint(e, eit.source()));

					iteratedEdges.add(e);
				}

				assertEquals(edges.size(), iteratedEdges.size());
				for (Integer e : gOrig.edges())
					assertEqualsBool(iteratedEdges.contains(e), edges.contains(e));
				assertFalse(edges.contains(GraphsTestUtils.nonExistingEdge(gMasked, rand)));

				assertTrue(edges.containsAll(new ArrayList<>(expectedEdges)));
				assertTrue(edges.containsAll(new IntArrayList(expectedEdges)));
				if (edges.size() != gMasked.edges().size())
					assertFalse(edges.containsAll(gMasked.edges()));
				if (!maskedEdges.isEmpty())
					assertFalse(edges.containsAll(maskedEdges));
				assertFalse(edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(gMasked, rand).intValue())));
				assertFalse(edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(gOrig, rand).intValue())));
			}
			for (int vIdx : indexGraph.vertices()) {
				Integer v = viMap.indexToId(vIdx);
				IEdgeSet edges = indexGraph.inEdges(vIdx);
				Set<Integer> expectedEdges = gOrig
						.inEdges(v)
						.stream()
						.filter(e -> !maskedEdges.contains(e))
						.mapToInt(e -> eiMap.idToIndex(e))
						.boxed()
						.collect(toSet());
				assertEquals(expectedEdges.size(), edges.size());
				assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
				assertEquals(expectedEdges, edges);

				IntSet iteratedEdges = new IntOpenHashSet();
				for (IEdgeIter eit = edges.iterator();;) {
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, eit::peekNextInt);
						break;
					}
					int peekNext = eit.peekNextInt();
					int eIdx = eit.nextInt();
					assertEquals(eIdx, peekNext);
					Integer e = eiMap.indexToId(eIdx);

					assertEquals(vIdx, eit.targetInt());
					assertEquals(viMap.idToIndex(gOrig.edgeEndpoint(e, v)), eit.sourceInt());
					assertEquals(indexGraph.edgeEndpoint(eIdx, vIdx), eit.sourceInt());
					assertEquals(vIdx, indexGraph.edgeEndpoint(eIdx, eit.sourceInt()));

					iteratedEdges.add(eIdx);
				}
				assertEquals(expectedEdges, iteratedEdges);
				for (int eIdx : range(gOrig.edges().size()))
					assertEqualsBool(iteratedEdges.contains(eIdx), edges.contains(eIdx));
				assertFalse(edges.contains(-1));
				assertFalse(edges.contains(indexGraph.edges().size()));

				assertTrue(edges.containsAll(new ArrayList<>(expectedEdges)));
				assertTrue(edges.containsAll(new IntArrayList(expectedEdges)));
				if (edges.size() != indexGraph.edges().size())
					assertFalse(edges.containsAll(indexGraph.edges()));
				assertFalse(
						edges.containsAll(IntList.of(GraphsTestUtils.nonExistingEdge(indexGraph, rand).intValue())));
			}

			for (Integer u : gMasked.vertices()) {
				foreachBoolConfig(out -> {
					Set<Integer> edges = out ? gMasked.outEdges(u) : gMasked.inEdges(u);
					IterToolsTest.testIterSkip(edges, rand);
				});
			}
			for (int uIdx : indexGraph.vertices()) {
				foreachBoolConfig(out -> {
					IntIterable edges = out ? indexGraph.outEdges(uIdx) : indexGraph.inEdges(uIdx);
					IterToolsTest.testIterSkip(edges, rand);
				});
			}
		});
	}

	@Test
	public void edgesSourceTarget() {
		final Random rand = new Random(0x5c652c33263629ddL);
		foreachBoolConfig((intGraph, directed, index, parallelEdges) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph, parallelEdges, true, rand);
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			IndexGraph indexGraph = gMasked.indexGraph();
			IndexIdMap<Integer> viMap = gMasked.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = gMasked.indexGraphEdgesMap();
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.outEdges(v).stream()).collect(toSet()));
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.inEdges(v).stream()).collect(toSet()));

			for (Integer u : gMasked.vertices()) {
				for (Integer v : gMasked.vertices()) {
					EdgeSet<Integer, Integer> edges = gMasked.getEdges(u, v);
					Set<Integer> expectedEdges =
							gOrig.getEdges(u, v).stream().filter(e -> !maskedEdges.contains(e)).collect(toSet());
					assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
					assertEquals(expectedEdges.size(), edges.size());
					assertEquals(expectedEdges, edges);

					if (edges.isEmpty()) {
						assertFalse(gMasked.containsEdge(u, v));
						assertNull(gMasked.getEdge(u, v));
					} else {
						Integer e = gMasked.getEdge(u, v);
						assertNotNull(e);
						assertTrue(edges.contains(e));
					}

					for (EdgeIter<Integer, Integer> eit = edges.iterator();;) {
						if (!eit.hasNext()) {
							assertThrows(NoSuchElementException.class, eit::peekNext);
							break;
						}
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, u), v);
						assertEquals(gOrig.edgeEndpoint(e, v), u);
						assertEquals(u, gMasked.edgeEndpoint(e, v));
						assertEquals(v, gMasked.edgeEndpoint(e, u));
					}
				}
			}
			if (!maskedVertices.isEmpty()) {
				Integer maskedVertex = maskedVertices.iterator().next();
				assertThrows(NoSuchVertexException.class, () -> gMasked.getEdges(maskedVertex, maskedVertex));
			}

			for (int uIdx : indexGraph.vertices()) {
				for (int vIdx : indexGraph.vertices()) {
					Integer u = viMap.indexToId(uIdx);
					Integer v = viMap.indexToId(vIdx);
					IEdgeSet edges = indexGraph.getEdges(uIdx, vIdx);
					Set<Integer> expectedEdges = gOrig
							.getEdges(u, v)
							.stream()
							.filter(e -> !maskedEdges.contains(e))
							.mapToInt(e -> eiMap.idToIndex(e))
							.boxed()
							.collect(toSet());
					assertEqualsBool(expectedEdges.isEmpty(), edges.isEmpty());
					assertEquals(expectedEdges.size(), edges.size());
					assertEquals(expectedEdges, edges);

					if (edges.isEmpty()) {
						assertFalse(indexGraph.containsEdge(uIdx, vIdx));
						assertEquals(-1, indexGraph.getEdge(uIdx, vIdx));
					} else {
						int eIdx = indexGraph.getEdge(uIdx, vIdx);
						assertTrue(edges.contains(eIdx));
					}

					for (IEdgeIter eit = edges.iterator();;) {
						if (!eit.hasNext()) {
							assertThrows(NoSuchElementException.class, eit::peekNextInt);
							break;
						}
						int peekNext = eit.peekNextInt();
						int eIdx = eit.nextInt();
						assertEquals(eIdx, peekNext);

						assertEquals(uIdx, eit.sourceInt());
						assertEquals(vIdx, eit.targetInt());
						Integer e = eiMap.indexToId(eIdx);
						assertEquals(gOrig.edgeEndpoint(e, u), v);
						assertEquals(gOrig.edgeEndpoint(e, v), u);
						assertEquals(uIdx, indexGraph.edgeEndpoint(eIdx, vIdx));
						assertEquals(vIdx, indexGraph.edgeEndpoint(eIdx, uIdx));
					}
				}
			}
			if (indexGraph.vertices().size() != gOrig.vertices().size()) {
				assertThrows(NoSuchVertexException.class, () -> indexGraph.getEdges(-1, 0));
				assertThrows(NoSuchVertexException.class, () -> indexGraph.getEdges(indexGraph.vertices().size(), 0));
				assertThrows(NoSuchVertexException.class, () -> indexGraph.getEdges(0, -1));
				assertThrows(NoSuchVertexException.class, () -> indexGraph.getEdges(0, indexGraph.vertices().size()));
			}
		});
	}

	@Test
	public void removeEdgesOf() {
		final Random rand = new Random(0xa2a1b7ffb316b300L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			IndexGraph indexGraph = gMasked.indexGraph();
			Integer v = gMasked.vertices().iterator().next();
			assertThrows(UnsupportedOperationException.class, () -> gMasked.removeEdgesOf(v));
			assertThrows(UnsupportedOperationException.class, () -> gMasked.removeOutEdgesOf(v));
			assertThrows(UnsupportedOperationException.class, () -> gMasked.removeInEdgesOf(v));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeEdgesOf(0));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeOutEdgesOf(0));
			assertThrows(UnsupportedOperationException.class, () -> indexGraph.removeInEdgesOf(0));
		});
	}

	@Test
	public void reverseEdge() {
		final Random rand = new Random(0xf9e457995456faf7L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;
			for (int i = 0; i < 10; i++) {
				Integer e, u, v;
				for (;;) {
					e = Graphs.randEdge(gMasked, rand);
					u = gMasked.edgeSource(e);
					v = gMasked.edgeTarget(e);
					if (!gMasked.isDirected())
						break;
					if (gMasked.isAllowParallelEdges() || gMasked.getEdge(v, u) == null)
						break;
				}

				Graph<Integer, Integer> origExpected = gOrig.copy(true, true);
				if (maskedIndex) {
					origExpected.reverseEdge(gMasked0.indexGraphEdgesMap().indexToId(e.intValue()));
				} else {
					origExpected.reverseEdge(e);
				}
				Graph<Integer, Integer> maskedExpected = gMasked.copy(true, true);
				maskedExpected.reverseEdge(e);

				gMasked.reverseEdge(e);
				assertEquals(gMasked.edgeSource(e), v);
				assertEquals(gMasked.edgeTarget(e), u);

				assertEquals(origExpected, gOrig);
				assertEquals(maskedExpected, gMasked);
			}
		});
	}

	@Test
	public void moveEdge() {
		final Random rand = new Random(0x19e689f919976cc2L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;
			for (int i = 0; i < 10; i++) {
				Integer e, newU, newV;
				for (;;) {
					e = Graphs.randEdge(gMasked, rand);
					newU = Graphs.randVertex(gMasked, rand);
					newV = Graphs.randVertex(gMasked, rand);
					if (!gMasked.isAllowSelfEdges() && newU.equals(newV))
						continue;
					if (gMasked.isAllowParallelEdges() || gMasked.getEdge(newU, newV) == null
							|| gMasked.getEdges(newU, newV).equals(Set.of(e)))
						break;
				}

				Graph<Integer, Integer> origExpected = gOrig.copy(true, true);
				Graph<Integer, Integer> maskedExpected = gMasked.copy(true, true);
				if (maskedIndex) {
					IndexIdMap<Integer> viMap = gMasked0.indexGraphVerticesMap();
					origExpected
							.moveEdge(gMasked0.indexGraphEdgesMap().indexToId(e.intValue()),
									viMap.indexToId(newU.intValue()), viMap.indexToId(newV.intValue()));
				} else {
					origExpected.moveEdge(e, newU, newV);
				}
				maskedExpected.moveEdge(e, newU, newV);

				gMasked.moveEdge(e, newU, newV);
				assertEquals(gMasked.edgeSource(e), newU);
				assertEquals(gMasked.edgeTarget(e), newV);

				assertEquals(origExpected, gOrig);
				assertEquals(maskedExpected, gMasked);
			}
		});
	}

	@Test
	public void edgeGetSourceTarget() {
		final Random rand = new Random(0xaec021b0b53adb6fL);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);
			for (Integer e : gMasked.edges()) {
				assertEquals(gOrig.edgeSource(e), gMasked.edgeSource(e));
				assertEquals(gOrig.edgeTarget(e), gMasked.edgeTarget(e));
			}
			if (gOrig.edges().size() != gMasked.edges().size()) {
				Integer maskedEdge = gOrig.edges().stream().filter(e -> !gMasked.edges().contains(e)).findAny().get();
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeSource(maskedEdge));
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeTarget(maskedEdge));
			}
		});
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			IndexGraph gMasked = gMasked0.indexGraph();
			IndexIdMap<Integer> viMap = gMasked0.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = gMasked0.indexGraphEdgesMap();
			for (int eIdx : gMasked.edges()) {
				Integer e = eiMap.indexToId(eIdx);
				assertEquals(gOrig.edgeSource(e), viMap.indexToId(gMasked.edgeSource(eIdx)));
				assertEquals(gOrig.edgeTarget(e), viMap.indexToId(gMasked.edgeTarget(eIdx)));
			}
			if (gOrig.edges().size() != gMasked.edges().size()) {
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeSource(-1));
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeSource(gMasked.edges().size()));
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeTarget(-1));
				assertThrows(NoSuchEdgeException.class, () -> gMasked.edgeTarget(gMasked.edges().size()));
			}
		});
	}

	@Test
	public void clear() {
		final Random rand = new Random(0xf14b7624542d710cL);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;
			assertThrows(UnsupportedOperationException.class, () -> gMasked.clear());
			assertThrows(UnsupportedOperationException.class, () -> gMasked.clearEdges());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesWeights() {
		final Random rand = new Random(0x6d19a5e706c80938L);
		testVerticesWeights(byte.class, weightFactoryByte(rand.nextLong()), (byte) 6, rand);
		testVerticesWeights(short.class, weightFactoryShort(rand.nextLong()), (short) 55, rand);
		testVerticesWeights(int.class, weightFactoryInt(rand.nextLong()), 17, rand);
		testVerticesWeights(long.class, weightFactoryLong(rand.nextLong()), (long) 128, rand);
		testVerticesWeights(float.class, weightFactoryFloat(rand.nextLong()), (float) 1.7, rand);
		testVerticesWeights(double.class, weightFactoryDouble(rand.nextLong()), 4.2, rand);
		testVerticesWeights(boolean.class, weightFactoryBool(rand.nextLong()), true, rand);
		testVerticesWeights(char.class, weightFactoryChar(rand.nextLong()), 'z', rand);
		testVerticesWeights(Object.class, weightFactoryObject(), new Object(), rand);

	}

	private static <T> void testVerticesWeights(Class<T> weightsClass, Supplier<T> weightsFactory, T defaultValue,
			Random rand) {
		final String weightsKey = "vWeights";
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Weights<Integer, T> wOrig = gOrig.addVerticesWeights(weightsKey, weightsClass, defaultValue);
			{
				for (Integer v : gOrig.vertices())
					assertEquals(defaultValue, wOrig.getAsObj(v));
				Map<Integer, T> expectedWeights = new Int2ObjectOpenHashMap<>();
				for (Integer v : gOrig.vertices()) {
					T w = weightsFactory.get();
					wOrig.setAsObj(v, w);
					expectedWeights.put(v, w);
				}
				for (Integer v : gOrig.vertices())
					assertEquals(expectedWeights.get(v), wOrig.getAsObj(v));
			}
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);

			assertEquals(gOrig.verticesWeightsKeys(), gMasked.verticesWeightsKeys());
			Weights<Integer, T> wMasked = gMasked.verticesWeights(weightsKey);

			for (Integer v : gMasked.vertices())
				assertEquals(wOrig.getAsObj(v), wMasked.getAsObj(v));
			if (gOrig.vertices().size() != gMasked.vertices().size()) {
				Integer maskedVertex =
						gOrig.vertices().stream().filter(v -> !gMasked.vertices().contains(v)).findAny().get();
				final Weights<Integer, T> wMasked0 = wMasked;
				assertThrows(NoSuchVertexException.class, () -> wMasked0.getAsObj(maskedVertex));
			}
			assertEquals(wOrig.defaultWeightAsObj(), wMasked.defaultWeightAsObj());

			Integer vertex = Graphs.randVertex(gMasked, rand);
			T weight = weightsFactory.get();
			wMasked.setAsObj(vertex, weight);
			assertEquals(weight, wMasked.getAsObj(vertex));
			assertEquals(weight, wOrig.getAsObj(vertex));

			assertTrue(gMasked.verticesWeightsKeys().contains(weightsKey));
			assertTrue(gOrig.verticesWeightsKeys().contains(weightsKey));
			gMasked.removeVerticesWeights(weightsKey);
			assertFalse(gMasked.verticesWeightsKeys().contains(weightsKey));
			assertFalse(gOrig.verticesWeightsKeys().contains(weightsKey));
			assertNull(gMasked.verticesWeights(weightsKey));

			wMasked = gMasked.addVerticesWeights(weightsKey, weightsClass, weightsFactory.get());
			wOrig = gOrig.verticesWeights(weightsKey);
			assertNotNull(wOrig);
			assertEquals(wMasked.defaultWeightAsObj(), wOrig.defaultWeightAsObj());

			vertex = Graphs.randVertex(gMasked, rand);
			weight = weightsFactory.get();
			wMasked.setAsObj(vertex, weight);
			assertEquals(weight, wMasked.getAsObj(vertex));
			assertEquals(weight, wOrig.getAsObj(vertex));

			assertEquals(gMasked, gMasked.copy(true, true));
			assertEquals(gMasked, gMasked.immutableCopy(true, true));
		});
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Weights<Integer, T> wOrig = gOrig.addVerticesWeights(weightsKey, weightsClass, defaultValue);
			{
				for (Integer v : gOrig.vertices())
					assertEquals(defaultValue, wOrig.getAsObj(v));
				Map<Integer, T> expectedWeights = new Int2ObjectOpenHashMap<>();
				for (Integer v : gOrig.vertices()) {
					T w = weightsFactory.get();
					wOrig.setAsObj(v, w);
					expectedWeights.put(v, w);
				}
				for (Integer v : gOrig.vertices())
					assertEquals(expectedWeights.get(v), wOrig.getAsObj(v));
			}
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			IndexGraph gMasked = gMasked0.indexGraph();
			IndexIdMap<Integer> viMap = gMasked0.indexGraphVerticesMap();

			assertEquals(gOrig.verticesWeightsKeys(), gMasked.verticesWeightsKeys());
			IWeights<T> wMasked = gMasked.verticesWeights(weightsKey);

			for (int vIdx : gMasked.vertices())
				assertEquals(wOrig.getAsObj(viMap.indexToId(vIdx)), wMasked.getAsObj(vIdx));
			assertEquals(wOrig.defaultWeightAsObj(), wMasked.defaultWeightAsObj());

			int vertex = Graphs.randVertex(gMasked, rand);
			T weight = weightsFactory.get();
			wMasked.setAsObj(vertex, weight);
			assertEquals(weight, wMasked.getAsObj(vertex));
			assertEquals(weight, wOrig.getAsObj(viMap.indexToId(vertex)));

			assertTrue(gMasked.verticesWeightsKeys().contains(weightsKey));
			assertTrue(gOrig.verticesWeightsKeys().contains(weightsKey));
			gMasked.removeVerticesWeights(weightsKey);
			assertFalse(gMasked.verticesWeightsKeys().contains(weightsKey));
			assertFalse(gOrig.verticesWeightsKeys().contains(weightsKey));
			assertNull(gMasked.verticesWeights(weightsKey));

			wMasked = gMasked.addVerticesWeights(weightsKey, weightsClass, weightsFactory.get());
			wOrig = gOrig.verticesWeights(weightsKey);
			assertNotNull(wOrig);
			assertEquals(wMasked.defaultWeightAsObj(), wOrig.defaultWeightAsObj());

			vertex = Graphs.randVertex(gMasked, rand);
			weight = weightsFactory.get();
			wMasked.setAsObj(vertex, weight);
			assertEquals(weight, wMasked.getAsObj(vertex));
			assertEquals(weight, wOrig.getAsObj(viMap.indexToId(vertex)));

			assertEquals(gMasked, gMasked.copy(true, true));
			assertEquals(gMasked, gMasked.immutableCopy(true, true));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void edgesWeights() {
		final Random rand = new Random(0xbf867b44a84df10eL);
		testEdgesWeights(byte.class, weightFactoryByte(rand.nextLong()), (byte) 11, rand);
		testEdgesWeights(short.class, weightFactoryShort(rand.nextLong()), (short) 42, rand);
		testEdgesWeights(int.class, weightFactoryInt(rand.nextLong()), 33, rand);
		testEdgesWeights(long.class, weightFactoryLong(rand.nextLong()), (long) 5, rand);
		testEdgesWeights(float.class, weightFactoryFloat(rand.nextLong()), (float) 6.7, rand);
		testEdgesWeights(double.class, weightFactoryDouble(rand.nextLong()), 88.9, rand);
		testEdgesWeights(boolean.class, weightFactoryBool(rand.nextLong()), false, rand);
		testEdgesWeights(char.class, weightFactoryChar(rand.nextLong()), 'c', rand);
		testEdgesWeights(Object.class, weightFactoryObject(), null, rand);
	}

	private static <T> void testEdgesWeights(Class<T> weightsClass, Supplier<T> weightsFactory, T defaultValue,
			Random rand) {
		final String weightsKey = "eWeights";
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Weights<Integer, T> wOrig = gOrig.addEdgesWeights(weightsKey, weightsClass, defaultValue);
			{
				for (Integer e : gOrig.edges())
					assertEquals(defaultValue, wOrig.getAsObj(e));
				Map<Integer, T> expectedWeights = new Int2ObjectOpenHashMap<>();
				for (Integer e : gOrig.edges()) {
					T w = weightsFactory.get();
					wOrig.setAsObj(e, w);
					expectedWeights.put(e, w);
				}
				for (Integer e : gOrig.edges())
					assertEquals(expectedWeights.get(e), wOrig.getAsObj(e));
			}
			Graph<Integer, Integer> gMasked = maskedGraphView(gOrig, rand);

			assertEquals(gOrig.edgesWeightsKeys(), gMasked.edgesWeightsKeys());
			Weights<Integer, T> wMasked = gMasked.edgesWeights(weightsKey);

			for (Integer e : gMasked.edges())
				assertEquals(wOrig.getAsObj(e), wMasked.getAsObj(e));
			if (gOrig.edges().size() != gMasked.edges().size()) {
				Integer maskedEdge = gOrig.edges().stream().filter(e -> !gMasked.edges().contains(e)).findAny().get();
				final Weights<Integer, T> wMasked0 = wMasked;
				assertThrows(NoSuchEdgeException.class, () -> wMasked0.getAsObj(maskedEdge));
			}
			assertEquals(wOrig.defaultWeightAsObj(), wMasked.defaultWeightAsObj());

			Integer edge = Graphs.randEdge(gMasked, rand);
			T weight = weightsFactory.get();
			wMasked.setAsObj(edge, weight);
			assertEquals(weight, wMasked.getAsObj(edge));
			assertEquals(weight, wOrig.getAsObj(edge));

			assertTrue(gMasked.edgesWeightsKeys().contains(weightsKey));
			assertTrue(gOrig.edgesWeightsKeys().contains(weightsKey));
			gMasked.removeEdgesWeights(weightsKey);
			assertFalse(gMasked.edgesWeightsKeys().contains(weightsKey));
			assertFalse(gOrig.edgesWeightsKeys().contains(weightsKey));
			assertNull(gMasked.edgesWeights(weightsKey));

			wMasked = gMasked.addEdgesWeights(weightsKey, weightsClass, weightsFactory.get());
			wOrig = gOrig.edgesWeights(weightsKey);
			assertNotNull(wOrig);
			assertEquals(wMasked.defaultWeightAsObj(), wOrig.defaultWeightAsObj());

			edge = Graphs.randEdge(gMasked, rand);
			weight = weightsFactory.get();
			wMasked.setAsObj(edge, weight);
			assertEquals(weight, wMasked.getAsObj(edge));
			assertEquals(weight, wOrig.getAsObj(edge));

			assertEquals(gMasked, gMasked.copy(true, true));
			assertEquals(gMasked, gMasked.immutableCopy(true, true));
		});
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Weights<Integer, T> wOrig = gOrig.addEdgesWeights(weightsKey, weightsClass, defaultValue);
			{
				for (Integer e : gOrig.edges())
					assertEquals(defaultValue, wOrig.getAsObj(e));
				Map<Integer, T> expectedWeights = new Int2ObjectOpenHashMap<>();
				for (Integer e : gOrig.edges()) {
					T w = weightsFactory.get();
					wOrig.setAsObj(e, w);
					expectedWeights.put(e, w);
				}
				for (Integer e : gOrig.edges())
					assertEquals(expectedWeights.get(e), wOrig.getAsObj(e));
			}
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			IndexGraph gMasked = gMasked0.indexGraph();
			IndexIdMap<Integer> eiMap = gMasked0.indexGraphEdgesMap();

			assertEquals(gOrig.edgesWeightsKeys(), gMasked.edgesWeightsKeys());
			IWeights<T> wMasked = gMasked.edgesWeights(weightsKey);

			for (int e : gMasked.edges())
				assertEquals(wOrig.getAsObj(eiMap.indexToId(e)), wMasked.getAsObj(e));
			assertEquals(wOrig.defaultWeightAsObj(), wMasked.defaultWeightAsObj());

			int edge = Graphs.randEdge(gMasked, rand);
			T weight = weightsFactory.get();
			wMasked.setAsObj(edge, weight);
			assertEquals(weight, wMasked.getAsObj(edge));
			assertEquals(weight, wOrig.getAsObj(eiMap.indexToId(edge)));

			assertTrue(gMasked.edgesWeightsKeys().contains(weightsKey));
			assertTrue(gOrig.edgesWeightsKeys().contains(weightsKey));
			gMasked.removeEdgesWeights(weightsKey);
			assertFalse(gMasked.edgesWeightsKeys().contains(weightsKey));
			assertFalse(gOrig.edgesWeightsKeys().contains(weightsKey));
			assertNull(gMasked.edgesWeights(weightsKey));

			wMasked = gMasked.addEdgesWeights(weightsKey, weightsClass, weightsFactory.get());
			wOrig = gOrig.edgesWeights(weightsKey);
			assertNotNull(wOrig);
			assertEquals(wMasked.defaultWeightAsObj(), wOrig.defaultWeightAsObj());

			edge = Graphs.randEdge(gMasked, rand);
			weight = weightsFactory.get();
			wMasked.setAsObj(edge, weight);
			assertEquals(weight, wMasked.getAsObj(edge));
			assertEquals(weight, wOrig.getAsObj(eiMap.indexToId(edge)));

			assertEquals(gMasked, gMasked.copy(true, true));
			assertEquals(gMasked, gMasked.immutableCopy(true, true));
		});
	}

	@Test
	public void graphCapabilities() {
		final Random rand = new Random(0x544bcb6caec7f17L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;

			assertEqualsBool(gOrig.isAllowParallelEdges(), gMasked.isAllowParallelEdges());
			assertEqualsBool(gOrig.isAllowSelfEdges(), gMasked.isAllowSelfEdges());
			assertEqualsBool(gOrig.isDirected(), gMasked.isDirected());
		});
	}

	@Test
	public void verticesAndEdgesIndexMaps() {
		final Random rand = new Random(0x8b4dc5e9ed0aaf90L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, index, rand);
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			IndexGraph indexGraph = gMasked.indexGraph();
			IndexIdMap<Integer> viMap = gMasked.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = gMasked.indexGraphEdgesMap();
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.outEdges(v).stream()).collect(toSet()));
			maskedEdges.addAll(maskedVertices.stream().flatMap(v -> gOrig.inEdges(v).stream()).collect(toSet()));

			assertEquals(range(gMasked.vertices().size()), indexGraph.vertices());
			assertEquals(range(gMasked.edges().size()), indexGraph.edges());

			for (int vIdx : range(gMasked.vertices().size())) {
				Integer v = viMap.indexToId(vIdx);
				assertEquals(vIdx, viMap.idToIndex(v));
			}
			assertThrows(NoSuchVertexException.class, () -> viMap.indexToId(-1));
			assertThrows(NoSuchVertexException.class, () -> viMap.indexToId(gMasked.vertices().size()));
			assertThrows(NoSuchVertexException.class,
					() -> viMap.idToIndex(GraphsTestUtils.nonExistingVertex(gMasked, rand)));
			for (Integer v : maskedVertices)
				assertThrows(NoSuchVertexException.class, () -> viMap.idToIndex(v));
			assertNull(viMap.indexToIdIfExist(-1));
			assertNull(viMap.indexToIdIfExist(gMasked.vertices().size()));
			assertEquals(-1, viMap.idToIndexIfExist(GraphsTestUtils.nonExistingVertex(gMasked, rand)));
			for (Integer v : maskedVertices)
				assertEquals(-1, viMap.idToIndexIfExist(v));

			for (int eIdx : range(gMasked.edges().size())) {
				Integer e = eiMap.indexToId(eIdx);
				assertEquals(eIdx, eiMap.idToIndex(e));
			}
			assertThrows(NoSuchEdgeException.class, () -> eiMap.indexToId(-1));
			assertThrows(NoSuchEdgeException.class, () -> eiMap.indexToId(gMasked.edges().size()));
			assertThrows(NoSuchEdgeException.class,
					() -> eiMap.idToIndex(GraphsTestUtils.nonExistingEdge(gMasked, rand)));
			for (Integer e : maskedEdges)
				assertThrows(NoSuchEdgeException.class, () -> eiMap.idToIndex(e));
			assertNull(eiMap.indexToIdIfExist(-1));
			assertNull(eiMap.indexToIdIfExist(gMasked.edges().size()));
			assertEquals(-1, eiMap.idToIndexIfExist(GraphsTestUtils.nonExistingEdge(gMasked, rand)));
			for (Integer e : maskedEdges)
				assertEquals(-1, eiMap.idToIndexIfExist(e));
		});
	}

	@Test
	public void indexRemoveListeners() {
		/* can't real test anything, just cover and see no exception is thrown */
		final Random rand = new Random(0x44abaaaa77518f1bL);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			IndexGraph gMasked = maskedGraphView(gOrig, rand).indexGraph();

			IndexRemoveListener listener = new IndexRemoveListener() {
				@Override
				public void removeLast(int removedIdx) {}

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {}
			};

			gMasked.addVertexRemoveListener(listener);
			gMasked.removeVertexRemoveListener(listener);
			gMasked.addEdgeRemoveListener(listener);
			gMasked.removeEdgeRemoveListener(listener);
		});
	}

	@Test
	public void getIdBuilder() {
		/* can't real test anything, just cover and see no exception is thrown */
		final Random rand = new Random(0x9830ec4d9599cc8dL);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;
			gMasked.vertexBuilder();
			gMasked.edgeBuilder();
		});
	}

	@Test
	public void testEquals() {
		final Random rand = new Random(0x5fe6f377bb5a9893L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Set<Integer> maskedVertices = randSubSet(gOrig.vertices(), 0.1, rand);
			Set<Integer> maskedEdges = randSubSet(gOrig.edges(), 0.1, rand);
			Graph<Integer, Integer> gMasked1_ = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			Graph<Integer, Integer> gMasked2_ = gOrig.maskedGraphView(maskedVertices, maskedEdges);
			Graph<Integer, Integer> gMasked1 = maskedIndex ? gMasked1_.indexGraph() : gMasked1_;
			Graph<Integer, Integer> gMasked2 = maskedIndex ? gMasked2_.indexGraph() : gMasked2_;

			assertTrue(gMasked1.equals(gMasked2));
			assertTrue(gMasked1.equals(gMasked1.copy(true, true)));

			assertFalse(gMasked1.equals(null));
			assertFalse(gMasked1.equals(new Object()));
		});
	}

	@Test
	public void testHashCode() {
		final Random rand = new Random(0x7b01e41d83613b95L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;

			assertEquals(gMasked.hashCode(), gMasked.copy(true, true).hashCode());
		});
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		final Random rand = new Random(0x4b1082cb5439f6c9L);
		foreachBoolConfig((intGraph, directed, origIndex, maskedIndex) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph, origIndex, rand);
			Graph<Integer, Integer> gMasked0 = maskedGraphView(gOrig, rand);
			Graph<Integer, Integer> gMasked = maskedIndex ? gMasked0.indexGraph() : gMasked0;

			gMasked.ensureVertexCapacity(gMasked.vertices().size() + 10);
			gMasked.ensureEdgeCapacity(gMasked.edges().size() + 10);
		});
	}

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph, Random rand) {
		return createGraph(directed, intGraph, true, true, rand);
	}

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph, boolean parallelEdges,
			boolean selfEdges, Random rand) {
		final int n = 30, m = 200;

		GraphFactory<Integer, Integer> factory =
				intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
		Graph<Integer, Integer> g = factory.allowSelfEdges(selfEdges).allowParallelEdges(parallelEdges).newGraph();

		for (int i : range(n))
			g.addVertex(Integer.valueOf(i + 1));

		for (int i : range(m)) {
			Integer e = Integer.valueOf(i + 1);
			Integer u, v;
			for (int retry = 0;; retry++) {
				if (retry > 100)
					throw new IllegalStateException("Can't find edge after 100 retries");
				u = Graphs.randVertex(g, rand);
				v = Graphs.randVertex(g, rand);
				if (!parallelEdges && g.getEdge(u, v) != null)
					continue;
				if (!selfEdges && u.equals(v))
					continue;
				break;
			}
			g.addEdge(u, v, e);
		}
		return g;
	}

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph, boolean index, Random rand) {
		Graph<Integer, Integer> g = createGraph(directed, intGraph, rand);
		if (index)
			return g.indexGraph();
		return g;
	}

	private static Graph<Integer, Integer> maskedGraphView(Graph<Integer, Integer> g, Random rand) {
		Set<Integer> maskedVertices = randSubSet(g.vertices(), 0.1, rand);
		Set<Integer> maskedEdges = randSubSet(g.edges(), 0.1, rand);
		return g.maskedGraphView(maskedVertices, maskedEdges);
	}

}
