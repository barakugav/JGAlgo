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

import static com.jgalgo.internal.util.Range.range;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;

public class GraphBuilderTest extends TestBase {

	@Test
	public void isDirected() {
		assertTrue(GraphBuilder.directed().isDirected());
		assertFalse(GraphBuilder.undirected().isDirected());
	}

	@Test
	public void newCopyOf() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = createGraph(directed);

			/* With weights */
			GraphBuilder<Integer, Integer> b2 = GraphBuilder.newCopyOf(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			GraphBuilder<Integer, Integer> b1 = GraphBuilder.newCopyOf(g);
			assertEqualsBool(directed, b1.isDirected());
			assertEquals(g.copy(/* no weights */), b1.build());
		});
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			List<Integer> vertices = IntList.of(48, 84, 66, 91, 3, 7);
			for (Integer v : vertices)
				b.addVertex(v);
			assertEquals(new HashSet<>(vertices), b.vertices());
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			factory.setVertexFactory(IdBuilder.defaultFactory(Integer.class));
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			Integer v1 = b.addVertex();
			Integer v2 = b.addVertex();
			Integer v3 = b.addVertex();
			assertEquals(Set.of(v1, v2, v3), b.build().vertices());
		});
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> b.addVertex());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addVertexDuplicateId() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			b.addVertex(5);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(5));
		});
	}

	@Test
	public void addVertexNull() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			assertThrows(NullPointerException.class, () -> b.addVertex(null));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addVertices() {
		final Random rand = new Random(0x73dfee707c122461L);
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);

			Set<Integer> vertices = new HashSet<>();
			List<Integer> verticesList = new ArrayList<>();
			for (int r : range(50)) {
				int num = rand.nextInt(5);
				List<Integer> vs = new ArrayList<>();
				while (vs.size() < num) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v) || vs.contains(v))
						continue;
					vs.add(v);
				}
				if (r % 4 == 0) {
					b.addVertices(vs);
					vertices.addAll(vs);
					verticesList.addAll(vs);
				} else if (r % 4 == 1) {
					vs.add(null);
					Collections.shuffle(vs, rand);
					assertThrows(NullPointerException.class, () -> b.addVertices(vs));
				} else if (r % 4 == 2 && vs.size() > 0) {
					vs.add(randElement(vs, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> b.addVertices(vs));
				} else if (r % 4 == 3 && vertices.size() > 0) {
					vs.add(randElement(verticesList, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> b.addVertices(vs));
				}
				assertEquals(vertices, b.vertices());
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdge() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			b.ensureVertexCapacity(10);
			b.ensureEdgeCapacity(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			assertEquals(range(3), b.edges());
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			factory.setEdgeFactory(IdBuilder.defaultFactory(Integer.class));
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			b.addVertices(range(10));
			Integer e1 = b.addEdge(0, 1);
			Integer e2 = b.addEdge(1, 2);
			Integer e3 = b.addEdge(2, 3);
			assertEquals(Set.of(e1, e2, e3), b.build().edges());
		});
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			assertThrows(UnsupportedOperationException.class, () -> b.addEdge(0, 1));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgeDuplicateId() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 77);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(2, 3, 77));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgeInvalidEndpoints() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1, 1));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0, 2));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10, 3));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgeNull() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			b.addVertex(0);
			b.addVertex(1);
			assertThrows(NullPointerException.class, () -> b.addEdge(0, 1, null));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdges() {
		final Random rand = new Random(0x3a886c0cebe85403L);

		Function<List<Pair<Integer, Pair<Integer, Integer>>>, EdgeSet<Integer, Integer>> toEdgeSet = es -> {
			List<Integer> ids = es.stream().map(Pair::first).collect(toList());
			List<Pair<Integer, Integer>> endpoints = es.stream().map(Pair::second).collect(toList());
			return GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);
		};
		BiConsumer<Map<Integer, Pair<Integer, Integer>>, EdgeSet<Integer, Integer>> addEdges = (edgesMap, edgeSet) -> {
			for (EdgeIter<Integer, Integer> eit = edgeSet.iterator(); eit.hasNext();) {
				Integer e = eit.next();
				Pair<Integer, Integer> endpoints = Pair.of(eit.source(), eit.target());
				Object oldVal = edgesMap.put(e, endpoints);
				assert oldVal == null;
			}
		};

		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphFactory
					.<Integer, Integer>newInstance(directed)
					.allowSelfEdges(true)
					.allowParallelEdges(true)
					.newBuilder();
			b.addVertices(range(1000));

			Map<Integer, Pair<Integer, Integer>> edges = new HashMap<>();
			List<Integer> edgesList = new ArrayList<>();
			for (int r = 0; r < 50; r++) {
				int num = rand.nextInt(5);
				List<Integer> esIds = new ArrayList<>();
				while (esIds.size() < num) {
					int e = rand.nextInt();
					if (e < 0 || edges.containsKey(e) || esIds.contains(e))
						continue;
					esIds.add(e);
				}
				List<Pair<Integer, Pair<Integer, Integer>>> esList = esIds
						.stream()
						.map(e -> Pair.of(e, Pair.of(rand.nextInt(1000), rand.nextInt(1000))))
						.collect(toList());

				if (r % 4 == 0) {
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					b.addEdges(es);
					addEdges.accept(edges, es);
					edgesList.addAll(es);
				} else if (r % 4 == 1) {
					esList.add(Pair.of(null, Pair.of(rand.nextInt(1000), rand.nextInt(1000))));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(NullPointerException.class, () -> b.addEdges(es));
				} else if (r % 4 == 2 && esList.size() > 0) {
					esList.add(randElement(esList, rand));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(IllegalArgumentException.class, () -> b.addEdges(es));
				} else if (r % 4 == 3 && edgesList.size() > 0) {
					Integer dupEdge = randElement(edgesList, rand);
					esList.add(Pair.of(dupEdge, edges.get(dupEdge)));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(IllegalArgumentException.class, () -> b.addEdges(es));
				}
				Graph<Integer, Integer> g = b.build();
				assertEquals(edges.keySet(), g.edges());
				for (Integer e : edges.keySet()) {
					Pair<Integer, Integer> endpoints = edges.get(e);
					Integer expectedSource = endpoints.first();
					Integer expectedTarget = endpoints.second();
					Integer actualSource = g.edgeSource(e);
					Integer actualTarget = g.edgeTarget(e);
					if (directed) {
						assertEquals(expectedSource, actualSource);
						assertEquals(expectedTarget, actualTarget);
					} else {
						assertTrue((expectedSource.equals(actualSource) && expectedTarget.equals(actualTarget))
								|| (expectedSource.equals(actualTarget) && expectedTarget.equals(actualSource)));
					}
				}
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void clear() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = GraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			b.addVerticesWeights("weights", int.class);
			b.addEdgesWeights("weights", int.class);
			b.clear();

			assertEquals(Set.of(), b.vertices());
			assertEquals(Set.of(), b.edges());
			assertEquals(Set.of(), b.verticesWeightsKeys());
			assertEquals(Set.of(), b.edgesWeightsKeys());

			Graph<Integer, Integer> g = b.build();
			assertEquals(Set.of(), g.vertices());
			assertEquals(Set.of(), g.edges());
			assertEquals(Set.of(), g.verticesWeightsKeys());
			assertEquals(Set.of(), g.edgesWeightsKeys());
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void build() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut, selfEdges) -> {
			GraphFactory<Integer, Integer> factory =
					GraphFactory.<Integer, Integer>newInstance(directed).allowSelfEdges(selfEdges).allowParallelEdges();
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			Graph<Integer, Integer> g = factory.newGraph();

			/* Add vertices and edges */
			final int n = 12 + rand.nextInt(12);
			final int m = 20 + rand.nextInt(20);
			while (g.vertices().size() < n) {
				Integer v = Integer.valueOf(rand.nextInt(2 * n));
				if (g.vertices().contains(v))
					continue;
				g.addVertex(v);
				b.addVertex(v);
			}
			while (g.edges().size() < m) {
				Integer e = Integer.valueOf(rand.nextInt(2 * m));
				if (g.edges().contains(e))
					continue;
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!selfEdges && u.equals(v))
					continue;
				g.addEdge(u, v, e);
				b.addEdge(u, v, e);
			}

			/* Add weights */
			AtomicInteger weightIdx = new AtomicInteger();
			@SuppressWarnings("rawtypes")
			BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
				foreachBoolConfig(edgesWeights -> {
					for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
						String key = "weight" + weightIdx.getAndIncrement();
						Object defVal = rand.nextBoolean() ? valSupplier.get() : null;
						Weights wG, wB;
						Set<Integer> elements;
						if (!edgesWeights) {
							if (defVal == null) {
								wG = g.addVerticesWeights(key, type);
								wB = b.addVerticesWeights(key, type);
							} else {
								wG = g.addVerticesWeights(key, type, defVal);
								wB = b.addVerticesWeights(key, type, defVal);
							}
							elements = g.vertices();
						} else {
							if (defVal == null) {
								wG = g.addEdgesWeights(key, type);
								wB = b.addEdgesWeights(key, type);
							} else {
								wG = g.addEdgesWeights(key, type, defVal);
								wB = b.addEdgesWeights(key, type, defVal);
							}
							elements = g.edges();
						}
						for (Integer elm : elements) {
							Object w = valSupplier.get();
							wG.setAsObj(elm, w);
							wB.setAsObj(elm, w);
						}
					}
				});
			};
			addWeights.accept(byte.class, () -> Byte.valueOf((byte) rand.nextInt()));
			addWeights.accept(short.class, () -> Short.valueOf((short) rand.nextInt()));
			addWeights.accept(int.class, () -> Integer.valueOf(rand.nextInt()));
			addWeights.accept(long.class, () -> Long.valueOf(rand.nextLong()));
			addWeights.accept(float.class, () -> Float.valueOf(rand.nextFloat()));
			addWeights.accept(double.class, () -> Double.valueOf(rand.nextDouble()));
			addWeights.accept(boolean.class, () -> Boolean.valueOf(rand.nextBoolean()));
			addWeights.accept(char.class, () -> Character.valueOf((char) rand.nextInt()));
			addWeights.accept(String.class, () -> String.valueOf(rand.nextInt()));

			assertEquals(g.verticesWeightsKeys(), b.verticesWeightsKeys());
			assertEquals(g.edgesWeightsKeys(), b.edgesWeightsKeys());
			assertNull(b.verticesWeights("dashpauht"));
			assertNull(b.edgesWeights("asdjeea"));

			Graph<Integer, Integer> gActual = buildMut ? b.buildMutable() : b.build();
			assertEquals(g, gActual);

			assertEquals(b.verticesWeightsKeys(), gActual.verticesWeightsKeys());
			assertEquals(b.edgesWeightsKeys(), gActual.edgesWeightsKeys());

			for (String key : g.verticesWeightsKeys())
				assertEquals(g.verticesWeights(key).defaultWeightAsObj(),
						gActual.verticesWeights(key).defaultWeightAsObj());
			for (String key : g.edgesWeightsKeys())
				assertEquals(g.edgesWeights(key).defaultWeightAsObj(), gActual.edgesWeights(key).defaultWeightAsObj());

			if (buildMut) {
				Integer nonExistingVertex = GraphsTestUtils.nonExistingVertexNonNegative(g, rand);
				gActual.addVertex(nonExistingVertex);
				assertTrue(gActual.vertices().contains(nonExistingVertex));

			} else {
				for (String key : gActual.verticesWeightsKeys()) {
					@SuppressWarnings("rawtypes")
					Weights w = gActual.verticesWeights(key);
					Integer v = Graphs.randVertex(gActual, rand);
					Object data = w.getAsObj(Graphs.randVertex(gActual, rand));
					assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(v, data));
				}
				for (String key : gActual.edgesWeightsKeys()) {
					@SuppressWarnings("rawtypes")
					Weights w = gActual.edgesWeights(key);
					Integer e = Graphs.randEdge(gActual, rand);
					Object data = w.getAsObj(Graphs.randEdge(gActual, rand));
					assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(e, data));
				}
			}
		});
	}

	private static Graph<Integer, Integer> createGraph(boolean directed) {
		final long seed = 0xa636ca816d4202c9L;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		GraphFactory<Integer, Integer> factory = GraphFactory.<Integer, Integer>newInstance(directed);
		Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		WeightsInt<Integer> vWeights = g.addVerticesWeights("weights", int.class);
		for (Integer v : range(n)) {
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt<Integer> eWeights = g.addEdgesWeights("weights", int.class);
		for (Integer e : range(m)) {
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

	@Test
	public void factorySetOptionUnknownOption() {
		foreachBoolConfig(directed -> {
			GraphFactory<String, String> factory = GraphFactory.newInstance(directed);
			assertThrows(IllegalArgumentException.class, () -> factory.setOption("unknown-option", "value"));
		});
	}

}
