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
import java.util.HashSet;
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

public class GraphBuilderTest extends TestBase {

	@Test
	public void isDirected() {
		assertTrue(GraphBuilder.newDirected().isDirected());
		assertFalse(GraphBuilder.newUndirected().isDirected());
	}

	@Test
	public void fromGraph() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = createGraph(directed);

			/* With weights */
			GraphBuilder<Integer, Integer> b2 = GraphBuilder.fromGraph(g, true, true);
			assertEqualsBool(directed, b2.isDirected());
			assertEquals(g, b2.build());

			/* Without weights */
			GraphBuilder<Integer, Integer> b1 = GraphBuilder.fromGraph(g);
			assertEqualsBool(directed, b1.isDirected());
			assertEquals(g.copy(/* no weights */), b1.build());
		});
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			List<Integer> vertices = IntList.of(48, 84, 66, 91, 3, 7);
			for (Integer v : vertices)
				b.addVertex(v);
			assertEquals(new HashSet<>(vertices), b.vertices());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addVertexDuplicateId() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			b.addVertex(5);
			assertThrows(IllegalArgumentException.class, () -> b.addVertex(5));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdge() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			b.expectedVerticesNum(10);
			b.expectedEdgesNum(3);
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 0);
			b.addEdge(0, 2, 1);
			b.addEdge(0, 3, 2);
			assertEquals(range(3), b.edges());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgeDuplicateId() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			range(10).forEach(b::addVertex);
			b.addEdge(0, 1, 77);
			assertThrows(IllegalArgumentException.class, () -> b.addEdge(2, 3, 77));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addEdgeInvalidEndpoints() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
			range(10).forEach(b::addVertex);
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(-1, 0, 0));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, -1, 1));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(10, 0, 2));
			assertThrows(NoSuchVertexException.class, () -> b.addEdge(0, 10, 3));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void clear() {
		foreachBoolConfig(directed -> {
			GraphBuilder<Integer, Integer> b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
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

			Graph<Integer, Integer> g = b.build();
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
				for (boolean edgesWeights : BooleanList.of(false, true)) {
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

			if (buildMut) {
				Integer nonExistingVertex;
				do {
					nonExistingVertex = Integer.valueOf(rand.nextInt(g.vertices().size() * 2));
				} while (g.vertices().contains(nonExistingVertex));
				gActual.addVertex(nonExistingVertex);
				assertTrue(gActual.vertices().contains(nonExistingVertex));

			} else {
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

	private static Graph<Integer, Integer> createGraph(boolean directed) {
		final long seed = 0xa636ca816d4202c9L;
		final Random rand = new Random(seed);
		final int n = 47, m = 1345;
		GraphFactory<Integer, Integer> factory = GraphFactory.<Integer, Integer>newInstance(directed);
		Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();

		WeightsInt<Integer> vWeights = g.addVerticesWeights("weights", int.class);
		for (int v0 = 0; v0 < n; v0++) {
			Integer v = Integer.valueOf(v0);
			g.addVertex(v);
			vWeights.set(v, rand.nextInt(10000));
		}

		WeightsInt<Integer> eWeights = g.addEdgesWeights("weights", int.class);
		for (int e0 = 0; e0 < m; e0++) {
			Integer e = Integer.valueOf(e0);
			g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
			eWeights.set(e, rand.nextInt(10000));
		}
		return g;
	}

}
