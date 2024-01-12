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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ReversedGraphViewTest extends TestBase {

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph) {
		final long seed = 0x97dc96ffefd7165bL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;

		GraphFactory<Integer, Integer> factory =
				intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
		Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		g.addVertices(range(1, n + 1));
		for (int i : range(m))
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), Integer.valueOf(i + 1));
		return g;
	}

	@Test
	public void testVertices() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			assertEquals(gOrig.vertices().size(), gRev.vertices().size());
			assertEquals(gOrig.vertices(), gRev.vertices());
		});
	}

	@Test
	public void testEdges() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			assertEquals(gOrig.edges().size(), gRev.edges().size());
			assertEquals(gOrig.edges(), gRev.edges());
		});
	}

	@Test
	public void testAddRemoveVertex() {
		final Random rand = new Random(0xda1132baabc04450L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Integer newVertex;
			if (gRev instanceof IndexGraph) {
				newVertex = GraphsTestUtils.nonExistingVertexNonNegative(gRev0, rand);

				/* index graphs should not support adding vertices with user defined identifiers */
				int newVertex0 = newVertex.intValue();
				if (newVertex0 != gRev.vertices().size())
					assertThrows(IllegalArgumentException.class, () -> ((IntGraph) gRev).addVertex(newVertex0));

				/* can't add new vertex directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = gRev0.indexGraphVerticesMap();
				gRev0.addVertex(newVertex);
				newVertex = Integer.valueOf(viMap.idToIndex(newVertex));

			} else if (gRev instanceof IntGraph) {
				newVertex = Integer.valueOf(((IntGraph) gRev).addVertexInt());
			} else {
				newVertex = GraphsTestUtils.nonExistingVertexNonNegative(gRev, rand);
				gRev.addVertex(newVertex);
			}
			assertTrue(gOrig.vertices().contains(newVertex));
			assertTrue(gRev.vertices().contains(newVertex));
			assertEquals(gOrig.vertices(), gRev.vertices());

			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(gRev, rand);
			if (!(gRev instanceof IndexGraph)) {
				gRev.addVertex(nonExistingVertex);
				assertTrue(gOrig.vertices().contains(nonExistingVertex));
				assertTrue(gRev.vertices().contains(nonExistingVertex));
				assertEquals(gOrig.vertices(), gRev.vertices());
			}

			Integer vertexToRemove = gRev.vertices().iterator().next();
			gRev.removeVertex(vertexToRemove);
			if (!(gRev instanceof IndexGraph)) {
				assertFalse(gOrig.vertices().contains(vertexToRemove));
				assertFalse(gRev.vertices().contains(vertexToRemove));
			}
			assertEquals(gOrig.vertices(), gRev.vertices());
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0xdaa7a8e62069d2b1L);
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();

			Integer nonExistingVertex1 = GraphsTestUtils.nonExistingVertexNonNegative(gRev, rand);
			Integer nonExistingVertex2 = GraphsTestUtils.nonExistingVertexNonNegative(gRev, rand);
			assert !nonExistingVertex1.equals(nonExistingVertex2) : "what are the chances? 2^-31.";

			List<Integer> newVertices = List.of(nonExistingVertex1, nonExistingVertex2);
			gRev.addVertices(newVertices);
			assertTrue(gOrig.vertices().containsAll(newVertices));
			assertTrue(gRev.vertices().containsAll(newVertices));
			assertEquals(gOrig.vertices(), gRev.vertices());
		});
	}

	@Test
	public void removeVertices() {
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();

			Iterator<Integer> vit = gRev.vertices().iterator();
			Integer v1 = vit.next(), v2 = vit.next();
			List<Integer> verticesToRemove = List.of(v1, v2);
			gRev.removeVertices(verticesToRemove);
			for (Integer v : verticesToRemove) {
				assertFalse(gRev.vertices().contains(v));
				assertFalse(gOrig.vertices().contains(v));
			}
			assertEquals(gOrig.vertices(), gRev.vertices());
		});
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0xd67c0c828d5b5d91L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(gRev, rand);
			Integer vertex = gOrig.vertices().iterator().next();

			if (index) {
				/* rename is not supported in index graphs */
				assertThrows(UnsupportedOperationException.class, () -> gRev.renameVertex(vertex, nonExistingVertex));
				return;
			}

			Graph<Integer, Integer> gExpected = gOrig.copy(true, true);
			Graph<Integer, Integer> gRevExpected = gRev.copy(true, true);
			gExpected.renameVertex(vertex, nonExistingVertex);
			gRevExpected.renameVertex(vertex, nonExistingVertex);

			gRev.renameVertex(vertex, nonExistingVertex);

			assertEquals(gRevExpected, gRev);
			assertEquals(gExpected, gOrig);
		});
	}

	@Test
	public void testAddRemoveEdge() {
		final Random rand = new Random(0x1dc9e6ab1fbd5897L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Iterator<Integer> vit = gRev.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer newEdge;
			if (gRev instanceof IndexGraph) {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev0, rand);

				/* index graphs should not support adding edges with user defined identifiers */
				int newEdge0 = newEdge.intValue();
				if (newEdge0 != gRev.edges().size())
					assertThrows(IllegalArgumentException.class,
							() -> ((IntGraph) gRev).addEdge(u.intValue(), v.intValue(), newEdge0));

				/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = gRev0.indexGraphVerticesMap();
				IndexIdMap<Integer> eiMap = gRev0.indexGraphEdgesMap();
				gRev0.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
				newEdge = Integer.valueOf(eiMap.idToIndex(newEdge));

			} else if (gRev instanceof IntGraph) {
				newEdge = Integer.valueOf(((IntGraph) gRev).addEdge(u.intValue(), v.intValue()));
			} else {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev, rand);
				gRev.addEdge(u, v, newEdge);
			}
			assertTrue(gOrig.edges().contains(newEdge));
			assertTrue(gRev.edges().contains(newEdge));
			assertEquals(gOrig.edges(), gRev.edges());

			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev, rand);
			if (!(gRev instanceof IndexGraph)) {
				gRev.addEdge(u, v, nonExistingEdge);
				assertTrue(gOrig.edges().contains(nonExistingEdge));
				assertTrue(gRev.edges().contains(nonExistingEdge));
				assertEquals(gOrig.edges(), gRev.edges());
			}

			Integer edgeToRemove = gRev.edges().iterator().next();
			gRev.removeEdge(edgeToRemove);
			if (!(gRev instanceof IndexGraph)) {
				assertFalse(gOrig.edges().contains(edgeToRemove));
				assertFalse(gRev.edges().contains(edgeToRemove));
			}
			assertEquals(gOrig.edges(), gRev.edges());
		});
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0x4bcafff33afc70f7L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Iterator<Integer> vit = gRev.vertices().iterator();
			Integer u = vit.next();
			Integer v = vit.next();

			Integer newEdge;
			if (gRev instanceof IndexGraph) {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev0, rand);

				/* index graphs should not support adding edges with user defined identifiers */
				if (newEdge.intValue() != gRev.edges().size()) {
					IntGraph gTemp = IntGraph.newDirected();
					gTemp.addVertices(IntList.of(u.intValue(), v.intValue()));
					gTemp.addEdge(u.intValue(), v.intValue(), newEdge.intValue());
					IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);
					assertThrows(IllegalArgumentException.class, () -> ((IndexGraph) gRev).addEdges(edgesToAdd));
				}

				/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
				IndexIdMap<Integer> viMap = gRev0.indexGraphVerticesMap();
				IndexIdMap<Integer> eiMap = gRev0.indexGraphEdgesMap();
				Graph<Integer, Integer> gTemp = IntGraph.newDirected();
				gTemp.addVertices(List.of(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue())));
				gTemp.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
				EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gTemp);
				gRev0.addEdges(edgesToAdd);
				newEdge = Integer.valueOf(eiMap.idToIndex(newEdge));

			} else {
				newEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev, rand);

				IntGraph gTemp = IntGraph.newDirected();
				gTemp.addVertices(IntList.of(u.intValue(), v.intValue()));
				gTemp.addEdge(u.intValue(), v.intValue(), newEdge.intValue());
				IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);
				gRev.addEdges(edgesToAdd);
			}
			assertTrue(gOrig.edges().contains(newEdge));
			assertTrue(gRev.edges().contains(newEdge));
			assertEquals(gOrig.edges(), gRev.edges());
		});
	}

	@Test
	public void addEdgesReassignIds() {
		foreachBoolConfig(directed -> {
			IndexGraph gOrig = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
			gOrig.addVertices(range(10));
			IndexGraph gRev = gOrig.reverseView();

			IntGraph gTemp = IntGraph.newDirected();
			gTemp.addVertices(range(10));
			gTemp.addEdge(0, 1, 111121);
			gTemp.addEdge(0, 2, 3252);
			gTemp.addEdge(5, 3, 546854);
			IEdgeSet edgesToAdd = IEdgeSet.allOf(gTemp);

			gRev.addEdgesReassignIds(edgesToAdd);

			IndexGraph expectedOrig = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
			expectedOrig.addVertices(range(10));
			for (int e : gTemp.edges())
				expectedOrig.addEdge(gTemp.edgeTarget(e), gTemp.edgeSource(e));
			IndexGraph expected = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
			expected.addVertices(range(10));
			for (int e : gTemp.edges())
				expected.addEdge(gTemp.edgeSource(e), gTemp.edgeTarget(e));

			assertEquals(expectedOrig, gOrig);
			assertEquals(expected, gRev);
		});
	}

	@Test
	public void removeEdges() {
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();

			Iterator<Integer> eit = gRev.edges().iterator();
			Integer e1 = eit.next(), e2 = eit.next();
			List<Integer> edgesToRemove = List.of(e1, e2);
			gRev.removeEdges(edgesToRemove);
			for (Integer e : edgesToRemove) {
				assertFalse(gRev.edges().contains(e));
				assertFalse(gOrig.edges().contains(e));
			}
			assertEquals(gOrig.edges(), gRev.edges());
		});
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0x79d009d0c26da823L);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Integer edge = gOrig.edges().iterator().next();
			Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gRev, rand);

			if (index) {
				/* rename is not supported in index graphs */
				assertThrows(UnsupportedOperationException.class, () -> gRev.renameEdge(edge, nonExistingEdge));
				return;
			}

			Graph<Integer, Integer> gExpected = gOrig.copy(true, true);
			Graph<Integer, Integer> gRevExpected = gRev.copy(true, true);
			gExpected.renameEdge(edge, nonExistingEdge);
			gRevExpected.renameEdge(edge, nonExistingEdge);

			gRev.renameEdge(edge, nonExistingEdge);

			assertEquals(gRevExpected, gRev);
			assertEquals(gExpected, gOrig);
		});
	}

	@Test
	public void removeUsingEdgeIter() {
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();

			Set<Integer> edgesExpected = new IntOpenHashSet(gOrig.edges());
			assertEquals(edgesExpected, gRev.edges());
			assertEquals(edgesExpected, gOrig.edges());

			EdgeIter<Integer, Integer> eit = gRev.outEdges(gRev.edgeSource(gRev.edges().iterator().next())).iterator();
			Integer edge = eit.next();
			eit.remove();

			assertFalse(gRev.edges().contains(edge));
			assertFalse(gOrig.edges().contains(edge));

			edgesExpected.remove(edge);
			assertEquals(edgesExpected, gRev.edges());
			assertEquals(edgesExpected, gOrig.edges());
		});
	}

	@Test
	public void testEdgesOutIn() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			for (Integer u : gRev.vertices()) {
				EdgeSet<Integer, Integer> edges = gRev.outEdges(u);
				assertEquals(gOrig.inEdges(u).size(), edges.size());
				assertEquals(gOrig.inEdges(u), edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(u, eit.source());
					assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
					assertEquals(gRev.edgeEndpoint(e, u), eit.target());
					assertEquals(u, gRev.edgeEndpoint(e, eit.target()));

					iteratedEdges.add(e);
				}

				assertEquals(edges.size(), iteratedEdges.size());
				for (Integer e : gOrig.edges()) {
					if (iteratedEdges.contains(e)) {
						assertTrue(edges.contains(e));
					} else {
						assertFalse(edges.contains(e));
					}
				}
			}
			for (Integer v : gRev.vertices()) {
				EdgeSet<Integer, Integer> edges = gRev.inEdges(v);
				assertEquals(gOrig.outEdges(v).size(), edges.size());
				assertEquals(gOrig.outEdges(v), edges);

				Set<Integer> iteratedEdges = new IntOpenHashSet();
				for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					assertEquals(v, eit.target());
					assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
					assertEquals(gRev.edgeEndpoint(e, v), eit.source());
					assertEquals(v, gRev.edgeEndpoint(e, eit.source()));

					iteratedEdges.add(e);
				}

				assertEquals(edges.size(), iteratedEdges.size());
				for (Integer e : gOrig.edges()) {
					if (iteratedEdges.contains(e)) {
						assertTrue(edges.contains(e));
					} else {
						assertFalse(edges.contains(e));
					}
				}
			}
		});
	}

	@Test
	public void testEdgesSourceTarget() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			for (Integer u : gRev.vertices()) {
				for (Integer v : gRev.vertices()) {
					EdgeSet<Integer, Integer> edges = gRev.getEdges(u, v);
					assertEquals(gOrig.getEdges(v, u).size(), edges.size());
					assertEquals(gOrig.getEdges(v, u), edges);

					if (edges.isEmpty()) {
						assertFalse(gRev.containsEdge(u, v));
					} else {
						Integer e = gRev.getEdge(u, v);
						assertNotNull(e);
						assertTrue(edges.contains(e));
					}

					for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, u), v);
						assertEquals(gOrig.edgeEndpoint(e, v), u);
						assertEquals(u, gRev.edgeEndpoint(e, v));
						assertEquals(v, gRev.edgeEndpoint(e, u));
					}
				}
			}
		});
	}

	@Test
	public void testRemoveEdgesOf() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			Integer v = gRev.vertices().iterator().next();

			gRev.removeEdgesOf(v);
			assertTrue(gRev.outEdges(v).isEmpty());
			assertTrue(gRev.inEdges(v).isEmpty());
			assertTrue(gOrig.outEdges(v).isEmpty());
			assertTrue(gOrig.inEdges(v).isEmpty());
		});
	}

	@Test
	public void testRemoveEdgesInOf() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			Iterator<Integer> vit = gRev.vertices().iterator();
			Integer v1 = vit.next();
			Integer v2 = vit.next();
			Integer v3 = vit.next();

			gRev.removeInEdgesOf(v1);
			assertTrue(gRev.inEdges(v1).isEmpty());
			assertTrue(gOrig.outEdges(v1).isEmpty());

			gRev.inEdges(v2).clear();
			assertTrue(gRev.inEdges(v2).isEmpty());
			assertTrue(gOrig.outEdges(v2).isEmpty());

			if (!index) {
				for (Integer e : new IntArrayList(gRev.inEdges(v3)))
					gRev.inEdges(v3).remove(e);
				assertTrue(gRev.inEdges(v3).isEmpty());
				assertTrue(gOrig.outEdges(v3).isEmpty());
			}
		});
	}

	@Test
	public void testRemoveEdgesOutOf() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			Iterator<Integer> vit = gRev.vertices().iterator();
			Integer v1 = vit.next();
			Integer v2 = vit.next();
			Integer v3 = vit.next();

			gRev.removeOutEdgesOf(v1);
			assertTrue(gRev.outEdges(v1).isEmpty());
			assertTrue(gOrig.inEdges(v1).isEmpty());

			gRev.outEdges(v2).clear();
			assertTrue(gRev.outEdges(v2).isEmpty());
			assertTrue(gOrig.inEdges(v2).isEmpty());

			if (!index) {
				for (Integer e : new IntArrayList(gRev.outEdges(v3)))
					gRev.outEdges(v3).remove(e);
				assertTrue(gRev.outEdges(v3).isEmpty());
				assertTrue(gOrig.inEdges(v3).isEmpty());
			}
		});
	}

	@Test
	public void testReverseEdge() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(true, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Integer e = gRev.edges().iterator().next();
			Integer s = gRev.edgeSource(e), t = gRev.edgeTarget(e);

			gRev.reverseEdge(e);
			assertEquals(s, gRev.edgeTarget(e));
			assertEquals(t, gRev.edgeSource(e));
		});
	}

	@Test
	public void moveEdge() {
		foreachBoolConfig((intGraph, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(true, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			Integer e = gRev.edges().iterator().next();
			Integer newSource = gRev.edgeTarget(e), newTarget = gRev.vertices().iterator().next();

			gRev.moveEdge(e, newSource, newTarget);
			assertEquals(newSource, gRev.edgeSource(e));
			assertEquals(newTarget, gRev.edgeTarget(e));
		});
	}

	@Test
	public void testEdgeGetSourceTarget() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			for (Integer e : gRev.edges()) {
				assertEquals(gOrig.edgeSource(e), gRev.edgeTarget(e));
				assertEquals(gOrig.edgeTarget(e), gRev.edgeSource(e));
			}
		});
	}

	@Test
	public void testClear() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			gRev.clear();
			assertTrue(gRev.vertices().isEmpty());
			assertTrue(gRev.edges().isEmpty());
			assertTrue(gOrig.vertices().isEmpty());
			assertTrue(gOrig.edges().isEmpty());
		});
	}

	@Test
	public void testClearEdges() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			gRev.clearEdges();
			assertTrue(gRev.edges().isEmpty());
			assertTrue(gOrig.edges().isEmpty());
		});
	}

	@Test
	public void testVerticesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		AtomicInteger keyCounter = new AtomicInteger();
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			String key1 = "key" + keyCounter.getAndIncrement(), key2 = "key" + keyCounter.getAndIncrement();
			{
				WeightsInt<Integer> vWeights1 = gOrig.addVerticesWeights(key1, int.class);
				for (Integer v : gOrig.vertices())
					vWeights1.set(v, rand.nextInt(10000));
				WeightsInt<Integer> vWeights2 = gRev.addVerticesWeights(key2, int.class);
				for (Integer v : gRev.vertices())
					vWeights2.set(v, rand.nextInt(10000));
			}

			assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
			for (String key : List.of(key1, key2)) {
				WeightsInt<Integer> wOrig = gOrig.getVerticesWeights(key);
				WeightsInt<Integer> wRev = gRev.getVerticesWeights(key);

				for (Integer v : gRev.vertices())
					assertEquals(wOrig.get(v), wRev.get(v));
				assertEquals(wOrig.defaultWeight(), wRev.defaultWeight());
			}

			gRev.removeVerticesWeights(key1);
			assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
		});
	}

	@Test
	public void testEdgesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		AtomicInteger keyCounter = new AtomicInteger();
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			String key1 = "key" + keyCounter.getAndIncrement(), key2 = "key" + keyCounter.getAndIncrement();
			{
				WeightsInt<Integer> eWeights1 = gOrig.addEdgesWeights(key1, int.class);
				for (Integer e : gOrig.edges())
					eWeights1.set(e, rand.nextInt(10000));
				WeightsInt<Integer> eWeights2 = gRev.addEdgesWeights(key2, int.class);
				for (Integer e : gRev.edges())
					eWeights2.set(e, rand.nextInt(10000));
			}

			assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
			for (String key : List.of(key1, key2)) {
				WeightsInt<Integer> wOrig = gOrig.getEdgesWeights(key);
				WeightsInt<Integer> wRev = gRev.getEdgesWeights(key);

				for (Integer e : gRev.edges())
					assertEquals(wOrig.get(e), wRev.get(e));
				assertEquals(wOrig.defaultWeight(), wRev.defaultWeight());
			}

			gRev.removeEdgesWeights(key1);
			assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
		});
	}

	@Test
	public void testGraphCapabilities() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;
			assertEqualsBool(gOrig.isAllowParallelEdges(), gRev.isAllowParallelEdges());
			assertEqualsBool(gOrig.isAllowSelfEdges(), gRev.isAllowSelfEdges());
			assertEqualsBool(gOrig.isDirected(), gRev.isDirected());
		});
	}

	@Test
	public void testRemoveListeners() {
		foreachBoolConfig((intGraph, directed) -> {
			IndexGraph gRev = createGraph(directed, intGraph).indexGraph().reverseView();
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

			gRev.addVertexRemoveListener(listener);
			called.set(false);
			gRev.removeVertex(gRev.vertices().iterator().nextInt());
			assertTrue(called.get());

			called.set(false);
			gRev.removeEdge(gRev.edges().iterator().nextInt());
			assertFalse(called.get());

			gRev.removeVertexRemoveListener(listener);
			called.set(false);
			gRev.removeVertex(gRev.vertices().iterator().nextInt());
			assertFalse(called.get());

			gRev.addEdgeRemoveListener(listener);
			called.set(false);
			gRev.removeEdge(gRev.edges().iterator().nextInt());
			assertTrue(called.get());

			int v = gRev.vertices().iterator().nextInt();
			gRev.removeEdgesOf(v);
			called.set(false);
			gRev.removeVertex(v);
			assertFalse(called.get());

			gRev.removeEdgeRemoveListener(listener);
			called.set(false);
			gRev.removeEdge(gRev.edges().iterator().nextInt());
			assertFalse(called.get());
		});
	}

	@Test
	public void reverseViewOfReverseView() {
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;

			assertEquals(gOrig, gOrig.reverseView().reverseView());
			assertTrue(gOrig == gOrig.reverseView().reverseView());
		});
	}

	@Test
	public void verticesAndEdgesIndexMaps() {
		final long seed = 0x6750ab3bf727a7e8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
			Graph<Integer, Integer> gRev = gOrig.reverseView();

			IndexIdMap<Integer> origViMap = gOrig.indexGraphVerticesMap();
			IndexIdMap<Integer> origEiMap = gOrig.indexGraphEdgesMap();
			IndexIdMap<Integer> revViMap = gRev.indexGraphVerticesMap();
			IndexIdMap<Integer> revEiMap = gRev.indexGraphEdgesMap();

			for (Integer v : gOrig.vertices()) {
				assertEquals(origViMap.idToIndex(v), revViMap.idToIndex(v));
				assertEquals(origViMap.idToIndexIfExist(v), revViMap.idToIndexIfExist(v));
			}
			for (int i = 0; i < 10; i++) {
				Integer v = Integer.valueOf(rand.nextInt());
				assertEquals(origViMap.idToIndexIfExist(v), revViMap.idToIndexIfExist(v));
			}
			for (int vIdx : gOrig.indexGraph().vertices()) {
				assertEquals(origViMap.indexToId(vIdx), revViMap.indexToId(vIdx));
				assertEquals(origViMap.indexToIdIfExist(vIdx), revViMap.indexToIdIfExist(vIdx));
			}
			for (int i = 0; i < 10; i++) {
				int vIdx = rand.nextInt();
				assertEquals(origViMap.indexToIdIfExist(vIdx), revViMap.indexToIdIfExist(vIdx));
			}

			for (Integer e : gOrig.edges()) {
				assertEquals(origEiMap.idToIndex(e), revEiMap.idToIndex(e));
				assertEquals(origEiMap.idToIndexIfExist(e), revEiMap.idToIndexIfExist(e));
			}
			for (int i = 0; i < 10; i++) {
				Integer e = Integer.valueOf(rand.nextInt());
				assertEquals(origEiMap.idToIndexIfExist(e), revEiMap.idToIndexIfExist(e));
			}
			for (int eIdx : gOrig.indexGraph().edges()) {
				assertEquals(origEiMap.indexToId(eIdx), revEiMap.indexToId(eIdx));
				assertEquals(origEiMap.indexToIdIfExist(eIdx), revEiMap.indexToIdIfExist(eIdx));
			}
			for (int i = 0; i < 10; i++) {
				int eIdx = rand.nextInt();
				assertEquals(origEiMap.indexToIdIfExist(eIdx), revEiMap.indexToIdIfExist(eIdx));
			}
		});
	}

	@Test
	public void vertexBuilder() {
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();
			assertTrue(gOrig.vertexBuilder() == gRev.vertexBuilder());
		});
	}

	@Test
	public void edgeBuilder() {
		foreachBoolConfig((intGraph, directed) -> {
			Graph<Integer, Integer> gOrig = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev = gOrig.reverseView();
			assertTrue(gOrig.edgeBuilder() == gRev.edgeBuilder());
		});
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		foreachBoolConfig((intGraph, directed, index) -> {
			Graph<Integer, Integer> gOrig0 = createGraph(directed, intGraph);
			Graph<Integer, Integer> gRev0 = gOrig0.reverseView();
			Graph<Integer, Integer> gRev = index ? gRev0.indexGraph() : gRev0;

			gRev.ensureVertexCapacity(gRev.vertices().size() + 10);
			gRev.ensureEdgeCapacity(gRev.edges().size() + 10);
		});
	}

}
