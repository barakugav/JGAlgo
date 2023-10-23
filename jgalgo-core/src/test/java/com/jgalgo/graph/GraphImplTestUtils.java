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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.MatchingBipartiteTestUtils;
import com.jgalgo.alg.MatchingWeightedTestUtils;
import com.jgalgo.alg.MaximumFlow;
import com.jgalgo.alg.MaximumFlowTestUtils;
import com.jgalgo.alg.MinimumDirectedSpanningTree;
import com.jgalgo.alg.MinimumDirectedSpanningTreeTarjanTest;
import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.alg.MinimumSpanningTreeTestUtils;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class GraphImplTestUtils extends TestUtils {

	static IntSet intSetOf(int... elms) {
		IntSet set = new IntOpenHashSet();
		for (int e : elms)
			set.add(e);
		return IntSets.unmodifiable(set);
	}

	static void testVertexAdd(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.get(directed);
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int i = 0; i < n; i++) {
				int v = g.addVertex();
				verticesSet.add(v);
			}
			assertEquals(verticesSet, g.vertices());
			assertEquals(IntSets.emptySet(), g.edges());
		}
	}

	static void testAddEdge(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Int2ObjectMap<int[]> edges = new Int2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					edges.put(e, new int[] { e, u, v });
				}
			}
			assertEquals(edges.keySet(), g.edges());
			for (int[] edge : edges.values()) {
				int e = edge[0], u = edge[1], v = edge[2];
				assertEndpoints(g, e, u, v);
			}
		}
	}

	private static void assertEndpoints(Graph g, int e, int source, int target) {
		if (g.getCapabilities().directed()) {
			assertEquals(source, g.edgeSource(e));
			assertEquals(target, g.edgeTarget(e));
		} else {
			assertEquals(intSetOf(source, target), intSetOf(g.edgeSource(e), g.edgeTarget(e)));
		}
		assertEquals(source, g.edgeEndpoint(e, target));
		assertEquals(target, g.edgeEndpoint(e, source));
	}

	static void testGetEdge(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Object2IntMap<IntCollection> edges = new Object2IntOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					if (uIdx == vIdx && !g.getCapabilities().selfEdges())
						continue;
					int u = vs[uIdx], v = vs[vIdx];
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(IntList.of(u, v), e);
					} else {
						edges.put(intSetOf(u, v), e);
					}
				}
			}
			for (Object2IntMap.Entry<IntCollection> edge : edges.object2IntEntrySet()) {
				IntCollection endpoints = edge.getKey();
				IntIterator endpointsIt = endpoints.iterator();
				int u = endpointsIt.nextInt(), v = endpointsIt.hasNext() ? endpointsIt.nextInt() : u;
				int e = edge.getIntValue();
				assertEquals(e, g.getEdge(u, v));
			}
		}

	}

	@SuppressWarnings("boxing")
	static void testGetEdgesOutIn(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Int2ObjectMap<IntSet> outEdges = new Int2ObjectOpenHashMap<>();
			Int2ObjectMap<IntSet> inEdges = new Int2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					if (directed) {
						outEdges.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						inEdges.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					} else {
						outEdges.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						outEdges.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					}
				}
			}
			for (int u : g.vertices()) {
				if (directed) {
					assertEquals(outEdges.get(u), g.outEdges(u));
					assertEquals(inEdges.get(u), g.inEdges(u));
					assertEquals(outEdges.get(u).isEmpty(), g.outEdges(u).isEmpty());
					assertEquals(inEdges.get(u).isEmpty(), g.inEdges(u).isEmpty());
				} else {
					assertEquals(outEdges.get(u), g.outEdges(u));
					assertEquals(outEdges.get(u), g.inEdges(u));
					assertEquals(outEdges.get(u).isEmpty(), g.outEdges(u).isEmpty());
					assertEquals(outEdges.get(u).isEmpty(), g.inEdges(u).isEmpty());
				}
			}
			if (directed) {
				for (int u : g.vertices()) {
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						assertEquals(u, eit.source());
						assertEquals(g.edgeEndpoint(e, u), eit.target());
					}
					assertEquals(outEdges.get(u), g.outEdges(u));
					assertEquals(outEdges.get(u), g.outEdges(u));
				}
				for (int v : g.vertices()) {
					IntSet vEdges = new IntOpenHashSet();
					for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						assertEquals(v, eit.target());
						assertEquals(g.edgeEndpoint(e, v), eit.source());
						vEdges.add(e);
					}
					assertEquals(inEdges.get(v), vEdges);
				}
			}
		}
	}

	static void testGetEdgesSourceTarget(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Object2ObjectMap<IntCollection, IntSet> edges = new Object2ObjectOpenHashMap<>();
			final int edgeRepeat = g.getCapabilities().parallelEdges() ? 3 : 1;
			for (int repeat = 0; repeat < edgeRepeat; repeat++) {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
						int u = vs[uIdx], v = vs[vIdx];
						if (u == v && !g.getCapabilities().selfEdges())
							continue;
						int e = g.addEdge(u, v);
						IntCollection key = directed ? IntList.of(u, v) : intSetOf(u, v);
						edges.computeIfAbsent(key, w -> new IntOpenHashSet()).add(e);
					}
				}
			}
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					IntCollection key = directed ? IntList.of(u, v) : intSetOf(u, v);
					assertEquals(edges.get(key), g.getEdges(u, v));
				}
			}
		}
	}

	static void testEdgeIter(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Int2ObjectMap<IntCollection> edges = new Int2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(e, IntList.of(u, v));
					} else {
						edges.put(e, intSetOf(u, v));
					}
				}
			}

			/* outEdges */
			for (int u : g.vertices()) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int peekNext = eit.peekNext();
					int e = eit.nextInt();
					assertEquals(e, peekNext);

					int v = eit.target();
					if (directed) {
						assertEquals(edges.get(e), IntList.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), intSetOf(eit.source(), eit.target()));
					}
					assertEquals(u, eit.source());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}

				/* do not call hasNext() */
				EdgeIter eit = g.outEdges(u).iterator();
				for (int s = g.outEdges(u).size(); s-- > 0;) {
					int e = eit.nextInt();
					assertEquals(u, eit.source());
					assertEquals(g.edgeEndpoint(e, u), eit.target());
				}
				assert !eit.hasNext();
			}

			/* inEdges */
			for (int v : g.vertices()) {
				for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int peekNext = eit.peekNext();
					int e = eit.nextInt();
					assertEquals(e, peekNext);

					int u = eit.source();
					if (directed) {
						assertEquals(edges.get(e), IntList.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), intSetOf(eit.source(), eit.target()));
					}
					assertEquals(v, eit.target());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}

				/* do not call hasNext() */
				EdgeIter eit = g.inEdges(v).iterator();
				for (int s = g.inEdges(v).size(); s-- > 0;) {
					int e = eit.nextInt();
					assertEquals(v, eit.target());
					assertEquals(g.edgeEndpoint(e, v), eit.source());
				}
				assert !eit.hasNext();
			}

			/* getEdges */
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					for (EdgeIter eit = g.getEdges(u, v).iterator(); eit.hasNext();) {
						int peekNext = eit.peekNext();
						int e = eit.nextInt();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(v, eit.target());
						if (directed) {
							assertEquals(edges.get(e), IntList.of(eit.source(), eit.target()));
						} else {
							assertEquals(edges.get(e), intSetOf(eit.source(), eit.target()));
						}
					}
				}
			}
		}
	}

	static void testDegree(Boolean2ObjectFunction<Graph> graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex();
			int[] vs = g.vertices().toIntArray();

			Int2IntMap degreeOut = new Int2IntOpenHashMap();
			Int2IntMap degreeIn = new Int2IntOpenHashMap();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs[uIdx], v = vs[vIdx];
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					g.addEdge(u, v);

					degreeOut.put(u, degreeOut.get(u) + 1);
					degreeIn.put(v, degreeIn.get(v) + 1);
					if (!directed && u != v) {
						degreeOut.put(v, degreeOut.get(v) + 1);
						degreeIn.put(u, degreeIn.get(u) + 1);
					}
				}
			}
			for (int u : g.vertices()) {
				assertEquals(degreeOut.get(u), g.outEdges(u).size(), "u=" + u);
				assertEquals(degreeIn.get(u), g.inEdges(u).size(), "u=" + u);
			}
		}
	}

	static void testClear(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		Random rand = new Random(seed);
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.get(directed);
			boolean parallelEdges = g.getCapabilities().parallelEdges();

			int totalOpNum = 1000;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedN = 0;
				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex();
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex();
						expectedN++;
					} else {
						int u, v;
						int[] vs = g.vertices().toIntArray();
						for (int retry = 20;;) {
							if (retry-- > 0)
								continue opsLoop;
							u = vs[rand.nextInt(vs.length)];
							v = vs[rand.nextInt(vs.length)];
							if (u == v)
								continue;
							if (!parallelEdges && g.getEdge(u, v) != -1)
								continue;
							break;
						}
						g.addEdge(u, v);
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clear();
				assertEquals(0, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		}
	}

	static void testClearEdges(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		Random rand = new Random(seed);
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.get(directed);
			boolean parallelEdges = g.getCapabilities().parallelEdges();

			int totalOpNum = 1000;
			int expectedN = 0;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex();
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex();
						expectedN++;
					} else {
						int u, v;
						int[] vs = g.vertices().toIntArray();
						for (int retry = 20;;) {
							if (retry-- > 0)
								continue opsLoop;
							u = vs[rand.nextInt(vs.length)];
							v = vs[rand.nextInt(vs.length)];
							if (u == v)
								continue;
							if (!parallelEdges && g.getEdge(u, v) != -1)
								continue;
							break;
						}
						g.addEdge(u, v);
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clearEdges();
				assertEquals(expectedN, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		}
	}

	static void testCopy(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (String copyType : List.of("origImpl", "array", "linked-list", "hashtable", "matrix")) {
			for (boolean directed : new boolean[] { true, false }) {
				/* Create a random graph g */
				Graph g =
						new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(300).directed(directed).parallelEdges(false)
								.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();

				/* assign some weights to the vertices of g */
				final Object gVDataKey = JGAlgoUtils.labeledObj("vData");
				Weights<Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
				Int2ObjectMap<Object> gVDataMap = new Int2ObjectOpenHashMap<>();
				for (int u : g.vertices()) {
					Object data = JGAlgoUtils.labeledObj("data" + u);
					gVData.set(u, data);
					gVDataMap.put(u, data);
				}

				/* assign some weights to the edges of g */
				final Object gEDataKey = JGAlgoUtils.labeledObj("eData");
				Weights<Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
				Int2ObjectMap<Object> gEDataMap = new Int2ObjectOpenHashMap<>();
				for (int e : g.edges()) {
					Object data = JGAlgoUtils.labeledObj("data" + e);
					gEData.set(e, data);
					gEDataMap.put(e, data);
				}

				/* Copy g */
				Graph copy;
				if (copyType.equals("origImpl")) {
					copy = g.copy();
				} else {
					copy = GraphFactory.newFrom(g).setOption("impl", copyType).newCopyOf(g);
				}

				/* Assert vertices and edges are the same */
				assertEquals(g.vertices().size(), copy.vertices().size());
				assertEquals(g.vertices(), copy.vertices());
				assertEquals(g.edges().size(), copy.edges().size());
				assertEquals(g.edges(), copy.edges());
				for (int u : g.vertices()) {
					assertEquals(g.outEdges(u), copy.outEdges(u));
					assertEquals(g.inEdges(u), copy.inEdges(u));
				}

				/* Assert no weights were copied */
				Weights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
				Weights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
				assertNull(copyVData);
				assertNull(copyEData);
				assertEquals(copy.getVerticesWeightsKeys(), Collections.emptySet());
				assertEquals(copy.getEdgesWeightsKeys(), Collections.emptySet());
			}
		}

	}

	static void testCopyWithWeights(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean directed : new boolean[] { true, false }) {
			/* Create a random graph g */
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(300).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();

			/* assign some weights to the vertices of g */
			final Object gVDataKey = JGAlgoUtils.labeledObj("vData");
			Weights<Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Int2ObjectMap<Object> gVDataMap = new Int2ObjectOpenHashMap<>();
			for (int u : g.vertices()) {
				Object data = JGAlgoUtils.labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final Object gEDataKey = JGAlgoUtils.labeledObj("eData");
			Weights<Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Int2ObjectMap<Object> gEDataMap = new Int2ObjectOpenHashMap<>();
			for (int e : g.edges()) {
				Object data = JGAlgoUtils.labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph copy = g.copy(true);

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (int u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			Weights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
			Weights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNotNull(copyVData);
			assertNotNull(copyEData);
			Int2ObjectMap<Object> copyVDataMap = new Int2ObjectOpenHashMap<>(gVDataMap);
			Int2ObjectMap<Object> copyEDataMap = new Int2ObjectOpenHashMap<>(gEDataMap);
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}

			/* Reassign some weights to both g and copy, and assert they are updated independently */
			int[] vs = g.vertices().toIntArray();
			for (int ops = 0; ops < g.vertices().size() / 4; ops++) {
				int u = vs[rand.nextInt(vs.length)];
				Object data = JGAlgoUtils.labeledObj("data" + u + "new");
				g.getVerticesWeights(gVDataKey).set(u, data);
				gVDataMap.put(u, data);
			}
			int[] copyVs = copy.vertices().toIntArray();
			for (int ops = 0; ops < copy.vertices().size() / 4; ops++) {
				int u = copyVs[rand.nextInt(vs.length)];
				Object data = JGAlgoUtils.labeledObj("data" + u + "new");
				copy.getVerticesWeights(gVDataKey).set(u, data);
				copyVDataMap.put(u, data);
			}
			int[] gEdges = g.edges().toIntArray();
			for (int ops = 0; ops < g.edges().size() / 4; ops++) {
				int e = gEdges[rand.nextInt(g.edges().size())];
				Object data = JGAlgoUtils.labeledObj("data" + e + "new");
				g.getEdgesWeights(gEDataKey).set(e, data);
				gEDataMap.put(e, data);
			}
			int[] copyEdges = copy.edges().toIntArray();
			for (int ops = 0; ops < copy.edges().size() / 4; ops++) {
				int e = copyEdges[rand.nextInt(copy.edges().size())];
				Object data = JGAlgoUtils.labeledObj("data" + e + "new");
				copy.getEdgesWeights(gEDataKey).set(e, data);
				copyEDataMap.put(e, data);
			}

			/* Assert the weights were updated independently */
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}
		}
	}

	static void testImmutableCopy(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (boolean directed : new boolean[] { true, false }) {
			/* Create a random graph g */
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(300).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();

			/* assign some weights to the vertices of g */
			final Object gVDataKey = JGAlgoUtils.labeledObj("vData");
			Weights<Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Int2ObjectMap<Object> gVDataMap = new Int2ObjectOpenHashMap<>();
			for (int u : g.vertices()) {
				Object data = JGAlgoUtils.labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final Object gEDataKey = JGAlgoUtils.labeledObj("eData");
			Weights<Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Int2ObjectMap<Object> gEDataMap = new Int2ObjectOpenHashMap<>();
			for (int e : g.edges()) {
				Object data = JGAlgoUtils.labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph copy = g.immutableCopy();

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (int u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			Weights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
			Weights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNull(copyVData);
			assertNull(copyEData);
			assertEquals(copy.getVerticesWeightsKeys(), Collections.emptySet());
			assertEquals(copy.getEdgesWeightsKeys(), Collections.emptySet());
		}
	}

	static void testImmutableCopyWithWeights(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean directed : new boolean[] { true, false }) {
			/* Create a random graph g */
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(300).directed(directed).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();

			/* assign some weights to the vertices of g */
			final Object gVDataKey = JGAlgoUtils.labeledObj("vData");
			Weights<Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Int2ObjectMap<Object> gVDataMap = new Int2ObjectOpenHashMap<>();
			for (int u : g.vertices()) {
				Object data = JGAlgoUtils.labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final Object gEDataKey = JGAlgoUtils.labeledObj("eData");
			Weights<Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Int2ObjectMap<Object> gEDataMap = new Int2ObjectOpenHashMap<>();
			for (int e : g.edges()) {
				Object data = JGAlgoUtils.labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph copy = g.immutableCopy(true);

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (int u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			Weights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
			Weights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNotNull(copyVData);
			assertNotNull(copyEData);
			Int2ObjectMap<Object> copyVDataMap = new Int2ObjectOpenHashMap<>(gVDataMap);
			Int2ObjectMap<Object> copyEDataMap = new Int2ObjectOpenHashMap<>(gEDataMap);
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}

			/* Reassign some weights to g, and assert they are updated independently */
			int[] vs = g.vertices().toIntArray();
			for (int ops = 0; ops < g.vertices().size() / 4; ops++) {
				int u = vs[rand.nextInt(vs.length)];
				Object data = JGAlgoUtils.labeledObj("data" + u + "new");
				g.getVerticesWeights(gVDataKey).set(u, data);
				gVDataMap.put(u, data);
			}
			int[] gEdges = g.edges().toIntArray();
			for (int ops = 0; ops < g.edges().size() / 4; ops++) {
				int e = gEdges[rand.nextInt(g.edges().size())];
				Object data = JGAlgoUtils.labeledObj("data" + e + "new");
				g.getEdgesWeights(gEDataKey).set(e, data);
				gEDataMap.put(e, data);
			}

			/* Assert the weights were updated independently */
			for (int u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (int e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}
		}
	}

	static void testUndirectedMST(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		MinimumSpanningTreeTestUtils.testRandGraph(MinimumSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMDST(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		MinimumDirectedSpanningTreeTarjanTest.testRandGraph(MinimumDirectedSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMaxFlow(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		MaximumFlowTestUtils.testRandGraphs(MaximumFlow.newInstance(), graphImpl, seed, /* directed= */ true);
	}

	static void testUndirectedBipartiteMatching(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(
				MatchingAlgo.newBuilder().setBipartite(true).setCardinality(true).build(), graphImpl, seed);
	}

	static void testUndirectedBipartiteMatchingWeighted(Boolean2ObjectFunction<Graph> graphImpl, long seed) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingAlgo.newBuilder().setBipartite(true).build(),
				graphImpl, seed);
	}

	static void testRandOps(Boolean2ObjectFunction<Graph> graphImpl, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(2056);
		tester.addPhase().withArgs(16, 16).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(32);
		tester.addPhase().withArgs(64, 64).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 1324).repeat(2);
		tester.addPhase().withArgs(1025, 2016).repeat(1);
		tester.run((n, m) -> {
			testRandOps(graphImpl, directed, n, m, seedGen.nextSeed());
		});
	}

	private static void testRandOps(Boolean2ObjectFunction<Graph> graphImpl, boolean directed, int n, int m,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
				.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();
		final int opsNum = 128;
		testRandOps(g, opsNum, seedGen.nextSeed());
	}

	private static class RandWeighted<E> {
		private final List<E> elms = new ObjectArrayList<>();
		private final IntList weights = new IntArrayList();
		private int totalWeight;

		void add(E elm, int weight) {
			if (weight <= 0)
				throw new IllegalArgumentException();
			elms.add(elm);
			weights.add(weight);
			totalWeight += weight;
		}

		E get(Random rand) {
			final int v = rand.nextInt(totalWeight);
			int s = 0;
			for (int i = 0; i < elms.size(); i++) {
				s += weights.getInt(i);
				if (v < s)
					return elms.get(i);
			}
			throw new IllegalStateException();
		}
	}

	private static class GraphTracker {
		private final Int2ObjectMap<Vertex> vertices = new Int2ObjectOpenHashMap<>();
		private final List<Vertex> verticesArr = new ObjectArrayList<>();
		// private final Int2ObjectMap<Edge> edges = new Int2ObjectOpenHashMap<>();
		private final List<Edge> edges = new ObjectArrayList<>();
		private final boolean directed;
		private final Object dataKey;
		private final boolean debugPrints = false;

		GraphTracker(Graph g, Object dataKey) {
			this.directed = g.getCapabilities().directed();
			this.dataKey = dataKey;

			if (g instanceof IndexGraph) {
				((IndexGraph) g).addVertexSwapListener((id1, id2) -> {
					Vertex v1 = getVertex(id1), v2 = getVertex(id2);
					v1.id = id2;
					v2.id = id1;
					vertices.put(id1, v2);
					vertices.put(id2, v1);
				});
			}

			if (debugPrints)
				System.out.println("\n\n*****");
		}

		int verticesNum() {
			return vertices.size();
		}

		int edgesNum() {
			return edges.size();
		}

		void addVertex(int v) {
			if (debugPrints)
				System.out.println("newVertex(" + v + ")");
			Vertex V = new Vertex(v);
			vertices.put(v, V);
			verticesArr.add(V);
		}

		void removeVertex(Vertex v) {
			if (debugPrints)
				System.out.println("removeVertex(" + v + ")");
			removeEdgesOf0(v);

			Vertex oldV = vertices.remove(v.id);
			assertTrue(v == oldV);
			verticesArr.remove(v);
		}

		Vertex getVertex(int id) {
			Vertex v = vertices.get(id);
			assertEquals(v.id, id);
			assert v.id == id;
			return v;
		}

		Vertex getRandVertex(Random rand) {
			return verticesArr.get(rand.nextInt(verticesArr.size()));
		}

		void addEdge(Vertex u, Vertex v, int data) {
			if (debugPrints)
				System.out.println("addEdge(" + u + ", " + v + ", " + data + ")");
			edges.add(new Edge(u, v, data));
		}

		Edge getEdge(int data) {
			for (Edge edge : edges)
				if (edge.data == data)
					return edge;
			fail("edge not found");
			return null;
		}

		Edge getEdge(Vertex u, Vertex v) {
			if (directed) {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if (e.u == u && e.v == v)
						return e;
				}
			} else {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if ((e.u == u && e.v == v) || (e.v == u && e.u == v))
						return e;
				}
			}
			return null;
		}

		void removeEdge(Edge edge) {
			if (debugPrints)
				System.out.println("removeEdge(" + edge.u + ", " + edge.v + ")");
			boolean removed = edges.remove(edge);
			assertTrue(removed);
		}

		void removeEdgesOf(Vertex u) {
			if (debugPrints)
				System.out.println("removeEdgesOf(" + u + ")");
			removeEdgesOf0(u);
		}

		private void removeEdgesOf0(Vertex u) {
			edges.removeIf(edge -> edge.u == u || edge.v == u);
		}

		void removeOutEdgesOf(Vertex u) {
			if (debugPrints)
				System.out.println("removeOutEdgesOf(" + u + ")");
			edges.removeIf(edge -> edge.u == u);
		}

		void removeInEdgesOf(Vertex v) {
			if (debugPrints)
				System.out.println("removeInEdgesOf(" + v + ")");
			edges.removeIf(edge -> edge.v == v);
		}

		void reverseEdge(Edge edge) {
			if (debugPrints)
				System.out.println("reverse(" + edge.u + ", " + edge.v + ")");
			Vertex temp = edge.u;
			edge.u = edge.v;
			edge.v = temp;
		}

		Edge getRandEdge(Random rand) {
			return edges.get(rand.nextInt(edges.size()));
		}

		@SuppressWarnings("unused")
		void clearEdges() {
			if (debugPrints)
				System.out.println("clearEdges()");
			edges.clear();
		}

		void checkEdgesEqual(Graph g) {
			assertEquals(edgesNum(), g.edges().size());
			Weights.Int edgeData = g.getEdgesWeights(dataKey);

			List<IntList> actual = new ObjectArrayList<>();
			List<IntList> expected = new ObjectArrayList<>();

			for (int e : g.edges()) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edgeData.getInt(e);
				actual.add(IntList.of(u, v, data));
			}

			for (Edge edge : edges) {
				int u = edge.u.id, v = edge.v.id;
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edge.data;
				expected.add(IntList.of(u, v, data));
			}

			Comparator<IntList> cmp = (e1, e2) -> {
				int u1 = e1.getInt(0), v1 = e1.getInt(1), d1 = e1.getInt(2);
				int u2 = e2.getInt(0), v2 = e2.getInt(1), d2 = e2.getInt(2);
				int c;
				if ((c = Integer.compare(u1, u2)) != 0)
					return c;
				if ((c = Integer.compare(v1, v2)) != 0)
					return c;
				if ((c = Integer.compare(d1, d2)) != 0)
					return c;
				return 0;
			};
			actual.sort(cmp);
			expected.sort(cmp);
			assertEquals(expected, actual);
		}

		private static class Vertex {
			int id;

			Vertex(int id) {
				this.id = id;
			}

			@Override
			public String toString() {
				return Integer.toString(id);
			}
		}

		private static class Edge {
			Vertex u, v;
			final int data;

			Edge(Vertex u, Vertex v, int data) {
				this.u = u;
				this.v = v;
				this.data = data;
			}

			@Override
			public String toString() {
				return "(" + u + ", " + v + ", " + data + ")";
			}
		}
	}

	private static enum GraphOp {
		GetEdge, GetVertexEdges, GetVertexEdgesOut, GetVertexEdgesIn,

		EdgeSource, EdgeTarget,

		Degree, DegreeIn, DegreeOut,

		AddEdge,

		RemoveEdge, RemoveEdgeUsingOutIter, RemoveEdgeUsingInIter, RemoveEdgeUsingOutEdgeSet, RemoveEdgeUsingInEdgeSet, RemoveEdgeUsingSourceTargetEdgeSet,

		RemoveEdgesOfVertex, RemoveEdgesOfVertexUsingEdgeSet, RemoveEdgesOfVertexUsingIter,

		RemoveEdgesInOfVertex, RemoveEdgesInOfVertexUsingEdgeSet, RemoveEdgesInOfVertexUsingIter,

		RemoveEdgesOutOfVertex, RemoveEdgesOutOfVertexUsingEdgeSet, RemoveEdgesOutOfVertexUsingIter,

		ReverseEdge,

		// ClearEdges,

		AddVertex, RemoveVertex,
	}

	private static class UniqueGenerator {
		private final Random rand;
		private final IntSet used;

		UniqueGenerator(long seed) {
			rand = new Random(seed);
			used = new IntOpenHashSet();
		}

		int next() {
			for (;;) {
				int x = rand.nextInt();
				if (!used.contains(x)) {
					used.add(x);
					return x;
				}
			}
		}
	}

	private static void testRandOps(Graph g, int opsNum, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		GraphCapabilities capabilities = g.getCapabilities();
		Random rand = new Random(seedGen.nextSeed());
		RandWeighted<GraphOp> opRand = new RandWeighted<>();

		opRand.add(GraphOp.AddVertex, 20);
		opRand.add(GraphOp.RemoveVertex, 3);

		opRand.add(GraphOp.AddEdge, 80);

		opRand.add(GraphOp.RemoveEdge, 3);
		opRand.add(GraphOp.RemoveEdgeUsingOutIter, 2);
		opRand.add(GraphOp.RemoveEdgeUsingInIter, 2);
		opRand.add(GraphOp.RemoveEdgeUsingOutEdgeSet, 2);
		opRand.add(GraphOp.RemoveEdgeUsingInEdgeSet, 2);
		opRand.add(GraphOp.RemoveEdgeUsingSourceTargetEdgeSet, 1);
		opRand.add(GraphOp.RemoveEdgesOfVertex, 1);
		opRand.add(GraphOp.RemoveEdgesOfVertexUsingEdgeSet, 1);
		opRand.add(GraphOp.RemoveEdgesOfVertexUsingIter, 1);
		// opRand.add(GraphOp.ClearEdges, 1);

		if (capabilities.directed()) {
			opRand.add(GraphOp.RemoveEdgesInOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesInOfVertexUsingEdgeSet, 1);
			opRand.add(GraphOp.RemoveEdgesInOfVertexUsingIter, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertexUsingEdgeSet, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertexUsingIter, 1);
			opRand.add(GraphOp.ReverseEdge, 6);
		}

		final Object dataKey = JGAlgoUtils.labeledObj("data");
		Weights.Int edgeData = g.addEdgesWeights(dataKey, int.class);
		UniqueGenerator dataGen = new UniqueGenerator(seedGen.nextSeed());

		GraphTracker tracker = new GraphTracker(g, dataKey);
		for (int v : g.vertices()) {
			// final int data = dataGen.next();
			// edgeData.set(e, data);
			tracker.addVertex(v);
		}
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			final int data = dataGen.next();
			edgeData.set(e, data);
			tracker.addEdge(tracker.getVertex(u), tracker.getVertex(v), data);
		}

		ToIntFunction<GraphTracker.Edge> getEdge = edge -> {
			int e = -1;
			for (EdgeIter eit = g.getEdges(edge.u.id, edge.v.id).iterator(); eit.hasNext();) {
				int e0 = eit.nextInt();
				if (edge.data == edgeData.getInt(e0)) {
					e = e0;
					break;
				}
			}
			assertTrue(e != -1, "edge not found");
			return e;
		};

		opLoop: for (; opsNum > 0;) {
			final GraphOp op = opRand.get(rand);
			switch (op) {
				case AddEdge: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u, v;
					for (int retry = 20;; retry--) {
						if (retry <= 0)
							continue opLoop;

						u = tracker.getRandVertex(rand);
						v = tracker.getRandVertex(rand);
						if (!capabilities.selfEdges() && u == v)
							continue;
						if (!capabilities.parallelEdges() && tracker.getEdge(u, v) != null)
							continue;
						break;
					}

					final int data = dataGen.next();
					int e = g.addEdge(u.id, v.id);
					edgeData.set(e, data);
					tracker.addEdge(u, v, data);
					break;
				}
				case RemoveEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					int e = getEdge.applyAsInt(edge);

					g.removeEdge(e);
					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingOutIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex source = edge.u;

					Set<GraphTracker.Edge> iterationExpected = new ObjectOpenHashSet<>();
					for (int eOther : g.outEdges(source.id)) {
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new ObjectOpenHashSet<>();
					for (EdgeIter it = g.outEdges(source.id).iterator(); it.hasNext();) {
						int eOther = it.nextInt();
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationActual.add(edgeOther);
							assertFalse(duplication);
						} else {
							assertFalse(removed);
							it.remove();
							tracker.removeEdge(edge);
							removed = true;
						}
					}
					assertTrue(removed);
					assertEquals(iterationExpected, iterationActual);
					break;
				}
				case RemoveEdgeUsingOutEdgeSet: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex source = edge.u;
					int e = getEdge.applyAsInt(edge);

					EdgeSet edgeSet = g.outEdges(source.id);
					assertTrue(edgeSet.contains(e));

					boolean removed = edgeSet.remove(e);
					assertTrue(removed);

					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingInEdgeSet: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex target = edge.v;
					int e = getEdge.applyAsInt(edge);

					EdgeSet edgeSet = g.inEdges(target.id);
					assertTrue(edgeSet.contains(e));

					boolean removed = edgeSet.remove(e);
					assertTrue(removed);

					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingSourceTargetEdgeSet: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex source = edge.u;
					GraphTracker.Vertex target = edge.v;
					int e = getEdge.applyAsInt(edge);

					EdgeSet edgeSet = g.getEdges(source.id, target.id);
					assertTrue(edgeSet.contains(e));

					boolean removed = edgeSet.remove(e);
					assertTrue(removed);

					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingInIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex target = edge.v;

					Set<GraphTracker.Edge> iterationExpected = new ObjectOpenHashSet<>();
					for (int eOther : g.inEdges(target.id)) {
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new ObjectOpenHashSet<>();
					for (EdgeIter it = g.inEdges(target.id).iterator(); it.hasNext();) {
						int eOther = it.nextInt();
						if (edgeData.getInt(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.getInt(eOther));
							boolean duplication = !iterationActual.add(edgeOther);
							assertFalse(duplication);
						} else {
							assertFalse(removed);
							it.remove();
							tracker.removeEdge(edge);
							removed = true;
						}
					}
					assertTrue(removed);
					assertEquals(iterationExpected, iterationActual);
					break;
				}
				case RemoveEdgesOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeEdgesOf(u.id);
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.outEdges(u.id).clear();
					assertTrue(g.outEdges(u.id).isEmpty());
					g.inEdges(u.id).clear();
					assertTrue(g.inEdges(u.id).isEmpty());
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.outEdges(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					for (EdgeIter it = g.inEdges(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeInEdgesOf(u.id);
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.inEdges(u.id).clear();
					assertTrue(g.inEdges(u.id).isEmpty());
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.inEdges(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeOutEdgesOf(u.id);
					tracker.removeOutEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.outEdges(u.id).clear();
					assertTrue(g.outEdges(u.id).isEmpty());
					tracker.removeOutEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter it = g.outEdges(u.id).iterator(); it.hasNext();) {
						it.nextInt();
						it.remove();
					}
					tracker.removeOutEdgesOf(u);
					break;
				}
				case ReverseEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					if (edge.u != edge.v && g.getEdge(edge.v.id, edge.u.id) != -1 && !capabilities.parallelEdges())
						continue;
					int e = getEdge.applyAsInt(edge);

					g.reverseEdge(e);
					tracker.reverseEdge(edge);
					break;
				}
				// case ClearEdges:
				// if (g.edges().size() == 0)
				// continue;
				// g.clearEdges();
				// tracker.clearEdges();
				// break;

				case AddVertex: {
					int v = g.addVertex();
					tracker.addVertex(v);
					break;
				}
				case RemoveVertex:
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex v = tracker.getRandVertex(rand);
					g.removeVertex(v.id);
					tracker.removeVertex(v);
					break;
				// case RemoveVertices: {
				// if (tracker.verticesNum() < 3)
				// continue;
				// Set<GraphTracker.Vertex> vertices = new ObjectOpenHashSet<>(3);
				// while (vertices.size() < 3)
				// vertices.add(tracker.getRandVertex(rand));
				// IntSet verticesInt = new IntOpenHashSet(3);
				// for (GraphTracker.Vertex vertex : vertices)
				// verticesInt.add(vertex.id);
				// g.removeVertices(verticesInt);
				// for (GraphTracker.Vertex vertex : vertices)
				// tracker.removeVertex(vertex);
				// break;
				// }

				default:
					throw new IllegalArgumentException("Unexpected value: " + op);
			}

			assertEquals(tracker.verticesNum(), g.vertices().size());
			assertEquals(tracker.edgesNum(), g.edges().size());
			if (opsNum % 10 == 0)
				tracker.checkEdgesEqual(g);

			opsNum--;
		}
	}

}
