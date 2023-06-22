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
package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class UnmodifiableGraphTest extends TestBase {

	private static final Object VerticesWeightsKey = new Utils.Obj("vWeights");
	private static final Object EdgesWeightsKey = new Utils.Obj("eWeights");

	private static Graph createGraph(boolean directed) {
		final long seed = 0x97dc96ffefd7165bL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		Graph g = Graph.newBuilderUndirected().setDirected(directed).build();

		IntList vertices = new IntArrayList(n);
		Weights.Int vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i = 0; i < n; i++) {
			int v = g.addVertex();
			vertices.add(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		Weights.Int eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int i = 0; i < m; i++) {
			int u = vertices.getInt(rand.nextInt(vertices.size()));
			int v = vertices.getInt(rand.nextInt(vertices.size()));
			int e = g.addEdge(u, v);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				assertEquals(gOrig.vertices().size(), gUnmod.vertices().size());
				assertEquals(gOrig.vertices(), gUnmod.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;
				assertEquals(gOrig.edges().size(), gUnmod.edges().size());
				assertEquals(gOrig.edges(), gUnmod.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				int nonExistingVertex;
				for (int v = 0;; v++) {
					if (!gUnmod.vertices().contains(v)) {
						nonExistingVertex = v;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addVertex());
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addVertex(nonExistingVertex));

				int vertexToRemove = gUnmod.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeVertex(vertexToRemove));
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				IntIterator vit = gUnmod.vertices().iterator();
				int u = vit.nextInt();
				int v = vit.nextInt();

				int nonExistingEdge;
				for (int e = 0;; e++) {
					if (!gUnmod.edges().contains(e)) {
						nonExistingEdge = e;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addEdge(u, v));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addEdge(u, v, nonExistingEdge));

				int edgeToRemove = gUnmod.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeEdge(edgeToRemove));
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				for (int u : gUnmod.vertices()) {
					EdgeSet edges = gUnmod.outEdges(u);
					assertEquals(gOrig.outEdges(u).size(), edges.size());
					assertEquals(gOrig.outEdges(u), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
						assertEquals(gUnmod.edgeEndpoint(e, u), eit.target());
						assertEquals(u, gUnmod.edgeEndpoint(e, eit.target()));

						iteratedEdges.add(e);
					}

					assertEquals(edges.size(), iteratedEdges.size());
					for (int e : gOrig.edges()) {
						if (iteratedEdges.contains(e)) {
							assertTrue(edges.contains(e));
						} else {
							assertFalse(edges.contains(e));
						}
					}
				}
				for (int v : gUnmod.vertices()) {
					EdgeSet edges = gUnmod.inEdges(v);
					assertEquals(gOrig.inEdges(v).size(), edges.size());
					assertEquals(gOrig.inEdges(v), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
						assertEquals(gUnmod.edgeEndpoint(e, v), eit.source());
						assertEquals(v, gUnmod.edgeEndpoint(e, eit.source()));

						iteratedEdges.add(e);
					}

					assertEquals(edges.size(), iteratedEdges.size());
					for (int e : gOrig.edges()) {
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
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				for (int u : gUnmod.vertices()) {
					for (int v : gUnmod.vertices()) {
						EdgeSet edges = gUnmod.getEdges(u, v);
						assertEquals(gOrig.getEdges(u, v).size(), edges.size());
						assertEquals(gOrig.getEdges(u, v), edges);

						if (edges.isEmpty()) {
							assertEquals(-1, gUnmod.getEdge(u, v));
						} else {
							int e = gUnmod.getEdge(u, v);
							assertNotEquals(-1, e);
							assertTrue(edges.contains(e));
						}

						for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
							int peekNext = eit.peekNext();
							int e = eit.nextInt();
							assertEquals(e, peekNext);

							assertEquals(u, eit.source());
							assertEquals(v, eit.target());
							assertEquals(gOrig.edgeEndpoint(e, u), v);
							assertEquals(gOrig.edgeEndpoint(e, v), u);
							assertEquals(u, gUnmod.edgeEndpoint(e, v));
							assertEquals(v, gUnmod.edgeEndpoint(e, u));
						}
					}
				}
			}
		}
	}

	@Test
	public void testRemoveEdgesOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;
				int v = gUnmod.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeOutEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeInEdgesOf(v));
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;
				int e = gUnmod.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.reverseEdge(e));
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;
				for (int e : gUnmod.edges()) {
					assertEquals(gOrig.edgeSource(e), gUnmod.edgeSource(e));
					assertEquals(gOrig.edgeTarget(e), gUnmod.edgeTarget(e));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.clear());
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.clearEdges());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				assertEquals(gOrig.getVerticesWeightsKeys(), gUnmod.getVerticesWeightsKeys());
				Weights.Int wOrig = gOrig.getVerticesWeights(VerticesWeightsKey);
				Weights.Int wUnmod = gUnmod.getVerticesWeights(VerticesWeightsKey);

				for (int v : gUnmod.vertices())
					assertEquals(wOrig.getInt(v), wUnmod.getInt(v));
				assertEquals(wOrig.defaultWeightInt(), wUnmod.defaultWeightInt());

				int vertex = gUnmod.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> wUnmod.set(vertex, 42));
				assertThrows(UnsupportedOperationException.class,
						() -> gUnmod.removeVerticesWeights(VerticesWeightsKey));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addVerticesWeights("key", Object.class));
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				assertEquals(gOrig.getEdgesWeightsKeys(), gUnmod.getEdgesWeightsKeys());
				Weights.Int wOrig = gOrig.getEdgesWeights(EdgesWeightsKey);
				Weights.Int wUnmod = gUnmod.getEdgesWeights(EdgesWeightsKey);

				for (int e : gUnmod.edges())
					assertEquals(wOrig.getInt(e), wUnmod.getInt(e));
				assertEquals(wOrig.defaultWeightInt(), wUnmod.defaultWeightInt());

				int edge = gUnmod.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> wUnmod.set(edge, 42));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.removeEdgesWeights(EdgesWeightsKey));
				assertThrows(UnsupportedOperationException.class, () -> gUnmod.addEdgesWeights("key", Object.class));
			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gUnmod0 = gOrig0.unmodifiableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gUnmod = index ? gUnmod0.indexGraph() : gUnmod0;

				GraphCapabilities capOrig = gOrig.getCapabilities();
				GraphCapabilities capUnmod = gUnmod.getCapabilities();

				assertEquals(capOrig.parallelEdges(), capUnmod.parallelEdges());
				assertEquals(capOrig.selfEdges(), capUnmod.selfEdges());
				assertEquals(capOrig.directed(), capUnmod.directed());
			}
		}
	}

}
