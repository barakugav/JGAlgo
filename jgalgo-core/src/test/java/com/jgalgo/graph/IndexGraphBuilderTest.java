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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
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
	public void fromGraph() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);

			/* With weights */
			IndexGraphBuilder b2 = IndexGraphBuilder.fromGraph(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			IndexGraphBuilder b1 = IndexGraphBuilder.fromGraph(g);
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
	static void addVertices() {
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
					if (rand.nextBoolean()) {
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

					if (verticesNum == 0) {
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
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					} else {
						IntList vs0 = new IntArrayList(vs);
						vs0.add(verticesNum + num + 1);
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
			b.expectedVerticesNum(g.vertices().size());
			b.addVertices(g.vertices());
			b.expectedEdgesNum(g.edges().size());
			for (int m = g.edges().size(), e = 0; e < m; e++)
				b.addEdge(g.edgeSource(e), g.edgeTarget(e));

			assertEquals(g.vertices(), b.vertices());
			assertEquals(g.edges(), b.edges());
			assertEquals(g.copy(/* no weights */), b.build());
		});
	}

	@Test
	public void addEdgeUserProvidedIds() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(g.vertices());
			int[] es = g.edges().toIntArray();
			IntArrays.shuffle(es, new Random(0x23138df96ec3b620L));
			for (int e : es)
				b.addEdge(g.edgeSource(e), g.edgeTarget(e), e);

			assertEquals(g.vertices(), b.vertices());
			assertEquals(g.edges(), b.edges());
			assertEquals(g.copy(/* no weights */), b.build());
		});

		/* missing edge 0 */
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			b.addEdge(0, 1, 1);
			b.addEdge(0, 2, 2);
			assertThrows(IllegalArgumentException.class, () -> b.build());
		});

		/* missing edge 1 */
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 2);
			assertThrows(IllegalArgumentException.class, () -> b.build());
		});

		/* duplicate edge 0 */
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 0);
			assertThrows(IllegalArgumentException.class, () -> b.build());
		});
	}

	@Test
	public void addEdgeNegativeId() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(range(10));
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(0, 1, -1));
		});
	}

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

	@Test
	public void addEdgeMixUserIdsAndImplIds() {
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			int v1 = b.addVertex();
			int v2 = b.addVertex();
			int v3 = b.addVertex();
			int v4 = b.addVertex();

			b.addEdge(v1, v2);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(v3, v4, 37));
		});
		foreachBoolConfig(directed -> {
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			int v1 = b.addVertex();
			int v2 = b.addVertex();
			int v3 = b.addVertex();
			int v4 = b.addVertex();

			b.addEdge(v1, v2, 37);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(v3, v4));
		});
	}

	@Test
	public void clear() {
		foreachBoolConfig(directed -> {
			IndexGraph g = createGraph(directed);
			IndexGraphBuilder b = IndexGraphBuilder.newInstance(directed);
			b.addVertices(g.vertices());
			for (int m = g.edges().size(), e = 0; e < m; e++)
				b.addEdge(g.edgeSource(e), g.edgeTarget(e));

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
				int vG = g.addVertex();
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
			for (int e : IntArrays.shuffle(g.edges().toIntArray(), rand)) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				b.addEdge(u, v, e);
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
			b.expectedVerticesNum(g.vertices().size());
			b.expectedVerticesNum(g.vertices().size());
			b.expectedEdgesNum(g.edges().size());
			b.expectedEdgesNum(g.edges().size());
			b.addVertices(g.vertices());
			for (int m = g.edges().size(), e = 0; e < m; e++)
				b.addEdge(g.edgeSource(e), g.edgeTarget(e));
			for (String key : g.getVerticesWeightsKeys()) {
				IWeightsInt w = g.getVerticesIWeights(key);
				IWeightsInt bw = b.addVerticesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
				for (int v : g.vertices())
					bw.set(v, w.get(v));
			}
			for (String key : g.getEdgesWeightsKeys()) {
				IWeightsInt w = g.getEdgesIWeights(key);
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
					IWeightsInt w = g.getVerticesIWeights(key);
					IWeightsInt wReIndexed =
							gReIndexedExpected.addVerticesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
					for (int v : gReIndexed.vertices())
						wReIndexed.set(v, w.get(vReIndexedToOrig.applyAsInt(v)));
				}
				for (String key : g.getEdgesWeightsKeys()) {
					IWeightsInt w = g.getEdgesIWeights(key);
					IWeightsInt wReIndexed =
							gReIndexedExpected.addEdgesWeights(key, int.class, Integer.valueOf(w.defaultWeight()));
					for (int e : gReIndexed.edges())
						wReIndexed.set(e, w.get(eReIndexedToOrig.applyAsInt(e)));
				}

				assertEquals(gReIndexedExpected, gReIndexed);

				if (mutable) {
					int v = gReIndexed.addVertex();
					assertTrue(v >= 0);
				} else {
					assertThrows(UnsupportedOperationException.class, () -> gReIndexed.addVertex());
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

}
