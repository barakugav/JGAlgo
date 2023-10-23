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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.JGAlgoUtils;
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
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean buildMut : BooleanList.of(false, true)) {
				IndexGraphBuilder b = directed ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
				IndexGraph g = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();

				/* Add vertices and edges */
				final int n = 12 + rand.nextInt(12);
				final int m = 20 + rand.nextInt(20);
				while (g.vertices().size() < n) {
					int vG = g.addVertex();
					int vB = b.addVertex();
					assertEquals(vG, vB);
				}
				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int e = rand.nextInt(2 * m);
					if (g.edges().contains(e))
						continue;
					int u = vs[rand.nextInt(vs.length)], v = vs[rand.nextInt(vs.length)];
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
							Object key = JGAlgoUtils.labeledObj("weight" + weightIdx.getAndIncrement());
							Object defVal = valSupplier.get();
							Weights wG, wB;
							IntSet elements;
							if (!edgesWeights) {
								wG = g.addVerticesWeights(key, type, defVal);
								wB = b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = g.addEdgesWeights(key, type, defVal);
								wB = b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (int elm : elements) {
								Object w = valSupplier.get();
								wG.set(elm, w);
								wB.set(elm, w);
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

				Graph gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				for (Object key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeight(),
							gActual.getVerticesWeights(key).defaultWeight());
				for (Object key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeight(), gActual.getEdgesWeights(key).defaultWeight());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void regularGraph() {
		final long seed = 0x1dbb0af52c6ad3e8L;
		final Random rand = new Random(seed);
		for (boolean directed : BooleanList.of(false, true)) {
			for (boolean buildMut : BooleanList.of(false, true)) {
				GraphBuilder b = directed ? GraphBuilder.newDirected() : GraphBuilder.newUndirected();
				Graph g = directed ? Graph.newDirected() : Graph.newUndirected();

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
				for (int[] vs = g.vertices().toIntArray(); g.edges().size() < m;) {
					int e = rand.nextInt(2 * m);
					if (g.edges().contains(e))
						continue;
					int u = vs[rand.nextInt(vs.length)], v = vs[rand.nextInt(vs.length)];
					g.addEdge(u, v, e);
					b.addEdge(u, v, e);
				}

				/* Add weights */
				AtomicInteger weightIdx = new AtomicInteger();
				@SuppressWarnings("rawtypes")
				BiConsumer<Class, Supplier> addWeights = (type, valSupplier) -> {
					for (boolean edgesWeights : BooleanList.of(false, true)) {
						for (int repeat = 1 + rand.nextInt(2); repeat > 0; repeat--) {
							Object key = JGAlgoUtils.labeledObj("weight" + weightIdx.getAndIncrement());
							Object defVal = valSupplier.get();
							Weights wG, wB;
							IntSet elements;
							if (!edgesWeights) {
								wG = g.addVerticesWeights(key, type, defVal);
								wB = b.addVerticesWeights(key, type, defVal);
								elements = g.vertices();
							} else {
								wG = g.addEdgesWeights(key, type, defVal);
								wB = b.addEdgesWeights(key, type, defVal);
								elements = g.edges();
							}
							for (int elm : elements) {
								Object w = valSupplier.get();
								wG.set(elm, w);
								wB.set(elm, w);
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

				Graph gActual = buildMut ? b.buildMutable() : b.build();
				assertEquals(g, gActual);

				for (Object key : g.getVerticesWeightsKeys())
					assertEquals(g.getVerticesWeights(key).defaultWeight(),
							gActual.getVerticesWeights(key).defaultWeight());
				for (Object key : g.getEdgesWeightsKeys())
					assertEquals(g.getEdgesWeights(key).defaultWeight(), gActual.getEdgesWeights(key).defaultWeight());

				if (!buildMut) {
					int[] vs = gActual.vertices().toIntArray();
					int[] es = gActual.edges().toIntArray();
					for (Object key : gActual.getVerticesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getVerticesWeights(key);
						int v = vs[rand.nextInt(vs.length)];
						Object data = w.get(vs[rand.nextInt(vs.length)]);
						assertThrows(UnsupportedOperationException.class, () -> w.set(v, data));
					}
					for (Object key : gActual.getEdgesWeightsKeys()) {
						@SuppressWarnings("rawtypes")
						Weights w = gActual.getEdgesWeights(key);
						int e = es[rand.nextInt(es.length)];
						Object data = w.get(es[rand.nextInt(es.length)]);
						assertThrows(UnsupportedOperationException.class, () -> w.set(e, data));
					}
				}
			}
		}
	}

}
