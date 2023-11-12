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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class UndirectedViewTest extends TestBase {

	private static final String VerticesWeightsKey = "vWeights";
	private static final String EdgesWeightsKey = "eWeights";

	private static Graph<Integer, Integer> createGraph(boolean intGraph) {
		final long seed = 0x2f8451a6708986baL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;

		Graph<Integer, Integer> g;
		if (intGraph) {
			g = IntGraphFactory.newDirected().newGraph();
		} else {
			g = GraphFactory.<Integer, Integer>newDirected().newGraph();
		}

		WeightsInt<Integer> vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i = 0; i < n; i++) {
			Integer v = Integer.valueOf(i + 1);
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt<Integer> eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int i = 0; i < m; i++) {
			Integer e = Integer.valueOf(i + 1);
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				assertEquals(g.vertices().size(), undirectedG.vertices().size());
				assertEquals(g.vertices(), undirectedG.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				assertEquals(g.edges().size(), undirectedG.edges().size());
				assertEquals(g.edges(), undirectedG.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

				Integer nonExistingVertex, newVertex;
				if (undirectedG instanceof IndexGraph) {
					for (int v = 0;; v++) {
						if (!undirectedG0.vertices().contains(Integer.valueOf(v))) {
							nonExistingVertex = Integer.valueOf(v);
							break;
						}
					}
					newVertex = nonExistingVertex;

					/* index graphs should not support adding vertices with user defined identifiers */
					int newVertex0 = newVertex.intValue();
					assertThrows(UnsupportedOperationException.class,
							() -> ((IntGraph) undirectedG).addVertex(newVertex0));

					/* can't add new vertex directly to IndexGraph, only via wrapper Int/Obj Graph */
					IndexIdMap<Integer> viMap = undirectedG0.indexGraphVerticesMap();

					undirectedG0.addVertex(newVertex);
					newVertex = viMap.indexToId(newVertex.intValue());

				} else if (undirectedG instanceof IntGraph) {
					newVertex = Integer.valueOf(((IntGraph) undirectedG).addVertex());
				} else {
					for (int v = 0;; v++) {
						if (!undirectedG.vertices().contains(Integer.valueOf(v))) {
							nonExistingVertex = Integer.valueOf(v);
							break;
						}
					}
					newVertex = nonExistingVertex;
					undirectedG.addVertex(newVertex);
				}
				assertTrue(g.vertices().contains(newVertex));
				assertTrue(undirectedG.vertices().contains(newVertex));
				assertEquals(g.vertices(), undirectedG.vertices());

				for (int v = 0;; v++) {
					if (!undirectedG.vertices().contains(Integer.valueOf(v))) {
						nonExistingVertex = Integer.valueOf(v);
						break;
					}
				}
				if (undirectedG instanceof IndexGraph) {
					final Integer nonExistingVertex0 = nonExistingVertex;
					assertThrows(UnsupportedOperationException.class, () -> undirectedG.addVertex(nonExistingVertex0));
				} else {
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
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			Graph<Integer, Integer> g0 = createGraph(intGraph);
			Graph<Integer, Integer> undirectedG0 = g0.undirectedView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				Graph<Integer, Integer> undirectedG = index ? undirectedG0.indexGraph() : undirectedG0;

				Iterator<Integer> vit = undirectedG.vertices().iterator();
				Integer u = vit.next();
				Integer v = vit.next();

				Integer nonExistingEdge, newEdge;
				if (undirectedG instanceof IndexGraph) {
					for (int e = 0;; e++) {
						if (!undirectedG0.edges().contains(Integer.valueOf(e))) {
							nonExistingEdge = Integer.valueOf(e);
							break;
						}
					}
					newEdge = nonExistingEdge;

					/* index graphs should not support adding edges with user defined identifiers */
					int newEdge0 = newEdge.intValue();
					assertThrows(UnsupportedOperationException.class,
							() -> ((IntGraph) undirectedG).addEdge(u.intValue(), v.intValue(), newEdge0));

					/* can't add new edge directly to IndexGraph, only via wrapper Int/Obj Graph */
					IndexIdMap<Integer> viMap = undirectedG0.indexGraphVerticesMap();
					IndexIdMap<Integer> eiMap = undirectedG0.indexGraphEdgesMap();
					undirectedG0.addEdge(viMap.indexToId(u.intValue()), viMap.indexToId(v.intValue()), newEdge);
					newEdge = eiMap.indexToId(newEdge.intValue());

				} else if (undirectedG instanceof IntGraph) {
					newEdge = Integer.valueOf(((IntGraph) undirectedG).addEdge(u.intValue(), v.intValue()));
				} else {
					for (int e = 0;; e++) {
						if (!undirectedG.edges().contains(Integer.valueOf(e))) {
							nonExistingEdge = Integer.valueOf(e);
							break;
						}
					}
					newEdge = nonExistingEdge;
					undirectedG.addEdge(u, v, newEdge);
				}
				assertTrue(g.edges().contains(newEdge));
				assertTrue(undirectedG.edges().contains(newEdge));
				assertEquals(g.edges(), undirectedG.edges());

				for (int e = 0;; e++) {
					if (!undirectedG.edges().contains(Integer.valueOf(e))) {
						nonExistingEdge = Integer.valueOf(e);
						break;
					}
				}
				if (undirectedG instanceof IndexGraph) {
					Integer nonExistingEdge0 = nonExistingEdge;
					assertThrows(UnsupportedOperationException.class,
							() -> undirectedG.addEdge(u, v, nonExistingEdge0));
				} else {
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
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
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
					for (Integer e : g.edges()) {
						if (iteratedEdges.contains(e)) {
							assertTrue(edges.contains(e));
						} else {
							assertFalse(edges.contains(e));
						}
					}
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
					for (Integer e : g.edges()) {
						if (iteratedEdges.contains(e)) {
							assertTrue(edges.contains(e));
						} else {
							assertFalse(edges.contains(e));
						}
					}
				}
			}
		}
	}

	@Test
	public void testEdgesSourceTarget() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
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
							assertNull(undirectedG.getEdge(u, v));
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
					}
				}
			}
		}
	}

	@Test
	public void testRemoveEdgesOf() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
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
			}
		}
	}

	@Test
	public void testRemoveEdgesInOf() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
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
			}
		}
	}

	@Test
	public void testRemoveEdgesOutOf() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
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
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				for (Integer e : undirectedG.edges()) {
					Integer s1 = g.edgeSource(e), t1 = g.edgeTarget(e);
					Integer s2 = undirectedG.edgeSource(e), t2 = undirectedG.edgeTarget(e);
					assertTrue((s1 == s2 && t1 == t2) || (s1 == t2 && t1 == s2));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				undirectedG.clear();
				assertTrue(undirectedG.vertices().isEmpty());
				assertTrue(undirectedG.edges().isEmpty());
				assertTrue(g.vertices().isEmpty());
				assertTrue(g.edges().isEmpty());
			}
		}
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				g.clear();
				assertTrue(undirectedG.vertices().isEmpty());
				assertTrue(undirectedG.edges().isEmpty());
				assertTrue(g.vertices().isEmpty());
				assertTrue(g.edges().isEmpty());
			}
		}
	}

	@Test
	public void testClearEdges() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				Set<Integer> verticesBeforeClear = new HashSet<>(g.vertices());
				undirectedG.clearEdges();
				assertTrue(undirectedG.edges().isEmpty());
				assertTrue(g.edges().isEmpty());
				assertEquals(verticesBeforeClear, undirectedG.vertices());
				assertEquals(verticesBeforeClear, g.vertices());
			}
		}
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				Set<Integer> verticesBeforeClear = new HashSet<>(g.vertices());
				g.clearEdges();
				assertTrue(undirectedG.edges().isEmpty());
				assertTrue(g.edges().isEmpty());
				assertEquals(verticesBeforeClear, undirectedG.vertices());
				assertEquals(verticesBeforeClear, g.vertices());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		final long seed = 0;
		Random rand = new Random(seed);
		int keyCounter = 0;
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();

				String key1 = "key" + keyCounter++, key2 = "key" + keyCounter++;
				{
					WeightsInt<Integer> vWeights1 = g.addVerticesWeights(key1, int.class);
					for (Integer v : g.vertices())
						vWeights1.set(v, rand.nextInt(10000));
					WeightsInt<Integer> vWeights2 = undirectedG.addVerticesWeights(key2, int.class);
					for (Integer v : undirectedG.vertices())
						vWeights2.set(v, rand.nextInt(10000));
				}

				assertEquals(g.getVerticesWeightsKeys(), undirectedG.getVerticesWeightsKeys());
				for (String key : List.of(key1, key2)) {
					WeightsInt<Integer> wOrig = g.getVerticesWeights(key);
					WeightsInt<Integer> wUnd = undirectedG.getVerticesWeights(key);

					for (Integer v : undirectedG.vertices())
						assertEquals(wOrig.get(v), wUnd.get(v));
					assertEquals(wOrig.defaultWeight(), wUnd.defaultWeight());
				}

				undirectedG.removeVerticesWeights(key1);
				assertEquals(g.getVerticesWeightsKeys(), undirectedG.getVerticesWeightsKeys());
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		final long seed = 0;
		Random rand = new Random(seed);
		int keyCounter = 0;
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();

				String key1 = "key" + keyCounter++, key2 = "key" + keyCounter++;
				{
					WeightsInt<Integer> eWeights1 = g.addEdgesWeights(key1, int.class);
					for (Integer e : g.edges())
						eWeights1.set(e, rand.nextInt(10000));
					WeightsInt<Integer> eWeights2 = undirectedG.addEdgesWeights(key2, int.class);
					for (Integer e : undirectedG.edges())
						eWeights2.set(e, rand.nextInt(10000));
				}

				assertEquals(g.getEdgesWeightsKeys(), undirectedG.getEdgesWeightsKeys());
				for (String key : List.of(key1, key2)) {
					WeightsInt<Integer> wOrig = g.getEdgesWeights(key);
					WeightsInt<Integer> wUnd = undirectedG.getEdgesWeights(key);

					for (Integer e : undirectedG.edges())
						assertEquals(wOrig.get(e), wUnd.get(e));
					assertEquals(wOrig.defaultWeight(), wUnd.defaultWeight());
				}

				undirectedG.removeEdgesWeights(key1);
				assertEquals(g.getEdgesWeightsKeys(), undirectedG.getEdgesWeightsKeys());
			}
		}
	}

	@Test
	public void testGraphCapabilities() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				assertEqualsBool(g.isAllowSelfEdges(), undirectedG.isAllowSelfEdges());
				assertFalse(undirectedG.isDirected());
				assertTrue(undirectedG.isAllowParallelEdges());
			}
		}
	}

	@Test
	public void testUndirectedViewOfUndirectedView() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = index ? createGraph(intGraph).indexGraph() : createGraph(intGraph);
				Graph<Integer, Integer> undirectedG = g.undirectedView();
				assertTrue(undirectedG == undirectedG.undirectedView());
			}
		}
	}

	@Test
	public void testRemoveListeners() {
		for (boolean intGraph : BooleanList.of(false, true)) {
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
		}
	}

}
