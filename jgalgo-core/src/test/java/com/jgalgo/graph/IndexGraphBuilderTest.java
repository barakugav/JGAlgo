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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

public class IndexGraphBuilderTest extends TestBase {

	@Test
	public void isDirected() {
		assertTrue(IndexGraphBuilder.directed().isDirected());
		assertFalse(IndexGraphBuilder.undirected().isDirected());
	}

	@Test
	public void newCopyOf() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);

			/* With weights */
			IndexGraphBuilder b2 = IndexGraphBuilder.newCopyOf(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			IndexGraphBuilder b1 = IndexGraphBuilder.newCopyOf(g);
			assertEqualsBool(directed, b1.isDirected());
			assertEquals(g.copy(/* no weights */), b1.build());
		});
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			for (int v = 0; v < 10; v++)
				b.addVertex();
			assertEquals(range(10), b.vertices());
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addVertexUserProvidedId() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			for (int i = 0; i < 20; i++) {
				if (i % 2 == 0) {
					b.addVertex(b.vertices().size());
				} else {
					assertThrows(IllegalArgumentException.class, () -> b.addVertex(b.vertices().size() * 2 + 7));
				}
			}
		});
	}

	@Test
	void addVertices() {
		final Random rand = new Random(0x7b329816727ad7e8L);

		/* addVertices() from range() */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					g.addVertices(range(verticesNum, verticesNum + num));
					verticesNum += num;
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from range() sometimes invalid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					if (num == 0 || rand.nextBoolean()) {
						g.addVertices(range(verticesNum, verticesNum + num));
						verticesNum += num;
					} else {
						int from = verticesNum + 1;
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(range(from, from + num)));
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from other graph vertices */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					IndexGraph g0 = IndexGraph.newUndirected();
					g0.addVertices(range(num));

					if (num == 0 || verticesNum == 0) {
						g.addVertices(g0.vertices());
						verticesNum += num;
					} else if (rand.nextBoolean()) {
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(g0.vertices()));
					} else {
						g.addVertices(range(verticesNum, verticesNum + num));
						verticesNum += num;
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from sorted list */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					List<Integer> vs = new IntArrayList(range(verticesNum, verticesNum + num).iterator());
					g.addVertices(vs);
					verticesNum += num;
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from arbitrary collection */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					int[] vs = range(verticesNum, verticesNum + num).toIntArray();
					IntArrays.shuffle(vs, rand);
					g.addVertices(IntList.of(vs));
					verticesNum += num;
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from arbitrary collection duplicate vertex (in list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					int[] vs = range(verticesNum, verticesNum + num).toIntArray();
					IntArrays.shuffle(vs, rand);
					if (vs.length == 0 || rand.nextBoolean()) {
						g.addVertices(IntList.of(vs));
						verticesNum += num;
					} else {
						IntList vs0 = new IntArrayList(vs);
						vs0.add(vs[rand.nextInt(vs.length)]); /* duplicate element */
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from arbitrary collection with existing vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					int[] vs = range(verticesNum, verticesNum + num).toIntArray();
					IntArrays.shuffle(vs, rand);
					if (verticesNum == 0 || rand.nextBoolean()) {
						g.addVertices(IntList.of(vs));
						verticesNum += num;
					} else {
						IntList vs0 = new IntArrayList(vs);
						vs0.add(rand.nextInt(g.vertices().size()));
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() from arbitrary collection not in range */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraphBuilder g = IndexGraphBuilder.newInstance(directed);

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					int[] vs = range(verticesNum, verticesNum + num).toIntArray();
					IntArrays.shuffle(vs, rand);
					if (rand.nextBoolean()) {
						g.addVertices(IntList.of(vs));
						verticesNum += num;
					} else if (rand.nextBoolean()) {
						IntList vs0 = new IntArrayList(vs);
						vs0.add(verticesNum - 1);
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					} else {
						IntList vs0 = new IntArrayList(vs);
						vs0.add(verticesNum + num + 1);
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});
	}

	@Test
	public void addEdge() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.ensureVertexCapacity(g.vertices().size());
			b.addVertices(g.vertices());
			b.ensureEdgeCapacity(g.edges().size());
			for (int m = g.edges().size(), e = 0; e < m; e++)
				b.addEdge(g.edgeSource(e), g.edgeTarget(e));

			assertEquals(g.vertices(), b.vertices());
			assertEquals(g.edges(), b.edges());
			assertEquals(g.copy(/* no weights */), b.build());
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addEdgeUserProvidedId() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(g.vertices());
			for (int i = 0; i < 20; i++) {
				int e = b.edges().size();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (i % 2 == 0) {
					b.addEdge(u, v, e);
				} else {
					assertThrows(IllegalArgumentException.class, () -> b.addEdge(u, v, b.edges().size() * 2 + 7));
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void addEdgeInvalidEndpoints() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10, 0));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdges() {
		foreachBoolConfig((directed, fromIntGraph) -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(50));

			Graph<Integer, Integer> g1 =
					(fromIntGraph ? IntGraphFactory.directed() : GraphFactory.<Integer, Integer>directed()).newGraph();
			g1.addVertices(range(50));
			g1.addEdge(2, 3, 0);
			g1.addEdge(3, 4, 1);
			b.addEdges(EdgeSet.allOf(g1));

			Graph<Integer, Integer> g2 =
					(fromIntGraph ? IntGraphFactory.directed() : GraphFactory.<Integer, Integer>directed()).newGraph();
			g2.addVertices(range(50));
			g2.addEdge(0, 1, 2);
			g2.addEdge(1, 2, 3);
			b.addEdges(EdgeSet.allOf(g2));

			IntGraph g = b.build();
			IntGraph g0 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			g0.addVertices(range(50));
			g0.addEdge(2, 3, 0);
			g0.addEdge(3, 4, 1);
			g0.addEdge(0, 1, 2);
			g0.addEdge(1, 2, 3);
			assertEquals(g0, g);
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgesUnsorted() {
		final Random rand = new Random(0x61da880dcc080dbbL);
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
				b.addVertices(range(50));

				IntList ids = IntList.of(0, 2, 1);
				List<Pair<Integer, Integer>> endpoints = List.of(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3));
				EdgeSet<Integer, Integer> edges = GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);

				b.addEdges(edges);
				IndexGraph g = b.build();
				assertEquals(edges, g.edges());
				assertEquals(0, g.edgeSource(0));
				assertEquals(1, g.edgeTarget(0));
				assertEquals(2, g.edgeSource(1));
				assertEquals(3, g.edgeTarget(1));
				assertEquals(1, g.edgeSource(2));
				assertEquals(2, g.edgeTarget(2));
			}
		});
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
				b.addVertices(range(50));

				IntList ids = new IntArrayList(IntList.of(0, 2, -1));
				Collections.shuffle(ids, rand);
				List<Pair<Integer, Integer>> endpoints = List.of(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3));
				EdgeSet<Integer, Integer> edges = GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);

				assertThrows(IllegalArgumentException.class, () -> b.addEdges(edges));
			}
		});
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
				b.addVertices(range(50));

				IntList ids = new IntArrayList(IntList.of(1, 2, 3));
				Collections.shuffle(ids, rand);
				List<Pair<Integer, Integer>> endpoints = List.of(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3));
				EdgeSet<Integer, Integer> edges = GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);

				assertThrows(IllegalArgumentException.class, () -> b.addEdges(edges));
			}
		});
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
				b.addVertices(range(50));

				IntList ids = new IntArrayList(IntList.of(0, 2, 2));
				Collections.shuffle(ids, rand);
				List<Pair<Integer, Integer>> endpoints = List.of(Pair.of(0, 1), Pair.of(1, 2), Pair.of(2, 3));
				EdgeSet<Integer, Integer> edges = GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);

				assertThrows(IllegalArgumentException.class, () -> b.addEdges(edges));
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgesInvalidEndpoint() {
		final Random rand = new Random(0x1dd1bcdbebf137b4L);
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
				b.addVertices(range(1));

				IntList ids = new IntArrayList(IntList.of(1, 0));
				List<Pair<Integer, Integer>> endpoints = new ArrayList<>(List.of(Pair.of(0, 1), Pair.of(1, 0)));
				Collections.shuffle(ids, rand);
				Collections.shuffle(endpoints, rand);
				EdgeSet<Integer, Integer> edges = GraphImplTestUtils.edgeSetFromList(ids, endpoints, rand);

				assertThrows(NoSuchVertexException.class, () -> b.addEdges(edges));
			}
		});
	}

	@Test
	public void addEdgesReassignIds() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(50));

			IntGraph g1 = IntGraph.newDirected();
			g1.addVertices(range(50));
			g1.addEdge(2, 3, 28946351);
			g1.addEdge(3, 4, 11);
			b.addEdgesReassignIds(IEdgeSet.allOf(g1));

			IntGraph g2 = IntGraph.newDirected();
			g2.addVertices(range(50));
			g2.addEdge(0, 1, 186);
			g2.addEdge(1, 2, 643);
			b.addEdgesReassignIds(IEdgeSet.allOf(g2));

			IntGraph g = b.build();
			IntGraph g0 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
			g0.addVertices(range(50));
			g0.addEdge(2, 3, 0);
			g0.addEdge(3, 4, 1);
			g0.addEdge(0, 1, 2);
			g0.addEdge(1, 2, 3);
			assertEquals(g0, g);
		});
	}

	@Test
	public void addEdgesReassignIdsInvalidEndpoint() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(1));

			IntGraph g1 = IntGraph.newDirected();
			g1.addVertices(range(50));
			g1.addEdge(0, 1, 0);
			assertThrows(NoSuchVertexException.class, () -> b.addEdgesReassignIds(IEdgeSet.allOf(g1)));
		});
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(1));

			IntGraph g1 = IntGraph.newDirected();
			g1.addVertices(range(50));
			g1.addEdge(1, 0, 831564);
			assertThrows(NoSuchVertexException.class, () -> b.addEdgesReassignIds(IEdgeSet.allOf(g1)));
		});
	}

	@Test
	public void clear() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(g.vertices());
			b.addEdges(EdgeSet.allOf(g));

			b.clear();
			assertEquals(IntSets.emptySet(), b.vertices());
			assertEquals(IntSets.emptySet(), b.edges());

			g.clear();
			assertEquals(g, b.build());
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void build() {
		final long seed = 0x56f68a18a0ca8d84L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut, selfEdges) -> {
			IndexGraphFactory factory =
					IndexGraphFactory.newInstance(directed).allowSelfEdges(selfEdges).allowParallelEdges();
			IndexGraphBuilder b = factory.newBuilder();
			IndexGraph g = factory.newGraph();

			assertEquals(IntSets.emptySet(), b.edges());
			assertEquals(IntSets.emptySet(), b.vertices());

			/* Add vertices and edges */
			final int n = 12 + rand.nextInt(12);
			final int m = 20 + rand.nextInt(20);
			while (g.vertices().size() < n) {
				int vG = g.addVertexInt();
				int vB = b.addVertex();
				assertEquals(vG, vB);
			}
			while (g.edges().size() < m) {
				int e = rand.nextInt(2 * m);
				if (g.edges().contains(e))
					continue;
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!selfEdges && u == v)
					continue;
				g.addEdge(u, v);
			}
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				b.addEdge(u, v);
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
		});
	}

	@Test
	public void reIndexAndBuild() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			IndexGraphBuilder b = factory.allowSelfEdges().allowParallelEdges().newBuilder();
			b.ensureVertexCapacity(g.vertices().size());
			b.ensureVertexCapacity(g.vertices().size());
			b.ensureEdgeCapacity(g.edges().size());
			b.ensureEdgeCapacity(g.edges().size());
			b.addVertices(g.vertices());
			b.addEdges(EdgeSet.allOf(g));
			for (String key : g.getVerticesWeightsKeys()) {
				IWeightsInt w = g.getVerticesWeights(key);
				IWeightsInt bw = b.addVerticesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
				for (int v : g.vertices())
					bw.set(v, w.get(v));
			}
			for (String key : g.getEdgesWeightsKeys()) {
				IWeightsInt w = g.getEdgesWeights(key);
				IWeightsInt bw = b.addEdgesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
				for (int e : g.edges())
					bw.set(e, w.get(e));
			}

			foreachBoolConfig((mutable, reIndexVertices, reIndexEdges) -> {
				IndexGraphBuilder.ReIndexedGraph gReIndexed0 =
						mutable ? b.reIndexAndBuildMutable(reIndexVertices, reIndexEdges)
								: b.reIndexAndBuild(reIndexVertices, reIndexEdges);

				IntUnaryOperator vOrigToReIndexed =
						gReIndexed0.verticesReIndexing().<IntUnaryOperator>map(m -> m::origToReIndexed).orElse(v -> v);
				IntUnaryOperator vReIndexedToOrig =
						gReIndexed0.verticesReIndexing().<IntUnaryOperator>map(m -> m::reIndexedToOrig).orElse(v -> v);
				IntUnaryOperator eOrigToReIndexed =
						gReIndexed0.edgesReIndexing().<IntUnaryOperator>map(m -> m::origToReIndexed).orElse(e -> e);
				IntUnaryOperator eReIndexedToOrig =
						gReIndexed0.edgesReIndexing().<IntUnaryOperator>map(m -> m::reIndexedToOrig).orElse(e -> e);

				IndexGraph gReIndexed = gReIndexed0.graph();
				for (int v : g.vertices())
					assertEquals(v, vReIndexedToOrig.applyAsInt(vOrigToReIndexed.applyAsInt(v)));
				for (int v : gReIndexed.vertices())
					assertEquals(v, vOrigToReIndexed.applyAsInt(vReIndexedToOrig.applyAsInt(v)));
				for (int e : g.edges())
					assertEquals(e, eReIndexedToOrig.applyAsInt(eOrigToReIndexed.applyAsInt(e)));
				for (int e : gReIndexed.edges())
					assertEquals(e, eOrigToReIndexed.applyAsInt(eReIndexedToOrig.applyAsInt(e)));

				IndexGraph gReIndexedExpected = factory.newGraph();
				gReIndexedExpected.addVertices(g.vertices());
				for (int m = gReIndexed.edges().size(), e = 0; e < m; e++) {
					int eOrig = eReIndexedToOrig.applyAsInt(e);
					int uOrig = g.edgeSource(eOrig), vOrig = g.edgeTarget(eOrig);
					int u = vOrigToReIndexed.applyAsInt(uOrig), v = vOrigToReIndexed.applyAsInt(vOrig);
					gReIndexedExpected.addEdge(u, v);
				}
				for (String key : g.getVerticesWeightsKeys()) {
					IWeightsInt w = g.getVerticesWeights(key);
					IWeightsInt wReIndexed =
							gReIndexedExpected.addVerticesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
					for (int v : gReIndexed.vertices())
						wReIndexed.set(v, w.get(vReIndexedToOrig.applyAsInt(v)));
				}
				for (String key : g.getEdgesWeightsKeys()) {
					IWeightsInt w = g.getEdgesWeights(key);
					IWeightsInt wReIndexed =
							gReIndexedExpected.addEdgesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
					for (int e : gReIndexed.edges())
						wReIndexed.set(e, w.get(eReIndexedToOrig.applyAsInt(e)));
				}

				assertEquals(gReIndexedExpected, gReIndexed);

				if (mutable) {
					int v = gReIndexed.addVertexInt();
					assertTrue(v >= 0);
				} else {
					assertThrows(UnsupportedOperationException.class, () -> gReIndexed.addVertexInt());
				}
			});
		});
	}

	private static IndexGraph createGraph(boolean directed) {
		final long seed = 0xa636ca816d4202c9L;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		IndexGraph g = IndexGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();

		IWeightsInt vWeights = g.addVerticesWeights("weights", int.class);
		g.addVertices(range(n));
		for (int v : g.vertices())
			vWeights.set(v, rand.nextInt(10000));

		IWeightsInt eWeights = g.addEdgesWeights("weights", int.class);
		for (int e = 0; e < m; e++) {
			int eId = g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand));
			eWeights.set(eId, rand.nextInt(10000));
		}
		return g;
	}

	@SuppressWarnings("boxing")
	@Test
	public void newBuilderCopyOfGraph() {
		foreachBoolConfig((directed, intGraph, index) -> {
			IndexGraph ig = createGraph(directed);
			GraphFactory<Integer, Integer> factory;
			if (index) {
				factory = IndexGraphFactory.newInstance(directed);
			} else if (intGraph) {
				factory = IntGraphFactory.newInstance(directed);
			} else {
				factory = GraphFactory.newInstance(directed);
			}
			Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();
			g.addVertices(ig.vertices());
			g.addEdges(EdgeSet.allOf(ig));

			WeightsByte<Integer> vByteWeights = g.addVerticesWeights("vByteWeights", byte.class);
			WeightsShort<Integer> vShortWeights = g.addVerticesWeights("vShortWeights", short.class);
			WeightsInt<Integer> vIntWeights = g.addVerticesWeights("vIntWeights", int.class);
			WeightsLong<Integer> vLongWeights = g.addVerticesWeights("vLongWeights", long.class);
			WeightsFloat<Integer> vFloatWeights = g.addVerticesWeights("vFloatWeights", float.class);
			WeightsDouble<Integer> vDoubleWeights = g.addVerticesWeights("vDoubleWeights", double.class);
			WeightsBool<Integer> vBoolWeights = g.addVerticesWeights("vBoolWeights", boolean.class);
			WeightsChar<Integer> vCharWeights = g.addVerticesWeights("vCharWeights", char.class);
			WeightsObj<Integer, String> vStringWeights = g.addVerticesWeights("vStringWeights", String.class);
			for (int v : g.vertices()) {
				vByteWeights.set(v, (byte) v);
				vShortWeights.set(v, (short) v);
				vIntWeights.set(v, v);
				vLongWeights.set(v, v);
				vFloatWeights.set(v, v);
				vDoubleWeights.set(v, v);
				vBoolWeights.set(v, v % 2 == 0);
				vCharWeights.set(v, (char) v);
				vStringWeights.set(v, String.valueOf(v));
			}
			WeightsByte<Integer> eByteWeights = g.addEdgesWeights("eByteWeights", byte.class);
			WeightsShort<Integer> eShortWeights = g.addEdgesWeights("eShortWeights", short.class);
			WeightsInt<Integer> eIntWeights = g.addEdgesWeights("eIntWeights", int.class);
			WeightsLong<Integer> eLongWeights = g.addEdgesWeights("eLongWeights", long.class);
			WeightsFloat<Integer> eFloatWeights = g.addEdgesWeights("eFloatWeights", float.class);
			WeightsDouble<Integer> eDoubleWeights = g.addEdgesWeights("eDoubleWeights", double.class);
			WeightsBool<Integer> eBoolWeights = g.addEdgesWeights("eBoolWeights", boolean.class);
			WeightsChar<Integer> eCharWeights = g.addEdgesWeights("eCharWeights", char.class);
			WeightsObj<Integer, String> eStringWeights = g.addEdgesWeights("eStringWeights", String.class);
			for (int e : g.edges()) {
				eByteWeights.set(e, (byte) e);
				eShortWeights.set(e, (short) e);
				eIntWeights.set(e, e);
				eLongWeights.set(e, e);
				eFloatWeights.set(e, e);
				eDoubleWeights.set(e, e);
				eBoolWeights.set(e, e % 2 == 0);
				eCharWeights.set(e, (char) e);
				eStringWeights.set(e, String.valueOf(e));
			}

			foreachBoolConfig((copyVerticesWeights, copyEdgesWeights) -> {
				IndexGraphFactory builderFactory =
						IndexGraphFactory.newInstance(g.isDirected()).allowSelfEdges().allowParallelEdges();
				IndexGraphBuilder b;
				if (!copyEdgesWeights && !copyVerticesWeights) {
					b = builderFactory.newBuilderCopyOf(g);
				} else {
					b = builderFactory.newBuilderCopyOf(g, copyVerticesWeights, copyEdgesWeights);
				}
				IndexGraph bg = b.build();
				if (copyVerticesWeights) {
					assertEquals(
							Set.of("vByteWeights", "vShortWeights", "vIntWeights", "vLongWeights", "vFloatWeights",
									"vDoubleWeights", "vBoolWeights", "vCharWeights", "vStringWeights"),
							bg.getVerticesWeightsKeys());
				} else {
					assertEquals(Set.of(), bg.getVerticesWeightsKeys());
				}
				if (copyEdgesWeights) {
					assertEquals(
							Set.of("eByteWeights", "eShortWeights", "eIntWeights", "eLongWeights", "eFloatWeights",
									"eDoubleWeights", "eBoolWeights", "eCharWeights", "eStringWeights"),
							bg.getEdgesWeightsKeys());
				} else {
					assertEquals(Set.of(), bg.getEdgesWeightsKeys());
				}
				assertEquals(g.copy(copyVerticesWeights, copyEdgesWeights), bg);
			});
		});
		foreachBoolConfig((directed, intGraph) -> {
			GraphFactory<Integer, Integer> factory;
			if (intGraph) {
				factory = IntGraphFactory.newInstance(directed);
			} else {
				factory = GraphFactory.newInstance(directed);
			}
			Graph<Integer, Integer> g = factory.newGraph();
			g.addVertices(IntList.of(0, 2));
			assertThrows(IllegalArgumentException.class,
					() -> IndexGraphFactory.newInstance(directed).newBuilderCopyOf(g));
		});
		foreachBoolConfig((directed, intGraph) -> {
			GraphFactory<Integer, Integer> factory;
			if (intGraph) {
				factory = IntGraphFactory.newInstance(directed);
			} else {
				factory = GraphFactory.newInstance(directed);
			}
			Graph<Integer, Integer> g = factory.newGraph();
			g.addVertices(IntList.of(0, 1));
			g.addEdge(0, 1, 3);
			assertThrows(IllegalArgumentException.class,
					() -> IndexGraphFactory.newInstance(directed).newBuilderCopyOf(g));
		});
	}

	@SuppressWarnings({ "deprecation", "boxing" })
	@Test
	public void factorySetVertexBuilder() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setVertexBuilder(ids -> ids.size() * 2));
		});
	}

	@SuppressWarnings({ "deprecation", "boxing" })
	@Test
	public void factorySetEdgeBuilder() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setEdgeBuilder(ids -> ids.size() * 2));
		});
	}

	@Test
	public void factorySetOptionUnknownOption() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(IllegalArgumentException.class, () -> factory.setOption("unknown-option", "value"));
		});
	}

}
