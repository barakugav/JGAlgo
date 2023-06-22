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

public class FixedGraphTest extends TestBase {

	private static final Object VerticesWeightsKey = new Utils.Obj("vWeights");
	private static final Object EdgesWeightsKey = new Utils.Obj("eWeights");

	private static Graph createGraph(boolean directed) {
		final long seed = 0xa06bac17dc99556dL;
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

	static Graph fixedCopy(Graph g) {
		if (g instanceof IndexGraph)
			return fixedCopy((IndexGraph) g);
		return GraphImpl.fixedCopy(g);
	}

	static IndexGraph fixedCopy(IndexGraph g) {
		if (g.getCapabilities().directed()) {
			return new GraphCSRUnmappedDirected(g);
		} else {
			return new GraphCSRUnmappedUndirected(g);
		}
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;

				assertEquals(gOrig.vertices().size(), gFixed.vertices().size());
				assertEquals(gOrig.vertices(), gFixed.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;
				assertEquals(gOrig.edges().size(), gFixed.edges().size());
				assertEquals(gOrig.edges(), gFixed.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;

				int nonExistingVertex;
				for (int v = 0;; v++) {
					if (!gFixed.vertices().contains(v)) {
						nonExistingVertex = v;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gFixed.addVertex());
				assertThrows(UnsupportedOperationException.class, () -> gFixed.addVertex(nonExistingVertex));

				int vertexToRemove = gFixed.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gFixed.removeVertex(vertexToRemove));
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;

				IntIterator vit = gFixed.vertices().iterator();
				int u = vit.nextInt();
				int v = vit.nextInt();

				int nonExistingEdge;
				for (int e = 0;; e++) {
					if (!gFixed.edges().contains(e)) {
						nonExistingEdge = e;
						break;
					}
				}

				assertThrows(UnsupportedOperationException.class, () -> gFixed.addEdge(u, v));
				assertThrows(UnsupportedOperationException.class, () -> gFixed.addEdge(u, v, nonExistingEdge));

				int edgeToRemove = gFixed.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gFixed.removeEdge(edgeToRemove));
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = createGraph(directed);
				if (index)
					gOrig = gOrig.indexGraph();
				Graph gFixed = fixedCopy(gOrig);

				for (int u : gFixed.vertices()) {
					EdgeSet edges = gFixed.outEdges(u);
					if (gOrig.outEdges(u).size() != edges.size())
						assertEquals(gOrig.outEdges(u).size(), edges.size());
					assertEquals(gOrig.outEdges(u), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						if (gOrig.edgeEndpoint(e, u) != eit.target())
							assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
						assertEquals(gFixed.edgeEndpoint(e, u), eit.target());
						assertEquals(u, gFixed.edgeEndpoint(e, eit.target()));

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
				for (int v : gFixed.vertices()) {
					EdgeSet edges = gFixed.inEdges(v);
					assertEquals(gOrig.inEdges(v).size(), edges.size());
					assertEquals(gOrig.inEdges(v), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
						assertEquals(gFixed.edgeEndpoint(e, v), eit.source());
						assertEquals(v, gFixed.edgeEndpoint(e, eit.source()));

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
				Graph gOrig = createGraph(directed);
				if (index)
					gOrig = gOrig.indexGraph();
				Graph gFixed = fixedCopy(gOrig);

				for (int u : gFixed.vertices()) {
					for (int v : gFixed.vertices()) {
						EdgeSet edges = gFixed.getEdges(u, v);
						assertEquals(gOrig.getEdges(u, v).size(), edges.size());
						assertEquals(gOrig.getEdges(u, v), edges);

						if (edges.isEmpty()) {
							assertEquals(-1, gFixed.getEdge(u, v));
						} else {
							int e = gFixed.getEdge(u, v);
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
							assertEquals(u, gFixed.edgeEndpoint(e, v));
							assertEquals(v, gFixed.edgeEndpoint(e, u));
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
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;
				int v = gFixed.vertices().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gFixed.removeEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gFixed.removeOutEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gFixed.removeInEdgesOf(v));
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;
				int e = gFixed.edges().iterator().nextInt();
				assertThrows(UnsupportedOperationException.class, () -> gFixed.reverseEdge(e));
			}
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = createGraph(directed);
				if (index)
					gOrig = gOrig.indexGraph();
				Graph gFixed = fixedCopy(gOrig);
				for (int e : gFixed.edges()) {
					assertEquals(gOrig.edgeSource(e), gFixed.edgeSource(e));
					assertEquals(gOrig.edgeTarget(e), gFixed.edgeTarget(e));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;
				assertThrows(UnsupportedOperationException.class, () -> gFixed.clear());
				assertThrows(UnsupportedOperationException.class, () -> gFixed.clearEdges());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig0 = createGraph(directed);
				Graph gFixed0 = fixedCopy(gOrig0);
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;

				assertEquals(gOrig.getVerticesWeightsKeys(), gFixed.getVerticesWeightsKeys());
				Weights.Int wOrig = gOrig.getVerticesWeights(VerticesWeightsKey);
				Weights.Int wFixed = gFixed.getVerticesWeights(VerticesWeightsKey);

				for (int v : gFixed.vertices())
					assertEquals(wOrig.getInt(v), wFixed.getInt(v));
				assertEquals(wOrig.defaultWeightInt(), wFixed.defaultWeightInt());

				int vertex = gFixed.vertices().iterator().nextInt();
				wFixed.set(vertex, 42);
				gFixed.removeVerticesWeights(VerticesWeightsKey);
				gFixed.addVerticesWeights("key", Object.class);
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = createGraph(directed);
				if (index)
					gOrig = gOrig.indexGraph();
				Graph gFixed = fixedCopy(gOrig);

				assertEquals(gOrig.getEdgesWeightsKeys(), gFixed.getEdgesWeightsKeys());
				Weights.Int wOrig = gOrig.getEdgesWeights(EdgesWeightsKey);
				Weights.Int wFixed = gFixed.getEdgesWeights(EdgesWeightsKey);

				for (int e : gFixed.edges())
					assertEquals(wOrig.getInt(e), wFixed.getInt(e));
				assertEquals(wOrig.defaultWeightInt(), wFixed.defaultWeightInt());

				int edge = gFixed.edges().iterator().nextInt();
				wFixed.set(edge, 42);
				gFixed.removeEdgesWeights(EdgesWeightsKey);
				gFixed.addEdgesWeights("key", Object.class);
			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gFixed0 = fixedCopy(gOrig0);
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gFixed = index ? gFixed0.indexGraph() : gFixed0;

				GraphCapabilities capOrig = gOrig.getCapabilities();
				GraphCapabilities capFixed = gFixed.getCapabilities();

				assertEquals(capOrig.parallelEdges(), capFixed.parallelEdges());
				assertEquals(capOrig.selfEdges(), capFixed.selfEdges());
				assertEquals(capOrig.directed(), capFixed.directed());
			}
		}
	}

}
