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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableGraph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@SuppressWarnings("boxing")
public class GuavaGraphAdapterTest {

	@Test
	public void adapterOfGraphWithParallelEdges() {
		com.jgalgo.graph.Graph<Integer, Integer> gOrig =
				IntGraphFactory.directed().allowParallelEdges().allowSelfEdges().newGraph();
		assertThrows(UnsupportedOperationException.class, () -> new GuavaGraphAdapter<>(gOrig));
	}

	@Test
	public void nodes() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);
			assertEquals(gOrig.vertices(), g.nodes());
		}
	}

	@Test
	public void edges() {
		final Random rand = new Random(0x5c82fa80c4cec01fL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
		final Random rand = new Random(0x7727d19a01718ddL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
	public void hasEdgeConnecting() {
		final Random rand = new Random(0x7d5adfa3e4cff2feL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
		final Random rand = new Random(0x4ba6b5de1d87b179L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

				for (Integer u : g.nodes()) {
					Set<EndpointPair<Integer>> actual = g.incidentEdges(u);
					Set<EndpointPair<Integer>> expected = Stream
							.concat(gOrig.outEdges(u).stream(), gOrig.inEdges(u).stream())
							.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed))
							.collect(toSet());
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
		final Random rand = new Random(0x65e5ac02fd8c2c2bL);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
		final Random rand = new Random(0x8f32ab1776e1546L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
		final Random rand = new Random(0x303e9d43e4c1d785L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

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
	public void capabilities() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges);
				Graph<Integer> g = new GuavaGraphAdapter<>(gOrig);

				assertEquals(gOrig.isDirected(), g.isDirected());
				assertEquals(gOrig.isAllowSelfEdges(), g.allowsSelfLoops());
				assertEquals(ElementOrder.Type.UNORDERED, g.nodeOrder().type());
				assertEquals(ElementOrder.Type.UNORDERED, g.incidentEdgeOrder().type());
			}
		}
	}

	@Test
	public void mutableAdapterWithoutEdgeBuilder() {
		GraphFactory<Integer, Integer> factory = GraphFactory.directed();
		factory.allowSelfEdges(false).allowParallelEdges(false);
		com.jgalgo.graph.Graph<Integer, Integer> gOrig = factory.newGraph();
		assertThrows(IllegalArgumentException.class, () -> new GuavaMutableGraphAdapter<>(gOrig));
	}

	@Test
	public void addNode() {
		final Random rand = new Random(0x68360559fa787b0L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			MutableGraph<Integer> g = new GuavaMutableGraphAdapter<>(gOrig);

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
			MutableGraph<Integer> g = new GuavaMutableGraphAdapter<>(gOrig);

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
		final Random rand = new Random(0xeef076eb5b67e4ecL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			gOrig.addVertices(range(20));
			MutableGraph<Integer> g = new GuavaMutableGraphAdapter<>(gOrig);

			/* put edges valid */
			Set<EndpointPair<Integer>> expectedEdges = new ObjectOpenHashSet<>();
			for (int i = 0; i < 30; i++) {
				EndpointPair<Integer> endpoints = validEndpointsToAdd(gOrig, rand);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				boolean modified = g.putEdge(endpoints);
				assertTrue(modified);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				expectedEdges.add(endpoints);
			}
			assertEquals(expectedEdges, g.edges());

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
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				boolean modified = g.putEdge(endpoints);
				assertFalse(modified);
				assertEquals(verticesNumBefore, g.nodes().size());
				assertEquals(edgesNumBefore, g.edges().size());
			}
		}

		/* on missing endpoint, node should be added silently */
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig =
					IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges(false).newGraph();
			MutableGraph<Integer> g = new GuavaMutableGraphAdapter<>(gOrig);
			Set<EndpointPair<Integer>> expectedEdges = new ObjectOpenHashSet<>();
			for (int i = 0; i < 10; i++) {
				EndpointPair<Integer> endpoints = endpoints(i * 2 + 0, i * 2 + 1, directed);
				int verticesNumBefore = g.nodes().size();
				int edgesNumBefore = g.edges().size();
				boolean modified = g.putEdge(endpoints);
				assertTrue(modified);
				assertEquals(verticesNumBefore + 2, g.nodes().size());
				assertEquals(edgesNumBefore + 1, g.edges().size());
				expectedEdges.add(endpoints);
			}
			assertEquals(expectedEdges, g.edges());
			assertEquals(range(20), g.nodes());
			assertEquals(range(20), gOrig.vertices());
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x17ef03cedc99e735L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			MutableGraph<Integer> g = new GuavaMutableGraphAdapter<>(gOrig);

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
					assertTrue(g.removeEdge(endpoints));
					assertEquals(verticesNumBefore, g.nodes().size());
					assertEquals(edgesNumBefore - 1, g.edges().size());
					assertFalse(g.edges().contains(endpoints));
					assertFalse(gOrig.containsEdge(endpoints.nodeU(), endpoints.nodeV()));
					expectedEdges.remove(endpoints);
				} else {
					int edgesNumBefore = g.edges().size();
					assertFalse(g.removeEdge(endpoints));
					assertEquals(edgesNumBefore, g.edges().size());
				}
			}
			assertEquals(expectedEdges, g.edges());
			assertEquals(expectedEdges,
					gOrig
							.edges()
							.stream()
							.map(e -> endpoints(gOrig.edgeSource(e), gOrig.edgeTarget(e), directed))
							.collect(toSet()));
		}
	}

	private static EndpointPair<Integer> endpoints(Integer u, Integer v, boolean directed) {
		return directed ? EndpointPair.ordered(u, v) : EndpointPair.unordered(u, v);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges) {
		final Random rand = new Random(0x2bf4b83f64d13c33L);
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
