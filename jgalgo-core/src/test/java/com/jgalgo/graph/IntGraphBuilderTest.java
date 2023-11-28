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
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntGraphBuilderTest extends TestBase {

	@Test
	public void isDirected() {
		assertTrue(IntGraphBuilder.newDirected().isDirected());
		assertFalse(IntGraphBuilder.newUndirected().isDirected());
	}

	@Test
	public void fromGraph() {
		foreachBoolConfig(directed -> {
			IntGraph g = createGraph(directed);

			/* With weights */
			IntGraphBuilder b2 = IntGraphBuilder.fromGraph(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			IntGraphBuilder b1 = IntGraphBuilder.fromGraph(g);
			assertEqualsBool(directed, b1.isDirected());
			assertEquals(g.copy(/* no weights */), b1.build());
		});
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			IntSet vertices = new IntOpenHashSet();
			for (int i = 0; i < 15; i++)
				assertTrue(vertices.add(b.addVertex()));;
			assertEquals(vertices, b.vertices());
		});
	}

	@Test
	public void addVertexUserProvidedId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			IntList vertices = IntList.of(48, 84, 66, 91, 3, 7);
			for (int v : vertices)
				b.addVertex(v);
			assertEquals(new IntOpenHashSet(vertices), b.vertices());
		});
	}

	@Test
	public void addVertexMixUserIdsAndImplIds() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			for (int v : IntList.of(48, 84, 66, 91, 3, 7))
				b.addVertex(v);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex());
		});
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			for (int i = 0; i < 15; i++)
				b.addVertex();
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(66));
		});
	}

	@Test
	public void addVertexDuplicateId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			b.addVertex(5);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(5));
		});
	}

	@Test
	public void addEdge() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			b.expectedVerticesNum(10);
			b.expectedEdgesNum(3);
			range(10).forEach(b::addVertex);
			int e1 = b.addEdge(0, 1);
			int e2 = b.addEdge(0, 2);
			int e3 = b.addEdge(0, 3);
			assertEquals(IntSet.of(e1, e2, e3), b.edges());
		});
	}

	@Test
	public void addEdgeUserProvidedIds() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			b.expectedVerticesNum(10);
			b.expectedEdgesNum(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			assertEquals(range(3), b.edges());
		});
	}

	@Test
	public void addEdgeMixUserIdsAndImplIds() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			b.expectedVerticesNum(10);
			b.expectedEdgesNum(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(0, 4));
		});
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			b.expectedVerticesNum(10);
			b.expectedEdgesNum(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1);
			b.addEdge(0, 2);
			b.addEdge(0, 3);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(0, 4, 77));
		});
	}

	@Test
	public void addEdgeDuplicateId() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 77);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(2, 3, 77));
		});
	}

	@Test
	public void addEdgeInvalidEndpoints() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
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
	public void clear() {
		foreachBoolConfig(directed -> {
			IntGraphBuilder b = directed ? IntGraphBuilder.newDirected() : IntGraphBuilder.newUndirected();
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
				IntGraphFactory factory = (directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected())
						.setOption("impl", impl);
				IntGraphBuilder b = factory.newBuilder();
				boolean selfEdges = factory.newGraph().isAllowSelfEdges();
				IntGraph g = (directed ? IntGraphFactory.newDirected() : IntGraphFactory.newUndirected())
						.allowSelfEdges(selfEdges).newGraph();

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
					if (g.getEdge(u, v) != -1)
						continue; /* avoid parallel edges */
					g.addEdge(u, v, e);
					b.addEdge(u, v, e);
				}

				/* duplicate edge */
				assertThrows(IllegalArgumentException.class, () -> b.addEdge(Graphs.randVertex(g, rand),
						Graphs.randVertex(g, rand), Graphs.randEdge(g, rand)));
				/* non existing endpoints */
				for (;;) {
					int v = rand.nextInt(2 * n);
					int e = rand.nextInt(2 * m);
					if (!g.vertices().contains(v) && !g.edges().contains(e)) {
						int u = Graphs.randVertex(g, rand);
						assertThrows(RuntimeException.class, () -> b.addEdge(u, v, e));
						assertThrows(RuntimeException.class, () -> b.addEdge(v, u, e));
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
		IntGraphFactory factory = IntGraphFactory.newUndirected().setDirected(directed);
		IntGraph g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		IWeightsInt vWeights = g.addVerticesWeights("weights", int.class);
		for (int v = 0; v < n; v++) {
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		IWeightsInt eWeights = g.addEdgesWeights("weights", int.class);
		for (int e = 0; e < m; e++) {
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

}
