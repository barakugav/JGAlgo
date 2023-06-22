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

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class ReversedGraphTest extends TestBase {

	private static Graph createGraph(boolean directed) {
		final long seed = 0x97dc96ffefd7165bL;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		Graph g = Graph.newBuilderUndirected().setDirected(directed).build();

		IntList vertices = new IntArrayList(n);
		for (int i = 0; i < n; i++)
			vertices.add(g.addVertex());

		for (int i = 0; i < m; i++) {
			int u = vertices.getInt(rand.nextInt(vertices.size()));
			int v = vertices.getInt(rand.nextInt(vertices.size()));
			g.addEdge(u, v);
		}
		return g;
	}

	@Test
	public void testVertices() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				assertEquals(gOrig.vertices().size(), gRev.vertices().size());
				assertEquals(gOrig.vertices(), gRev.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				assertEquals(gOrig.edges().size(), gRev.edges().size());
				assertEquals(gOrig.edges(), gRev.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				int newVertex = gRev.addVertex();
				assertTrue(gOrig.vertices().contains(newVertex));
				assertTrue(gRev.vertices().contains(newVertex));
				assertEquals(gOrig.vertices(), gRev.vertices());

				int nonExistingVertex;
				for (int v = 0;; v++) {
					if (!gRev.vertices().contains(v)) {
						nonExistingVertex = v;
						break;
					}
				}
				if (gRev instanceof IndexGraph) {
					assertThrows(UnsupportedOperationException.class, () -> gRev.addVertex(nonExistingVertex));
				} else {
					gRev.addVertex(nonExistingVertex);
					assertTrue(gOrig.vertices().contains(nonExistingVertex));
					assertTrue(gRev.vertices().contains(nonExistingVertex));
					assertEquals(gOrig.vertices(), gRev.vertices());
				}

				int vertexToRemove = gRev.vertices().iterator().nextInt();
				gRev.removeVertex(vertexToRemove);
				if (!(gRev instanceof IndexGraph)) {
					assertFalse(gOrig.vertices().contains(vertexToRemove));
					assertFalse(gRev.vertices().contains(vertexToRemove));
				}
				assertEquals(gOrig.vertices(), gRev.vertices());
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				IntIterator vit = gRev.vertices().iterator();
				int u = vit.nextInt();
				int v = vit.nextInt();

				int newEdge = gRev.addEdge(u, v);
				assertTrue(gOrig.edges().contains(newEdge));
				assertTrue(gRev.edges().contains(newEdge));
				assertEquals(gOrig.edges(), gRev.edges());

				int nonExistingEdge;
				for (int e = 0;; e++) {
					if (!gRev.edges().contains(e)) {
						nonExistingEdge = e;
						break;
					}
				}
				if (gRev instanceof IndexGraph) {
					assertThrows(UnsupportedOperationException.class, () -> gRev.addEdge(u, v, nonExistingEdge));
				} else {
					gRev.addEdge(u, v, nonExistingEdge);
					assertTrue(gOrig.edges().contains(nonExistingEdge));
					assertTrue(gRev.edges().contains(nonExistingEdge));
					assertEquals(gOrig.edges(), gRev.edges());
				}

				int edgeToRemove = gRev.edges().iterator().nextInt();
				gRev.removeEdge(edgeToRemove);
				if (!(gRev instanceof IndexGraph)) {
					assertFalse(gOrig.edges().contains(edgeToRemove));
					assertFalse(gRev.edges().contains(edgeToRemove));
				}
				assertEquals(gOrig.edges(), gRev.edges());
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				for (int u : gRev.vertices()) {
					EdgeSet edges = gRev.outEdges(u);
					assertEquals(gOrig.inEdges(u).size(), edges.size());
					assertEquals(gOrig.inEdges(u), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
						assertEquals(gRev.edgeEndpoint(e, u), eit.target());
						assertEquals(u, gRev.edgeEndpoint(e, eit.target()));

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
				for (int v : gRev.vertices()) {
					EdgeSet edges = gRev.inEdges(v);
					assertEquals(gOrig.outEdges(v).size(), edges.size());
					assertEquals(gOrig.outEdges(v), edges);

					IntSet iteratedEdges = new IntOpenHashSet();
					for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
						assertEquals(gRev.edgeEndpoint(e, v), eit.source());
						assertEquals(v, gRev.edgeEndpoint(e, eit.source()));

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
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				for (int u : gRev.vertices()) {
					for (int v : gRev.vertices()) {
						EdgeSet edges = gRev.getEdges(u, v);
						assertEquals(gOrig.getEdges(v, u).size(), edges.size());
						assertEquals(gOrig.getEdges(v, u), edges);

						if (edges.isEmpty()) {
							assertEquals(-1, gRev.getEdge(u, v));
						} else {
							int e = gRev.getEdge(u, v);
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
							assertEquals(u, gRev.edgeEndpoint(e, v));
							assertEquals(v, gRev.edgeEndpoint(e, u));
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
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				int v = gRev.vertices().iterator().nextInt();

				gRev.removeEdgesOf(v);
				assertTrue(gRev.outEdges(v).isEmpty());
				assertTrue(gRev.inEdges(v).isEmpty());
				assertTrue(gOrig.outEdges(v).isEmpty());
				assertTrue(gOrig.inEdges(v).isEmpty());
			}
		}
	}

	@Test
	public void testRemoveEdgesInOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				int v = gRev.vertices().iterator().nextInt();

				gRev.removeInEdgesOf(v);
				assertTrue(gRev.inEdges(v).isEmpty());
				assertTrue(gOrig.outEdges(v).isEmpty());
			}
		}
	}

	@Test
	public void testRemoveEdgesOutOf() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				int v = gRev.vertices().iterator().nextInt();

				gRev.removeOutEdgesOf(v);
				assertTrue(gRev.outEdges(v).isEmpty());
				assertTrue(gOrig.inEdges(v).isEmpty());
			}
		}
	}

	@Test
	public void testReverseEdge() {
		Graph gOrig0 = createGraph(true);
		Graph gRev0 = gOrig0.reverseView();
		for (boolean index : BooleanList.of(false, true)) {
			Graph gRev = index ? gRev0.indexGraph() : gRev0;

			int e = gRev.edges().iterator().nextInt();
			int s = gRev.edgeSource(e), t = gRev.edgeTarget(e);

			gRev.reverseEdge(e);
			assertEquals(s, gRev.edgeTarget(e));
			assertEquals(t, gRev.edgeSource(e));
		}
	}

	@Test
	public void testEdgeGetSourceTarget() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				for (int e : gRev.edges()) {
					assertEquals(gOrig.edgeSource(e), gRev.edgeTarget(e));
					assertEquals(gOrig.edgeTarget(e), gRev.edgeSource(e));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				gRev.clear();
				assertTrue(gRev.vertices().isEmpty());
				assertTrue(gRev.edges().isEmpty());
				assertTrue(gOrig.vertices().isEmpty());
				assertTrue(gOrig.edges().isEmpty());
			}
		}
	}

	@Test
	public void testClearEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;
				gRev.clearEdges();
				assertTrue(gRev.edges().isEmpty());
				assertTrue(gOrig.edges().isEmpty());
			}
		}
	}

	@Test
	public void testVerticesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				Object key1 = new Object(), key2 = new Object();
				{
					Weights.Int vWeights1 = gOrig.addVerticesWeights(key1, int.class);
					for (int v : gOrig.vertices())
						vWeights1.set(v, rand.nextInt(10000));
					Weights.Int vWeights2 = gRev.addVerticesWeights(key2, int.class);
					for (int v : gRev.vertices())
						vWeights2.set(v, rand.nextInt(10000));
				}

				assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
				for (Object key : List.of(key1, key2)) {
					Weights.Int wOrig = gOrig.getVerticesWeights(key);
					Weights.Int wRev = gRev.getVerticesWeights(key);

					for (int v : gRev.vertices())
						assertEquals(wOrig.getInt(v), wRev.getInt(v));
					assertEquals(wOrig.defaultWeightInt(), wRev.defaultWeightInt());
				}

				gRev.removeVerticesWeights(key1);
				assertEquals(gOrig.getVerticesWeightsKeys(), gRev.getVerticesWeightsKeys());
			}
		}
	}

	@Test
	public void testEdgesWeights() {
		final long seed = 0xd0c0957ff17f0eb4L;
		Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				Object key1 = new Object(), key2 = new Object();
				{
					Weights.Int eWeights1 = gOrig.addEdgesWeights(key1, int.class);
					for (int e : gOrig.edges())
						eWeights1.set(e, rand.nextInt(10000));
					Weights.Int eWeights2 = gRev.addEdgesWeights(key2, int.class);
					for (int e : gRev.edges())
						eWeights2.set(e, rand.nextInt(10000));
				}

				assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
				for (Object key : List.of(key1, key2)) {
					Weights.Int wOrig = gOrig.getEdgesWeights(key);
					Weights.Int wRev = gRev.getEdgesWeights(key);

					for (int e : gRev.edges())
						assertEquals(wOrig.getInt(e), wRev.getInt(e));
					assertEquals(wOrig.defaultWeightInt(), wRev.defaultWeightInt());
				}

				gRev.removeEdgesWeights(key1);
				assertEquals(gOrig.getEdgesWeightsKeys(), gRev.getEdgesWeightsKeys());
			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testGraphCapabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			Graph gOrig0 = createGraph(directed);
			Graph gRev0 = gOrig0.reverseView();
			for (boolean index : BooleanList.of(false, true)) {
				Graph gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph gRev = index ? gRev0.indexGraph() : gRev0;

				GraphCapabilities capOrig = gOrig.getCapabilities();
				GraphCapabilities capUnmod = gRev.getCapabilities();

				assertEquals(capOrig.parallelEdges(), capUnmod.parallelEdges());
				assertEquals(capOrig.selfEdges(), capUnmod.selfEdges());
				assertEquals(capOrig.directed(), capUnmod.directed());
			}
		}
	}

}
