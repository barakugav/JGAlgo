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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

@SuppressWarnings("boxing")
public class GuavaNetworkAdapterTest {

	@Test
	public void nodes() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);
			assertEquals(gOrig.vertices(), g.nodes());
		}
	}

	@Test
	public void edges() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);
			assertEquals(gOrig.edges(), g.edges());
		}
	}

	@Test
	public void incidentNodes() {
		final Random rand = new Random(0xe2715882c3c98be1L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);
			for (Integer e : g.edges()) {
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				EndpointPair<Integer> expected = endpoints(u, v, directed);
				assertEquals(expected, g.incidentNodes(e));
			}
			assertThrows(IllegalArgumentException.class, () -> g.incidentNodes(nonExistingEdge(gOrig, rand)));
		}
	}

	@Test
	public void successors() {
		final Random rand = new Random(0x7727d19a01718ddL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

			for (Integer u : g.nodes()) {
				Set<Integer> actual = g.successors(u);
				Set<Integer> expected = gOrig.outEdges(u).stream().map(e -> gOrig.edgeEndpoint(e, u)).collect(toSet());
				assertEquals(expected, actual);
				assertEquals(actual, expected);

				for (int i = 0; i < 20; i++) {
					Integer v = Graphs.randVertex(gOrig, rand);
					assertEquals(expected.contains(v), actual.contains(v));
				}
				for (int i = 0; i < 20; i++) {
					Integer v = Integer.valueOf(rand.nextInt());
					assertEquals(expected.contains(v), actual.contains(v));
				}
			}
			assertThrows(IllegalArgumentException.class, () -> g.successors(nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void predecessors() {
		final Random rand = new Random(0x7d5adfa3e4cff2feL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

			for (Integer u : g.nodes()) {
				Set<Integer> actual = g.predecessors(u);
				Set<Integer> expected = gOrig.inEdges(u).stream().map(e -> gOrig.edgeEndpoint(e, u)).collect(toSet());
				assertEquals(expected, actual);
				assertEquals(actual, expected);

				for (int i = 0; i < 20; i++) {
					Integer v = Graphs.randVertex(gOrig, rand);
					assertEquals(expected.contains(v), actual.contains(v));
				}
				for (int i = 0; i < 20; i++) {
					Integer v = Integer.valueOf(rand.nextInt());
					assertEquals(expected.contains(v), actual.contains(v));
				}
			}
			assertThrows(IllegalArgumentException.class, () -> g.predecessors(nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void adjacentNodes() {
		final Random rand = new Random(0x7d5adfa3e4cff2feL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

			for (Integer u : g.nodes()) {
				Set<Integer> actual = g.adjacentNodes(u);
				Set<Integer> expected = Stream
						.concat(gOrig.outEdges(u).stream(), gOrig.inEdges(u).stream())
						.map(e -> gOrig.edgeEndpoint(e, u))
						.collect(toSet());
				assertEquals(expected, actual);
				assertEquals(actual, expected);

				for (int i = 0; i < 20; i++) {
					Integer v = Graphs.randVertex(gOrig, rand);
					assertEquals(expected.contains(v), actual.contains(v));
				}
				for (int i = 0; i < 20; i++) {
					Integer v = Integer.valueOf(rand.nextInt());
					assertEquals(expected.contains(v), actual.contains(v));
				}
			}
			assertThrows(IllegalArgumentException.class, () -> g.adjacentNodes(nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void adjacentEdges() {
		final Random rand = new Random(0xe5f4970282966bfdL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

			for (Integer e : g.edges()) {
				Set<Integer> actual = g.adjacentEdges(e);
				Set<Integer> expected = new IntOpenHashSet();
				expected.addAll(gOrig.outEdges(gOrig.edgeSource(e)));
				expected.addAll(gOrig.inEdges(gOrig.edgeSource(e)));
				expected.addAll(gOrig.outEdges(gOrig.edgeTarget(e)));
				expected.addAll(gOrig.inEdges(gOrig.edgeTarget(e)));
				expected.remove(e);
				assertEquals(expected, actual);
				assertEquals(actual, expected);

				for (int i = 0; i < 20; i++) {
					Integer e1 = Graphs.randEdge(gOrig, rand);
					assertEquals(expected.contains(e1), actual.contains(e1));
				}
				for (int i = 0; i < 20; i++) {
					Integer e1 = Integer.valueOf(rand.nextInt());
					assertEquals(expected.contains(e1), actual.contains(e1));
				}
			}
			assertThrows(IllegalArgumentException.class, () -> g.adjacentEdges(nonExistingVertex(gOrig, rand)));
		}
	}

	@Test
	public void hasEdgeConnecting() {
		final Random rand = new Random(0x7d5adfa3e4cff2feL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

			for (Integer e : gOrig.edges()) {
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (!directed && rand.nextBoolean()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				assertTrue(g.hasEdgeConnecting(u, v));
				EndpointPair<Integer> endpoints = endpoints(u, v, directed);
				assertTrue(g.hasEdgeConnecting(endpoints));

				Integer e1 = g.edgeConnectingOrNull(u, v);
				assertEquals(endpoints, g.incidentNodes(e1));
			}
			for (int i = 0; i < 20; i++) {
				Integer u = Graphs.randVertex(gOrig, rand);
				Integer v = Graphs.randVertex(gOrig, rand);
				assertEquals(gOrig.containsEdge(u, v), g.hasEdgeConnecting(u, v));
				assertEquals(gOrig.containsEdge(u, v), g.hasEdgeConnecting(endpoints(u, v, directed)));
			}
			Integer existingVertex = Graphs.randVertex(gOrig, rand);
			Integer nonExistingVertex = nonExistingVertex(gOrig, rand);
			assertFalse(g.hasEdgeConnecting(nonExistingVertex, existingVertex));
			assertFalse(g.hasEdgeConnecting(existingVertex, nonExistingVertex));
			assertFalse(g.hasEdgeConnecting(endpoints(nonExistingVertex, existingVertex, directed)));
			assertFalse(g.hasEdgeConnecting(endpoints(existingVertex, nonExistingVertex, directed)));

			if (directed) {
				Integer e = Graphs.randEdge(gOrig, rand);
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				assertFalse(g.hasEdgeConnecting(endpoints(u, v, !directed)));
			}
		}
	}

	@Test
	public void incidentEdges() {
		final Random rand = new Random(0x4ba6b5de1d87b179L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

					for (Integer u : g.nodes()) {
						Set<Integer> actual = g.incidentEdges(u);
						Set<Integer> expected =
								Stream.concat(gOrig.outEdges(u).stream(), gOrig.inEdges(u).stream()).collect(toSet());
						assertEquals(expected.size(), actual.size());
						assertEquals(expected.isEmpty(), actual.isEmpty());
						assertEquals(expected, actual);
						assertEquals(actual, expected);

						for (int i = 0; i < 20; i++) {
							Integer e = Graphs.randEdge(gOrig, rand);
							assertEquals(expected.contains(e), actual.contains(e));
						}
						for (int i = 0; i < 20; i++) {
							Integer e = nonExistingEdge(gOrig, rand);
							assertEquals(expected.contains(e), actual.contains(e));
						}
						try {
							assertFalse(actual.contains((Object) "wrong type"));
						} catch (ClassCastException e) {
							/* also fine */
						}
					}
					assertThrows(IllegalArgumentException.class, () -> g.incidentEdges(nonExistingVertex(gOrig, rand)));
				}
			}
		}
	}

	@Test
	public void degree() {
		final Random rand = new Random(0x65e5ac02fd8c2c2bL);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

					for (Integer v : g.nodes()) {
						int expected;
						if (gOrig.isDirected()) {
							expected = gOrig.outEdges(v).size() + gOrig.inEdges(v).size();
						} else {
							expected = expectedDegreeUndirected(gOrig, v);
						}
						assertEquals(expected, g.degree(v));
					}
					assertThrows(IllegalArgumentException.class, () -> g.degree(nonExistingVertex(gOrig, rand)));
				}
			}
		}
	}

	@Test
	public void outDegree() {
		final Random rand = new Random(0x8f32ab1776e1546L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

					for (Integer v : g.nodes()) {
						int expected;
						if (gOrig.isDirected()) {
							expected = gOrig.outEdges(v).size();
						} else {
							expected = expectedDegreeUndirected(gOrig, v);
						}
						assertEquals(expected, g.outDegree(v));
					}
					assertThrows(IllegalArgumentException.class, () -> g.outDegree(nonExistingVertex(gOrig, rand)));
				}
			}
		}
	}

	@Test
	public void inDegree() {
		final Random rand = new Random(0x303e9d43e4c1d785L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

					for (Integer v : g.nodes()) {
						int expected;
						if (gOrig.isDirected()) {
							expected = gOrig.inEdges(v).size();
						} else {
							expected = expectedDegreeUndirected(gOrig, v);
						}
						assertEquals(expected, g.inDegree(v));
					}
					assertThrows(IllegalArgumentException.class, () -> g.inDegree(nonExistingVertex(gOrig, rand)));
				}
			}
		}
	}

	private static int expectedDegreeUndirected(com.jgalgo.graph.Graph<Integer, Integer> g, Integer v) {
		int degree = g.outEdges(v).size();
		for (Integer e : g.outEdges(v))
			if (g.edgeSource(e).equals(g.edgeTarget(e)))
				degree++; /* self edges are counted twice in Guava */
		return degree;
	}

	@Test
	public void capabilities() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, parallelEdges);
					Network<Integer, Integer> g = new GuavaNetworkAdapter<>(gOrig);

					assertEquals(gOrig.isDirected(), g.isDirected());
					assertEquals(gOrig.isAllowSelfEdges(), g.allowsSelfLoops());
					assertEquals(gOrig.isAllowParallelEdges(), g.allowsParallelEdges());
					assertEquals(ElementOrder.Type.UNORDERED, g.nodeOrder().type());
					assertEquals(ElementOrder.Type.UNORDERED, g.edgeOrder().type());
				}
			}
		}
	}

	@Test
	public void addNode() {
		final Random rand = new Random(0x68360559fa787b0L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
			MutableNetwork<Integer, Integer> g = new GuavaMutableNetworkAdapter<>(gOrig);

			Set<Integer> expectedVertices = new IntOpenHashSet();
			for (int i = 0; i < 100; i++) {
				Integer v = Integer.valueOf(rand.nextInt(gOrig.vertices().size() * 2 + 1));
				if (gOrig.vertices().contains(v)) {
					int verticesNumBefore = g.nodes().size();
					assertFalse(g.addNode(v));
					assertEquals(verticesNumBefore, g.nodes().size());
				} else {
					int verticesNumBefore = g.nodes().size();
					assertTrue(g.addNode(v));
					assertEquals(verticesNumBefore + 1, g.nodes().size());
					assertTrue(g.nodes().contains(v));
					assertTrue(gOrig.vertices().contains(v));
					expectedVertices.add(v);
				}
			}
			assertEquals(expectedVertices, gOrig.vertices());
		}
	}

	@Test
	public void removeNode() {
		final Random rand = new Random(0x356ed8856308022bL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			MutableNetwork<Integer, Integer> g = new GuavaMutableNetworkAdapter<>(gOrig);

			Integer[] originalVertices = gOrig.vertices().toArray(Integer[]::new);
			Set<Integer> expectedVertices = new IntOpenHashSet(gOrig.vertices());
			for (int i = 0; i < 100; i++) {
				Integer v = rand.nextBoolean() ? Graphs.randVertex(gOrig, rand)
						: originalVertices[rand.nextInt(originalVertices.length)];
				if (expectedVertices.contains(v)) {
					Set<Integer> expectedRemoveEdges = gOrig
							.edges()
							.stream()
							.filter(e -> v.equals(gOrig.edgeSource(e)) || v.equals(gOrig.edgeTarget(e)))
							.collect(toSet());
					Set<EndpointPair<Integer>> expectedRemoveEndpoints = expectedRemoveEdges
							.stream()
							.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed))
							.collect(toSet());
					int verticesNumBefore = g.nodes().size();
					int edgesNumBefore = g.edges().size();
					assertTrue(g.removeNode(v));
					assertEquals(verticesNumBefore - 1, g.nodes().size());
					assertEquals(edgesNumBefore - expectedRemoveEdges.size(), g.edges().size());
					assertFalse(g.nodes().contains(v));
					assertFalse(gOrig.vertices().contains(v));
					for (EndpointPair<Integer> e : expectedRemoveEndpoints)
						assertFalse(g.hasEdgeConnecting(e));
					for (Integer e : expectedRemoveEdges) {
						assertFalse(g.edges().contains(e));
						assertFalse(gOrig.edges().contains(e));
					}
					expectedVertices.remove(v);
				} else {
					int verticesNumBefore = g.nodes().size();
					assertFalse(g.removeNode(v));
					assertEquals(verticesNumBefore, g.nodes().size());
				}
			}
			assertEquals(expectedVertices, g.nodes());
			assertEquals(expectedVertices, gOrig.vertices());
		}
	}

	@Test
	public void putEdge() {
		final Random rand = new Random(0xeef076eb5b67e4ecL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
			gOrig.addVertices(range(20));
			MutableNetwork<Integer, Integer> g = new GuavaMutableNetworkAdapter<>(gOrig);

			/* put edges valid */
			Map<Integer, EndpointPair<Integer>> expectedEdges = new Object2ObjectOpenHashMap<>();
			for (int i = 0; i < 30; i++) {
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				Integer e = nonExistingEdge(gOrig, rand);
				boolean modified = g.addEdge(endpoints, e);
				assertTrue(modified);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				Object oldVal = expectedEdges.put(e, endpoints);
				assertNull(oldVal);
			}
			assertEquals(expectedEdges.keySet(), g.edges());
			for (Integer e : expectedEdges.keySet()) {
				EndpointPair<Integer> endpoints = expectedEdges.get(e);
				assertEquals(endpoints, g.incidentNodes(e));
			}

			/* re-put edge, should not modify */
			for (int i = 0; i < 10; i++) {
				Integer e = Graphs.randEdge(gOrig, rand);
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (!directed && rand.nextBoolean()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				EndpointPair<Integer> endpoints = endpoints(u, v, directed);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				boolean modified = g.addEdge(endpoints, e);
				assertFalse(modified);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore, g.edges().size());
			}

			/* re-put edge, different endpoints */
			for (int i = 0; i < 10; i++) {
				Integer e = Graphs.randEdge(gOrig, rand);
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (!directed && rand.nextBoolean()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				EndpointPair<Integer> oldEndpoints = endpoints(u, v, directed);
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				if (endpoints.equals(oldEndpoints))
					continue;
				assertThrows(IllegalArgumentException.class, () -> g.addEdge(endpoints, e));
			}
		}

		/* on missing endpoint, node should be added silently */
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
			MutableNetwork<Integer, Integer> g = new GuavaMutableNetworkAdapter<>(gOrig);
			Map<Integer, EndpointPair<Integer>> expectedEdges = new Object2ObjectOpenHashMap<>();
			for (int i = 0; i < 10; i++) {
				EndpointPair<Integer> endpoints = endpoints(i * 2 + 0, i * 2 + 1, directed);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				Integer e = nonExistingEdge(gOrig, rand);
				boolean modified = g.addEdge(endpoints, e);
				assertTrue(modified);
				assertEquals(verticesNumBefore + 2, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				Object oldVal = expectedEdges.put(e, endpoints);
				assertNull(oldVal);
			}
			assertEquals(expectedEdges.keySet(), g.edges());
			for (Integer e : expectedEdges.keySet()) {
				EndpointPair<Integer> endpoints = expectedEdges.get(e);
				assertEquals(endpoints, g.incidentNodes(e));
			}
			assertEquals(range(20), g.nodes());
			assertEquals(range(20), gOrig.vertices());
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x17ef03cedc99e735L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			MutableNetwork<Integer, Integer> g = new GuavaMutableNetworkAdapter<>(gOrig);

			List<Pair<Integer, EndpointPair<Integer>>> originalEdges =
					g.edges().stream().map(e -> Pair.of(e, g.incidentNodes(e))).collect(toList());
			Map<Integer, EndpointPair<Integer>> expectedEdges = new Object2ObjectOpenHashMap<>();
			for (Pair<Integer, EndpointPair<Integer>> p : originalEdges)
				expectedEdges.put(p.left(), p.right());
			for (int i = 0; i < 100; i++) {
				Integer edgeToRemove;
				if (rand.nextBoolean()) {
					edgeToRemove = Graphs.randEdge(gOrig, rand);
				} else {
					edgeToRemove = originalEdges.get(rand.nextInt(originalEdges.size())).first();
				}
				if (expectedEdges.containsKey(edgeToRemove)) {
					int verticesNumBefore = g.nodes().size();
					int edgesNumBefore = g.edges().size();
					assertTrue(g.removeEdge(edgeToRemove));
					assertEquals(verticesNumBefore, g.nodes().size());
					assertEquals(edgesNumBefore - 1, g.edges().size());
					assertFalse(g.edges().contains(edgeToRemove));
					assertFalse(gOrig.edges().contains(edgeToRemove));
					expectedEdges.remove(edgeToRemove);
				} else {
					int edgesNumBefore = g.edges().size();
					assertFalse(g.removeEdge(edgeToRemove));
					assertEquals(edgesNumBefore, g.edges().size());
				}
			}
			assertEquals(expectedEdges.keySet(), g.edges());
			for (Integer e : expectedEdges.keySet()) {
				EndpointPair<Integer> endpoints = expectedEdges.get(e);
				assertEquals(endpoints, g.incidentNodes(e));
			}
		}
	}

	private static EndpointPair<Integer> endpoints(Integer u, Integer v, boolean directed) {
		return directed ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true, true);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges,
			boolean parallelEdges) {
		final Random rand = new Random(0x2bf4b83f64d13c33L);
		GraphFactory<Integer, Integer> factory = IntGraphFactory.newInstance(directed);
		com.jgalgo.graph.Graph<Integer, Integer> g =
				factory.allowSelfEdges(selfEdges).allowParallelEdges(parallelEdges).newGraph();
		g.addVertices(range(50 + rand.nextInt(50)));
		for (int m = 300 + rand.nextInt(100); g.edges().size() < m;) {
			EndpointPair<Integer> endpoints = validEndpointsToAdd(g, rand);
			int u = endpoints.nodeU();
			int v = endpoints.nodeV();
			int e = rand.nextInt(1 + g.edges().size() * 2);
			if (g.edges().contains(e))
				continue;
			g.addEdge(u, v, e);
		}
		return g;
	}

	private static EndpointPair<Integer> validEndpointsToAdd(com.jgalgo.graph.Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			int u = Graphs.randVertex(g, rand);
			int v = Graphs.randVertex(g, rand);
			if (!g.isAllowSelfEdges() && u == v)
				continue;
			if (!g.isAllowParallelEdges() && g.containsEdge(u, v))
				continue;
			int e = rand.nextInt(1 + g.edges().size() * 2);
			if (g.edges().contains(e))
				continue;
			return endpoints(u, v, g.isDirected());
		}
	}

	private static Integer nonExistingVertex(com.jgalgo.graph.Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			Integer v = Integer.valueOf(rand.nextInt());
			if (v.intValue() >= 0 && !g.vertices().contains(v))
				return v;
		}
	}

	private static Integer nonExistingEdge(com.jgalgo.graph.Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			Integer e = Integer.valueOf(rand.nextInt());
			if (e.intValue() >= 0 && !g.edges().contains(e))
				return e;
		}
	}

}
