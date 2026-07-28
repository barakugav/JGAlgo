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
package com.jgalgo.adapt.jgrapht;

import static com.jgalgo.internal.util.Range.range;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexRemoveListener;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

@SuppressWarnings("boxing")
public class JGraphTWrapperTest {

	@Test
	public void constructor() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertEquals(directed, g.isDirected());
			assertEquals(Set.of(), g.edgesWeightsKeys());
			assertIndexGraphValid(g);
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, null);
			assertEquals(directed, g.isDirected());
			assertEquals(Set.of(), g.edgesWeightsKeys());
			assertIndexGraphValid(g);
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, "weights");
			assertEquals(directed, g.isDirected());
			assertEquals(Set.of("weights"), g.edgesWeightsKeys());
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void vertices() {
		final Random rand = new Random(0x2c3dfefd3970b9b7L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertEquals(gOrig.vertexSet(), g.vertices());
			for (Integer v : gOrig.vertexSet())
				assertEquals(gOrig.containsVertex(v), g.vertices().contains(v));
			assertFalse(g.vertices().contains(nonExistingVertex(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edges() {
		final Random rand = new Random(0xd3c4747c2c850fe3L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertEquals(gOrig.edgeSet(), g.edges());
			for (Integer e : gOrig.edgeSet())
				assertEquals(gOrig.containsEdge(e), g.edges().contains(e));
			assertFalse(g.edges().contains(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeSource() {
		final Random rand = new Random(0xe9aabe356d0a5fe4L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer e : gOrig.edgeSet())
				assertEquals(gOrig.getEdgeSource(e), g.edgeSource(e));
			assertThrows(NoSuchEdgeException.class, () -> g.edgeSource(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeTarget() {
		final Random rand = new Random(0x671cf8c5c138aa5L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer e : gOrig.edgeSet())
				assertEquals(gOrig.getEdgeTarget(e), g.edgeTarget(e));
			assertThrows(NoSuchEdgeException.class, () -> g.edgeTarget(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeEndpoint() {
		final Random rand = new Random(0xc6d4ba0ee6f4a2beL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer e : gOrig.edgeSet()) {
				Integer u = gOrig.getEdgeSource(e);
				Integer v = gOrig.getEdgeTarget(e);
				assertEquals(u, g.edgeEndpoint(e, v));
				assertEquals(v, g.edgeEndpoint(e, u));
				Integer w = Graphs.randVertex(g, rand);
				if (!w.equals(u) && !w.equals(v))
					assertThrows(IllegalArgumentException.class, () -> g.edgeEndpoint(e, w));
			}
			assertThrows(NoSuchEdgeException.class,
					() -> g.edgeEndpoint(nonExistingEdge(gOrig, rand), g.vertices().iterator().next()));
			assertThrows(NoSuchVertexException.class,
					() -> g.edgeEndpoint(Graphs.randEdge(g, rand), nonExistingVertex(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void getEdge() {
		final Random rand = new Random(0x9374223c18e15ff7L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, false, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer e : gOrig.edgeSet())
				assertEquals(e, g.getEdge(gOrig.getEdgeSource(e), gOrig.getEdgeTarget(e)));
			for (int i = 0; i < 10; i++) {
				Integer u = Graphs.randVertex(g, rand);
				Integer v = Graphs.randVertex(g, rand);
				assertEquals(gOrig.getEdge(u, v), g.getEdge(u, v));
			}
			Integer existingVertex = g.vertices().iterator().next();
			Integer nonExistingVertex = nonExistingVertex(gOrig, rand);
			assertThrows(NoSuchVertexException.class, () -> g.getEdge(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> g.getEdge(nonExistingVertex, existingVertex));
			assertIndexGraphValid(g);
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer eEndpoints : gOrig.edgeSet()) {
				Integer u = gOrig.getEdgeSource(eEndpoints);
				Integer v = gOrig.getEdgeTarget(eEndpoints);
				Integer e = g.getEdge(u, v);
				if (directed) {
					assertEquals(u, g.edgeSource(e));
					assertEquals(v, g.edgeTarget(e));
				} else {
					assertTrue(u.equals(g.edgeSource(e)) || u.equals(g.edgeTarget(e)));
					assertTrue(v.equals(g.edgeSource(e)) || v.equals(g.edgeTarget(e)));
				}
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void getEdges() {
		final Random rand = new Random(0xcd7cca642e51ffb7L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer u : gOrig.vertexSet()) {
				for (Integer v : gOrig.vertexSet()) {
					EdgeSet<Integer, Integer> edges = g.getEdges(u, v);
					assertEquals(gOrig.getAllEdges(u, v), edges);
					boolean first = true;
					for (EdgeIter<Integer, Integer> eit = edges.iterator();;) {
						if (first) {
							assertThrows(IllegalStateException.class, () -> eit.source());
							assertThrows(IllegalStateException.class, () -> eit.target());
							first = false;
						}
						if (!eit.hasNext()) {
							assertThrows(NoSuchElementException.class, () -> eit.next());
							assertThrows(NoSuchElementException.class, () -> eit.peekNext());
							break;
						}
						Integer peek = eit.peekNext();
						Integer e = eit.next();
						assertEquals(peek, e);
						Integer eitSource = eit.source();
						Integer eitTarget = eit.target();
						assertEquals(u, eitSource);
						assertEquals(v, eitTarget);
						if (directed) {
							assertEquals(u, g.edgeSource(e));
							assertEquals(v, g.edgeTarget(e));
						} else {
							assertTrue(u.equals(g.edgeSource(e)) || u.equals(g.edgeTarget(e)));
							assertTrue(v.equals(g.edgeSource(e)) || v.equals(g.edgeTarget(e)));
						}
					}

					assertEquals(edges, new HashSet<>(List.of(edges.toArray())));
					assertEquals(edges, new HashSet<>(List.of(edges.toArray(new Integer[0]))));
				}
			}
			Integer existingVertex = g.vertices().iterator().next();
			Integer nonExistingVertex = nonExistingVertex(gOrig, rand);
			assertThrows(NoSuchVertexException.class, () -> g.getEdges(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> g.getEdges(nonExistingVertex, existingVertex));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void outEdges() {
		final Random rand = new Random(0x6e634575741fc3bdL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer u : gOrig.vertexSet()) {
				EdgeSet<Integer, Integer> outEdges = g.outEdges(u);
				assertEquals(gOrig.outgoingEdgesOf(u), outEdges);

				if (directed) {
					assertEquals(gOrig.outDegreeOf(u), outEdges.size());
				} else {
					int expectedOutDegree = gOrig.outDegreeOf(u);
					for (Integer e : gOrig.outgoingEdgesOf(u))
						if (gOrig.getEdgeSource(e).equals(gOrig.getEdgeTarget(e)))
							expectedOutDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedOutDegree, outEdges.size());
				}

				for (Integer e : g.edges()) {
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.getEdgeSource(e).equals(u);
					} else {
						expectedContains = gOrig.getEdgeSource(e).equals(u) || gOrig.getEdgeTarget(e).equals(u);
					}
					assertEquals(expectedContains, outEdges.contains(e));
				}
				assertFalse(outEdges.contains(nonExistingEdge(gOrig, rand)));

				boolean first = true;
				for (EdgeIter<Integer, Integer> eit = outEdges.iterator();;) {
					if (first) {
						assertThrows(IllegalStateException.class, () -> eit.source());
						assertThrows(IllegalStateException.class, () -> eit.target());
						first = false;
					}
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, () -> eit.next());
						assertThrows(NoSuchElementException.class, () -> eit.peekNext());
						break;
					}
					Integer peek = eit.peekNext();
					Integer e = eit.next();
					assertEquals(peek, e);
					assertEquals(u, eit.source());
					assertEquals(g.edgeEndpoint(e, u), eit.target());
				}
			}
			assertThrows(NoSuchVertexException.class, () -> g.outEdges(nonExistingVertex(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void inEdges() {
		final Random rand = new Random(0x97049eb841916575L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (Integer u : gOrig.vertexSet()) {
				EdgeSet<Integer, Integer> inEdges = g.inEdges(u);
				assertEquals(gOrig.incomingEdgesOf(u), inEdges);

				if (directed) {
					assertEquals(gOrig.inDegreeOf(u), inEdges.size());
				} else {
					int expectedInDegree = gOrig.inDegreeOf(u);
					for (Integer e : gOrig.incomingEdgesOf(u))
						if (gOrig.getEdgeSource(e).equals(gOrig.getEdgeTarget(e)))
							expectedInDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedInDegree, inEdges.size());
				}

				for (Integer e : g.edges()) {
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.getEdgeTarget(e).equals(u);
					} else {
						expectedContains = gOrig.getEdgeSource(e).equals(u) || gOrig.getEdgeTarget(e).equals(u);
					}
					assertEquals(expectedContains, inEdges.contains(e));
				}
				assertFalse(inEdges.contains(nonExistingEdge(gOrig, rand)));

				boolean first = true;
				for (EdgeIter<Integer, Integer> eit = inEdges.iterator();;) {
					if (first) {
						assertThrows(IllegalStateException.class, () -> eit.source());
						assertThrows(IllegalStateException.class, () -> eit.target());
						first = false;
					}
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, () -> eit.next());
						assertThrows(NoSuchElementException.class, () -> eit.peekNext());
						break;
					}
					Integer peek = eit.peekNext();
					Integer e = eit.next();
					assertEquals(peek, e);
					assertEquals(g.edgeEndpoint(e, u), eit.source());
					assertEquals(u, eit.target());
				}
			}
			assertThrows(NoSuchVertexException.class, () -> g.inEdges(nonExistingVertex(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void capabilities() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges, false);
					Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
					assertEquals(directed, g.isDirected());
					assertEquals(selfEdges, g.isAllowSelfEdges());
					assertEquals(parallelEdges, g.isAllowParallelEdges());
					assertIndexGraphValid(g);
				}
			}
		}
	}

	@Test
	public void addVertex() {
		final Random rand = new Random(0x6e634575741fc3bdL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (int i = 0; i < 10; i++) {
				Integer v = nonExistingVertex(gOrig, rand);
				int verticesNumBefore = gOrig.vertexSet().size();
				g.addVertex(v);
				assertEquals(verticesNumBefore + 1, gOrig.vertexSet().size());
				assertTrue(g.vertices().contains(v));
				assertTrue(gOrig.containsVertex(v));
				assertIndexGraphValid(g);
			}
			assertThrows(IllegalArgumentException.class, () -> g.addVertex(Graphs.randVertex(g, rand)));
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertNull(g.vertexBuilder());
			assertThrows(UnsupportedOperationException.class, () -> g.addVertex());
		}
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0x733d781fed52657bL);
		for (boolean directed : new boolean[] { false, true }) {
			for (int i = 0; i < 10; i++) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
				Set<Integer> verticesToAdd = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToAdd.add(nonExistingVertex(gOrig, rand));
				int verticesNumBefore = gOrig.vertexSet().size();
				g.addVertices(verticesToAdd);
				assertEquals(verticesNumBefore + verticesToAdd.size(), gOrig.vertexSet().size());
				for (Integer v : verticesToAdd) {
					assertTrue(g.vertices().contains(v));
					assertTrue(gOrig.containsVertex(v));
				}
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeVertex() {
		final Random rand = new Random(0x35cb0b2a1678dac0L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);

			assertThrows(NoSuchVertexException.class, () -> g.removeVertex(nonExistingVertex(gOrig, rand)));

			while (gOrig.vertexSet().size() > 0) {
				Integer v = Graphs.randVertex(g, rand);
				int verticesNumBefore = gOrig.vertexSet().size();
				g.removeVertex(v);
				assertEquals(verticesNumBefore - 1, gOrig.vertexSet().size());
				assertFalse(g.vertices().contains(v));
				assertFalse(gOrig.containsVertex(v));
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeVertices() {
		final Random rand = new Random(0xce23cf4ecfd8eac1L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);

			while (gOrig.vertexSet().size() > 0) {
				Set<Integer> verticesToRemove = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToRemove.add(Graphs.randVertex(g, rand));
				int verticesNumBefore = gOrig.vertexSet().size();
				g.removeVertices(verticesToRemove);
				assertEquals(verticesNumBefore - verticesToRemove.size(), gOrig.vertexSet().size());
				for (Integer v : verticesToRemove) {
					assertFalse(g.vertices().contains(v));
					assertFalse(gOrig.containsVertex(v));
				}
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0x7bc30111633c945eL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.renameVertex(Graphs.randVertex(g, rand), nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void addEdge() {
		final Random rand = new Random(0x90d91fc7350b8357L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			for (int i = 0; i < 10; i++) {
				Pair<Integer, Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				Integer u = endpoints.left();
				Integer v = endpoints.right();
				Integer e = nonExistingEdge(gOrig, rand);
				int edgesNumBefore = gOrig.edgeSet().size();
				g.addEdge(u, v, e);
				assertEquals(edgesNumBefore + 1, gOrig.edgeSet().size());
				assertTrue(g.edges().contains(e));
				assertEquals(u, g.edgeSource(e));
				assertEquals(v, g.edgeTarget(e));
				assertTrue(gOrig.containsEdge(e));
				assertEquals(u, gOrig.getEdgeSource(e));
				assertEquals(v, gOrig.getEdgeTarget(e));
				assertIndexGraphValid(g);
			}
			assertThrows(IllegalArgumentException.class, () -> {
				Pair<Integer, Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				int u = endpoints.left();
				int v = endpoints.right();
				g.addEdge(u, v, Graphs.randEdge(g, rand));
			});
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertNull(g.edgeBuilder());
			assertThrows(UnsupportedOperationException.class,
					() -> g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0x3dd1d1628c28d086L);
		for (boolean directed : new boolean[] { false, true }) {
			for (int i = 0; i < 10; i++) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);

				Graph<Integer, Integer> gToAdd =
						IntGraphFactory.directed().allowSelfEdges().allowParallelEdges().newGraph();
				gToAdd.addVertices(gOrig.vertexSet());
				for (int s = rand.nextInt(5), j = 0; j < s; j++) {
					Pair<Integer, Integer> endpoints = validEndpointsToAdd(gOrig, rand);
					Integer u = endpoints.left();
					Integer v = endpoints.right();
					Integer e;
					do {
						e = nonExistingEdge(gOrig, rand);
					} while (g.edges().contains(e) || gToAdd.edges().contains(e));
					gToAdd.addEdge(u, v, e);
				}
				EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gToAdd);

				int edgesNumBefore = gOrig.edgeSet().size();
				g.addEdges(edgesToAdd);
				assertEquals(edgesNumBefore + edgesToAdd.size(), gOrig.edgeSet().size());
				for (EdgeIter<Integer, Integer> eit = edgesToAdd.iterator(); eit.hasNext();) {
					Integer e = eit.next();
					Integer u = eit.source();
					Integer v = eit.target();
					assertTrue(g.edges().contains(e));
					assertEquals(u, g.edgeSource(e));
					assertEquals(v, g.edgeTarget(e));
					assertTrue(gOrig.containsEdge(e));
					assertEquals(u, gOrig.getEdgeSource(e));
					assertEquals(v, gOrig.getEdgeTarget(e));
				}
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x457feb00dce39a72L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			while (gOrig.edgeSet().size() > 0) {
				Integer e = Graphs.randEdge(g, rand);
				int edgesNumBefore = gOrig.edgeSet().size();
				g.removeEdge(e);
				assertEquals(edgesNumBefore - 1, gOrig.edgeSet().size());
				assertFalse(g.edges().contains(e));
				assertFalse(gOrig.containsEdge(e));
				assertIndexGraphValid(g);
			}
			assertThrows(NoSuchEdgeException.class, () -> g.removeEdge(nonExistingEdge(gOrig, rand)));
		}
	}

	@Test
	public void removeEdges() {
		final Random rand = new Random(0xdc79a2b120e708daL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			while (gOrig.edgeSet().size() > 0) {
				Set<Integer> edgesToRemove = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					edgesToRemove.add(Graphs.randEdge(g, rand));

				int edgesNumBefore = gOrig.edgeSet().size();
				g.removeEdges(edgesToRemove);
				assertEquals(edgesNumBefore - edgesToRemove.size(), gOrig.edgeSet().size());
				for (Integer e : edgesToRemove) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.containsEdge(e));
				}
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeEdgesOf() {
		final Random rand = new Random(0x89b961a8efdd34L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			while (gOrig.edgeSet().size() > 0) {
				Integer vertex = g.edgeSource(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet();
				expectedRemovedEdges.addAll(g.outEdges(vertex));
				expectedRemovedEdges.addAll(g.inEdges(vertex));
				int edgesNumBefore = gOrig.edgeSet().size();
				g.removeEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edgeSet().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.containsEdge(e));
				}
				assertTrue(g.outEdges(vertex).isEmpty());
				assertTrue(g.inEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeOutEdgesOf() {
		final Random rand = new Random(0x9bf18a64c3812b86L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			while (gOrig.edgeSet().size() > 0) {
				Integer vertex = g.edgeSource(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet(g.outEdges(vertex));
				int edgesNumBefore = gOrig.edgeSet().size();
				g.removeOutEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edgeSet().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.containsEdge(e));
				}
				assertTrue(g.outEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void removeInEdgesOf() {
		final Random rand = new Random(0x9bf18a64c3812b86L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			while (gOrig.edgeSet().size() > 0) {
				Integer vertex = g.edgeTarget(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet(g.inEdges(vertex));
				int edgesNumBefore = gOrig.edgeSet().size();
				g.removeInEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edgeSet().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.containsEdge(e));
				}
				assertTrue(g.inEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0x16304de85b9e702aL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.renameEdge(Graphs.randEdge(g, rand), nonExistingEdge(gOrig, rand)));
		}
	}

	@Test
	public void moveEdge() {
		final Random rand = new Random(0x742cfb6935114b5dL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.moveEdge(Graphs.randEdge(g, rand), Graphs.randVertex(g, rand), Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void clear() {
		for (boolean directed : new boolean[] { false, true }) {
			for (int i = 0; i < 10; i++) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
				g.clear();
				assertEquals(Set.of(), g.vertices());
				assertEquals(Set.of(), g.edges());
				assertEquals(Set.of(), gOrig.vertexSet());
				assertEquals(Set.of(), gOrig.edgeSet());
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void clearEdges() {
		for (boolean directed : new boolean[] { false, true }) {
			for (int i = 0; i < 10; i++) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
				Set<Integer> expectedVertices = new IntOpenHashSet(gOrig.vertexSet());
				g.clearEdges();
				assertEquals(expectedVertices, g.vertices());
				assertEquals(Set.of(), g.edges());
				assertEquals(expectedVertices, gOrig.vertexSet());
				assertEquals(Set.of(), gOrig.edgeSet());
				assertIndexGraphValid(g);
			}
		}
	}

	@Test
	public void weights() {
		final Random rand = new Random(0x7ba17beb69ef3b9aL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, "weights");

			assertEquals(Set.of("weights"), g.edgesWeightsKeys());
			WeightsDouble<Integer> weights = g.edgesWeights("weights");
			assertNotNull(weights);

			assertEquals(1, weights.defaultWeight());
			for (Integer e : gOrig.edgeSet())
				assertEquals(gOrig.getEdgeWeight(e), weights.get(e));

			Object2DoubleMap<Integer> expectedWeights = new Object2DoubleOpenHashMap<>();
			for (Integer e : g.edges()) {
				double w = rand.nextInt(100);
				weights.set(e, w);
				expectedWeights.put(e, w);
			}
			for (Integer e : g.edges()) {
				assertEquals(expectedWeights.getDouble(e), weights.get(e));
				assertEquals(expectedWeights.getDouble(e), gOrig.getEdgeWeight(e));
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			assertThrows(IllegalArgumentException.class, () -> new JGraphTWrapper<>(gOrig, "weights"));
		}
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean weighted : new boolean[] { false, true }) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, weighted);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, weighted ? "weights" : null);

				assertEquals(Set.of(), g.verticesWeightsKeys());
				assertNull(g.verticesWeights("weights"));
				assertThrows(UnsupportedOperationException.class, () -> g.addVerticesWeights("newWeights", int.class));
				assertThrows(IllegalArgumentException.class, () -> g.removeVerticesWeights("non-existing-weights"));

				assertEquals(weighted ? Set.of("weights") : Set.of(), g.edgesWeightsKeys());
				assertNull(g.edgesWeights("non-existing-weights"));
				if (weighted) {
					assertNotNull(g.edgesWeights("weights"));
				} else {
					assertNull(g.edgesWeights("weights"));
				}
				assertThrows(UnsupportedOperationException.class, () -> g.addEdgesWeights("newWeights", int.class));
				assertThrows(IllegalArgumentException.class, () -> g.removeEdgesWeights("non-existing-weights"));
				if (weighted)
					assertThrows(UnsupportedOperationException.class, () -> g.removeEdgesWeights("weights"));
			}
		}
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			g.ensureVertexCapacity(gOrig.vertexSet().size() + 10);
			g.ensureEdgeCapacity(gOrig.edgeSet().size() + 10);
		}
	}

	@Test
	public void verticesIndexGraph() {
		final Random rand = new Random(0xe03d4253f135a0e3L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			final int n = gOrig.vertexSet().size();

			IntSet vertices = g.indexGraph().vertices();
			assertEquals(range(n), vertices);
			for (int i : range(-15, n + 15))
				assertEquals(0 <= i && i < n, vertices.contains(i));
			for (int i = 0; i < 10; i++) {
				IntList l = new IntArrayList();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					l.add(rand.nextInt(n * 3 / 2));
				assertEquals(range(n).containsAll(l), vertices.containsAll(l));
				assertEquals(range(n).containsAll(l), vertices.containsAll(new ArrayList<>(l)));
			}
			assertArrayEquals(range(n).toArray(), vertices.toArray());
			assertArrayEquals(range(n).toArray(new Integer[0]), vertices.toArray(new Integer[0]));
			assertArrayEquals(range(n).toIntArray(), vertices.toIntArray());
			assertArrayEquals(range(n).toIntArray(), vertices.toArray(new int[0]));
		}
	}

	@Test
	public void edgesIndexGraph() {
		final Random rand = new Random(0xf6706cd69781812aL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			final int m = gOrig.edgeSet().size();

			IntSet edges = g.indexGraph().edges();
			assertEquals(range(m), edges);
			for (int i : range(-15, m + 15))
				assertEquals(0 <= i && i < m, edges.contains(i));
			for (int i = 0; i < 10; i++) {
				IntList l = new IntArrayList();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					l.add(rand.nextInt(m * 3 / 2));
				assertEquals(range(m).containsAll(l), edges.containsAll(l));
				assertEquals(range(m).containsAll(l), edges.containsAll(new ArrayList<>(l)));
			}
			assertArrayEquals(range(m).toArray(), edges.toArray());
			assertArrayEquals(range(m).toArray(new Integer[0]), edges.toArray(new Integer[0]));
			assertArrayEquals(range(m).toIntArray(), edges.toIntArray());
			assertArrayEquals(range(m).toIntArray(), edges.toArray(new int[0]));
		}
	}

	@Test
	public void edgeSourceIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edgeSet()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = ig.edgeSource(eIdx);
				Integer u = viMap.indexToId(uIdx);
				assertEquals(gOrig.getEdgeSource(e), u);
			}
			assertThrows(NoSuchEdgeException.class, () -> ig.edgeSource(-8));
		}
	}

	@Test
	public void edgeTargetIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edgeSet()) {
				int eIdx = eiMap.idToIndex(e);
				int vIdx = ig.edgeTarget(eIdx);
				Integer v = viMap.indexToId(vIdx);
				assertEquals(gOrig.getEdgeTarget(e), v);
			}
			assertThrows(NoSuchEdgeException.class, () -> ig.edgeTarget(-8));
		}
	}

	@Test
	public void edgeEndpointIndexGraph() {
		final Random rand = new Random(0xe029331e9ed608bbL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edgeSet()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = viMap.idToIndex(gOrig.getEdgeSource(e));
				int vIdx = viMap.idToIndex(gOrig.getEdgeTarget(e));
				assertEquals(uIdx, ig.edgeEndpoint(eIdx, vIdx));
				assertEquals(vIdx, ig.edgeEndpoint(eIdx, uIdx));
				int wIdx = Graphs.randVertex(ig, rand);
				if (wIdx != uIdx && wIdx != vIdx)
					assertThrows(IllegalArgumentException.class, () -> ig.edgeEndpoint(eIdx, wIdx));
			}
			assertThrows(NoSuchEdgeException.class, () -> ig.edgeEndpoint(-8, ig.vertices().iterator().nextInt()));
			assertThrows(NoSuchVertexException.class, () -> ig.edgeEndpoint(Graphs.randEdge(ig, rand), -8));
		}
	}

	@Test
	public void getEdgeIndexGraph() {
		final Random rand = new Random(0xd1dcd8612d17b796L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, false, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edgeSet()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = viMap.idToIndex(gOrig.getEdgeSource(e));
				int vIdx = viMap.idToIndex(gOrig.getEdgeTarget(e));
				assertEquals(eIdx, ig.getEdge(uIdx, vIdx));
			}
			for (int i = 0; i < 10; i++) {
				int uIdx = Graphs.randVertex(ig, rand);
				int vIdx = Graphs.randVertex(ig, rand);
				assertEquals(gOrig.getEdge(viMap.indexToId(uIdx), viMap.indexToId(vIdx)),
						eiMap.indexToIdIfExist(ig.getEdge(uIdx, vIdx)));
			}
			int existingVertex = ig.vertices().iterator().nextInt();
			int nonExistingVertex = -8;
			assertThrows(NoSuchVertexException.class, () -> ig.getEdge(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> ig.getEdge(nonExistingVertex, existingVertex));
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			for (Integer eEndpoints : gOrig.edgeSet()) {
				int u = viMap.idToIndex(gOrig.getEdgeSource(eEndpoints));
				int v = viMap.idToIndex(gOrig.getEdgeTarget(eEndpoints));
				int e = ig.getEdge(u, v);
				if (directed) {
					assertEquals(u, ig.edgeSource(e));
					assertEquals(v, ig.edgeTarget(e));
				} else {
					assertTrue(u == ig.edgeSource(e) || u == ig.edgeTarget(e));
					assertTrue(v == ig.edgeSource(e) || v == ig.edgeTarget(e));
				}
			}
		}
	}

	@Test
	public void getEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, false);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.vertexSet()) {
				for (Integer v : gOrig.vertexSet()) {
					int uIdx = viMap.idToIndex(u);
					int vIdx = viMap.idToIndex(v);
					IEdgeSet edges = ig.getEdges(uIdx, vIdx);
					assertEquals(gOrig.getAllEdges(u, v),
							edges.intStream().mapToObj(eiMap::indexToId).collect(toSet()));
					boolean first = true;
					for (IEdgeIter eit = edges.iterator();;) {
						if (first) {
							assertThrows(IllegalStateException.class, () -> eit.sourceInt());
							assertThrows(IllegalStateException.class, () -> eit.targetInt());
							first = false;
						}
						if (!eit.hasNext()) {
							assertThrows(NoSuchElementException.class, () -> eit.nextInt());
							assertThrows(NoSuchElementException.class, () -> eit.peekNextInt());
							break;
						}
						int peek = eit.peekNextInt();
						int eIdx = eit.nextInt();
						Integer e = eiMap.indexToId(eIdx);
						assertEquals(peek, eIdx);
						int eitSource = eit.sourceInt();
						int eitTarget = eit.targetInt();
						assertEquals(uIdx, eitSource);
						assertEquals(vIdx, eitTarget);
						if (directed) {
							assertEquals(uIdx, ig.edgeSource(eIdx));
							assertEquals(vIdx, ig.edgeTarget(eIdx));
							assertEquals(u, g.edgeSource(e));
							assertEquals(v, g.edgeTarget(e));
						} else {
							assertTrue(uIdx == ig.edgeSource(eIdx) || uIdx == ig.edgeTarget(eIdx));
							assertTrue(vIdx == ig.edgeSource(eIdx) || vIdx == ig.edgeTarget(eIdx));
							assertTrue(u.equals(g.edgeSource(e)) || u.equals(g.edgeTarget(e)));
							assertTrue(v.equals(g.edgeSource(e)) || v.equals(g.edgeTarget(e)));
						}
					}

					assertEquals(edges, new HashSet<>(List.of(edges.toArray())));
					assertEquals(edges, new HashSet<>(List.of(edges.toArray(new Integer[0]))));
					assertEquals(edges, new IntOpenHashSet(edges.toIntArray()));
					assertEquals(edges, new IntOpenHashSet(edges.toArray(new int[0])));
				}
			}
			int existingVertex = ig.vertices().iterator().nextInt();
			int nonExistingVertex = -8;
			assertThrows(NoSuchVertexException.class, () -> ig.getEdges(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> ig.getEdges(nonExistingVertex, existingVertex));
		}
	}

	@Test
	public void outEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.vertexSet()) {
				int uIdx = viMap.idToIndex(u);
				IEdgeSet outEdges = ig.outEdges(uIdx);
				assertEquals(gOrig.outgoingEdgesOf(u),
						outEdges.intStream().mapToObj(eiMap::indexToId).collect(toSet()));

				if (directed) {
					assertEquals(gOrig.outDegreeOf(u), outEdges.size());
				} else {
					int expectedOutDegree = gOrig.outDegreeOf(u);
					for (Integer e : gOrig.outgoingEdgesOf(u))
						if (gOrig.getEdgeSource(e).equals(gOrig.getEdgeTarget(e)))
							expectedOutDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedOutDegree, outEdges.size());
				}

				for (Integer e : g.edges()) {
					int eIdx = eiMap.idToIndex(e);
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.getEdgeSource(e).equals(u);
					} else {
						expectedContains = gOrig.getEdgeSource(e).equals(u) || gOrig.getEdgeTarget(e).equals(u);
					}
					assertEquals(expectedContains, outEdges.contains(eIdx));
				}
				assertFalse(outEdges.contains(-8));
				assertFalse(outEdges.contains(ig.edges().size() + 8));

				boolean first = true;
				for (IEdgeIter eit = outEdges.iterator();;) {
					if (first) {
						assertThrows(IllegalStateException.class, () -> eit.sourceInt());
						assertThrows(IllegalStateException.class, () -> eit.targetInt());
						first = false;
					}
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, () -> eit.nextInt());
						assertThrows(NoSuchElementException.class, () -> eit.peekNextInt());
						break;
					}
					int peek = eit.peekNextInt();
					int e = eit.nextInt();
					assertEquals(peek, e);
					assertEquals(uIdx, eit.sourceInt());
					assertEquals(ig.edgeEndpoint(e, uIdx), eit.targetInt());
				}
			}
			assertThrows(NoSuchVertexException.class, () -> ig.outEdges(-8));
		}
	}

	@Test
	public void inEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.vertexSet()) {
				int uIdx = viMap.idToIndex(u);
				IEdgeSet inEdges = ig.inEdges(uIdx);
				assertEquals(gOrig.incomingEdgesOf(u), inEdges.intStream().mapToObj(eiMap::indexToId).collect(toSet()));

				if (directed) {
					assertEquals(gOrig.inDegreeOf(u), inEdges.size());
				} else {
					int expectedInDegree = gOrig.inDegreeOf(u);
					for (Integer e : gOrig.incomingEdgesOf(u))
						if (gOrig.getEdgeSource(e).equals(gOrig.getEdgeTarget(e)))
							expectedInDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedInDegree, inEdges.size());
				}

				for (Integer e : g.edges()) {
					int eIdx = eiMap.idToIndex(e);
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.getEdgeTarget(e).equals(u);
					} else {
						expectedContains = gOrig.getEdgeSource(e).equals(u) || gOrig.getEdgeTarget(e).equals(u);
					}
					assertEquals(expectedContains, inEdges.contains(eIdx));
				}
				assertFalse(inEdges.contains(-8));
				assertFalse(inEdges.contains(ig.edges().size() + 8));

				boolean first = true;
				for (IEdgeIter eit = inEdges.iterator();;) {
					if (first) {
						assertThrows(IllegalStateException.class, () -> eit.sourceInt());
						assertThrows(IllegalStateException.class, () -> eit.targetInt());
						first = false;
					}
					if (!eit.hasNext()) {
						assertThrows(NoSuchElementException.class, () -> eit.nextInt());
						assertThrows(NoSuchElementException.class, () -> eit.peekNextInt());
						break;
					}
					int peek = eit.peekNextInt();
					int e = eit.nextInt();
					assertEquals(peek, e);
					assertEquals(ig.edgeEndpoint(e, uIdx), eit.sourceInt());
					assertEquals(uIdx, eit.targetInt());
				}
			}
			assertThrows(NoSuchVertexException.class, () -> ig.inEdges(-8));
		}
	}

	@Test
	public void capabilitiesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges, false);
					Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
					IndexGraph ig = g.indexGraph();
					assertEquals(directed, ig.isDirected());
					assertEquals(selfEdges, ig.isAllowSelfEdges());
					assertEquals(parallelEdges, ig.isAllowParallelEdges());
					assertIndexGraphValid(ig);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addVertexIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addVertex(ig.vertices().size()));
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addVertexInt());
		}
	}

	@Test
	public void addVerticesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class,
					() -> ig.addVertices(IntList.of(ig.vertices().size(), ig.vertices().size() + 1)));
		}
	}

	@Test
	public void removeVertexIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVertex(0));
		}
	}

	@Test
	public void removeVerticesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVertices(IntList.of(0, 1, 2)));
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addEdgeIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdge(0, 1, ig.edges().size()));
		}
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdge(0, 1));
		}
	}

	@Test
	public void addEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();

			IntGraph gToAdd = IntGraph.newDirected();
			gToAdd.addVertices(ig.vertices());
			gToAdd.addEdge(0, 1, ig.edges().size());
			gToAdd.addEdge(2, 3, ig.edges().size() + 1);
			IEdgeSet edgesToAdd = IEdgeSet.allOf(gToAdd);

			assertThrows(UnsupportedOperationException.class, () -> ig.addEdges(edgesToAdd));
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdgesReassignIds(edgesToAdd));
		}
	}

	@Test
	public void removeEdgeIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdge(0));
		}
	}

	@Test
	public void removeEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdges(range(5)));
		}
	}

	@Test
	public void removeEdgesOfIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdgesOf(ig.edgeSource(0)));
		}
	}

	@Test
	public void removeOutEdgesOfIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeOutEdgesOf(ig.edgeSource(0)));
		}
	}

	@Test
	public void removeInEdgesOfIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeInEdgesOf(ig.edgeTarget(0)));
		}
	}

	@Test
	public void moveEdgeIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.moveEdge(0, 0, 1));
		}
	}

	@Test
	public void clearIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.clear());
		}
	}

	@Test
	public void clearEdgesIndexGraph() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.clearEdges());
		}
	}

	@Test
	public void weightsIndexGraph() {
		final Random rand = new Random(0x2b7e7f7d7cd46401L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, "weights");
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();

			assertEquals(Set.of("weights"), ig.edgesWeightsKeys());
			IWeightsDouble weights = ig.edgesWeights("weights");
			assertNotNull(weights);

			assertEquals(1, weights.defaultWeight());
			for (Integer e : gOrig.edgeSet())
				assertEquals(gOrig.getEdgeWeight(e), weights.get(eiMap.idToIndex(e)));

			Object2DoubleMap<Integer> expectedWeights = new Object2DoubleOpenHashMap<>();
			for (Integer e : g.edges()) {
				double w = rand.nextInt(100);
				weights.set(eiMap.idToIndex(e), w);
				expectedWeights.put(e, w);
			}
			for (Integer e : g.edges()) {
				assertEquals(expectedWeights.getDouble(e), weights.get(eiMap.idToIndex(e)));
				assertEquals(expectedWeights.getDouble(e), gOrig.getEdgeWeight(e));
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean weighted : new boolean[] { false, true }) {
				org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed, true, true, weighted);
				Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig, weighted ? "weights" : null);
				IndexGraph ig = g.indexGraph();

				assertEquals(Set.of(), ig.verticesWeightsKeys());
				assertNull(ig.verticesWeights("weights"));
				assertThrows(UnsupportedOperationException.class, () -> ig.addVerticesWeights("newWeights", int.class));
				assertThrows(UnsupportedOperationException.class,
						() -> ig.removeVerticesWeights("non-existing-weights"));

				assertEquals(weighted ? Set.of("weights") : Set.of(), ig.edgesWeightsKeys());
				assertNull(ig.edgesWeights("non-existing-weights"));
				if (weighted) {
					assertNotNull(ig.edgesWeights("weights"));
				} else {
					assertNull(ig.edgesWeights("weights"));
				}
				assertThrows(UnsupportedOperationException.class, () -> ig.addEdgesWeights("newWeights", int.class));
				assertThrows(UnsupportedOperationException.class, () -> ig.removeEdgesWeights("weights"));
			}
		}
	}

	@Test
	public void ensureCapacityIndexGraph() {
		/* can't real test anything, just cover and see no exception is thrown */
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			ig.ensureVertexCapacity(gOrig.vertexSet().size() + 10);
			ig.ensureEdgeCapacity(gOrig.edgeSet().size() + 10);
		}
	}

	@Test
	public void indexGraphRemoveListeners() {
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexRemoveListener listener = new IndexRemoveListener() {
				@Override
				public void removeLast(int removedIdx) {}

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {}
			};
			assertThrows(UnsupportedOperationException.class, () -> ig.addVertexRemoveListener(listener));
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdgeRemoveListener(listener));
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVertexRemoveListener(listener));
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdgeRemoveListener(listener));
		}
	}

	@Test
	public void indexGraphVerticesMap() {
		final Random rand = new Random(0xa1fac4326855753aL);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();

			IntSet indicesSet = new IntOpenHashSet();
			for (Integer v : g.vertices()) {
				int idx1 = viMap.idToIndex(v);
				int idx2 = viMap.idToIndexIfExist(v);
				assertEquals(idx1, idx2);
				indicesSet.add(idx1);
			}
			assertEquals(range(g.vertices().size()), indicesSet);
			assertEquals(indicesSet, ig.vertices());

			for (int i = 0; i < 10; i++) {
				Integer nonExistingVertex = rand.nextInt();
				if (g.vertices().contains(nonExistingVertex))
					continue;
				assertThrows(NoSuchVertexException.class, () -> viMap.idToIndex(nonExistingVertex));
				assertEquals(-1, viMap.idToIndexIfExist(nonExistingVertex));
			}

			final int n = g.vertices().size();
			Set<Integer> idsSet = new IntOpenHashSet();
			for (int idx : range(-15, n + 15)) {
				if (0 <= idx && idx < n) {
					Integer id1 = viMap.indexToId(idx);
					Integer id2 = viMap.indexToIdIfExist(idx);
					assertEquals(id1, id2);
					idsSet.add(id1);
				} else {
					int idx0 = idx;
					assertThrows(NoSuchVertexException.class, () -> viMap.indexToId(idx0));
					assertEquals(null, viMap.indexToIdIfExist(idx));
				}
			}
			assertEquals(g.vertices(), idsSet);
		}
	}

	@Test
	public void indexGraphEdgesMap() {
		final Random rand = new Random(0xada6daca8802e6e2L);
		for (boolean directed : new boolean[] { false, true }) {
			org.jgrapht.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();

			IntSet indicesSet = new IntOpenHashSet();
			for (Integer e : g.edges()) {
				int idx1 = eiMap.idToIndex(e);
				int idx2 = eiMap.idToIndexIfExist(e);
				assertEquals(idx1, idx2);
				indicesSet.add(idx1);
			}
			assertEquals(range(g.edges().size()), indicesSet);
			assertEquals(indicesSet, ig.edges());

			for (int i = 0; i < 10; i++) {
				Integer nonExistingEdge = rand.nextInt();
				if (g.edges().contains(nonExistingEdge))
					continue;
				assertThrows(NoSuchEdgeException.class, () -> eiMap.idToIndex(nonExistingEdge));
				assertEquals(-1, eiMap.idToIndexIfExist(nonExistingEdge));
			}

			final int m = g.edges().size();
			Set<Integer> idsSet = new IntOpenHashSet();
			for (int idx : range(-15, m + 15)) {
				if (0 <= idx && idx < m) {
					Integer id1 = eiMap.indexToId(idx);
					Integer id2 = eiMap.indexToIdIfExist(idx);
					assertEquals(id1, id2);
					idsSet.add(id1);
				} else {
					int idx0 = idx;
					assertThrows(NoSuchEdgeException.class, () -> eiMap.indexToId(idx0));
					assertEquals(null, eiMap.indexToIdIfExist(idx));
				}
			}
			assertEquals(g.edges(), idsSet);
		}
	}

	private static void assertIndexGraphValid(Graph<Integer, Integer> g) {
		final Random rand = new Random(0x13c5c05c9bc160c1L);
		final int n = g.vertices().size();
		final int m = g.edges().size();
		IndexGraph ig = g.indexGraph();
		IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
		IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();

		assertEquals(range(n), ig.vertices());
		assertEquals(range(m), ig.edges());
		Set<Integer> verticesIds = new IntOpenHashSet();
		Set<Integer> edgesIds = new IntOpenHashSet();
		IntSet verticesIndices = new IntOpenHashSet();
		IntSet edgesIndices = new IntOpenHashSet();
		for (int vIdx : range(n)) {
			Integer v = viMap.indexToId(vIdx);
			boolean duplicate = !verticesIds.add(v);
			assertFalse(duplicate);
		}
		for (int eIdx : range(m)) {
			Integer e = eiMap.indexToId(eIdx);
			boolean duplicate = !edgesIds.add(e);
			assertFalse(duplicate);
		}
		for (Integer v : g.vertices()) {
			int vIdx = viMap.idToIndex(v);
			boolean duplicate = !verticesIndices.add(vIdx);
			assertFalse(duplicate);
		}
		for (Integer e : g.edges()) {
			int eIdx = eiMap.idToIndex(e);
			boolean duplicate = !edgesIndices.add(eIdx);
			assertFalse(duplicate);
		}
		assertEquals(g.vertices(), verticesIds);
		assertEquals(g.edges(), edgesIds);
		assertEquals(range(n), verticesIndices);
		assertEquals(range(m), edgesIndices);
		for (int vIdx : range(-15, n + 15)) {
			Integer v = viMap.indexToIdIfExist(vIdx);
			if (vIdx < 0 || vIdx >= n) {
				assertEquals(null, v);
			} else {
				assertEquals(vIdx, viMap.idToIndex(v));
			}
		}
		for (int eIdx : range(-15, m + 15)) {
			Integer e = eiMap.indexToIdIfExist(eIdx);
			if (eIdx < 0 || eIdx >= m) {
				assertEquals(null, e);
			} else {
				assertEquals(eIdx, eiMap.idToIndex(e));
			}
		}
		for (int i = 0; i < 15; i++) {
			Integer v = rand.nextInt();
			if (g.vertices().contains(v)) {
				assertEquals(viMap.idToIndex(v), viMap.idToIndexIfExist(v));
			} else {
				assertEquals(-1, viMap.idToIndexIfExist(v));
			}
		}
		for (int i = 0; i < 15; i++) {
			Integer e = rand.nextInt();
			if (g.edges().contains(e)) {
				assertEquals(eiMap.idToIndex(e), eiMap.idToIndexIfExist(e));
			} else {
				assertEquals(-1, eiMap.idToIndexIfExist(e));
			}
		}
	}

	private static org.jgrapht.Graph<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true, true, true);
	}

	private static org.jgrapht.Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges,
			boolean parallelEdges, boolean weighted) {
		final Random rand = new Random(0x6be7f2c8297f31b1L);
		org.jgrapht.Graph<Integer, Integer> g = newJGraphTGraph(directed, selfEdges, parallelEdges, weighted);
		for (int v : range(50 + rand.nextInt(50)))
			g.addVertex(v);
		for (int m = 100 + rand.nextInt(100); g.edgeSet().size() < m;) {
			Pair<Integer, Integer> endpoints = validEndpointsToAdd(g, rand);
			int u = endpoints.left();
			int v = endpoints.right();
			int e = rand.nextInt(1 + g.edgeSet().size() * 2);
			if (g.containsEdge(e))
				continue;
			g.addEdge(u, v, e);
		}
		if (weighted)
			for (int e : g.edgeSet())
				g.setEdgeWeight(e, rand.nextInt(100));
		return g;
	}

	private static <V, E> org.jgrapht.Graph<V, E> newJGraphTGraph(boolean directed, boolean selfEdges,
			boolean parallelEdges, boolean weighted) {
		GraphTypeBuilder<V, E> builder = directed ? GraphTypeBuilder.directed() : GraphTypeBuilder.undirected();
		return builder
				.allowingSelfLoops(selfEdges)
				.allowingMultipleEdges(parallelEdges)
				.weighted(weighted)
				.buildGraph();
	}

	private static Pair<Integer, Integer> validEndpointsToAdd(org.jgrapht.Graph<Integer, Integer> g, Random rand) {
		Integer[] vertices = g.vertexSet().toArray(Integer[]::new);
		for (;;) {
			int u = vertices[rand.nextInt(vertices.length)];
			int v = vertices[rand.nextInt(vertices.length)];
			if (!g.getType().isAllowingSelfLoops() && u == v)
				continue;
			if (!g.getType().isAllowingMultipleEdges() && g.containsEdge(u, v))
				continue;
			int e = rand.nextInt(1 + g.edgeSet().size() * 2);
			if (g.containsEdge(e))
				continue;
			return Pair.of(u, v);
		}
	}

	private static Integer nonExistingVertex(org.jgrapht.Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			int v = rand.nextInt();
			if (v >= 0 && !g.containsVertex(v))
				return v;
		}
	}

	private static Integer nonExistingEdge(org.jgrapht.Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			int e = rand.nextInt();
			if (e >= 0 && !g.containsEdge(e))
				return e;
		}
	}

}
