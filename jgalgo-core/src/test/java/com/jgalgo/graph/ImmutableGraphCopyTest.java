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
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ImmutableGraphCopyTest extends TestBase {

	private static final String VerticesWeightsKey = "vWeights";
	private static final String EdgesWeightsKey = "eWeights";

	private static IntGraph createGraph(boolean directed) {
		final long seed = 0x4ff62bb8f3a0b831L;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		IntGraph g = IntGraphFactory.newUndirected().setDirected(directed).newGraph();

		IWeightsInt vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int i = 0; i < n; i++) {
			int v = g.addVertex();
			vWeights.set(v, rand.nextInt(10000));
		}

		IWeightsInt eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int i = 0; i < m; i++) {
			int e = g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand));
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

				assertEquals(gOrig.vertices().size(), gImmutable.vertices().size());
				assertEquals(gOrig.vertices(), gImmutable.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();
				assertEquals(gOrig.edges().size(), gImmutable.edges().size());
				assertEquals(gOrig.edges(), gImmutable.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

				for (int u : gImmutable.vertices()) {
					IEdgeSet edges = gImmutable.outEdges(u);
					assertEquals(gOrig.outEdges(u).size(), edges.size());
					assertEquals(gOrig.outEdges(u), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (IEdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNextInt();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.sourceInt());
						assertEquals(gOrig.edgeEndpoint(e, u), eit.targetInt());
						assertEquals(gImmutable.edgeEndpoint(e, u), eit.targetInt());
						assertEquals(u, gImmutable.edgeEndpoint(e, eit.targetInt()));

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
					IEdgeSet edges = gImmutable.inEdges(v);
					assertEquals(gOrig.inEdges(v).size(), edges.size());
					assertEquals(gOrig.inEdges(v), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (IEdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNextInt();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(v, eit.targetInt());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.sourceInt());
						assertEquals(gImmutable.edgeEndpoint(e, v), eit.sourceInt());
						assertEquals(v, gImmutable.edgeEndpoint(e, eit.sourceInt()));

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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

				for (int u : gImmutable.vertices()) {
					for (int v : gImmutable.vertices()) {
						IEdgeSet edges = gImmutable.getEdges(u, v);
						assertEquals(gOrig.getEdges(u, v).size(), edges.size());
						assertEquals(gOrig.getEdges(u, v), edges);

						if (edges.isEmpty()) {
							assertEquals(-1, gImmutable.getEdge(u, v));
						} else {
							int e = gImmutable.getEdge(u, v);
							assertNotEquals(-1, e);
							assertTrue(edges.contains(e));
						}

						for (IEdgeIter eit = edges.iterator(); eit.hasNext();) {
							int peekNext = eit.peekNextInt();
							int e = eit.nextInt();
							assertEquals(e, peekNext);

							assertEquals(u, eit.sourceInt());
							assertEquals(v, eit.targetInt());
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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();
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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();
				int e = gImmutable.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.reverseEdge(e));
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();
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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clear());
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clearEdges());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy(true, true);

				assertEquals(gOrig.getVerticesWeightsKeys(), gImmutable.getVerticesWeightsKeys());
				IWeightsInt wOrig = gOrig.getVerticesWeights(VerticesWeightsKey);
				IWeightsInt wImmutable = gImmutable.getVerticesWeights(VerticesWeightsKey);

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
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy(true, true);

				assertEquals(gOrig.getEdgesWeightsKeys(), gImmutable.getEdgesWeightsKeys());
				IWeightsInt wOrig = gOrig.getEdgesWeights(EdgesWeightsKey);
				IWeightsInt wImmutable = gImmutable.getEdgesWeights(EdgesWeightsKey);

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

	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				IntGraph gOrig0 = createGraph(directed);
				IntGraph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				IntGraph gImmutable = gOrig.immutableCopy();

				assertEqualsBool(gOrig.isDirected(), gImmutable.isDirected());
				assertEqualsBool(gOrig.isAllowSelfEdges(), gImmutable.isAllowSelfEdges());
				assertEqualsBool(gOrig.isAllowParallelEdges(), gImmutable.isAllowParallelEdges());
			}
		}
	}

	@Test
	public void testImmutableViewOfImmutableCopy() {
		for (boolean directed : BooleanList.of(false, true)) {
			IndexGraph gOrig = createGraph(directed).indexGraph();
			IndexGraph gImmutable = gOrig.immutableCopy();

			assertTrue(gImmutable == gImmutable.immutableView());
		}
	}

}
