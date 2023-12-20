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
package com.jgalgo.adapt.guava;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexRemoveListener;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GuavaNetworkWrapperTest {

	@Test
	public void constructor() {
		Network<Integer, Integer> gOrig = NetworkBuilder.directed().<Integer, Integer>immutable().build();
		assertThrows(IllegalArgumentException.class, () -> new GuavaNetworkWrapper<>(gOrig, true));
	}

	@Test
	public void vertices() {
		final Random rand = new Random(0xee0fe0aff09bd445L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertEquals(gOrig.nodes(), g.vertices());
			for (Integer v : gOrig.nodes())
				assertEquals(gOrig.nodes().contains(v), g.vertices().contains(v));
			assertFalse(g.vertices().contains(nonExistingVertex(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edges() {
		final Random rand = new Random(0xdaacd665c80f0277L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertEquals(gOrig.edges(), g.edges());
			for (Integer e : gOrig.edges())
				assertEquals(gOrig.edges().contains(e), g.edges().contains(e));
			assertFalse(g.edges().contains(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeSource() {
		final Random rand = new Random(0x23266c9685421effL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(gOrig.incidentNodes(e).nodeU(), g.edgeSource(e));
			assertThrows(NoSuchEdgeException.class, () -> g.edgeSource(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeTarget() {
		final Random rand = new Random(0xa202bb0cfd590188L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(gOrig.incidentNodes(e).nodeV(), g.edgeTarget(e));
			assertThrows(NoSuchEdgeException.class, () -> g.edgeTarget(nonExistingEdge(gOrig, rand)));
			assertIndexGraphValid(g);
		}
	}

	@Test
	public void edgeEndpoint() {
		final Random rand = new Random(0x350d412f62601cf1L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer e : gOrig.edges()) {
				Integer u = gOrig.incidentNodes(e).nodeU();
				Integer v = gOrig.incidentNodes(e).nodeV();
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
		final Random rand = new Random(0x30e0c2a4e3186c80L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, false);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(e, g.getEdge(gOrig.incidentNodes(e).nodeU(), gOrig.incidentNodes(e).nodeV()));
			for (int i = 0; i < 10; i++) {
				Integer u = Graphs.randVertex(g, rand);
				Integer v = Graphs.randVertex(g, rand);
				assertEquals(gOrig.edgeConnectingOrNull(u, v), g.getEdge(u, v));
			}
			Integer existingVertex = g.vertices().iterator().next();
			Integer nonExistingVertex = nonExistingVertex(gOrig, rand);
			assertThrows(NoSuchVertexException.class, () -> g.getEdge(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> g.getEdge(nonExistingVertex, existingVertex));
			assertIndexGraphValid(g);
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer eEndpoints : gOrig.edges()) {
				Integer u = gOrig.incidentNodes(eEndpoints).nodeU();
				Integer v = gOrig.incidentNodes(eEndpoints).nodeV();
				Integer e = g.getEdges(u, v).iterator().next();
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
		final Random rand = new Random(0xbcca7e883cd9c66dL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer u : gOrig.nodes()) {
				for (Integer v : gOrig.nodes()) {
					EdgeSet<Integer, Integer> edges = g.getEdges(u, v);
					assertEquals(gOrig.edgesConnecting(u, v), edges);
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
		final Random rand = new Random(0xf2c8368fe6310cc2L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer u : gOrig.nodes()) {
				EdgeSet<Integer, Integer> outEdges = g.outEdges(u);
				assertEquals(gOrig.outEdges(u), outEdges);

				if (directed) {
					assertEquals(gOrig.outDegree(u), outEdges.size());
				} else {
					int expectedOutDegree = gOrig.outDegree(u);
					for (Integer e : gOrig.outEdges(u))
						if (gOrig.incidentNodes(e).nodeU().equals(gOrig.incidentNodes(e).nodeV()))
							expectedOutDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedOutDegree, outEdges.size());
				}

				for (Integer e : g.edges()) {
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.incidentNodes(e).nodeU().equals(u);
					} else {
						expectedContains =
								gOrig.incidentNodes(e).nodeU().equals(u) || gOrig.incidentNodes(e).nodeV().equals(u);
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
		final Random rand = new Random(0x1378308b855a19b5L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (Integer u : gOrig.nodes()) {
				EdgeSet<Integer, Integer> inEdges = g.inEdges(u);
				assertEquals(gOrig.inEdges(u), inEdges);

				if (directed) {
					assertEquals(gOrig.inDegree(u), inEdges.size());
				} else {
					int expectedInDegree = gOrig.inDegree(u);
					for (Integer e : gOrig.inEdges(u))
						if (gOrig.incidentNodes(e).nodeU().equals(gOrig.incidentNodes(e).nodeV()))
							expectedInDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedInDegree, inEdges.size());
				}

				for (Integer e : g.edges()) {
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.incidentNodes(e).nodeV().equals(u);
					} else {
						expectedContains =
								gOrig.incidentNodes(e).nodeU().equals(u) || gOrig.incidentNodes(e).nodeV().equals(u);
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
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				for (boolean parallelEdges : BooleanList.of(false, true)) {
					MutableNetwork<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
		final Random rand = new Random(0x12d379bcfc0d8e52L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (int i = 0; i < 10; i++) {
				Integer v = nonExistingVertex(gOrig, rand);
				int verticesNumBefore = gOrig.nodes().size();
				g.addVertex(v);
				assertEquals(verticesNumBefore + 1, gOrig.nodes().size());
				assertTrue(g.vertices().contains(v));
				assertTrue(gOrig.nodes().contains(v));
				assertIndexGraphValid(g);
			}
			assertThrows(IllegalArgumentException.class, () -> g.addVertex(Graphs.randVertex(g, rand)));
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertNull(g.vertexBuilder());
			assertThrows(UnsupportedOperationException.class, () -> g.addVertex());
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.addVertex(nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0x60b3f7d78bcbbef0L);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int i = 0; i < 10; i++) {
				MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
				Set<Integer> verticesToAdd = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToAdd.add(nonExistingVertex(gOrig, rand));
				int verticesNumBefore = gOrig.nodes().size();
				g.addVertices(verticesToAdd);
				assertEquals(verticesNumBefore + verticesToAdd.size(), gOrig.nodes().size());
				for (Integer v : verticesToAdd) {
					assertTrue(g.vertices().contains(v));
					assertTrue(gOrig.nodes().contains(v));
				}
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Set<Integer> expectedVertices = new IntOpenHashSet(g.vertices());

			for (int i = 0; i < 20; i++) {
				Set<Integer> verticesToAdd = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToAdd.add(nonExistingVertex(gOrig, rand));
				int verticesNumBefore = gOrig.nodes().size();
				if (rand.nextBoolean() || (expectedVertices.isEmpty() && verticesToAdd.isEmpty())) {
					g.addVertices(verticesToAdd);
					expectedVertices.addAll(verticesToAdd);
					assertEquals(verticesNumBefore + verticesToAdd.size(), gOrig.nodes().size());
					for (Integer v : verticesToAdd) {
						assertTrue(g.vertices().contains(v));
						assertTrue(gOrig.nodes().contains(v));
					}
				} else {
					List<Integer> verticesToAddList = new ArrayList<>(verticesToAdd);
					if (verticesToAddList.size() > 0 && rand.nextBoolean() || expectedVertices.isEmpty()) {
						verticesToAddList.add(verticesToAddList.get(rand.nextInt(verticesToAddList.size())));
					} else {
						verticesToAddList.add(Graphs.randVertex(g, rand));
					}
					Collections.shuffle(verticesToAddList, rand);
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(verticesToAddList));
				}
				assertEquals(expectedVertices, g.vertices());
				assertEquals(expectedVertices, gOrig.nodes());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class,
					() -> g.addVertices(List.of(nonExistingVertex(gOrig, rand), nonExistingVertex(gOrig, rand))));
		}
	}

	@Test
	public void removeVertex() {
		final Random rand = new Random(0x7884014ec0a91891L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);

			assertThrows(NoSuchVertexException.class, () -> g.removeVertex(nonExistingVertex(gOrig, rand)));

			while (gOrig.nodes().size() > 0) {
				Integer v = Graphs.randVertex(g, rand);
				int verticesNumBefore = gOrig.nodes().size();
				g.removeVertex(v);
				assertEquals(verticesNumBefore - 1, gOrig.nodes().size());
				assertFalse(g.vertices().contains(v));
				assertFalse(gOrig.nodes().contains(v));
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.removeVertex(Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void removeVertices() {
		final Random rand = new Random(0x24666d0fe1dbc304L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);

			while (gOrig.nodes().size() > 0) {
				Set<Integer> verticesToRemove = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToRemove.add(Graphs.randVertex(g, rand));
				int verticesNumBefore = gOrig.nodes().size();
				g.removeVertices(verticesToRemove);
				assertEquals(verticesNumBefore - verticesToRemove.size(), gOrig.nodes().size());
				for (Integer v : verticesToRemove) {
					assertFalse(g.vertices().contains(v));
					assertFalse(gOrig.nodes().contains(v));
				}
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Set<Integer> expectedVertices = new IntOpenHashSet(g.vertices());

			while (gOrig.nodes().size() > 0) {
				Set<Integer> verticesToRemove = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					verticesToRemove.add(Graphs.randVertex(g, rand));

				if (rand.nextBoolean() || (verticesToRemove.isEmpty() && expectedVertices.isEmpty())) {
					int verticesNumBefore = gOrig.nodes().size();
					g.removeVertices(rand.nextBoolean() ? verticesToRemove : new ArrayList<>(verticesToRemove));
					expectedVertices.removeAll(verticesToRemove);
					assertEquals(verticesNumBefore - verticesToRemove.size(), gOrig.nodes().size());
					for (Integer v : verticesToRemove) {
						assertFalse(g.vertices().contains(v));
						assertFalse(gOrig.nodes().contains(v));
					}
				} else {
					List<Integer> verticesToRemoveList = new ArrayList<>(verticesToRemove);
					if (verticesToRemoveList.size() > 0 && rand.nextBoolean() || expectedVertices.isEmpty()) {
						verticesToRemoveList.add(verticesToRemoveList.get(rand.nextInt(verticesToRemoveList.size())));
					} else {
						verticesToRemoveList.add(nonExistingVertex(gOrig, rand));
					}
					Collections.shuffle(verticesToRemoveList, rand);
					assertThrows(IllegalArgumentException.class, () -> g.removeVertices(verticesToRemoveList));
				}
				assertEquals(expectedVertices, g.vertices());
				assertEquals(expectedVertices, gOrig.nodes());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class,
					() -> g.removeVertices(List.of(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand))));
		}
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0x78387f223c9b44eaL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.renameVertex(Graphs.randVertex(g, rand), nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void addEdge() {
		final Random rand = new Random(0x8aab5a5aa163bf8L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			for (int i = 0; i < 10; i++) {
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				Integer u = endpoints.nodeU();
				Integer v = endpoints.nodeV();
				Integer e = nonExistingEdge(gOrig, rand);
				int edgesNumBefore = gOrig.edges().size();
				g.addEdge(u, v, e);
				assertEquals(edgesNumBefore + 1, gOrig.edges().size());
				assertTrue(g.edges().contains(e));
				assertEquals(endpoints(u, v, directed), endpoints(g.edgeSource(e), g.edgeTarget(e), directed));
				assertTrue(gOrig.edges().contains(e));
				assertEquals(endpoints(u, v, directed), gOrig.incidentNodes(e));
				assertIndexGraphValid(g);
			}
			assertThrows(IllegalArgumentException.class, () -> {
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				int u = endpoints.nodeU();
				int v = endpoints.nodeV();
				g.addEdge(u, v, Graphs.randEdge(g, rand));
			});
		}
		/* duplicate edge id */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = Graphs.randEdge(g, rand);
			EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
			Integer u = endpoints.nodeU();
			Integer v = endpoints.nodeV();
			assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));
		}
		/* duplicate edge including endpoints */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = Graphs.randEdge(g, rand);
			Integer u = g.edgeSource(e);
			Integer v = g.edgeTarget(e);
			assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));
		}
		/* invalid source */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = nonExistingEdge(gOrig, rand);
			Integer u = nonExistingVertex(gOrig, rand);
			Integer v = Graphs.randVertex(g, rand);
			assertThrows(NoSuchVertexException.class, () -> g.addEdge(u, v, e));
		}
		/* invalid target */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = nonExistingEdge(gOrig, rand);
			Integer u = Graphs.randVertex(g, rand);
			Integer v = nonExistingVertex(gOrig, rand);
			assertThrows(NoSuchVertexException.class, () -> g.addEdge(u, v, e));
		}
		/* invalid self edge */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, false, false);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = nonExistingEdge(gOrig, rand);
			Integer u = Graphs.randVertex(g, rand);
			Integer v = u;
			assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));
		}
		/* invalid parallel edge */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, false, false);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Integer e = nonExistingEdge(gOrig, rand);
			Integer eEndpoints = Graphs.randEdge(g, rand);
			Integer u = g.edgeSource(eEndpoints);
			Integer v = g.edgeTarget(eEndpoints);
			assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));
		}
		/* no edge builder */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertNull(g.edgeBuilder());
			assertThrows(UnsupportedOperationException.class,
					() -> g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand)));
		}
		/* immutable graph */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.addEdge(Graphs.randVertex(g, rand),
					Graphs.randVertex(g, rand), nonExistingEdge(gOrig, rand)));
		}
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0x111310f44f770461L);
		for (boolean directed : BooleanList.of(false, true)) {
			for (int i = 0; i < 10; i++) {
				MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
				Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);

				Graph<Integer, Integer> gToAdd =
						IntGraphFactory.directed().allowSelfEdges().allowParallelEdges().newGraph();
				gToAdd.addVertices(gOrig.nodes());
				for (int s = rand.nextInt(5), j = 0; j < s; j++) {
					EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
					Integer u = endpoints.nodeU();
					Integer v = endpoints.nodeV();
					Integer e;
					do {
						e = nonExistingEdge(gOrig, rand);
					} while (g.edges().contains(e) || gToAdd.edges().contains(e));
					gToAdd.addEdge(u, v, e);
				}
				EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gToAdd);

				int edgesNumBefore = gOrig.edges().size();
				g.addEdges(edgesToAdd);
				assertEquals(edgesNumBefore + edgesToAdd.size(), gOrig.edges().size());
				for (EdgeIter<Integer, Integer> eit = edgesToAdd.iterator(); eit.hasNext();) {
					Integer e = eit.next();
					Integer u = eit.source();
					Integer v = eit.target();
					assertTrue(g.edges().contains(e));
					assertEquals(endpoints(u, v, directed), endpoints(g.edgeSource(e), g.edgeTarget(e), directed));
					assertTrue(gOrig.edges().contains(e));
					assertEquals(endpoints(u, v, directed), gOrig.incidentNodes(e));
				}
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Map<Integer, EndpointPair<Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
			for (Integer e : gOrig.edges())
				expectedEdges.put(e, gOrig.incidentNodes(e));

			for (int i = 0; i < 20; i++) {
				Graph<Integer, Integer> gToAdd =
						IntGraphFactory.directed().allowSelfEdges().allowParallelEdges().newGraph();
				gToAdd.addVertices(gOrig.nodes());
				for (int s = rand.nextInt(5), j = 0; j < s; j++) {
					EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
					Integer u = endpoints.nodeU();
					Integer v = endpoints.nodeV();
					Integer e;
					do {
						e = nonExistingEdge(gOrig, rand);
					} while (g.edges().contains(e) || gToAdd.edges().contains(e));
					gToAdd.addEdge(u, v, e);
				}

				if (rand.nextBoolean() || expectedEdges.isEmpty()) {
					EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gToAdd);
					int edgesNumBefore = gOrig.edges().size();
					g.addEdges(edgesToAdd);
					assertEquals(edgesNumBefore + edgesToAdd.size(), gOrig.edges().size());

					for (EdgeIter<Integer, Integer> eit = edgesToAdd.iterator(); eit.hasNext();) {
						Integer e = eit.next();
						Integer u = eit.source();
						Integer v = eit.target();
						Object oldVal = expectedEdges.put(e, endpoints(u, v, directed));
						assertNull(oldVal);
					}

				} else {
					gToAdd.addEdge(Graphs.randVertex(gToAdd, rand), Graphs.randVertex(gToAdd, rand),
							Graphs.randEdge(g, rand));
					EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gToAdd);
					assertThrows(IllegalArgumentException.class, () -> g.addEdges(edgesToAdd));
				}

				assertEquals(expectedEdges.keySet(), g.edges());
				for (Integer e : expectedEdges.keySet()) {
					EndpointPair<Integer> endpoints = expectedEdges.get(e);
					assertEquals(endpoints, endpoints(g.edgeSource(e), g.edgeTarget(e), directed));
				}
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);

			Graph<Integer, Integer> gToAdd =
					IntGraphFactory.directed().allowSelfEdges().allowParallelEdges().newGraph();
			gToAdd.addVertices(gOrig.nodes());
			gToAdd.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), nonExistingEdge(gOrig, rand));
			gToAdd.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), nonExistingEdge(gOrig, rand));
			EdgeSet<Integer, Integer> edgesToAdd = EdgeSet.allOf(gToAdd);

			assertThrows(UnsupportedOperationException.class, () -> g.addEdges(edgesToAdd));
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x1a261a4f2f5784b2L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			while (gOrig.edges().size() > 0) {
				Integer e = Graphs.randEdge(g, rand);
				int edgesNumBefore = gOrig.edges().size();
				g.removeEdge(e);
				assertEquals(edgesNumBefore - 1, gOrig.edges().size());
				assertFalse(g.edges().contains(e));
				assertFalse(gOrig.edges().contains(e));
				assertIndexGraphValid(g);
			}
			assertThrows(NoSuchEdgeException.class, () -> g.removeEdge(nonExistingEdge(gOrig, rand)));
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.removeEdge(Graphs.randEdge(g, rand)));
		}
	}

	@Test
	public void removeEdges() {
		final Random rand = new Random(0x34c380bce9a2bca7L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			Map<Integer, EndpointPair<Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
			for (Integer e : gOrig.edges())
				expectedEdges.put(e, gOrig.incidentNodes(e));

			while (expectedEdges.size() > 0) {
				Set<Integer> edgesToRemove = new IntOpenHashSet();
				for (int s = rand.nextInt(5), j = 0; j < s; j++)
					edgesToRemove.add(Graphs.randEdge(g, rand));

				if (rand.nextBoolean() || (edgesToRemove.isEmpty() && expectedEdges.isEmpty())) {
					int edgesNumBefore = gOrig.edges().size();
					g.removeEdges(rand.nextBoolean() ? edgesToRemove : new ArrayList<>(edgesToRemove));
					assertEquals(edgesNumBefore - edgesToRemove.size(), gOrig.edges().size());
					for (Integer e : edgesToRemove) {
						assertFalse(g.edges().contains(e));
						assertFalse(gOrig.edges().contains(e));
					}
					for (Integer e : edgesToRemove) {
						Object oldVal = expectedEdges.remove(e);
						assertNotNull(oldVal);
					}

				} else {
					List<Integer> edgesToRemoveList = new ArrayList<>(edgesToRemove);
					if (edgesToRemoveList.size() > 0 && rand.nextBoolean() || expectedEdges.isEmpty()) {
						edgesToRemoveList.add(edgesToRemoveList.get(rand.nextInt(edgesToRemoveList.size())));
					} else {
						edgesToRemoveList.add(nonExistingEdge(gOrig, rand));
					}
					Collections.shuffle(edgesToRemoveList, rand);
					assertThrows(IllegalArgumentException.class, () -> g.removeEdges(edgesToRemoveList));
				}

				assertEquals(expectedEdges.keySet(), g.edges());
				for (Integer e : expectedEdges.keySet()) {
					EndpointPair<Integer> endpoints = expectedEdges.get(e);
					assertEquals(endpoints, endpoints(g.edgeSource(e), g.edgeTarget(e), directed));
				}
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class,
					() -> g.removeEdges(List.of(Graphs.randEdge(g, rand), Graphs.randEdge(g, rand))));
		}
	}

	@Test
	public void removeEdgesOf() {
		final Random rand = new Random(0x4d2b2cab867fbd31L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			while (gOrig.edges().size() > 0) {
				Integer vertex = g.edgeSource(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet();
				expectedRemovedEdges.addAll(g.outEdges(vertex));
				expectedRemovedEdges.addAll(g.inEdges(vertex));
				int edgesNumBefore = gOrig.edges().size();
				g.removeEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edges().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.edges().contains(e));
				}
				assertTrue(g.outEdges(vertex).isEmpty());
				assertTrue(g.inEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.removeEdgesOf(Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void removeOutEdgesOf() {
		final Random rand = new Random(0xf264145055d1a5c0L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			while (gOrig.edges().size() > 0) {
				Integer vertex = g.edgeSource(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet(g.outEdges(vertex));
				int edgesNumBefore = gOrig.edges().size();
				g.removeOutEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edges().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.edges().contains(e));
				}
				assertTrue(g.outEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.removeOutEdgesOf(Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void removeInEdgesOf() {
		final Random rand = new Random(0xac2a0bf3c8dfb7ccL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			while (gOrig.edges().size() > 0) {
				Integer vertex = g.edgeTarget(Graphs.randEdge(g, rand));
				Set<Integer> expectedRemovedEdges = new IntOpenHashSet(g.inEdges(vertex));
				int edgesNumBefore = gOrig.edges().size();
				g.removeInEdgesOf(vertex);
				assertEquals(edgesNumBefore - expectedRemovedEdges.size(), gOrig.edges().size());
				for (Integer e : expectedRemovedEdges) {
					assertFalse(g.edges().contains(e));
					assertFalse(gOrig.edges().contains(e));
				}
				assertTrue(g.inEdges(vertex).isEmpty());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.removeInEdgesOf(Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0xf788f10c47780050L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.renameEdge(Graphs.randEdge(g, rand), nonExistingEdge(gOrig, rand)));
		}
	}

	@Test
	public void moveEdge() {
		final Random rand = new Random(0xd5b504228e62b8e3L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.moveEdge(Graphs.randEdge(g, rand), Graphs.randVertex(g, rand), Graphs.randVertex(g, rand)));
		}
	}

	@Test
	public void clear() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (int i = 0; i < 10; i++) {
				MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
				g.clear();
				assertEquals(Set.of(), g.vertices());
				assertEquals(Set.of(), g.edges());
				assertEquals(Set.of(), gOrig.nodes());
				assertEquals(Set.of(), gOrig.edges());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.clear());
		}
	}

	@Test
	public void clearEdges() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (int i = 0; i < 10; i++) {
				MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
				Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
				Set<Integer> expectedVertices = new IntOpenHashSet(gOrig.nodes());
				g.clearEdges();
				assertEquals(expectedVertices, g.vertices());
				assertEquals(Set.of(), g.edges());
				assertEquals(expectedVertices, gOrig.nodes());
				assertEquals(Set.of(), gOrig.edges());
				assertIndexGraphValid(g);
			}
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig, false);
			assertThrows(UnsupportedOperationException.class, () -> g.clearEdges());
		}
	}

	@Test
	public void weights() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			assertEquals(Set.of(), g.getVerticesWeightsKeys());
			assertEquals(Set.of(), g.getEdgesWeightsKeys());
			assertNull(g.getVerticesWeights("weights"));
			assertNull(g.getEdgesWeights("weights"));
			assertThrows(IllegalArgumentException.class, () -> g.removeVerticesWeights("nonexistingedges"));
			assertThrows(IllegalArgumentException.class, () -> g.removeEdgesWeights("nonexistingedges"));
			assertThrows(UnsupportedOperationException.class, () -> g.addVerticesWeights("weights", double.class));
			assertThrows(UnsupportedOperationException.class, () -> g.addEdgesWeights("weights", double.class));
		}
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			g.ensureVertexCapacity(gOrig.nodes().size() + 10);
			g.ensureEdgeCapacity(gOrig.edges().size() + 10);
		}
	}

	@Test
	public void verticesIndexGraph() {
		final Random rand = new Random(0xf8d0400a5f128c84L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			final int n = gOrig.nodes().size();

			IntSet vertices = g.indexGraph().vertices();
			assertEquals(range(n), vertices);
			for (int i = -15; i < n + 15; i++)
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
		final Random rand = new Random(0x3db5c525cff3d2b7L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			final int m = gOrig.edges().size();

			IntSet edges = g.indexGraph().edges();
			assertEquals(range(m), edges);
			for (int i = -15; i < m + 15; i++)
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edges()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = ig.edgeSource(eIdx);
				Integer u = viMap.indexToId(uIdx);
				assertEquals(gOrig.incidentNodes(e).nodeU(), u);
			}
			assertThrows(NoSuchEdgeException.class, () -> ig.edgeSource(-8));
		}
	}

	@Test
	public void edgeTargetIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edges()) {
				int eIdx = eiMap.idToIndex(e);
				int vIdx = ig.edgeTarget(eIdx);
				Integer v = viMap.indexToId(vIdx);
				assertEquals(gOrig.incidentNodes(e).nodeV(), v);
			}
			assertThrows(NoSuchEdgeException.class, () -> ig.edgeTarget(-8));
		}
	}

	@Test
	public void edgeEndpointIndexGraph() {
		final Random rand = new Random(0x8568cc8e526b7badL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edges()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = viMap.idToIndex(gOrig.incidentNodes(e).nodeU());
				int vIdx = viMap.idToIndex(gOrig.incidentNodes(e).nodeV());
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
		final Random rand = new Random(0xd1eedec2d7cc050bL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, false);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer e : gOrig.edges()) {
				int eIdx = eiMap.idToIndex(e);
				int uIdx = viMap.idToIndex(gOrig.incidentNodes(e).nodeU());
				int vIdx = viMap.idToIndex(gOrig.incidentNodes(e).nodeV());
				assertEquals(eIdx, ig.getEdge(uIdx, vIdx));
			}
			for (int i = 0; i < 10; i++) {
				int uIdx = Graphs.randVertex(ig, rand);
				int vIdx = Graphs.randVertex(ig, rand);
				assertEquals(gOrig.edgeConnectingOrNull(viMap.indexToId(uIdx), viMap.indexToId(vIdx)),
						eiMap.indexToIdIfExist(ig.getEdge(uIdx, vIdx)));
			}
			int existingVertex = ig.vertices().iterator().nextInt();
			int nonExistingVertex = -8;
			assertThrows(NoSuchVertexException.class, () -> ig.getEdge(existingVertex, nonExistingVertex));
			assertThrows(NoSuchVertexException.class, () -> ig.getEdge(nonExistingVertex, existingVertex));
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			for (Integer eEndpoints : gOrig.edges()) {
				int u = viMap.idToIndex(gOrig.incidentNodes(eEndpoints).nodeU());
				int v = viMap.idToIndex(gOrig.incidentNodes(eEndpoints).nodeV());
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.nodes()) {
				for (Integer v : gOrig.nodes()) {
					int uIdx = viMap.idToIndex(u);
					int vIdx = viMap.idToIndex(v);
					IEdgeSet edges = ig.getEdges(uIdx, vIdx);
					assertEquals(gOrig.edgesConnecting(u, v),
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.nodes()) {
				int uIdx = viMap.idToIndex(u);
				IEdgeSet outEdges = ig.outEdges(uIdx);
				assertEquals(gOrig.outEdges(u), outEdges.intStream().mapToObj(eiMap::indexToId).collect(toSet()));

				if (directed) {
					assertEquals(gOrig.outDegree(u), outEdges.size());
				} else {
					int expectedOutDegree = gOrig.outDegree(u);
					for (Integer e : gOrig.outEdges(u))
						if (gOrig.incidentNodes(e).nodeU().equals(gOrig.incidentNodes(e).nodeV()))
							expectedOutDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedOutDegree, outEdges.size());
				}

				for (Integer e : g.edges()) {
					int eIdx = eiMap.idToIndex(e);
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.incidentNodes(e).nodeU().equals(u);
					} else {
						expectedContains =
								gOrig.incidentNodes(e).nodeU().equals(u) || gOrig.incidentNodes(e).nodeV().equals(u);
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			for (Integer u : gOrig.nodes()) {
				int uIdx = viMap.idToIndex(u);
				IEdgeSet inEdges = ig.inEdges(uIdx);
				assertEquals(gOrig.inEdges(u), inEdges.intStream().mapToObj(eiMap::indexToId).collect(toSet()));

				if (directed) {
					assertEquals(gOrig.inDegree(u), inEdges.size());
				} else {
					int expectedInDegree = gOrig.inDegree(u);
					for (Integer e : gOrig.inEdges(u))
						if (gOrig.incidentNodes(e).nodeU().equals(gOrig.incidentNodes(e).nodeV()))
							expectedInDegree--; /* JGraphT counts self edges twice */
					assertEquals(expectedInDegree, inEdges.size());
				}

				for (Integer e : g.edges()) {
					int eIdx = eiMap.idToIndex(e);
					boolean expectedContains;
					if (directed) {
						expectedContains = gOrig.incidentNodes(e).nodeV().equals(u);
					} else {
						expectedContains =
								gOrig.incidentNodes(e).nodeU().equals(u) || gOrig.incidentNodes(e).nodeV().equals(u);
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
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				for (boolean parallelEdges : BooleanList.of(false, true)) {
					MutableNetwork<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addVertex(ig.vertices().size()));
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addVertexInt());
		}
	}

	@Test
	public void addVerticesIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class,
					() -> ig.addVertices(IntList.of(ig.vertices().size(), ig.vertices().size() + 1)));
		}
	}

	@Test
	public void removeVertexIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVertex(0));
		}
	}

	@Test
	public void removeVerticesIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVertices(IntList.of(0, 1, 2)));
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addEdgeIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdge(0, 1, ig.edges().size()));
		}
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdge(0, 1));
		}
	}

	@Test
	public void addEdgesIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdge(0));
		}
	}

	@Test
	public void removeEdgesIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdges(range(5)));
		}
	}

	@Test
	public void removeEdgesOfIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdgesOf(ig.edgeSource(0)));
		}
	}

	@Test
	public void removeOutEdgesOfIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeOutEdgesOf(ig.edgeSource(0)));
		}
	}

	@Test
	public void removeInEdgesOfIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.removeInEdgesOf(ig.edgeTarget(0)));
		}
	}

	@Test
	public void moveEdgeIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.moveEdge(0, 0, 1));
		}
	}

	@Test
	public void clearIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.clear());
		}
	}

	@Test
	public void clearEdgesIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertThrows(UnsupportedOperationException.class, () -> ig.clearEdges());
		}
	}

	@Test
	public void weightsIndexGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			assertEquals(Set.of(), ig.getVerticesWeightsKeys());
			assertEquals(Set.of(), ig.getEdgesWeightsKeys());
			assertNull(ig.getVerticesWeights("weights"));
			assertNull(ig.getEdgesWeights("weights"));
			assertThrows(UnsupportedOperationException.class, () -> ig.removeVerticesWeights("nonexistingedges"));
			assertThrows(UnsupportedOperationException.class, () -> ig.removeEdgesWeights("nonexistingedges"));
			assertThrows(UnsupportedOperationException.class, () -> ig.addVerticesWeights("weights", double.class));
			assertThrows(UnsupportedOperationException.class, () -> ig.addEdgesWeights("weights", double.class));
		}
	}

	@Test
	public void ensureCapacityIndexGraph() {
		/* can't real test anything, just cover and see no exception is thrown */
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
			IndexGraph ig = g.indexGraph();
			ig.ensureVertexCapacity(gOrig.nodes().size() + 10);
			ig.ensureEdgeCapacity(gOrig.edges().size() + 10);
		}
	}

	@Test
	public void indexGraphRemoveListeners() {
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
		final Random rand = new Random(0x68face870c8e859dL);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
			for (int idx = -15; idx < n + 15; idx++) {
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
		final Random rand = new Random(0x867fa9e746ef6206L);
		for (boolean directed : BooleanList.of(false, true)) {
			MutableNetwork<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new GuavaNetworkWrapper<>(gOrig);
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
			for (int idx = -15; idx < m + 15; idx++) {
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
		final Random rand = new Random(0x1ea7abdf1a9d55a5L);
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
		for (int vIdx = 0; vIdx < n; vIdx++) {
			Integer v = viMap.indexToId(vIdx);
			boolean duplicate = !verticesIds.add(v);
			assertFalse(duplicate);
		}
		for (int eIdx = 0; eIdx < m; eIdx++) {
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
		for (int vIdx = -15; vIdx < n + 15; vIdx++) {
			Integer v = viMap.indexToIdIfExist(vIdx);
			if (vIdx < 0 || vIdx >= n) {
				assertEquals(null, v);
			} else {
				assertEquals(vIdx, viMap.idToIndex(v));
			}
		}
		for (int eIdx = -15; eIdx < m + 15; eIdx++) {
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

	private static EndpointPair<Integer> endpoints(Integer u, Integer v, boolean directed) {
		return directed ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	private static MutableNetwork<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true, true);
	}

	private static MutableNetwork<Integer, Integer> createGraph(boolean directed, boolean selfEdges,
			boolean parallelEdges) {
		final Random rand = new Random(0x9de2f077d5ce9dddL);
		NetworkBuilder<Object, Object> builder = directed ? NetworkBuilder.directed() : NetworkBuilder.undirected();
		MutableNetwork<Integer, Integer> g =
				builder.allowsSelfLoops(selfEdges).allowsParallelEdges(parallelEdges).build();

		for (Integer v : range(50 + rand.nextInt(50)))
			g.addNode(v);
		Integer[] vertices = g.nodes().toArray(Integer[]::new);
		for (int m = 300 + rand.nextInt(100); g.edges().size() < m;) {
			EndpointPair<Integer> endpoints = validEndpointsToAdd(g, vertices, rand);
			int u = endpoints.nodeU();
			int v = endpoints.nodeV();
			int e = rand.nextInt(1 + g.edges().size() * 2);
			if (g.edges().contains(e))
				continue;
			g.addEdge(u, v, e);
		}
		return g;
	}

	private static EndpointPair<Integer> validEndpointsToAdd(Network<Integer, Integer> g, Random rand) {
		return validEndpointsToAdd(g, g.nodes().toArray(Integer[]::new), rand);
	}

	private static EndpointPair<Integer> validEndpointsToAdd(Network<Integer, Integer> g, Integer[] vertices,
			Random rand) {
		for (;;) {
			int u = vertices[rand.nextInt(vertices.length)];
			int v = vertices[rand.nextInt(vertices.length)];
			if (!g.allowsSelfLoops() && u == v)
				continue;
			if (!g.allowsParallelEdges() && g.hasEdgeConnecting(u, v))
				continue;
			int e = rand.nextInt(1 + g.edges().size() * 2);
			if (g.edges().contains(e))
				continue;
			return endpoints(u, v, g.isDirected());
		}
	}

	private static Integer nonExistingVertex(Network<Integer, Integer> g, Random rand) {
		for (;;) {
			int v = rand.nextInt();
			if (v >= 0 && !g.nodes().contains(v))
				return v;
		}
	}

	private static Integer nonExistingEdge(Network<Integer, Integer> g, Random rand) {
		for (;;) {
			int e = rand.nextInt();
			if (e >= 0 && !g.edges().contains(e))
				return e;
		}
	}

}
