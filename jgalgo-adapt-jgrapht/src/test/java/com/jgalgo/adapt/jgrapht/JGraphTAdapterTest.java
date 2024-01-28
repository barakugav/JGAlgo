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
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.WeightsDouble;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

@SuppressWarnings("boxing")
public class JGraphTAdapterTest {

	@Test
	public void constructor() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertFalse(g.getType().isWeighted());
			assertEquals(directed, g.getType().isDirected());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig, null);
			assertFalse(g.getType().isWeighted());
			assertEquals(directed, g.getType().isDirected());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig, "weights");
			assertTrue(g.getType().isWeighted());
			assertEquals(directed, g.getType().isDirected());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			assertThrows(IllegalArgumentException.class, () -> new JGraphTAdapter<>(gOrig, "non-existing-weights"));
		}
	}

	@Test
	public void vertexSet() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertEquals(gOrig.vertices(), g.vertexSet());
		}
	}

	@Test
	public void edgeSet() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertEquals(gOrig.edges(), g.edgeSet());
		}
	}

	@Test
	public void containsVertex() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices())
				assertTrue(g.containsVertex(v));
			assertFalse(g.containsVertex(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void containsEdge() {
		final Random rand = new Random(0xc1dfdcacf76522a0L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);

			/* containsEdge(E edge) */
			for (Integer e : gOrig.edges())
				assertTrue(g.containsEdge(e));
			assertFalse(g.containsEdge(nonExistingEdge(gOrig)));

			/* containsEdge(V source, V target) */
			for (Integer e : gOrig.edges())
				assertTrue(g.containsEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e)));
			for (int i = 0; i < 100; i++) {
				Integer u = Graphs.randVertex(gOrig, rand);
				Integer v = Graphs.randVertex(gOrig, rand);
				assertEquals(gOrig.containsEdge(u, v), g.containsEdge(u, v));
			}
			Integer nonExistingVertex = nonExistingVertex(gOrig);
			Integer existingVertex = gOrig.vertices().iterator().next();
			assertFalse(g.containsEdge(existingVertex, nonExistingVertex));
			assertFalse(g.containsEdge(nonExistingVertex, existingVertex));
		}
	}

	@Test
	public void getEdgeSource() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(gOrig.edgeSource(e), g.getEdgeSource(e));

			assertThrows(RuntimeException.class, () -> g.getEdgeSource(nonExistingEdge(gOrig)));
		}
	}

	@Test
	public void getEdgeTarget() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(gOrig.edgeTarget(e), g.getEdgeTarget(e));

			assertThrows(RuntimeException.class, () -> g.getEdgeTarget(nonExistingEdge(gOrig)));
		}
	}

	@Test
	public void getEdge() {
		final Random rand = new Random(0xebeda915b31eebb5L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, true, false);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(e, g.getEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e)));

			for (int i = 0; i < 100; i++) {
				Integer u = Graphs.randVertex(gOrig, rand);
				Integer v = Graphs.randVertex(gOrig, rand);
				assertEquals(gOrig.getEdge(u, v), g.getEdge(u, v));
			}

			Integer nonExistingVertex = nonExistingVertex(gOrig);
			Integer existingVertex = gOrig.vertices().iterator().next();
			assertNull(g.getEdge(existingVertex, nonExistingVertex));
			assertNull(g.getEdge(nonExistingVertex, existingVertex));
		}
	}

	@Test
	public void getAllEdges() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer u : gOrig.vertices())
				for (Integer v : gOrig.vertices())
					assertEquals(gOrig.getEdges(u, v), g.getAllEdges(u, v));

			Integer nonExistingVertex = nonExistingVertex(gOrig);
			Integer existingVertex = gOrig.vertices().iterator().next();
			assertNull(g.getAllEdges(existingVertex, nonExistingVertex));
			assertNull(g.getAllEdges(nonExistingVertex, existingVertex));
		}
	}

	@Test
	public void edgesOf() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices()) {
				Set<Integer> expected = new IntOpenHashSet();
				expected.addAll(gOrig.outEdges(v));
				expected.addAll(gOrig.inEdges(v));
				assertEquals(expected, g.edgesOf(v));
			}

			assertThrows(IllegalArgumentException.class, () -> g.edgesOf(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void outgoingEdgesOf() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices())
				assertEquals(gOrig.outEdges(v), g.outgoingEdgesOf(v));

			assertThrows(IllegalArgumentException.class, () -> g.outgoingEdgesOf(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void incomingEdgesOf() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices())
				assertEquals(gOrig.inEdges(v), g.incomingEdgesOf(v));

			assertThrows(IllegalArgumentException.class, () -> g.incomingEdgesOf(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void degreeOf() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, selfEdges, true);
				Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
				for (Integer v : gOrig.vertices()) {
					int expected;
					if (gOrig.isDirected()) {
						expected = gOrig.outEdges(v).size() + gOrig.inEdges(v).size();
					} else {
						expected = gOrig.outEdges(v).size();
						for (int e : gOrig.outEdges(v))
							if (gOrig.edgeSource(e).intValue() == gOrig.edgeTarget(e).intValue())
								expected++; /* self edges are counted twice in JGraphT */
					}
					assertEquals(expected, g.degreeOf(v));
				}

				assertThrows(IllegalArgumentException.class, () -> g.degreeOf(nonExistingVertex(gOrig)));
			}
		}
	}

	@Test
	public void outDegreeOf() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices()) {
				int expected;
				if (directed) {
					expected = gOrig.outEdges(v).size();
				} else {
					expected = gOrig.outEdges(v).size();
					for (int e : gOrig.outEdges(v))
						if (gOrig.edgeSource(e).intValue() == gOrig.edgeTarget(e).intValue())
							expected++; /* self edges are counted twice in JGraphT */
				}
				assertEquals(expected, g.outDegreeOf(v));
			}

			assertThrows(IllegalArgumentException.class, () -> g.outDegreeOf(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void inDegreeOf() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer v : gOrig.vertices()) {
				int expected;
				if (directed) {
					expected = gOrig.inEdges(v).size();
				} else {
					expected = gOrig.inEdges(v).size();
					for (int e : gOrig.inEdges(v))
						if (gOrig.edgeSource(e).intValue() == gOrig.edgeTarget(e).intValue())
							expected++; /* self edges are counted twice in JGraphT */
				}
				assertEquals(expected, g.inDegreeOf(v));
			}

			assertThrows(IllegalArgumentException.class, () -> g.inDegreeOf(nonExistingVertex(gOrig)));
		}
	}

	@Test
	public void addVertex() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);
			for (int i = 0; i < 100; i++) {
				Integer v = nonExistingVertex(gOrig);
				int verticesNumBefore = g.vertexSet().size();
				assertTrue(g.addVertex(v));
				assertEquals(verticesNumBefore + 1, g.vertexSet().size());
				assertGraph(gOrig, g);
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			Iterator<Integer> vIter = gOrig.vertices().iterator();
			for (int i = 0; i < 20; i++) {
				int verticesNumBefore = g.vertexSet().size();
				assertFalse(g.addVertex(vIter.next()));
				assertEquals(verticesNumBefore, g.vertexSet().size());
			}
			assertGraph(gOrig, g);
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			JGraphTAdapter<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			Supplier<Integer> vertexSupplier = new AtomicInteger(10000)::getAndIncrement;
			g.setVertexSupplier(vertexSupplier);
			assertEquals(vertexSupplier, g.getVertexSupplier());
			assertGraph(gOrig, g);
			for (int i = 0; i < 100; i++) {
				int verticesNumBefore = g.vertexSet().size();
				Integer v = g.addVertex();
				assertTrue(g.containsVertex(v));
				assertTrue(g.vertexSet().contains(v));
				assertTrue(gOrig.vertices().contains(v));
				assertEquals(verticesNumBefore + 1, g.vertexSet().size());
				assertGraph(gOrig, g);
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			/* remove all vertices with odd id */
			gOrig.removeVertices(gOrig.vertices().stream().filter(v -> v % 2 != 0).collect(toList()));

			JGraphTAdapter<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			g.setVertexSupplier(new AtomicInteger(0)::getAndIncrement);
			assertGraph(gOrig, g);
			for (int v : range(g.vertexSet().size())) {
				boolean expectedAdded = !gOrig.vertices().contains(v);
				int verticesNumBefore = g.vertexSet().size();
				if (expectedAdded) {
					Integer addedVertex = g.addVertex();
					assertEquals(v, addedVertex);
					assertEquals(verticesNumBefore + 1, g.vertexSet().size());
					assertTrue(g.containsVertex(v));

				} else {
					assertThrows(IllegalArgumentException.class, () -> g.addVertex());
					assertEquals(verticesNumBefore, g.vertexSet().size());
				}
			}
			assertGraph(gOrig, g);
		}
	}

	@Test
	public void removeVertex() {
		final Random rand = new Random(0xf485d9e60f98b995L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			for (int i = gOrig.vertices().size(); i-- > 0;) {
				Integer v = Graphs.randVertex(gOrig, rand);
				assertTrue(g.containsVertex(v));
				Set<Integer> edges = new IntOpenHashSet(g.edgesOf(v));
				int verticesNumBefore = g.vertexSet().size();
				int edgesNumBefore = g.edgeSet().size();
				assertTrue(g.removeVertex(v));
				assertEquals(verticesNumBefore - 1, g.vertexSet().size());
				assertEquals(edgesNumBefore - edges.size(), g.edgeSet().size());
				assertFalse(g.containsVertex(v));
				for (Integer e : edges)
					assertFalse(g.containsEdge(e));
				assertGraph(gOrig, g);
			}
			assertTrue(g.vertexSet().isEmpty());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			Integer nonExistingVertex = nonExistingVertex(gOrig);
			assertFalse(g.containsVertex(nonExistingVertex));
			int verticesNumBefore = g.vertexSet().size();
			assertFalse(g.removeVertex(nonExistingVertex));
			assertEquals(verticesNumBefore, g.vertexSet().size());
			assertFalse(g.containsVertex(nonExistingVertex));
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertThrows(UnsupportedOperationException.class, () -> g.addVertex());
		}
	}

	@Test
	public void removeAllVertices() {
		final Random rand = new Random(0x41abc77e1bc382a3L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, true, true);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			while (gOrig.vertices().size() > 0) {
				List<Integer> verticesToRemove = new ArrayList<>();
				int verticesToRemoveNum = Math.min(rand.nextInt(5), gOrig.vertices().size());
				while (verticesToRemove.size() < verticesToRemoveNum)
					verticesToRemove.add(Graphs.randVertex(gOrig, rand)); /* duplications are fine */
				Set<Integer> verticesToRemoveSet = new HashSet<>(verticesToRemove);

				Set<Integer> removedEdgesExpected = new IntOpenHashSet();
				for (Integer v : verticesToRemove)
					removedEdgesExpected.addAll(g.edgesOf(v));

				if (verticesToRemove.size() > 0 && rand.nextBoolean())
					verticesToRemove.add(verticesToRemove.get(rand.nextInt(verticesToRemove.size())));

				if (rand.nextBoolean())
					verticesToRemove.add(nonExistingVertex(gOrig));

				Collections.shuffle(verticesToRemove, rand);
				int verticesNumBefore = g.vertexSet().size();
				int edgesNumBefore = g.edgeSet().size();
				assertEquals(!verticesToRemoveSet.isEmpty(), g.removeAllVertices(verticesToRemove));
				assertEquals(verticesNumBefore - verticesToRemoveSet.size(), g.vertexSet().size());
				assertEquals(edgesNumBefore - removedEdgesExpected.size(), g.edgeSet().size());
				for (Integer e : removedEdgesExpected)
					assertFalse(g.containsEdge(e));

				gOrig.removeVertices(verticesToRemoveSet);
				assertGraph(gOrig, g);
			}
			assertTrue(g.vertexSet().isEmpty());
		}
	}

	@Test
	public void addEdge() {
		final Random rand = new Random(0x2223aee3dc4b0886L);
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean parallelEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, true, parallelEdges);
				Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
				assertGraph(gOrig, g);
				for (int i : range(100)) {
					if (i % 5 == 0) {
						Integer e = nonExistingEdge(gOrig);
						Pair<Integer, Integer> uv = validEndpointsToAdd(gOrig, rand);
						Integer u = uv.left();
						Integer v = uv.right();
						int edgesNumBefore = g.edgeSet().size();
						assertTrue(g.addEdge(u, v, e));
						assertEquals(edgesNumBefore + 1, g.edgeSet().size());
						assertTrue(g.containsEdge(e));
						assertTrue(g.containsEdge(u, v));
						assertTrue(g.edgeSet().contains(e));
						assertTrue(gOrig.edges().contains(e));
						assertGraph(gOrig, g);

					} else if (i % 5 == 1) {
						Integer e = nonExistingEdge(gOrig);
						Integer u = Graphs.randVertex(gOrig, rand);
						Integer v = nonExistingVertex(gOrig);
						assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));

					} else if (i % 5 == 2) {
						Integer e = nonExistingEdge(gOrig);
						Integer u = nonExistingVertex(gOrig);
						Integer v = Graphs.randVertex(gOrig, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, e));

					} else if (i % 5 == 3) {
						Integer e = Graphs.randEdge(gOrig, rand);
						Pair<Integer, Integer> uv = validEndpointsToAdd(gOrig, rand);
						Integer u = uv.left();
						Integer v = uv.right();
						int edgesNumBefore = g.edgeSet().size();
						assertFalse(g.addEdge(u, v, e));
						assertEquals(edgesNumBefore, g.edgeSet().size());

					} else if (i % 5 == 4 && !g.getType().isAllowingMultipleEdges()) {
						Integer e = nonExistingEdge(gOrig);
						Integer eEndpoints = Graphs.randEdge(gOrig, rand);
						Integer u = gOrig.edgeSource(eEndpoints);
						Integer v = gOrig.edgeTarget(eEndpoints);
						assertTrue(gOrig.containsEdge(u, v));
						assertTrue(g.containsEdge(u, v));
						int edgesNumBefore = g.edgeSet().size();
						assertFalse(g.addEdge(u, v, e));
						assertEquals(edgesNumBefore, g.edgeSet().size());
					}
				}
				assertGraph(gOrig, g);
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean parallelEdges : new boolean[] { false, true }) {
				com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, true, parallelEdges);
				JGraphTAdapter<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
				AtomicInteger edgeSupplierNext = new AtomicInteger(10000);
				Supplier<Integer> edgeSupplier = edgeSupplierNext::getAndIncrement;
				g.setEdgeSupplier(edgeSupplier);
				assertEquals(edgeSupplier, g.getEdgeSupplier());
				assertGraph(gOrig, g);
				for (int i : range(100)) {
					int nextEdge = edgeSupplierNext.get();

					if (i % 5 == 0) {
						Pair<Integer, Integer> uv = validEndpointsToAdd(gOrig, rand);
						Integer u = uv.left();
						Integer v = uv.right();
						int edgesNumBefore = g.edgeSet().size();
						Integer addedEdge = g.addEdge(u, v);
						assertNotNull(addedEdge);
						assertEquals(nextEdge, addedEdge);
						assertEquals(edgesNumBefore + 1, g.edgeSet().size());
						assertTrue(g.containsEdge(addedEdge));
						assertTrue(g.containsEdge(u, v));
						assertTrue(g.edgeSet().contains(addedEdge));
						assertTrue(gOrig.edges().contains(addedEdge));
						assertGraph(gOrig, g);

					} else if (i % 5 == 1) {
						Integer u = Graphs.randVertex(gOrig, rand);
						Integer v = nonExistingVertex(gOrig);
						assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v));

					} else if (i % 5 == 2) {
						Integer u = nonExistingVertex(gOrig);
						Integer v = Graphs.randVertex(gOrig, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v));

					} else if (i % 5 == 3) {
						Pair<Integer, Integer> uv = validEndpointsToAdd(gOrig, rand);
						Integer u = uv.left();
						Integer v = uv.right();
						int edgesNumBefore = g.edgeSet().size();
						assertTrue(g.addEdge(u, v, nextEdge));
						assertEquals(edgesNumBefore + 1, g.edgeSet().size());

						uv = validEndpointsToAdd(gOrig, rand);
						u = uv.left();
						v = uv.right();
						edgesNumBefore = g.edgeSet().size();
						assertNull(g.addEdge(u, v));
						assertEquals(edgesNumBefore, g.edgeSet().size());

					} else if (i % 5 == 4 && !g.getType().isAllowingMultipleEdges()) {
						Integer eEndpoints = Graphs.randEdge(gOrig, rand);
						Integer u = gOrig.edgeSource(eEndpoints);
						Integer v = gOrig.edgeTarget(eEndpoints);
						assertTrue(gOrig.containsEdge(u, v));
						assertTrue(g.containsEdge(u, v));
						int edgesNumBefore = g.edgeSet().size();
						assertNull(g.addEdge(u, v));
						assertEquals(edgesNumBefore, g.edgeSet().size());
					}
				}
				assertGraph(gOrig, g);
			}
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			Pair<Integer, Integer> endpoints = validEndpointsToAdd(gOrig, rand);
			Integer u = endpoints.left();
			Integer v = endpoints.right();
			assertThrows(UnsupportedOperationException.class, () -> g.addEdge(u, v));
		}
	}

	@Test
	public void removeEdge() {
		final Random rand = new Random(0x24d502e7f38069fcL);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			for (int i = gOrig.edges().size(); i-- > 0;) {
				Integer e = Graphs.randEdge(gOrig, rand);
				assertTrue(g.containsEdge(e));
				int edgesNumBefore = g.edgeSet().size();
				assertTrue(g.removeEdge(e));
				assertEquals(edgesNumBefore - 1, g.edgeSet().size());
				assertFalse(g.containsEdge(e));
				assertGraph(gOrig, g);
			}
			assertTrue(g.edgeSet().isEmpty());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			Integer nonExistingEdge = nonExistingEdge(gOrig);
			assertFalse(g.containsEdge(nonExistingEdge));
			int edgesNumBefore = g.edgeSet().size();
			assertFalse(g.removeEdge(nonExistingEdge));
			assertEquals(edgesNumBefore, g.edgeSet().size());
			assertFalse(g.containsEdge(nonExistingEdge));
			assertGraph(gOrig, g);
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			for (int i : range(100)) {
				if (i % 4 == 0) {
					Integer eEndpoints = Graphs.randEdge(gOrig, rand);
					Integer u = g.getEdgeSource(eEndpoints);
					Integer v = g.getEdgeTarget(eEndpoints);
					Integer e = g.removeEdge(u, v);
					assertNotNull(e);
					if (directed) {
						assertEquals(u, gOrig.edgeSource(e));
						assertEquals(v, gOrig.edgeTarget(e));
					} else {
						assertTrue(u.equals(gOrig.edgeSource(e)) && v.equals(gOrig.edgeTarget(e))
								|| u.equals(gOrig.edgeTarget(e)) && v.equals(gOrig.edgeSource(e)));
					}
					gOrig.removeEdge(e);

				} else if (i % 4 == 1) {
					Integer u = Graphs.randVertex(gOrig, rand);
					Integer v = nonExistingVertex(gOrig);
					assertNull(g.removeEdge(u, v));

				} else if (i % 4 == 2) {
					Integer u = nonExistingVertex(gOrig);
					Integer v = Graphs.randVertex(gOrig, rand);
					assertNull(g.removeEdge(u, v));

				} else if (i % 4 == 3) {
					Integer u, v;
					do {
						u = Graphs.randVertex(gOrig, rand);
						v = Graphs.randVertex(gOrig, rand);
					} while (gOrig.containsEdge(u, v));
					assertFalse(g.containsEdge(u, v));
					assertNull(g.removeEdge(u, v));
				}
			}
		}
	}

	@Test
	public void removeAllEdges() {
		final Random rand = new Random(0x65fb4d38d4809b8L);
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			while (gOrig.edges().size() > 0) {
				List<Integer> edgesToRemove = new ArrayList<>();
				int edgesToRemoveNum = Math.min(rand.nextInt(5), gOrig.edges().size());
				while (edgesToRemove.size() < edgesToRemoveNum)
					edgesToRemove.add(Graphs.randEdge(gOrig, rand)); /* duplications are fine */
				Set<Integer> edgesToRemoveSet = new HashSet<>(edgesToRemove);

				if (edgesToRemove.size() > 0 && rand.nextBoolean())
					edgesToRemove.add(edgesToRemove.get(rand.nextInt(edgesToRemove.size())));

				if (rand.nextBoolean())
					edgesToRemove.add(nonExistingEdge(gOrig));

				Collections.shuffle(edgesToRemove, rand);
				int edgesNumBefore = g.edgeSet().size();
				assertEquals(!edgesToRemoveSet.isEmpty(), g.removeAllEdges(edgesToRemove));
				assertEquals(edgesNumBefore - edgesToRemoveSet.size(), g.edgeSet().size());

				gOrig.removeEdges(edgesToRemoveSet);
				assertGraph(gOrig, g);
			}
			assertTrue(g.edgeSet().isEmpty());
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertGraph(gOrig, g);

			gOrig = gOrig.copy(); /* copy a snapshot of the graph */
			for (int i : range(100)) {
				if (gOrig.edges().isEmpty())
					break;
				if (i % 3 == 0) {
					Integer eEndpoints = Graphs.randEdge(gOrig, rand);
					Integer u = g.getEdgeSource(eEndpoints);
					Integer v = g.getEdgeTarget(eEndpoints);
					Set<Integer> removedEdgesExpected = gOrig.getEdges(u, v);
					int edgesNumBefore = g.edgeSet().size();
					Set<Integer> removedEdges = g.removeAllEdges(u, v);
					assertEquals(removedEdgesExpected, removedEdges);
					assertEquals(edgesNumBefore - removedEdgesExpected.size(), g.edgeSet().size());
					for (Integer e : removedEdgesExpected)
						assertFalse(g.containsEdge(e));
					gOrig.removeEdges(removedEdges);
					assertGraph(gOrig, g);

				} else if (i % 3 == 1) {
					Integer u = Graphs.randVertex(gOrig, rand);
					Integer v = nonExistingVertex(gOrig);
					Set<Integer> removedEdges = g.removeAllEdges(u, v);
					assertNull(removedEdges);

				} else if (i % 3 == 2) {
					Integer u = nonExistingVertex(gOrig);
					Integer v = Graphs.randVertex(gOrig, rand);
					Set<Integer> removedEdges = g.removeAllEdges(u, v);
					assertNull(removedEdges);

				}
			}
			assertGraph(gOrig, g);
		}
	}

	private static void assertGraph(com.jgalgo.graph.Graph<Integer, Integer> expected, Graph<Integer, Integer> g) {
		assertEquals(expected.vertices(), g.vertexSet());
		assertEquals(expected.edges(), g.edgeSet());
		for (Integer v : expected.vertices()) {
			assertEquals(expected.outEdges(v), g.outgoingEdgesOf(v));
			assertEquals(expected.inEdges(v), g.incomingEdgesOf(v));
			int expectedDegree;
			int expectedOutDegree;
			int expectedInDegree;
			if (expected.isDirected()) {
				expectedOutDegree = expected.outEdges(v).size();
				expectedInDegree = expected.inEdges(v).size();
				expectedDegree = expectedOutDegree + expectedInDegree;
			} else {
				expectedDegree = expected.outEdges(v).size();
				for (int e : expected.outEdges(v))
					if (expected.edgeSource(e).intValue() == expected.edgeTarget(e).intValue())
						expectedDegree++; /* self edges are counted twice in JGraphT */
				expectedOutDegree = expectedInDegree = expectedDegree;
			}
			assertEquals(expectedDegree, g.degreeOf(v));
			assertEquals(expectedOutDegree, g.outDegreeOf(v));
			assertEquals(expectedInDegree, g.inDegreeOf(v));
		}
	}

	@Test
	public void getType() {
		for (boolean directed : new boolean[] { false, true }) {
			for (boolean selfEdges : new boolean[] { false, true }) {
				for (boolean parallelEdges : new boolean[] { false, true }) {
					for (boolean weighted : new boolean[] { false, true }) {
						com.jgalgo.graph.Graph<Integer, Integer> gOrig =
								createGraph(directed, selfEdges, parallelEdges);
						Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig, weighted ? "weights" : null);
						assertEquals(directed, g.getType().isDirected());
						assertEquals(selfEdges, g.getType().isAllowingSelfLoops());
						assertEquals(parallelEdges, g.getType().isAllowingMultipleEdges());
						assertEquals(weighted, g.getType().isWeighted());
					}
				}
			}
		}
	}

	@Test
	public void getEdgeWeight() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig, "weights");
			WeightsDouble<Integer> weights = gOrig.edgesWeights("weights");
			for (Integer e : gOrig.edges())
				assertEquals(weights.get(e), g.getEdgeWeight(e));

			assertThrows(RuntimeException.class, () -> g.getEdgeWeight(nonExistingEdge(gOrig)));
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			for (Integer e : gOrig.edges())
				assertEquals(1.0, g.getEdgeWeight(e));
		}
	}

	@Test
	public void setEdgeWeight() {
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed, false, false);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig, "weights");
			WeightsDouble<Integer> weights = gOrig.edgesWeights("weights");
			for (Integer e : gOrig.edges()) {
				double newWeight = g.getEdgeWeight(e) + 1;
				g.setEdgeWeight(e, newWeight);
				assertEquals(newWeight, g.getEdgeWeight(e));
				assertEquals(newWeight, weights.get(e));
			}

			assertThrows(RuntimeException.class, () -> g.setEdgeWeight(nonExistingEdge(gOrig), 0));
		}
		for (boolean directed : new boolean[] { false, true }) {
			com.jgalgo.graph.Graph<Integer, Integer> gOrig = createGraph(directed);
			Graph<Integer, Integer> g = new JGraphTAdapter<>(gOrig);
			assertThrows(UnsupportedOperationException.class,
					() -> g.setEdgeWeight(g.edgeSet().iterator().next(), 789.4));
		}
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed) {
		return createGraph(directed, true, true);
	}

	private static com.jgalgo.graph.Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges,
			boolean parallelEdges) {
		final Random rand = new Random(0xa289d8b5e9949e3bL);
		GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
		com.jgalgo.graph.Graph<Integer, Integer> g =
				factory.allowSelfEdges(selfEdges).allowParallelEdges(parallelEdges).newGraph();
		g.addVertices(range(50 + rand.nextInt(50)));
		for (int m = 300 + rand.nextInt(100); g.edges().size() < m;) {
			Pair<Integer, Integer> endpoints = validEndpointsToAdd(g, rand);
			int u = endpoints.left();
			int v = endpoints.right();
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

	private static Integer nonExistingVertex(com.jgalgo.graph.Graph<Integer, Integer> g) {
		int nonExistingVertex;
		for (nonExistingVertex = 500; g.vertices().contains(nonExistingVertex); nonExistingVertex++);
		return nonExistingVertex;
	}

	private static Integer nonExistingEdge(com.jgalgo.graph.Graph<Integer, Integer> g) {
		int nonExistingEdge;
		for (nonExistingEdge = 500; g.edges().contains(nonExistingEdge); nonExistingEdge++);
		return nonExistingEdge;
	}

	private static Pair<Integer, Integer> validEndpointsToAdd(com.jgalgo.graph.Graph<Integer, Integer> g, Random rand) {
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
			return Pair.of(u, v);
		}
	}

}
