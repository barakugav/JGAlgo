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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.IterToolsTest;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class UndirectedViewTest extends TestBase {

	private static final String VerticesWeightsKey = "vWeights";
	private static final String EdgesWeightsKey = "eWeights";

	private static Graph<Integer, Integer> createGraph(boolean intGraph) {
		final long seed = 0x2f8451a6708986baL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;

		GraphFactory<Integer, Integer> factory = intGraph ? IntGraphFactory.directed() : GraphFactory.directed();
		Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		WeightsInt<Integer> vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i : range(n)) {
			Integer v = Integer.valueOf(i + 1);
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt<Integer> eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int i : range(m)) {
			Integer e = Integer.valueOf(i + 1);
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void testVertices() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			assertEquals(g.vertices().size(), undirectedG.vertices().size());
			assertEquals(g.vertices(), undirectedG.vertices());
		});
	}

	@Test
	public void testEdges() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			assertEquals(g.edges().size(), undirectedG.edges().size());
			assertEquals(g.edges(), undirectedG.edges());
		});
	}

	@Test
	public void testAddRemoveVertex() {
		final Random rand = new Random(0x151cb4a8c0092b5bL);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
			Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

			Integer newVertex;
			if (undirectedG instanceof IndexGraph) {
				newVertex = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG0, rand);

				/* index graphs should not support adding vertices with user defined identifiers */
				int newVertex0 = newVertex.intValue();
				if (newVertex0 != undirectedG.vertices().size())
					assertThrows(IllegalArgumentException.class, () -> ((IntGraph) undirectedG).addVertex(newVertex0));

				/* can't add new vertex directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = undirectedG0.indexGraphVerticesMap();

				undirectedG0.addVertex(newVertex);
				newVertex = Integer.valueOf(viMap.idToIndex(newVertex));

			} else if (undirectedG instanceof IntGraph) {
				newVertex = Integer.valueOf(((IntGraph) undirectedG).addVertexInt());
			} else {
				newVertex = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG, rand);
				undirectedG.addVertex(newVertex);
			}
			assertTrue(g.vertices().contains(newVertex));
			assertTrue(undirectedG.vertices().contains(newVertex));
			assertEquals(g.vertices(), undirectedG.vertices());

			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG0, rand);
			if (!(undirectedG instanceof IndexGraph)) {
				undirectedG.addVertex(nonExistingVertex);
				assertTrue(g.vertices().contains(nonExistingVertex));
				assertTrue(undirectedG.vertices().contains(nonExistingVertex));
				assertEquals(g.vertices(), undirectedG.vertices());
			}

			Integer vertexToRemove = undirectedG.vertices().iterator().next();
			undirectedG.removeVertex(vertexToRemove);
			if (!(undirectedG instanceof IndexGraph)) {
				assertFalse(g.vertices().contains(vertexToRemove));
				assertFalse(undirectedG.vertices().contains(vertexToRemove));
			}
			assertEquals(g.vertices(), undirectedG.vertices());
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0x94a8847b653dde07L);
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> gOrig = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();

			Integer nonExistingVertex1 = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG, rand);
			Integer nonExistingVertex2 = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG, rand);
			assert !nonExistingVertex1.equals(nonExistingVertex2);

			List<Integer> newVertices = List.of(nonExistingVertex1, nonExistingVertex2);
			undirectedG.addVertices(newVertices);
			assertTrue(gOrig.vertices().containsAll(newVertices));
			assertTrue(undirectedG.vertices().containsAll(newVertices));
			assertEquals(gOrig.vertices(), undirectedG.vertices());
		});
	}

	@Test
	public void removeVertices() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> gOrig = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();

			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer v1 = vit.next(), v2 = vit.next();
			List<Integer> verticesToRemove = List.of(v1, v2);
			undirectedG.removeVertices(verticesToRemove);
			for (Integer v : verticesToRemove) {
				assertFalse(undirectedG.vertices().contains(v));
				assertFalse(gOrig.vertices().contains(v));
			}
			assertEquals(gOrig.vertices(), undirectedG.vertices());
		});
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0x131a77e301a04258L);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
			Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

			Integer vertex = g.vertices().iterator().next();
			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(undirectedG, rand);

			if (index) {
				/* rename is not supported in index graphs */
				assertThrows(UnsupportedOperationException.class,
						() -> undirectedG.renameVertex(vertex, nonExistingVertex));
				return;
			}

			Graph<Integer, Integer> gExpected = g.copy(true, true);
			Graph<Integer, Integer> undirectedGExpected = undirectedG.copy(true, true);
			gExpected.renameVertex(vertex, nonExistingVertex);
			undirectedGExpected.renameVertex(vertex, nonExistingVertex);

			undirectedG.renameVertex(vertex, nonExistingVertex);

			assertEquals(undirectedGExpected, undirectedG);
			assertEquals(gExpected, g);
		});
	}

	@Test
	public void testAddRemoveEdge() {
		final Random rand = new Random(0xd3534a50a7d6c94L);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
			Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer newEdge;
			if (undirectedG instanceof IndexGraph) {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG0, rand);

				/* index graphs should not support adding edges with user defined identifiers */
				int newEdge0 = newEdge.intValue();
				if (newEdge0 != undirectedG.edges().size())
					assertThrows(IllegalArgumentException.class,
							() -> ((IntGraph) undirectedG).addEdge(u.intValue(), v.intValue(), newEdge0));

				/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = undirectedG0.indexGraphVerticesMap();
				IndexIdMap<Integer> eiMap = undirectedG0.indexGraphEdgesMap();
				undirectedG0.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
				newEdge = Integer.valueOf(eiMap.idToIndex(newEdge));

			} else if (undirectedG instanceof IntGraph) {
				newEdge = Integer.valueOf(((IntGraph) undirectedG).addEdge(u.intValue(), v.intValue()));
			} else {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG, rand);
				undirectedG.addEdge(u, v, newEdge);
			}
			assertTrue(g.edges().contains(newEdge));
			assertTrue(undirectedG.edges().contains(newEdge));
			assertEquals(g.edges(), undirectedG.edges());

			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG, rand);
			if (!(undirectedG instanceof IndexGraph)) {
				undirectedG.addEdge(u, v, nonExistingEdge);
				assertTrue(g.edges().contains(nonExistingEdge));
				assertTrue(undirectedG.edges().contains(nonExistingEdge));
				assertEquals(g.edges(), undirectedG.edges());
			}

			Integer edgeToRemove = undirectedG.edges().iterator().next();
			undirectedG.removeEdge(edgeToRemove);
			if (!(undirectedG instanceof IndexGraph)) {
				assertFalse(g.edges().contains(edgeToRemove));
				assertFalse(undirectedG.edges().contains(edgeToRemove));
			}
			assertEquals(g.edges(), undirectedG.edges());
		});
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0xf6410f5e2e8c472dL);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
			Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer newEdge;
			if (undirectedG instanceof IndexGraph) {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG0, rand);

				/* index graphs should not support adding edges with user defined identifiers */
				int newEdge0 = newEdge.intValue();
				if (newEdge0 != undirectedG.edges().size()) {
					IntGraph gTemp = IntGraph.newDirected();
					gTemp.addVertices(IntList.of(u.intValue(), v.intValue()));
					gTemp.addEdge(u.intValue(), v.intValue(), newEdge0);
					IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);
					assertThrows(IllegalArgumentException.class, () -> ((IndexGraph) undirectedG).addEdges(edgesToAdd));
				}

				/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = undirectedG0.indexGraphVerticesMap();
				IndexIdMap<Integer> eiMap = undirectedG0.indexGraphEdgesMap();
				Graph<Integer, Integer> gTemp = IntGraph.newDirected();
				gTemp.addVertices(List.of(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue())));
				gTemp.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
				EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gTemp);
				undirectedG0.addEdges(edgesToAdd);
				newEdge = Integer.valueOf(eiMap.idToIndex(newEdge));

			} else {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG, rand);

				IntGraph gTemp = IntGraph.newDirected();
				gTemp.addVertices(IntList.of(u.intValue(), v.intValue()));
				gTemp.addEdge(u.intValue(), v.intValue(), newEdge.intValue());
				IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);
				undirectedG.addEdges(edgesToAdd);
			}
			assertTrue(g.edges().contains(newEdge));
			assertTrue(undirectedG.edges().contains(newEdge));
			assertEquals(g.edges(), undirectedG.edges());
		});
	}

	@Test
	public void addEdgesReassignIds() {
		IndexGraph gOrig = IndexGraph.newDirected();
		gOrig.addVertices(range(10));
		IndexGraph undirectedG = gOrig.undirectedView();

		IntGraph gTemp = IntGraph.newDirected();
		gTemp.addVertices(range(10));
		gTemp.addEdge(0, 1, 111121);
		gTemp.addEdge(0, 2, 3252);
		gTemp.addEdge(5, 3, 546854);
		IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);

		undirectedG.addEdgesReassignIds(edgesToAdd);

		IndexGraph expected = IndexGraph.newUndirected();
		expected.addVertices(range(10));
		for (int e : gTemp.edges())
			expected.addEdge(gTemp.edgeSource(e), gTemp.edgeTarget(e));

		assertEquals(expected, undirectedG);
	}

	@Test
	public void removeEdges() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> gOrig = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();

			Iterator<Integer> eit = undirectedG.edges().iterator();
			Integer e1 = eit.next(), e2 = eit.next();
			List<Integer> edgesToRemove = List.of(e1, e2);
			undirectedG.removeEdges(edgesToRemove);
			for (Integer e : edgesToRemove) {
				assertFalse(undirectedG.edges().contains(e));
				assertFalse(gOrig.edges().contains(e));
			}
			assertEquals(gOrig.edges(), undirectedG.edges());
		});
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0x416dbed48feaefc6L);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
			Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

			Integer edge = g.edges().iterator().next();
			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(undirectedG, rand);

			if (index) {
				/* rename is not supported in index graphs */
				assertThrows(UnsupportedOperationException.class, () -> undirectedG.renameEdge(edge, nonExistingEdge));
				return;
			}

			Graph<Integer, Integer> gExpected = g.copy(true, true);
			Graph<Integer, Integer> undirectedGExpected = undirectedG.copy(true, true);
			gExpected.renameEdge(edge, nonExistingEdge);
			undirectedGExpected.renameEdge(edge, nonExistingEdge);

			undirectedG.renameEdge(edge, nonExistingEdge);

			assertEquals(undirectedGExpected, undirectedG);
			assertEquals(gExpected, g);
		});
	}

	@Test
	public void edgesOutIn() {
		final Random rand = new Random(0x18488a1e7889a7aL);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			for (Integer u : undirectedG.vertices()) {
				Set<Integer> expectedEdges = new IntOpenHashSet();
				expectedEdges.addAll(g.outEdges(u));
				expectedEdges.addAll(g.inEdges(u));
				EdgeSet<Integer, Integer> edges = undirectedG.outEdges(u);
				assertEquals(expectedEdges.size(), edges.size());
				assertEquals(expectedEdges, edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(u, eit.source());
					assertEquals(g.edgeEndpoint(e, u), eit.target());
					assertEquals(undirectedG.edgeEndpoint(e, u), eit.target());
					assertEquals(u, undirectedG.edgeEndpoint(e, eit.target()));

					iteratedEdges.add(e);
				}

				assertEquals(edges.size(), iteratedEdges.size());
				assertEquals(edges, iteratedEdges);
				for (Integer e : g.edges())
					assertEqualsBool(iteratedEdges.contains(e), edges.contains(e));
				assertFalse(edges.contains(GraphsTestUtils.nonExistingEdge(undirectedG, rand)));
			}
			for (Integer v : undirectedG.vertices()) {
				Set<Integer> expectedEdges = new IntOpenHashSet();
				expectedEdges.addAll(g.outEdges(v));
				expectedEdges.addAll(g.inEdges(v));
				EdgeSet<Integer, Integer> edges = undirectedG.inEdges(v);
				assertEquals(expectedEdges.size(), edges.size());
				assertEquals(expectedEdges, edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(v, eit.target());
					assertEquals(g.edgeEndpoint(e, v), eit.source());
					assertEquals(undirectedG.edgeEndpoint(e, v), eit.source());
					assertEquals(v, undirectedG.edgeEndpoint(e, eit.source()));

					iteratedEdges.add(e);
				}

				assertEquals(edges.size(), iteratedEdges.size());
				assertEquals(edges, iteratedEdges);
				for (Integer e : g.edges())
					assertEqualsBool(iteratedEdges.contains(e), edges.contains(e));
				assertFalse(edges.contains(GraphsTestUtils.nonExistingEdge(undirectedG, rand)));
			}

			for (Integer u : undirectedG.vertices()) {
				foreachBoolConfig(out -> {
					Set<Integer> edges = out ? undirectedG.outEdges(u) : undirectedG.inEdges(u);
					IterToolsTest.testIterSkip(edges, rand);
				});
			}
		});
	}

	@Test
	public void getEdges() {
		final Random rand = new Random(0xa49851434b3778e7L);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			for (Integer u : undirectedG.vertices()) {
				for (Integer v : undirectedG.vertices()) {
					Set<Integer> expectedEdges = new IntOpenHashSet();
					expectedEdges.addAll(g.getEdges(u, v));
					expectedEdges.addAll(g.getEdges(v, u));
					EdgeSet<Integer, Integer> edges = undirectedG.getEdges(u, v);
					assertEquals(expectedEdges.size(), edges.size());
					assertEquals(expectedEdges, edges);

					if (edges.isEmpty()) {
						assertFalse(undirectedG.containsEdge(u, v));
					} else {
						Integer e = undirectedG.getEdge(u, v);
						assertNotNull(e);
						assertTrue(edges.contains(e));
					}

					for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(v, eit.target());
						assertEquals(g.edgeEndpoint(e, u), v);
						assertEquals(g.edgeEndpoint(e, v), u);
						assertEquals(u, undirectedG.edgeEndpoint(e, v));
						assertEquals(v, undirectedG.edgeEndpoint(e, u));
					}

					IterToolsTest.testIterSkip(edges, rand);
				}
			}
		});
	}

	@Test
	public void testRemoveEdgesOf() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer v1 = vit.next();
			Integer v2 = vit.next();

			undirectedG.removeEdgesOf(v1);
			assertTrue(undirectedG.outEdges(v1).isEmpty());
			assertTrue(undirectedG.inEdges(v1).isEmpty());
			assertTrue(g.outEdges(v1).isEmpty());
			assertTrue(g.inEdges(v1).isEmpty());

			g.removeEdgesOf(v2);
			assertTrue(undirectedG.outEdges(v2).isEmpty());
			assertTrue(undirectedG.inEdges(v2).isEmpty());
			assertTrue(g.outEdges(v2).isEmpty());
			assertTrue(g.inEdges(v2).isEmpty());
		});
	}

	@Test
	public void testRemoveEdgesInOf() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer v1 = vit.next();
			Integer v2 = vit.next();
			Integer v3 = vit.next();
			Integer v4 = vit.next();

			undirectedG.removeInEdgesOf(v1);
			assertTrue(undirectedG.outEdges(v1).isEmpty());
			assertTrue(undirectedG.inEdges(v1).isEmpty());
			assertTrue(g.outEdges(v1).isEmpty());
			assertTrue(g.inEdges(v1).isEmpty());

			undirectedG.inEdges(v2).clear();
			assertTrue(undirectedG.outEdges(v2).isEmpty());
			assertTrue(undirectedG.inEdges(v2).isEmpty());
			assertTrue(g.outEdges(v2).isEmpty());
			assertTrue(g.inEdges(v2).isEmpty());

			if (!index) {
				undirectedG.inEdges(v3).removeAll(new IntArrayList(undirectedG.inEdges(v3)));
				assertTrue(undirectedG.outEdges(v3).isEmpty());
				assertTrue(undirectedG.inEdges(v3).isEmpty());
				assertTrue(g.outEdges(v3).isEmpty());
				assertTrue(g.inEdges(v3).isEmpty());

				for (Integer e : new IntArrayList(undirectedG.inEdges(v4)))
					undirectedG.inEdges(v4).remove(e);
				assertTrue(undirectedG.outEdges(v4).isEmpty());
				assertTrue(undirectedG.inEdges(v4).isEmpty());
				assertTrue(g.outEdges(v4).isEmpty());
				assertTrue(g.inEdges(v4).isEmpty());

				/* remove non existing edges */
				assertFalse(g.outEdges(v4).remove(Integer.valueOf(54698156)));
				assertFalse(g.inEdges(v4).remove(Integer.valueOf(-98985)));
			}

			Integer v5 = null;
			while (vit.hasNext()) {
				v5 = vit.next();
				if (!g.outEdges(v5).isEmpty() && !g.outEdges(v5).isEmpty())
					break;
			}
			if (v5 != null) {
				g.removeInEdgesOf(v5);
				assertFalse(undirectedG.outEdges(v5).isEmpty());
				assertFalse(undirectedG.inEdges(v5).isEmpty());
				assertFalse(g.outEdges(v5).isEmpty());
				assertTrue(g.inEdges(v5).isEmpty());
			}
		});
	}

	@Test
	public void testRemoveEdgesOutOf() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			Iterator<Integer> vit = undirectedG.vertices().iterator();
			Integer v1 = vit.next();
			Integer v2 = vit.next();
			Integer v3 = vit.next();
			Integer v4 = vit.next();

			undirectedG.removeOutEdgesOf(v1);
			assertTrue(undirectedG.outEdges(v1).isEmpty());
			assertTrue(undirectedG.inEdges(v1).isEmpty());
			assertTrue(g.outEdges(v1).isEmpty());
			assertTrue(g.inEdges(v1).isEmpty());

			undirectedG.outEdges(v2).clear();
			assertTrue(undirectedG.outEdges(v2).isEmpty());
			assertTrue(undirectedG.inEdges(v2).isEmpty());
			assertTrue(g.outEdges(v2).isEmpty());
			assertTrue(g.inEdges(v2).isEmpty());

			if (!index) {
				undirectedG.outEdges(v3).removeAll(new IntArrayList(undirectedG.outEdges(v3)));
				assertTrue(undirectedG.outEdges(v3).isEmpty());
				assertTrue(undirectedG.inEdges(v3).isEmpty());
				assertTrue(g.outEdges(v3).isEmpty());
				assertTrue(g.inEdges(v3).isEmpty());

				for (Integer e : new IntArrayList(undirectedG.outEdges(v4)))
					undirectedG.outEdges(v4).remove(e);
				assertTrue(undirectedG.outEdges(v4).isEmpty());
				assertTrue(undirectedG.inEdges(v4).isEmpty());
				assertTrue(g.outEdges(v4).isEmpty());
				assertTrue(g.inEdges(v4).isEmpty());
			}

			Integer v5 = null;
			while (vit.hasNext()) {
				v5 = vit.next();
				if (!g.outEdges(v5).isEmpty() && !g.outEdges(v5).isEmpty())
					break;
			}
			if (v5 != null) {
				g.removeOutEdgesOf(v5);
				assertFalse(undirectedG.outEdges(v5).isEmpty());
				assertFalse(undirectedG.inEdges(v5).isEmpty());
				assertTrue(g.outEdges(v5).isEmpty());
				assertFalse(g.inEdges(v5).isEmpty());
			}
		});
	}

	@Test
	public void moveEdge() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			Integer e = undirectedG.edges().iterator().next();
			Integer newSource = undirectedG.edgeTarget(e), newTarget = undirectedG.vertices().iterator().next();

			undirectedG.moveEdge(e, newSource, newTarget);
			assertEquals(newSource, undirectedG.edgeSource(e));
			assertEquals(newTarget, undirectedG.edgeTarget(e));
		});
	}

	@Test
	public void testEdgeGetSourceTarget() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			for (Integer e : undirectedG.edges()) {
				Integer s1 = g.edgeSource(e), t1 = g.edgeTarget(e);
				Integer s2 = undirectedG.edgeSource(e), t2 = undirectedG.edgeTarget(e);
				assertTrue((s1 == s2 && t1 == t2) || (s1 == t2 && t1 == s2));
			}
		});
	}

	@Test
	public void testClear() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			undirectedG.clear();
			assertTrue(undirectedG.vertices().isEmpty());
			assertTrue(undirectedG.edges().isEmpty());
			assertTrue(g.vertices().isEmpty());
			assertTrue(g.edges().isEmpty());
		});
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			g.clear();
			assertTrue(undirectedG.vertices().isEmpty());
			assertTrue(undirectedG.edges().isEmpty());
			assertTrue(g.vertices().isEmpty());
			assertTrue(g.edges().isEmpty());
		});
	}

	@Test
	public void testClearEdges() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			Set<Integer> verticesBeforeClear = new HashSet<>(g.vertices());
			undirectedG.clearEdges();
			assertTrue(undirectedG.edges().isEmpty());
			assertTrue(g.edges().isEmpty());
			assertEquals(verticesBeforeClear, undirectedG.vertices());
			assertEquals(verticesBeforeClear, g.vertices());
		});
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			Set<Integer> verticesBeforeClear = new HashSet<>(g.vertices());
			g.clearEdges();
			assertTrue(undirectedG.edges().isEmpty());
			assertTrue(g.edges().isEmpty());
			assertEquals(verticesBeforeClear, undirectedG.vertices());
			assertEquals(verticesBeforeClear, g.vertices());
		});
	}

	@Test
	public void testVerticesWeights() {
		final long seed = 0xf8c0980b446e4bfdL;
		Random rand = new Random(seed);
		AtomicInteger keyCounter = new AtomicInteger();
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			String key1 = "key" + keyCounter.getAndIncrement(), key2 = "key" + keyCounter.getAndIncrement();
			{
				WeightsInt<Integer> vWeights1 = g.addVerticesWeights(key1, int.class);
				for (Integer v : g.vertices())
					vWeights1.set(v, rand.nextInt(10000));
				WeightsInt<Integer> vWeights2 = undirectedG.addVerticesWeights(key2, int.class);
				for (Integer v : undirectedG.vertices())
					vWeights2.set(v, rand.nextInt(10000));
			}

			assertEquals(g.verticesWeightsKeys(), undirectedG.verticesWeightsKeys());
			for (String key : List.of(key1, key2)) {
				WeightsInt<Integer> wOrig = g.verticesWeights(key);
				WeightsInt<Integer> wUnd = undirectedG.verticesWeights(key);

				for (Integer v : undirectedG.vertices())
					assertEquals(wOrig.get(v), wUnd.get(v));
				assertEquals(wOrig.defaultWeight(), wUnd.defaultWeight());
			}

			undirectedG.removeVerticesWeights(key1);
			assertEquals(g.verticesWeightsKeys(), undirectedG.verticesWeightsKeys());
		});
	}

	@Test
	public void testEdgesWeights() {
		final long seed = 0xc017705f906caff1L;
		Random rand = new Random(seed);
		AtomicInteger keyCounter = new AtomicInteger();
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			String key1 = "key" + keyCounter.getAndIncrement(), key2 = "key" + keyCounter.getAndIncrement();
			{
				WeightsInt<Integer> eWeights1 = g.addEdgesWeights(key1, int.class);
				for (Integer e : g.edges())
					eWeights1.set(e, rand.nextInt(10000));
				WeightsInt<Integer> eWeights2 = undirectedG.addEdgesWeights(key2, int.class);
				for (Integer e : undirectedG.edges())
					eWeights2.set(e, rand.nextInt(10000));
			}

			assertEquals(g.edgesWeightsKeys(), undirectedG.edgesWeightsKeys());
			for (String key : List.of(key1, key2)) {
				WeightsInt<Integer> wOrig = g.edgesWeights(key);
				WeightsInt<Integer> wUnd = undirectedG.edgesWeights(key);

				for (Integer e : undirectedG.edges())
					assertEquals(wOrig.get(e), wUnd.get(e));
				assertEquals(wOrig.defaultWeight(), wUnd.defaultWeight());
			}

			undirectedG.removeEdgesWeights(key1);
			assertEquals(g.edgesWeightsKeys(), undirectedG.edgesWeightsKeys());
		});
	}

	@Test
	public void testGraphCapabilities() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			assertEqualsBool(g.isAllowSelfEdges(), undirectedG.isAllowSelfEdges());
			assertFalse(undirectedG.isDirected());
			assertTrue(undirectedG.isAllowParallelEdges());
		});
	}

	@Test
	public void testUndirectedViewOfUndirectedView() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();
			assertTrue(undirectedG == undirectedG.undirectedView());
		});
	}

	@Test
	public void verticesAndEdgesIndexMaps() {
		final long seed = 0x886220cddb50189dL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> gOrig = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();

			IndexIdMap<Integer> origViMap = gOrig.indexGraphVerticesMap();
			IndexIdMap<Integer> origEiMap = gOrig.indexGraphEdgesMap();
			IndexIdMap<Integer> undirectedViMap = undirectedG.indexGraphVerticesMap();
			IndexIdMap<Integer> undirectedEiMap = undirectedG.indexGraphEdgesMap();

			for (Integer v : gOrig.vertices()) {
				assertEquals(origViMap.idToIndex(v), undirectedViMap.idToIndex(v));
				assertEquals(origViMap.idToIndexIfExist(v), undirectedViMap.idToIndexIfExist(v));
			}
			for (int i = 0; i < 10; i++) {
				Integer v = Integer.valueOf(rand.nextInt());
				assertEquals(origViMap.idToIndexIfExist(v), undirectedViMap.idToIndexIfExist(v));
			}
			for (int vIdx : gOrig.indexGraph().vertices()) {
				assertEquals(origViMap.indexToId(vIdx), undirectedViMap.indexToId(vIdx));
				assertEquals(origViMap.indexToIdIfExist(vIdx), undirectedViMap.indexToIdIfExist(vIdx));
			}
			for (int i = 0; i < 10; i++) {
				int vIdx = rand.nextInt();
				assertEquals(origViMap.indexToIdIfExist(vIdx), undirectedViMap.indexToIdIfExist(vIdx));
			}

			for (Integer e : gOrig.edges()) {
				assertEquals(origEiMap.idToIndex(e), undirectedEiMap.idToIndex(e));
				assertEquals(origEiMap.idToIndexIfExist(e), undirectedEiMap.idToIndexIfExist(e));
			}
			for (int i = 0; i < 10; i++) {
				Integer e = Integer.valueOf(rand.nextInt());
				assertEquals(origEiMap.idToIndexIfExist(e), undirectedEiMap.idToIndexIfExist(e));
			}
			for (int eIdx : gOrig.indexGraph().edges()) {
				assertEquals(origEiMap.indexToId(eIdx), undirectedEiMap.indexToId(eIdx));
				assertEquals(origEiMap.indexToIdIfExist(eIdx), undirectedEiMap.indexToIdIfExist(eIdx));
			}
			for (int i = 0; i < 10; i++) {
				int eIdx = rand.nextInt();
				assertEquals(origEiMap.indexToIdIfExist(eIdx), undirectedEiMap.indexToIdIfExist(eIdx));
			}
		});
	}

	@Test
	public void testRemoveListeners() {
		foreachBoolConfig(intGraph -> {
			IndexGraph undirectedG = createGraph(intGraph).indexGraph().undirectedView();
			AtomicBoolean called = new AtomicBoolean();
			IndexRemoveListener listener = new IndexRemoveListener() {
				@Override
				public void removeLast(int removedIdx) {
					called.set(true);
				}

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {
					called.set(true);
				}
			};

			undirectedG.addVertexRemoveListener(listener);
			called.set(false);
			undirectedG.removeVertex(undirectedG.vertices().iterator().nextInt());
			assertTrue(called.get());

			called.set(false);
			undirectedG.removeEdge(undirectedG.edges().iterator().nextInt());
			assertFalse(called.get());

			undirectedG.removeVertexRemoveListener(listener);
			called.set(false);
			undirectedG.removeVertex(undirectedG.vertices().iterator().nextInt());
			assertFalse(called.get());

			undirectedG.addEdgeRemoveListener(listener);
			called.set(false);
			undirectedG.removeEdge(undirectedG.edges().iterator().nextInt());
			assertTrue(called.get());

			int v = undirectedG.vertices().iterator().nextInt();
			undirectedG.removeEdgesOf(v);
			called.set(false);
			undirectedG.removeVertex(v);
			assertFalse(called.get());

			undirectedG.removeEdgeRemoveListener(listener);
			called.set(false);
			undirectedG.removeEdge(undirectedG.edges().iterator().nextInt());
			assertFalse(called.get());
		});
	}

	@Test
	public void vertexBuilder() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> gOrig = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();
			assertTrue(gOrig.vertexBuilder() == undirectedG.vertexBuilder());
		});
	}

	@Test
	public void edgeBuilder() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> gOrig = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = gOrig.undirectedView();
			assertTrue(gOrig.edgeBuilder() == undirectedG.edgeBuilder());
		});
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
			Graph<Integer, Integer> undirectedG = g.undirectedView();

			undirectedG.ensureVertexCapacity(undirectedG.vertices().size() + 10);
			undirectedG.ensureEdgeCapacity(undirectedG.edges().size() + 10);
		});
	}

}
