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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ImmutableGraphViewTest extends TestBase {

	private static final Object VerticesWeightsKey = JGAlgoUtils.labeledObj("vWeights");
	private static final Object EdgesWeightsKey = JGAlgoUtils.labeledObj("eWeights");

	private static Graph createGraph(boolean directed) {
		final long seed = 0x97dc96ffefd7165bL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		Graph g = GraphFactory.newUndirected().setDirected(directed).newGraph();

		IntList vertices = new IntArrayList(n);
		WeightsInt vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i = 0; i < n; i++) {
			int v = g.addVertex();
			vertices.add(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
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
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				assertEquals(gOrig.vertices().size(), gImmutable.vertices().size());
				assertEquals(gOrig.vertices(), gImmutable.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
				assertEquals(gOrig.edges().size(), gImmutable.edges().size());
				assertEquals(gOrig.edges(), gImmutable.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				int nonExistingVertex;
				for (int v = 0;; v++) {
					if (!gImmutable.vertices().contains(v)) {
						nonExistingVertex = v;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex());
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(nonExistingVertex));

				int vertexToRemove = gImmutable.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeVertex(vertexToRemove));
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				IntIterator vit = gImmutable.vertices().iterator();
				int u = vit.nextInt();
				int v = vit.nextInt();

				int nonExistingEdge;
				for (int e = 0;; e++) {
					if (!gImmutable.edges().contains(e)) {
						nonExistingEdge = e;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdge(u, v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdge(u, v, nonExistingEdge));

				int edgeToRemove = gImmutable.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdge(edgeToRemove));
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				for (int u : gImmutable.vertices()) {
					EdgeSet edges = gImmutable.outEdges(u);
					assertEquals(gOrig.outEdges(u).size(), edges.size());
					assertEquals(gOrig.outEdges(u), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
						assertEquals(gImmutable.edgeEndpoint(e, u), eit.target());
						assertEquals(u, gImmutable.edgeEndpoint(e, eit.target()));

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
				for (int v : gImmutable.vertices()) {
					EdgeSet edges = gImmutable.inEdges(v);
					assertEquals(gOrig.inEdges(v).size(), edges.size());
					assertEquals(gOrig.inEdges(v), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
						assertEquals(gImmutable.edgeEndpoint(e, v), eit.source());
						assertEquals(v, gImmutable.edgeEndpoint(e, eit.source()));

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
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				for (int u : gImmutable.vertices()) {
					for (int v : gImmutable.vertices()) {
						EdgeSet edges = gImmutable.getEdges(u, v);
						assertEquals(gOrig.getEdges(u, v).size(), edges.size());
						assertEquals(gOrig.getEdges(u, v), edges);

						if (edges.isEmpty()) {
							assertEquals(-1, gImmutable.getEdge(u, v));
						} else {
							int e = gImmutable.getEdge(u, v);
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
							assertEquals(u, gImmutable.edgeEndpoint(e, v));
							assertEquals(v, gImmutable.edgeEndpoint(e, u));
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
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
				int v = gImmutable.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeOutEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeInEdgesOf(v));
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
				int e = gImmutable.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.reverseEdge(e));
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
				for (int e : gImmutable.edges()) {
					assertEquals(gOrig.edgeSource(e), gImmutable.edgeSource(e));
					assertEquals(gOrig.edgeTarget(e), gImmutable.edgeTarget(e));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clear());
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clearEdges());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				assertEquals(gOrig.getVerticesWeightsKeys(), gImmutable.getVerticesWeightsKeys());
				WeightsInt wOrig = gOrig.getVerticesWeights(VerticesWeightsKey);
				WeightsInt wImmutable = gImmutable.getVerticesWeights(VerticesWeightsKey);

				for (int v : gImmutable.vertices())
					assertEquals(wOrig.get(v), wImmutable.get(v));
				assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

				int vertex = gImmutable.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(vertex, 42));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.removeVerticesWeights(VerticesWeightsKey));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.addVerticesWeights("key", Object.class));
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				assertEquals(gOrig.getEdgesWeightsKeys(), gImmutable.getEdgesWeightsKeys());
				WeightsInt wOrig = gOrig.getEdgesWeights(EdgesWeightsKey);
				WeightsInt wImmutable = gImmutable.getEdgesWeights(EdgesWeightsKey);

				for (int e : gImmutable.edges())
					assertEquals(wOrig.get(e), wImmutable.get(e));
				assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

				int edge = gImmutable.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(edge, 42));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdgesWeights(EdgesWeightsKey));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.addEdgesWeights("key", Object.class));
			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gImmutable0 = gOrig0.immutableView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = index ? gImmutable0.indexGraph() : gImmutable0;

				assertEquals(gOrig.isAllowParallelEdges(), gImmutable.isAllowParallelEdges());
				assertEquals(gOrig.isAllowSelfEdges(), gImmutable.isAllowSelfEdges());
				assertEquals(gOrig.isDirected(), gImmutable.isDirected());
			}
		}
	}

	@Test
	public void testImmutableViewOfImmutableView() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig0 = createGraph(directed);
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gImmutable = gOrig.immutableView();

				assertTrue(gImmutable == gImmutable.immutableView());
			}
		}
	}

}
