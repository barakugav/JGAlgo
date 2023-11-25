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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphBuilderTest extends TestBase {

	@SuppressWarnings("unchecked")
	@Test
	public void indexGraph() {
		final long seed = 0x56f68a18a0ca8d84L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut, selfEdges) -> {
			IndexGraphFactory factory =
					IndexGraphFactory.newUndirected().setDirected(directed).allowSelfEdges(selfEdges);
			IndexGraphBuilder b = factory.newBuilder();
			IndexGraph g = factory.newGraph();

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

	@SuppressWarnings("unchecked")
	@Test
	public void intGraph() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut) -> {
			for (String impl : List.of("array", "array-selfedges", "linked-list", "linked-list-ptr", "hashtable",
					"hashtable-selfedges", "matrix")) {
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

	@SuppressWarnings("unchecked")
	@Test
	public void objGraph() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((directed, buildMut, selfEdges) -> {
			GraphFactory<Integer, Integer> factory =
					GraphFactory.<Integer, Integer>newUndirected().setDirected(directed).allowSelfEdges(selfEdges);
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
				for (boolean edgesWeights : BooleanList.of(false, true)) {
					for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
						String key = "weight" + weightIdx.getAndIncrement();
						Object defVal = valSupplier.get();
						Weights wG, wB;
						Set<Integer> elements;
						if (!edgesWeights) {
							wG = g.addVerticesWeights(key, type, defVal);
							wB = b.addVerticesWeights(key, type, defVal);
							elements = g.vertices();
						} else {
							wG = g.addEdgesWeights(key, type, defVal);
							wB = b.addEdgesWeights(key, type, defVal);
							elements = g.edges();
						}
						for (Integer elm : elements) {
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

			Graph<Integer, Integer> gActual = buildMut ? b.buildMutable() : b.build();
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
					Integer v = Graphs.randVertex(gActual, rand);
					Object data = w.getAsObj(Graphs.randVertex(gActual, rand));
					assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(v, data));
				}
				for (String key : gActual.getEdgesWeightsKeys()) {
					@SuppressWarnings("rawtypes")
					Weights w = gActual.getEdgesWeights(key);
					Integer e = Graphs.randEdge(gActual, rand);
					Object data = w.getAsObj(Graphs.randEdge(gActual, rand));
					assertThrows(UnsupportedOperationException.class, () -> w.setAsObj(e, data));
				}
			}
		});
	}

}
