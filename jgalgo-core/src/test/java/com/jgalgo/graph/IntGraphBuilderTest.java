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
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntGraphBuilderTest extends TestBase {

	@Test
	public void isDirected() {
		assertTrue(IntGraphBuilder.directed().isDirected());
		assertFalse(IntGraphBuilder.undirected().isDirected());
	}

	@Test
	public void newCopyOf() {
		foreachBoolConfig(directed -> {
			IntGraph g = createGraph(directed);

			/* With weights */
			IntGraphBuilder b2 = IntGraphBuilder.newCopyOf(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			IntGraphBuilder b1 = IntGraphBuilder.newCopyOf(g);
			assertEqualsBool(directed, b1.isDirected());
			assertEquals(g.copy(/* no weights */), b1.build());
		});
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			IntSet vertices = new IntOpenHashSet();
			for (int i = 0; i < 15; i++)
				assertTrue(vertices.add(b.addVertexInt()));
			assertEquals(vertices, b.vertices());
		});
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			IntSet vertices = new IntOpenHashSet();
			for (int i = 0; i < 15; i++) {
				@SuppressWarnings("deprecation")
				int v = b.addVertex().intValue();
				assertTrue(vertices.add(v));
			}
			assertEquals(vertices, b.vertices());
		});
		foreachBoolConfig(directed -> {
			IntGraphFactory factory = IntGraphFactory.newInstance(directed);
			factory.setVertexFactory(IdBuilder.defaultFactory(Integer.class));
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			Integer v1 = b.addVertex();
			Integer v2 = b.addVertex();
			Integer v3 = b.addVertex();
			assertEquals(Set.of(v1, v2, v3), b.build().vertices());
		});
		foreachBoolConfig(directed -> {
			IntGraphFactory factory = IntGraphFactory.newInstance(directed);
			factory.setVertexFactory(null);
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			assertThrows(UnsupportedOperationException.class, () -> b.addVertex());
		});
	}

	@Test
	public void addVertexUserProvidedId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			IntList vertices = IntList.of(48, 84, 66, 91, 3, 7);
			for (int v : vertices)
				b.addVertex(v);
			assertEquals(new IntOpenHashSet(vertices), b.vertices());
		});
	}

	@Test
	public void addVertexDuplicateId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			b.addVertex(5);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(5));
		});
	}

	@Test
	public void addVertexNegative() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(-1));
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addVertexNull() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			assertThrows(NullPointerException.class, () -> b.addVertex(null));
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0xf58228fb5d6c94eeL);
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);

			IntSet vertices = new IntOpenHashSet();
			IntList verticesList = new IntArrayList();
			for (int r = 0; r < 50; r++) {
				int num = rand.nextInt(5);
				IntList vs = new IntArrayList();
				while (vs.size() < num) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v) || vs.contains(v))
						continue;
					vs.add(v);
				}
				if (r % 5 == 0) {
					b.addVertices(rand.nextBoolean() ? vs : new ArrayList<>(vs));
					vertices.addAll(vs);
					verticesList.addAll(vs);
				} else if (r % 5 == 1) {
					vs.add(-1);
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> b.addVertices(vs));
				} else if (r % 5 == 2 && vs.size() > 0) {
					vs.add(randElement(vs, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> b.addVertices(vs));
				} else if (r % 5 == 3 && vertices.size() > 0) {
					vs.add(randElement(verticesList, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> b.addVertices(vs));
				} else if (r % 5 == 4) {
					List<Integer> vs0 = new ArrayList<>(vs);
					vs0.add(null);
					Collections.shuffle(vs0, rand);
					assertThrows(NullPointerException.class, () -> b.addVertices(vs0));
				}
				assertEquals(vertices, b.vertices());
			}
		});
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			b.addVertexInt();
			assertThrows(IllegalArgumentException.class, () -> b.addVertices(IntList.of(1, 2)));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdge() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			b.ensureVertexCapacity(10);
			b.ensureEdgeCapacity(3);
			range(10).forEach(b::addVertex);
			int e1 = b.addEdge(0, 1);
			int e2 = b.addEdge(0, 2);
			int e3 = b.addEdge(0, 3);
			assertEquals(IntSet.of(e1, e2, e3), b.edges());
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = IntGraphFactory.newInstance(directed);
			factory.setEdgeFactory(IdBuilder.defaultFactory(Integer.class));
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			b.addVertices(range(10));
			Integer e1 = b.addEdge(0, 1);
			Integer e2 = b.addEdge(1, 2);
			Integer e3 = b.addEdge(2, 3);
			assertEquals(Set.of(e1, e2, e3), b.build().edges());
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = IntGraphFactory.newInstance(directed);
			factory.setEdgeFactory(null);
			GraphBuilder<Integer, Integer> b = factory.newBuilder();
			b.addVertices(range(10));
			assertThrows(UnsupportedOperationException.class, () -> b.addEdge(0, 1));
		});
	}

	@Test
	public void addEdgeUserProvidedIds() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			b.ensureVertexCapacity(10);
			b.ensureEdgeCapacity(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			assertEquals(range(3), b.edges());
		});
	}

	@Test
	public void addEdgeDuplicateId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 77);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(2, 3, 77));
		});
	}

	@Test
	public void addEdgeInvalidEndpoints() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1, 1));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0, 2));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10, 3));
		});
	}

	@Test
	public void addEdgeNegativeId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(1, 0, -1));
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
			IntGraphBuilder b =
					IntGraphFactory.newInstance(directed).allowSelfEdges(true).allowParallelEdges(true).newBuilder();
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

				if (r % 5 == 0) {
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					b.addEdges(es);
					addEdges.accept(edges, es);
					edgesList.addAll(es);
				} else if (r % 5 == 1) {
					esList.add(Pair.of(null, Pair.of(rand.nextInt(1000), rand.nextInt(1000))));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(NullPointerException.class, () -> b.addEdges(es));
				} else if (r % 5 == 2 && esList.size() > 0) {
					esList.add(randElement(esList, rand));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(IllegalArgumentException.class, () -> b.addEdges(es));
				} else if (r % 5 == 3 && edgesList.size() > 0) {
					Integer dupEdge = randElement(edgesList, rand);
					esList.add(Pair.of(dupEdge, edges.get(dupEdge)));
					Collections.shuffle(esList, rand);
					EdgeSet<Integer, Integer> es = toEdgeSet.apply(esList);
					assertThrows(IllegalArgumentException.class, () -> b.addEdges(es));
				} else if (r % 5 == 4) {
					esList.add(Pair.of(-1, Pair.of(rand.nextInt(1000), rand.nextInt(1000))));
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

	@Test
	public void clear() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			b.addVerticesWeights("weights", int.class);
			b.addEdgesWeights("weights", int.class);
			b.clear();

			assertEquals(Set.of(), b.vertices());
			assertEquals(Set.of(), b.edges());
			assertEquals(Set.of(), b.getVerticesWeightsKeys());
			assertEquals(Set.of(), b.getEdgesWeightsKeys());

			IntGraph g = b.build();
			assertEquals(Set.of(), g.vertices());
			assertEquals(Set.of(), g.edges());
			assertEquals(Set.of(), g.getVerticesWeightsKeys());
			assertEquals(Set.of(), g.getEdgesWeightsKeys());
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void build() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut) -> {
			List<String> impls = new ArrayList<>();
			impls.add("array");
			impls.add("array-selfedges");
			impls.add("linked-list");
			impls.add("linked-list-selfedges");
			impls.add("linked-list-ptr");
			impls.add("linked-list-ptr-selfedges");
			impls.add("hashtable");
			impls.add("hashtable-selfedges");
			impls.add("hashtable-multi");
			impls.add("hashtable-multi-selfedges");
			impls.add("matrix");
			impls.add("matrix-selfedges");
			for (String impl : impls) {
				IntGraphFactory factory = IntGraphFactory.newInstance(directed).setOption("impl", impl);
				IntGraphBuilder b = factory.newBuilder();
				boolean selfEdges = factory.newGraph().isAllowSelfEdges();
				IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges(selfEdges).newGraph();

				/* Add vertices and edges */
				final int n = 12 + rand.nextInt(12);
				final int m = 20 + rand.nextInt(20);
				while (g.vertices().size() < n) {
					int v = rand.nextInt(2 * n);
					if (g.vertices().contains(v))
						continue;
					g.addVertex(v);
					b.addVertex(v);
				}

				/* duplicate vertex */
				assertThrows(IllegalArgumentException.class, () -> b.addVertex(Graphs.randVertex(g, rand)));

				while (g.edges().size() < m) {
					int e = rand.nextInt(2 * m);
					if (g.edges().contains(e))
						continue;
					int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
					if (!selfEdges && u == v)
						continue;
					if (g.containsEdge(u, v))
						continue; /* avoid parallel edges */
					g.addEdge(u, v, e);
					b.addEdge(u, v, e);
				}

				/* duplicate edge */
				assertThrows(IllegalArgumentException.class, () -> b
						.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), Graphs.randEdge(g, rand)));
				/* non existing endpoints */
				for (;;) {
					int v = rand.nextInt(2 * n);
					int e = rand.nextInt(2 * m);
					if (!g.vertices().contains(v) && !g.edges().contains(e)) {
						int u = Graphs.randVertex(g, rand);
						assertThrows(NoSuchVertexException.class, () -> b.addEdge(u, v, e));
						assertThrows(NoSuchVertexException.class, () -> b.addEdge(v, u, e));
						break;
					}
				}

				/* Add weights */
				AtomicInteger weightIdx = new AtomicInteger();
				@SuppressWarnings("rawtypes")
				BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
					for (boolean edgesWeights : BooleanList.of(false, true)) {
						for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
							String key = "weight" + weightIdx.getAndIncrement();
							Object defVal = valSupplier.get();
							IWeights wG, wB;
							IntSet elements;
							if (!edgesWeights) {
								wG = (IWeights) g.addVerticesWeights(key, type, defVal);
								wB = (IWeights) b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = (IWeights) g.addEdgesWeights(key, type, defVal);
								wB = (IWeights) b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (int elm : elements) {
								Object w = valSupplier.get();
								wG.setAsObj(elm, w);
								wB.setAsObj(elm, w);
							}
						}
					}
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

				assertEquals(g.getVerticesWeightsKeys(), b.getVerticesWeightsKeys());
				assertEquals(g.getEdgesWeightsKeys(), b.getEdgesWeightsKeys());
				assertNull(b.getVerticesWeights("dashpauht"));
				assertNull(b.getEdgesWeights("asdjeea"));

				IntGraph gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				assertEquals(b.getVerticesWeightsKeys(), gActual.getVerticesWeightsKeys());
				assertEquals(b.getEdgesWeightsKeys(), gActual.getEdgesWeightsKeys());

				for (String key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeightAsObj(),
							gActual.getVerticesWeights(key).defaultWeightAsObj());
				for (String key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeightAsObj(),
							gActual.getEdgesWeights(key).defaultWeightAsObj());

				if (!buildMut) {
					for (String key : gActual.getVerticesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getVerticesWeights(key);
						int v = Graphs.randVertex(gActual, rand);
						Object data = w.getAsObj(Integer.valueOf(Graphs.randVertex(gActual, rand)));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(Integer.valueOf(v), data));
					}
					for (String key : gActual.getEdgesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getEdgesWeights(key);
						int e = Graphs.randEdge(gActual, rand);
						Object data = w.getAsObj(Integer.valueOf(Graphs.randEdge(gActual, rand)));
						assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(Integer.valueOf(e), data));
					}
				}
			}
		});
	}

	private static IntGraph createGraph(boolean directed) {
		final long seed = 0xa636ca816d4202c9L;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		IntGraphFactory factory = IntGraphFactory.newInstance(directed);
		IntGraph g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		IWeightsInt vWeights = g.addVerticesWeights("weights", int.class);
		g.addVertices(range(n));
		for (int v : g.vertices())
			vWeights.set(v, rand.nextInt(10000));

		IWeightsInt eWeights = g.addEdgesWeights("weights", int.class);
		for (int e = 0; e < m; e++) {
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

}
