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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@SuppressWarnings("boxing")
public class GuavaValueGraphAdapterTest {

	@Test
	public void adapterOfGraphWithParallelEdges() {
		com.jgalgo.graph.Graph<Integer, Integer> gOrig =
				IntGraphFactory.directed().allowParallelEdges().allowSelfEdges().newGraph();
		assertThrows(UnsupportedOperationException.class, () -> new GuavaValueGraphAdapter<>(gOrig, "weights"));
	}

	@Test
	public void adapterWithoutWeights() {
		com.jgalgo.graph.Graph<Integer, Integer> gOrig =
				IntGraphFactory.directed().allowSelfEdges(false).allowParallelEdges(false).newGraph();
		assertThrows(IllegalArgumentException.class, () -> new GuavaValueGraphAdapter<>(gOrig, "no-such-weights"));
	}

	@Test
	public void nodes() {
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");
			assertEquals(gOrig.vertices(), g.nodes());
		}
	}

	@Test
	public void edges() {
		final Random rand = new Random(0xa3c60794d5723a88L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

			assertEquals(gOrig.edges().size(), g.edges().size());

			Set<Integer> iteratedEdges = new IntOpenHashSet();
			for (EndpointPair<Integer> e : g.edges()) {
				assertEquals(directed, e.isOrdered());
				Integer u = e.nodeU(), v = e.nodeV();
				Integer eOrig = gOrig.getEdge(u, v);
				assertNotNull(eOrig);
				boolean dup = !iteratedEdges.add(eOrig);
				assertFalse(dup);
			}
			assertEquals(gOrig.edges(), iteratedEdges);

			for (int e : gOrig.edges()) {
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (!directed && rand.nextBoolean()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				EndpointPair<Integer> endpoints = endpoints(u, v, directed);
				assertTrue(g.edges().contains(endpoints));
			}
			for (int i = 0; i < 20; i++) {
				Integer u = Graphs.randVertex(gOrig, rand);
				Integer v = Graphs.randVertex(gOrig, rand);
				EndpointPair<Integer> endpoints = endpoints(u, v, directed);
				assertEquals(gOrig.containsEdge(u, v), g.edges().contains(endpoints));
			}
			assertFalse(g.edges().contains((Object) "wrong type"));
		}
	}

	@Test
	public void successors() {
		final Random rand = new Random(0x61f3311581791dd4L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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
		final Random rand = new Random(0x9f4ad8f981413b36L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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
		final Random rand = new Random(0x17e3589767979baaL);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

			for (Integer u : g.nodes()) {
				Set<Integer> actual = g.adjacentNodes(u);
				Set<Integer> expected = Stream.concat(gOrig.outEdges(u).stream(), gOrig.inEdges(u).stream())
						.map(e -> gOrig.edgeEndpoint(e, u)).collect(toSet());
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
	public void hasEdgeConnecting() {
		final Random rand = new Random(0x887781f49e315a7cL);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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
		final Random rand = new Random(0x488d87682f6c9639L);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

				for (Integer u : g.nodes()) {
					Set<EndpointPair<Integer>> actual = g.incidentEdges(u);
					Set<EndpointPair<Integer>> expected = Stream
							.concat(gOrig.outEdges(u).stream(), gOrig.inEdges(u).stream())
							.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed)).collect(toSet());
					assertEquals(expected.size(), actual.size());
					assertEquals(expected.isEmpty(), actual.isEmpty());
					assertEquals(expected, actual);
					assertEquals(actual, expected);

					for (int i = 0; i < 20; i++) {
						Integer v = Graphs.randVertex(gOrig, rand);
						Integer w = Graphs.randVertex(gOrig, rand);
						EndpointPair<Integer> endpoints = endpoints(v, w, directed);
						assertEquals(expected.contains(endpoints), actual.contains(endpoints));
					}
					for (int i = 0; i < 20; i++) {
						Integer v = Integer.valueOf(rand.nextInt());
						Integer w = Graphs.randVertex(gOrig, rand);
						if (rand.nextBoolean()) {
							Integer tmp = v;
							v = w;
							w = tmp;
						}
						EndpointPair<Integer> endpoints = endpoints(v, w, directed);
						assertEquals(expected.contains(endpoints), actual.contains(endpoints));
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

	@Test
	public void degree() {
		final Random rand = new Random(0x82020fb5caf888a3L);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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

	@Test
	public void outDegree() {
		final Random rand = new Random(0x31d935db775158c4L);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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

	@Test
	public void inDegree() {
		final Random rand = new Random(0xebf6dd79be54fc7eL);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

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

	private static int expectedDegreeUndirected(com.jgalgo.graph.Graph<Integer, Integer> g, Integer v) {
		int degree = g.outEdges(v).size();
		for (Integer e : g.outEdges(v))
			if (g.edgeSource(e).equals(g.edgeTarget(e)))
				degree++; /* self edges are counted twice in Guava */
		return degree;
	}

	@Test
	public void edgeValueOrDefault() {
		final Random rand = new Random(0x78520be26e536bc6L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			WeightsDouble<Integer> weights = gOrig.getEdgesWeights("weights");
			ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

			for (EndpointPair<Integer> e : g.edges())
				assertEquals(weights.get(gOrig.getEdge(e.nodeU(), e.nodeV())),
						g.edgeValueOrDefault(e, Double.POSITIVE_INFINITY));
			for (int i = 0; i < 5; i++) {
				Integer u = Graphs.randVertex(gOrig, rand);
				Integer v = Graphs.randVertex(gOrig, rand);
				if (gOrig.containsEdge(u, v))
					continue;
				assertEquals(Double.POSITIVE_INFINITY, g.edgeValueOrDefault(u, v, Double.POSITIVE_INFINITY));
				assertEquals(Double.NaN, g.edgeValueOrDefault(u, v, Double.NaN));
			}
		}
	}

	@Test
	public void capabilities() {
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean selfEdges : BooleanList.of(false, true)) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				ValueGraph<Integer, Double> g = new GuavaValueGraphAdapter<>(gOrig, "weights");

				assertEquals(gOrig.isDirected(), g.isDirected());
				assertEquals(gOrig.isAllowSelfEdges(), g.allowsSelfLoops());
				assertEquals(ElementOrder.Type.UNORDERED, g.nodeOrder().type());
				assertEquals(ElementOrder.Type.UNORDERED, g.incidentEdgeOrder().type());
			}
		}
	}

	@Test
	public void asGraph() {
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			ValueGraph<Integer, Double> g0 = new GuavaValueGraphAdapter<>(gOrig, "weights");
			Graph<Integer> g = g0.asGraph();

			assertEquals(gOrig.isDirected(), g.isDirected());
			assertEquals(gOrig.isAllowSelfEdges(), g.allowsSelfLoops());
			assertEquals(ElementOrder.Type.UNORDERED, g.nodeOrder().type());
			assertEquals(ElementOrder.Type.UNORDERED, g.incidentEdgeOrder().type());

			assertEquals(g0.nodes(), g.nodes());
			assertEquals(g0.edges(), g.edges());
			for (Integer v : g0.nodes()) {
				assertEquals(g0.successors(v), g.successors(v));
				assertEquals(g0.predecessors(v), g.predecessors(v));
				assertEquals(g0.adjacentNodes(v), g.adjacentNodes(v));
				assertEquals(g0.incidentEdges(v), g.incidentEdges(v));
				assertEquals(g0.degree(v), g.degree(v));
				assertEquals(g0.outDegree(v), g.outDegree(v));
				assertEquals(g0.inDegree(v), g.inDegree(v));
			}
		}
	}

	@Test
	public void mutableAdapterWithoutEdgeBuilder() {
		GraphFactory<Integer, Integer> factory = GraphFactory.directed();
		factory.allowSelfEdges(false).allowParallelEdges(false);
		com.jgalgo.graph.Graph<Integer, Integer> gOrig = factory.newGraph();
		gOrig.addEdgesWeights("weights", double.class);
		assertThrows(IllegalArgumentException.class, () -> new GuavaMutableValueGraphAdapter<>(gOrig, "weights"));
	}

	@Test
	public void addNode() {
		final Random rand = new Random(0x9c05ec24986155b3L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			gOrig.addEdgesWeights("weights", double.class);
			MutableValueGraph<Integer, Double> g = new GuavaMutableValueGraphAdapter<>(gOrig, "weights");

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
		final Random rand = new Random(0x1ab24976d9d62627L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			MutableValueGraph<Integer, Double> g = new GuavaMutableValueGraphAdapter<>(gOrig, "weights");

			Integer[] originalVertices = gOrig.vertices().toArray(Integer[]::new);
			Set<Integer> expectedVertices = new IntOpenHashSet(gOrig.vertices());
			for (int i = 0; expectedVertices.size() > 0 && i < 100; i++) {
				Integer v = rand.nextBoolean() ? Graphs.randVertex(gOrig, rand)
						: originalVertices[rand.nextInt(originalVertices.length)];
				if (expectedVertices.contains(v)) {
					Set<Integer> expectedRemoveEdges = gOrig.edges().stream()
							.filter(e -> v.equals(gOrig.edgeSource(e)) || v.equals(gOrig.edgeTarget(e)))
							.collect(toSet());
					Set<EndpointPair<Integer>> expectedRemoveEndpoints = expectedRemoveEdges.stream()
							.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed)).collect(toSet());
					int verticesNumBefore = g.nodes().size();
					int edgesNumBefore = g.edges().size();
					assertTrue(g.removeNode(v));
					assertEquals(verticesNumBefore - 1, g.nodes().size());
					assertEquals(edgesNumBefore - expectedRemoveEndpoints.size(), g.edges().size());
					assertFalse(g.nodes().contains(v));
					assertFalse(gOrig.vertices().contains(v));
					for (EndpointPair<Integer> e : expectedRemoveEndpoints) {
						assertFalse(g.hasEdgeConnecting(e));
						assertFalse(g.edges().contains(e));
					}
					for (Integer e : expectedRemoveEdges)
						assertFalse(gOrig.edges().contains(e));
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
		final Random rand = new Random(0xbfd3de3d04588814L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			gOrig.addVertices(range(20));
			WeightsDouble<Integer> weights = gOrig.addEdgesWeights("weights", double.class);
			MutableValueGraph<Integer, Double> g = new GuavaMutableValueGraphAdapter<>(gOrig, "weights");

			/* put edges valid */
			Map<EndpointPair<Integer>, Double> expectedEdges = new Object2DoubleOpenHashMap<>();
			for (int i = 0; i < 30; i++) {
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				double val = rand.nextInt(100);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				Object oldVal = g.putEdgeValue(endpoints, val);
				assertNull(oldVal);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				expectedEdges.put(endpoints, val);
			}
			assertEquals(expectedEdges.keySet(), g.edges());
			for (EndpointPair<Integer> e : g.edges()) {
				assertEquals(expectedEdges.get(e), g.edgeValueOrDefault(e, null));
				assertEquals(expectedEdges.get(e), weights.get(gOrig.getEdge(e.nodeU(), e.nodeV())));
			}

			/* put parallel edge, should not modify */
			for (int i = 0; i < 10; i++) {
				Integer e = Graphs.randEdge(gOrig, rand);
				Integer u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (!directed && rand.nextBoolean()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				EndpointPair<Integer> endpoints = endpoints(u, v, directed);
				double val = rand.nextInt(100);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				Object oldVal = g.putEdgeValue(endpoints, val);
				assertNotNull(oldVal);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore, g.edges().size());
			}
		}

		/* on missing endpoint, node should be added silently */
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			WeightsDouble<Integer> weights = gOrig.addEdgesWeights("weights", double.class);
			MutableValueGraph<Integer, Double> g = new GuavaMutableValueGraphAdapter<>(gOrig, "weights");
			Map<EndpointPair<Integer>, Double> expectedEdges = new Object2DoubleOpenHashMap<>();
			for (int i = 0; i < 10; i++) {
				EndpointPair<Integer> endpoints = endpoints(i * 2 + 0, i * 2 + 1, directed);
				double val = rand.nextInt(100);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				Object oldVal = g.putEdgeValue(endpoints, val);
				assertNull(oldVal);
				assertEquals(verticesNumBefore + 2, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				expectedEdges.put(endpoints, val);
			}
			assertEquals(expectedEdges.keySet(), g.edges());
			for (EndpointPair<Integer> e : g.edges()) {
				assertEquals(expectedEdges.get(e), g.edgeValueOrDefault(e, null));
				assertEquals(expectedEdges.get(e), weights.get(gOrig.getEdge(e.nodeU(), e.nodeV())));
			}
			assertEquals(range(20), g.nodes());
			assertEquals(range(20), gOrig.vertices());
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x50ed947de5c08a58L);
		for (boolean directed : BooleanList.of(false, true)) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			MutableValueGraph<Integer, Double> g = new GuavaMutableValueGraphAdapter<>(gOrig, "weights");

			List<EndpointPair<Integer>> originalEdges = new ArrayList<>(g.edges());
			Set<EndpointPair<Integer>> expectedEdges = new ObjectOpenHashSet<>(g.edges());
			for (int i = 0; i < 100; i++) {
				EndpointPair<Integer> endpoints;
				if (rand.nextBoolean()) {
					Integer e = Graphs.randEdge(gOrig, rand);
					endpoints = endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed);
				} else {
					endpoints = originalEdges.get(rand.nextInt(originalEdges.size()));
				}
				if (expectedEdges.contains(endpoints)) {
					int verticesNumBefore = g.nodes().size();
					int edgesNumBefore = g.edges().size();
					assertNotNull(g.removeEdge(endpoints));
					assertEquals(verticesNumBefore, g.nodes().size());
					assertEquals(edgesNumBefore - 1, g.edges().size());
					assertFalse(g.edges().contains(endpoints));
					assertFalse(gOrig.containsEdge(endpoints.nodeU(), endpoints.nodeV()));
					expectedEdges.remove(endpoints);
				} else {
					int edgesNumBefore = g.edges().size();
					assertNull(g.removeEdge(endpoints));
					assertEquals(edgesNumBefore, g.edges().size());
				}
			}
			assertEquals(expectedEdges, g.edges());
			assertEquals(expectedEdges, gOrig.edges().stream()
					.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed)).collect(toSet()));
		}
	}

	private static EndpointPair<Integer> endpoints(Integer u, Integer v, boolean directed) {
		return directed ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges) {
		final Random rand = new Random(0xd8bc48e93bae55adL);
		GraphFactory<Integer, Integer> factory = IntGraphFactory.newInstance(directed);
		com.jgalgo.graph.Graph<Integer, Integer> g =
				factory.allowSelfEdges(selfEdges).allowParallelEdges(false).newGraph();
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
		WeightsDouble<Integer> weights = g.addEdgesWeights("weights", double.class);
		for (int e : g.edges())
			weights.set(e, rand.nextInt(100));
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
			if (!g.vertices().contains(v))
				return v;
		}
	}

}
