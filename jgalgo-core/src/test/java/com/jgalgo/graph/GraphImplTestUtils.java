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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
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
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

class GraphImplTestUtils extends TestUtils {

	@SafeVarargs
	static <K> Set<K> setOf(K... elms) {
		ObjectSet<K> set = new ObjectOpenHashSet<>();
		for (K e : elms)
			set.add(e);
		return ObjectSets.unmodifiable(set);
	}

	@SuppressWarnings("boxing")
	static void testVertexAdd(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.get(directed);
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int i = 0; i < n; i++) {
				int v = i + 1;
				g.addVertex(Integer.valueOf(v));
				verticesSet.add(v);
			}
			assertEquals(verticesSet, g.vertices());
			assertEquals(IntSets.emptySet(), g.edges());

			assertThrows(NoSuchVertexException.class, () -> g.outEdges(6687));
		});
	}

	@SuppressWarnings("boxing")
	static void testAddEdge(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Int2ObjectMap<int[]> edges = new Int2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = uIdx + 1; vIdx < n; vIdx++) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					Integer e = Integer.valueOf(g.edges().size() + 1);
					g.addEdge(u, v, e);
					assertEndpoints(g, e, u, v);
					edges.put(e.intValue(), new int[] { e, u, v });
				}
			}
			assertEquals(edges.keySet(), g.edges());
			for (int[] edge : edges.values()) {
				int e = edge[0], u = edge[1], v = edge[2];
				assertEndpoints(g, e, u, v);
			}

			assertThrows(NoSuchEdgeException.class, () -> g.edgeSource(6687));
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.get(directed);
			if (!g.isAllowSelfEdges()) {
				g.addVertex(0);
				assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 0, 0));
			}
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.get(directed);
			if (!g.isAllowParallelEdges()) {
				g.addVertex(0);
				g.addVertex(1);
				g.addEdge(0, 1, 0);
				assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 1, 1));
				if (!directed)
					assertThrows(IllegalArgumentException.class, () -> g.addEdge(1, 0, 1));
			}
		});
	}

	private static <V, E> void assertEndpoints(Graph<V, E> g, E e, V source, V target) {
		if (g.isDirected()) {
			assertEquals(source, g.edgeSource(e));
			assertEquals(target, g.edgeTarget(e));
		} else {
			assertEquals(setOf(source, target), setOf(g.edgeSource(e), g.edgeTarget(e)));
		}
		assertEquals(source, g.edgeEndpoint(e, target));
		assertEquals(target, g.edgeEndpoint(e, source));
	}

	static void testEndpoints(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		final long seed = 0x62f7c169c6fbd294L;
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			final int n = 30;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			Map<Integer, IntIntPair> edges = new HashMap<>();
			while (g.edges().size() < 60) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!g.isAllowSelfEdges() && u.equals(v))
					continue;
				if (!g.isAllowParallelEdges() && g.getEdge(u, v) != null)
					continue;
				Integer e = Integer.valueOf(g.edges().size() + 1);
				g.addEdge(u, v, e);
				edges.put(e, IntIntPair.of(u.intValue(), v.intValue()));
			}

			for (Integer e : g.edges()) {
				IntIntPair endpoints = edges.get(e);
				Integer u = Integer.valueOf(endpoints.leftInt()), v = Integer.valueOf(endpoints.rightInt());
				assertEquals(u, g.edgeSource(e));
				assertEquals(v, g.edgeTarget(e));
				assertEquals(u, g.edgeEndpoint(e, v));
				assertEquals(v, g.edgeEndpoint(e, u));

				Integer nonEndpointVertex;
				do {
					nonEndpointVertex = Graphs.randVertex(g, rand);
				} while (nonEndpointVertex.equals(u) || nonEndpointVertex.equals(v));
				Integer nonEndpointVertex0 = nonEndpointVertex;
				assertThrows(IllegalArgumentException.class, () -> g.edgeEndpoint(e, nonEndpointVertex0));
			}
		});
	}

	static void testGetEdge(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Collection<Integer>, Integer> edges = new Object2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					if (uIdx == vIdx && !g.isAllowSelfEdges())
						continue;
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					Integer e = Integer.valueOf(g.edges().size() + 1);
					g.addEdge(u, v, e);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(List.of(u, v), e);
					} else {
						edges.put(setOf(u, v), e);
					}
				}
			}
			for (Object2ObjectMap.Entry<Collection<Integer>, Integer> edge : edges.object2ObjectEntrySet()) {
				Collection<Integer> endpoints = edge.getKey();
				Iterator<Integer> endpointsIt = endpoints.iterator();
				Integer u = endpointsIt.next(), v = endpointsIt.hasNext() ? endpointsIt.next() : u;
				Integer e = edge.getValue();
				assertEquals(e, g.getEdge(u, v));
			}
		});
	}

	@SuppressWarnings("boxing")
	static void testGetEdgesOutIn(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(i + 1);
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Integer, Set<Integer>> outEdges = new Object2ObjectOpenHashMap<>();
			Object2ObjectMap<Integer, Set<Integer>> inEdges = new Object2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					int u = vs.get(uIdx), v = vs.get(vIdx);
					if (u == v && !g.isAllowSelfEdges())
						continue;
					Integer e = g.edges().size() + 1;
					g.addEdge(u, v, e);
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
				for (Integer u : g.vertices()) {
					for (EdgeIter<Integer, Integer> eit = g.outEdges(u).iterator();;) {
						if (!eit.hasNext()) {
							assertThrows(NoSuchElementException.class, () -> eit.next());
							break;
						}
						Integer e = eit.next();
						assertEquals(u, eit.source());
						assertEquals(g.edgeEndpoint(e, u), eit.target());
					}
					assertEquals(outEdges.get(u), g.outEdges(u));
					assertEquals(outEdges.get(u), g.outEdges(u));
				}
				for (Integer v : g.vertices()) {
					Set<Integer> vEdges = new ObjectOpenHashSet<>();
					for (EdgeIter<Integer, Integer> eit = g.inEdges(v).iterator(); eit.hasNext();) {
						Integer e = eit.next();
						assertEquals(v, eit.target());
						assertEquals(g.edgeEndpoint(e, v), eit.source());
						vEdges.add(e);
					}
					assertEquals(inEdges.get(v), vEdges);
				}
			}
		});
	}

	static void testGetEdgesSourceTarget(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			BiFunction<Integer, Integer, Collection<Integer>> key = directed ? List::of : GraphImplTestUtils::setOf;

			Object2ObjectMap<Collection<Integer>, Set<Integer>> edges = new Object2ObjectOpenHashMap<>();
			final int edgeRepeat = g.isAllowParallelEdges() ? 3 : 1;
			for (int repeat = 0; repeat < edgeRepeat; repeat++) {
				for (int uIdx = 0; uIdx < n; uIdx++) {
					for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
						Integer u = vs.get(uIdx), v = vs.get(vIdx);
						if (u.equals(v) && !g.isAllowSelfEdges())
							continue;
						Integer e = Integer.valueOf(g.edges().size() + 1);
						g.addEdge(u, v, e);
						edges.computeIfAbsent(key.apply(u, v), w -> new ObjectOpenHashSet<>()).add(e);
					}
				}
			}
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					if (u.equals(v) && !g.isAllowSelfEdges())
						continue;
					EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);
					assertEquals(edges.get(key.apply(u, v)), edges0);
				}
			}

			/* contains() */
			for (int i = 0; i < 5; i++) {
				Integer e = Graphs.randEdge(g, rand);
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);

				assertTrue(edges0.contains(e));
				for (@SuppressWarnings("unused")
				Integer e1 : g.edges()) {
					Integer u1 = g.edgeSource(e), v1 = g.edgeTarget(e);
					assertEqualsBool(key.apply(u, v).equals(key.apply(u1, v1)), edges0.contains(e));
				}

				Integer nonParallelEdge;
				do {
					nonParallelEdge = Graphs.randEdge(g, rand);
				} while (key.apply(u, v)
						.equals(key.apply(g.edgeSource(nonParallelEdge), g.edgeTarget(nonParallelEdge))));
				assertFalse(edges0.contains(nonParallelEdge));

				Integer nonExistingEdge;
				do {
					nonExistingEdge = Integer.valueOf(rand.nextInt());
				} while (g.edges().contains(nonExistingEdge));
				assertFalse(edges0.contains(nonExistingEdge));
			}

			/* remove() */
			for (int i = 0; i < 5; i++) {
				Integer e = Graphs.randEdge(g, rand);
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);

				int sizeBeforeRemove = edges0.size();
				assertTrue(edges0.remove(e));
				assertEquals(sizeBeforeRemove - 1, edges0.size());
				assertFalse(edges0.remove(e));
				assertFalse(edges0.contains(e));

				Integer nonParallelEdge;
				do {
					nonParallelEdge = Graphs.randEdge(g, rand);
				} while (key.apply(u, v)
						.equals(key.apply(g.edgeSource(nonParallelEdge), g.edgeTarget(nonParallelEdge))));
				assertFalse(edges0.remove(nonParallelEdge));

				Integer nonExistingEdge;
				do {
					nonExistingEdge = Integer.valueOf(rand.nextInt());
				} while (g.edges().contains(nonExistingEdge));
				assertFalse(edges0.remove(nonExistingEdge));
			}

			/* iterator().remove() */
			for (int i = 0; i < 5; i++) {
				Integer e = Graphs.randEdge(g, rand);
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);

				int sizeBeforeRemove = edges0.size();
				EdgeIter<Integer, Integer> eit = edges0.iterator();
				e = eit.next();
				eit.remove();
				assertEquals(sizeBeforeRemove - 1, edges0.size());
				assertFalse(edges0.remove(e));
				assertFalse(edges0.contains(e));
				assertEquals(edges0, new ObjectOpenHashSet<>(eit));
			}

			/* clear() */
			for (int i = 0; i < 5; i++) {
				Integer e = Graphs.randEdge(g, rand);
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);

				edges0.clear();
				assertEquals(0, edges0.size());
				edges0.clear();
				assertTrue(edges0.isEmpty());
				assertFalse(edges0.contains(e));
			}

			/* empty edge set */
			for (int i = 0; i < 5; i++) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				g.getEdges(u, v).clear();
				EdgeSet<Integer, Integer> edges0 = g.getEdges(u, v);

				assertTrue(edges0.isEmpty());
				assertEquals(0, edges0.size());
				assertEquals(Collections.emptySet(), edges0);
				assertFalse(edges0.iterator().hasNext());
			}

		});
	}

	static void testEdgeIter(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Integer, Collection<Integer>> edges = new Object2ObjectOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					if (u.equals(v) && !g.isAllowSelfEdges())
						continue;
					Integer e = Integer.valueOf(g.edges().size() + 1);
					g.addEdge(u, v, e);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(e, List.of(u, v));
					} else {
						edges.put(e, setOf(u, v));
					}
				}
			}

			/* outEdges */
			for (Integer u : g.vertices()) {
				for (EdgeIter<Integer, Integer> eit = g.outEdges(u).iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					Integer v = eit.target();
					if (directed) {
						assertEquals(edges.get(e), List.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), setOf(eit.source(), eit.target()));
					}
					assertEquals(u, eit.source());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}

				/* do not call hasNext() */
				EdgeIter<Integer, Integer> eit = g.outEdges(u).iterator();
				for (int s = g.outEdges(u).size(); s-- > 0;) {
					Integer e = eit.next();
					assertEquals(u, eit.source());
					assertEquals(g.edgeEndpoint(e, u), eit.target());
				}
				assert !eit.hasNext();
			}

			/* inEdges */
			for (Integer v : g.vertices()) {
				for (EdgeIter<Integer, Integer> eit = g.inEdges(v).iterator(); eit.hasNext();) {
					Integer peekNext = eit.peekNext();
					Integer e = eit.next();
					assertEquals(e, peekNext);

					Integer u = eit.source();
					if (directed) {
						assertEquals(edges.get(e), List.of(eit.source(), eit.target()));
					} else {
						assertEquals(edges.get(e), setOf(eit.source(), eit.target()));
					}
					assertEquals(v, eit.target());
					assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						assertEquals(g.edgeSource(e), eit.source());
						assertEquals(g.edgeTarget(e), eit.target());
					}
				}

				/* do not call hasNext() */
				EdgeIter<Integer, Integer> eit = g.inEdges(v).iterator();
				for (int s = g.inEdges(v).size(); s-- > 0;) {
					Integer e = eit.next();
					assertEquals(v, eit.target());
					assertEquals(g.edgeEndpoint(e, v), eit.source());
				}
				assert !eit.hasNext();
			}

			/* getEdges */
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					if (u.equals(v) && !g.isAllowSelfEdges())
						continue;
					for (EdgeIter<Integer, Integer> eit = g.getEdges(u, v).iterator(); eit.hasNext();) {
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(v, eit.target());
						if (directed) {
							assertEquals(edges.get(e), List.of(eit.source(), eit.target()));
						} else {
							assertEquals(edges.get(e), setOf(eit.source(), eit.target()));
						}
					}
				}
			}
		});
	}

	static void testDegree(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.get(directed);
			for (int i = 0; i < n; i++)
				g.addVertex(Integer.valueOf(i + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2IntMap<Integer> degreeOut = new Object2IntOpenHashMap<>();
			Object2IntMap<Integer> degreeIn = new Object2IntOpenHashMap<>();
			for (int uIdx = 0; uIdx < n; uIdx++) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					if (u.equals(v) && !g.isAllowSelfEdges())
						continue;
					g.addEdge(u, v, Integer.valueOf(g.edges().size() + 1));

					degreeOut.put(u, degreeOut.getInt(u) + 1);
					degreeIn.put(v, degreeIn.getInt(v) + 1);
					if (!directed && u != v) {
						degreeOut.put(v, degreeOut.getInt(v) + 1);
						degreeIn.put(u, degreeIn.getInt(u) + 1);
					}
				}
			}
			for (Integer u : g.vertices()) {
				assertEquals(degreeOut.getInt(u), g.outEdges(u).size(), "u=" + u);
				assertEquals(degreeIn.getInt(u), g.inEdges(u).size(), "u=" + u);
			}
		});
	}

	static void testClear(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.get(directed);
			boolean parallelEdges = g.isAllowParallelEdges();

			int totalOpNum = 1000;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedN = 0;
				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex(Integer.valueOf(i + 1));
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex(Integer.valueOf(g.vertices().size() + 1));
						expectedN++;
					} else {
						Integer u, v;
						for (int retry = 20;;) {
							if (retry-- > 0)
								continue opsLoop;
							u = Graphs.randVertex(g, rand);
							v = Graphs.randVertex(g, rand);
							if (u.equals(v))
								continue;
							if (!parallelEdges && g.getEdge(u, v) != null)
								continue;
							break;
						}
						g.addEdge(u, v, Integer.valueOf(g.edges().size() + 1));
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clear();
				assertEquals(0, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		});
	}

	static void testClearEdges(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.get(directed);
			boolean parallelEdges = g.isAllowParallelEdges();

			int totalOpNum = 1000;
			int expectedN = 0;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedM = 0;

				for (int i = 0; i < 2; i++) {
					g.addVertex(Integer.valueOf(g.vertices().size() + 1));
					expectedN++;
				}
				opsLoop: while (ops-- > 0) {
					if (rand.nextInt(5) == 0) {
						g.addVertex(Integer.valueOf(g.vertices().size() + 1));
						expectedN++;
					} else {
						Integer u, v;
						for (int retry = 20;;) {
							if (retry-- == 0)
								continue opsLoop;
							u = Graphs.randVertex(g, rand);
							v = Graphs.randVertex(g, rand);
							if (u.equals(v))
								continue;
							if (!parallelEdges && g.getEdge(u, v) != null)
								continue;
							break;
						}
						g.addEdge(u, v, Integer.valueOf(g.edges().size() + 1));
						expectedM++;
					}
					assertEquals(expectedN, g.vertices().size());
					assertEquals(expectedM, g.edges().size());
				}
				g.clearEdges();
				assertEquals(expectedN, g.vertices().size());
				assertEquals(0, g.edges().size());
			}
		});
	}

	static void testCopy(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (String copyType : List.of("origImpl", "array", "linked-list", "linked-list-ptr", "hashtable", "matrix")) {
			foreachBoolConfig(directed -> {
				/* Create a random graph g */
				Graph<Integer, Integer> g = GraphsTestUtils.withImpl(
						GraphsTestUtils.randGraph(100, 300, directed, false, false, seedGen.nextSeed()), graphImpl);

				/* assign some weights to the vertices of g */
				final String gVDataKey = "vData";
				WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
				Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
				for (Integer u : g.vertices()) {
					Object data = labeledObj("data" + u);
					gVData.set(u, data);
					gVDataMap.put(u, data);
				}

				/* assign some weights to the edges of g */
				final String gEDataKey = "eData";
				WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
				Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
				for (Integer e : g.edges()) {
					Object data = labeledObj("data" + e);
					gEData.set(e, data);
					gEDataMap.put(e, data);
				}

				/* Copy g */
				Graph<Integer, Integer> copy;
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
				for (Integer u : g.vertices()) {
					assertEquals(g.outEdges(u), copy.outEdges(u));
					assertEquals(g.inEdges(u), copy.inEdges(u));
				}

				/* Assert no weights were copied */
				IWeights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
				IWeights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
				assertNull(copyVData);
				assertNull(copyEData);
				assertEquals(copy.getVerticesWeightsKeys(), Collections.emptySet());
				assertEquals(copy.getEdgesWeightsKeys(), Collections.emptySet());
			});
		}
	}

	static void testCopyWithWeights(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			Graph<Integer, Integer> g = GraphsTestUtils.withImpl(
					GraphsTestUtils.randGraph(100, 300, directed, false, false, seedGen.nextSeed()), graphImpl);

			/* assign some weights to the vertices of g */
			final String gVDataKey = "vData";
			WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer u : g.vertices()) {
				Object data = labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final String gEDataKey = "eData";
			WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer e : g.edges()) {
				Object data = labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph<Integer, Integer> copy = g.copy(true, true);

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (Integer u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			WeightsObj<Integer, Object> copyVData = copy.getVerticesWeights(gVDataKey);
			WeightsObj<Integer, Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNotNull(copyVData);
			assertNotNull(copyEData);
			Object2ObjectMap<Integer, Object> copyVDataMap = new Object2ObjectOpenHashMap<>(gVDataMap);
			Object2ObjectMap<Integer, Object> copyEDataMap = new Object2ObjectOpenHashMap<>(gEDataMap);
			for (Integer u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (Integer e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}

			/* Reassign some weights to both g and copy, and assert they are updated independently */
			for (int ops = 0; ops < g.vertices().size() / 4; ops++) {
				Integer u = Graphs.randVertex(g, rand);
				Object data = labeledObj("data" + u + "new");
				g.<Object, WeightsObj<Integer, Object>>getVerticesWeights(gVDataKey).set(u, data);
				gVDataMap.put(u, data);
			}
			for (int ops = 0; ops < copy.vertices().size() / 4; ops++) {
				Integer u = Graphs.randVertex(copy, rand);
				Object data = labeledObj("data" + u + "new");
				copy.<Object, WeightsObj<Integer, Object>>getVerticesWeights(gVDataKey).set(u, data);
				copyVDataMap.put(u, data);
			}
			for (int ops = 0; ops < g.edges().size() / 4; ops++) {
				Integer e = Graphs.randEdge(g, rand);
				Object data = labeledObj("data" + e + "new");
				g.<Object, WeightsObj<Integer, Object>>getEdgesWeights(gEDataKey).set(e, data);
				gEDataMap.put(e, data);
			}
			for (int ops = 0; ops < copy.edges().size() / 4; ops++) {
				Integer e = Graphs.randEdge(copy, rand);
				Object data = labeledObj("data" + e + "new");
				copy.<Object, WeightsObj<Integer, Object>>getEdgesWeights(gEDataKey).set(e, data);
				copyEDataMap.put(e, data);
			}

			/* Assert the weights were updated independently */
			for (Integer u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (Integer e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}
		});
	}

	static void testImmutableCopy(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			boolean selfEdges = graphImpl.get(directed).isAllowSelfEdges();
			Graph<Integer, Integer> g = GraphsTestUtils.withImpl(
					GraphsTestUtils.randGraph(100, 300, directed, selfEdges, false, seedGen.nextSeed()), graphImpl);

			/* assign some weights to the vertices of g */
			final String gVDataKey = "vData";
			WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer u : g.vertices()) {
				Object data = labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final String gEDataKey = "eData";
			WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer e : g.edges()) {
				Object data = labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph<Integer, Integer> copy = g.immutableCopy();

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (Integer u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			IWeights<Object> copyVData = copy.getVerticesWeights(gVDataKey);
			IWeights<Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNull(copyVData);
			assertNull(copyEData);
			assertEquals(copy.getVerticesWeightsKeys(), Collections.emptySet());
			assertEquals(copy.getEdgesWeightsKeys(), Collections.emptySet());
		});
	}

	static void testImmutableCopyWithWeights(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			boolean selfEdges = graphImpl.get(directed).isAllowSelfEdges();
			Graph<Integer, Integer> g = GraphsTestUtils.withImpl(
					GraphsTestUtils.randGraph(100, 300, directed, selfEdges, false, seedGen.nextSeed()), graphImpl);

			/* assign some weights to the vertices of g */
			final String gVDataKey = "vData";
			WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer u : g.vertices()) {
				Object data = labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final String gEDataKey = "eData";
			WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer e : g.edges()) {
				Object data = labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* Copy g */
			Graph<Integer, Integer> copy = g.immutableCopy(true, true);

			/* Assert vertices and edges are the same */
			assertEquals(g.vertices().size(), copy.vertices().size());
			assertEquals(g.vertices(), copy.vertices());
			assertEquals(g.edges().size(), copy.edges().size());
			assertEquals(g.edges(), copy.edges());
			for (Integer u : g.vertices()) {
				assertEquals(g.outEdges(u), copy.outEdges(u));
				assertEquals(g.inEdges(u), copy.inEdges(u));
			}

			/* Assert weights were copied */
			WeightsObj<Integer, Object> copyVData = copy.getVerticesWeights(gVDataKey);
			WeightsObj<Integer, Object> copyEData = copy.getEdgesWeights(gEDataKey);
			assertNotNull(copyVData);
			assertNotNull(copyEData);
			Object2ObjectMap<Integer, Object> copyVDataMap = new Object2ObjectOpenHashMap<>(gVDataMap);
			Object2ObjectMap<Integer, Object> copyEDataMap = new Object2ObjectOpenHashMap<>(gEDataMap);
			for (Integer u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (Integer e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}

			/* Reassign some weights to g, and assert they are updated independently */
			for (int ops = 0; ops < g.vertices().size() / 4; ops++) {
				Integer u = Graphs.randVertex(g, rand);
				Object data = labeledObj("data" + u + "new");
				g.<Object, WeightsObj<Integer, Object>>getVerticesWeights(gVDataKey).set(u, data);
				gVDataMap.put(u, data);
			}
			for (int ops = 0; ops < g.edges().size() / 4; ops++) {
				Integer e = Graphs.randEdge(g, rand);
				Object data = labeledObj("data" + e + "new");
				g.<Object, WeightsObj<Integer, Object>>getEdgesWeights(gEDataKey).set(e, data);
				gEDataMap.put(e, data);
			}

			/* Assert the weights were updated independently */
			for (Integer u : g.vertices()) {
				assertEquals(gVDataMap.get(u), gVData.get(u));
				assertEquals(copyVDataMap.get(u), copyVData.get(u));
			}
			for (Integer e : g.edges()) {
				assertEquals(gEDataMap.get(e), gEData.get(e));
				assertEquals(copyEDataMap.get(e), copyEData.get(e));
			}
		});
	}

	static void testCopyConstructor(Function<IndexGraph, IndexGraph> copyConstructor, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			final boolean selfEdges = copyConstructor
					.apply(directed ? IndexGraph.newDirected() : IndexGraph.newUndirected()).isAllowSelfEdges();
			final boolean parallelEdges = copyConstructor
					.apply(directed ? IndexGraph.newDirected() : IndexGraph.newUndirected()).isAllowParallelEdges();
			IndexGraph g = GraphsTestUtils.randGraph(100, 300, directed, selfEdges, parallelEdges, seedGen.nextSeed())
					.indexGraph();

			/* assign some weights to the vertices of g */
			final String gVDataKey = "vData";
			WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer u : g.vertices()) {
				Object data = labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final String gEDataKey = "eData";
			WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer e : g.edges()) {
				Object data = labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* check copy constructor */
			assertEquals(g, copyConstructor.apply(g));
			assertEquals(g, copyConstructor.apply(g.immutableCopy(true, true)));
			assertEquals(g, copyConstructor.apply(g.immutableView()));
			assertEquals(g, copyConstructor.apply(copyConstructor.apply(g)));

			if (!selfEdges) {
				IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
				IndexGraph g1 = factory.allowSelfEdges().newGraph();
				g1.addVertex();
				g1.addEdge(0, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraph g1 = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
				g1.addVertex();
				g1.addVertex();
				g1.addEdge(0, 1);
				g1.addEdge(0, 1);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraph g1 = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
				g1.addVertex();
				g1.addVertex();
				g1.addEdge(1, 0);
				g1.addEdge(1, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
		});
	}

	static void testBuilderConstructor(Function<IndexGraphBuilder, IndexGraph> copyConstructor, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			final boolean selfEdges = copyConstructor.apply(IndexGraphBuilder.newInstance(directed)).isAllowSelfEdges();
			final boolean parallelEdges =
					copyConstructor.apply(IndexGraphBuilder.newInstance(directed)).isAllowParallelEdges();
			IndexGraph g = GraphsTestUtils.randGraph(100, 300, directed, selfEdges, parallelEdges, seedGen.nextSeed())
					.indexGraph();

			/* assign some weights to the vertices of g */
			final String gVDataKey = "vData";
			WeightsObj<Integer, Object> gVData = g.addVerticesWeights(gVDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gVDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer u : g.vertices()) {
				Object data = labeledObj("data" + u);
				gVData.set(u, data);
				gVDataMap.put(u, data);
			}

			/* assign some weights to the edges of g */
			final String gEDataKey = "eData";
			WeightsObj<Integer, Object> gEData = g.addEdgesWeights(gEDataKey, Object.class);
			Object2ObjectMap<Integer, Object> gEDataMap = new Object2ObjectOpenHashMap<>();
			for (Integer e : g.edges()) {
				Object data = labeledObj("data" + e);
				gEData.set(e, data);
				gEDataMap.put(e, data);
			}

			/* check builder constructor */
			assertEquals(g, copyConstructor.apply(IndexGraphBuilder.fromGraph(g, true, true)));

			if (!selfEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertex();
				g1.addEdge(0, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertex();
				g1.addVertex();
				g1.addEdge(0, 1);
				g1.addEdge(0, 1);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertex();
				g1.addVertex();
				g1.addEdge(1, 0);
				g1.addEdge(1, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
		});
	}

	static void testReverseEdge(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		final boolean selfEdges = graphImpl.get(true).isAllowSelfEdges();
		final boolean parallelEdges = graphImpl.get(true).isAllowParallelEdges();
		Graph<Integer, Integer> g1 = GraphsTestUtils.withImpl(
				GraphsTestUtils.randGraph(100, 300, true, selfEdges, parallelEdges, seedGen.nextSeed()), graphImpl);
		Graph<Integer, Integer> g2 = g1.copy(true, true);

		for (int ops = 0; ops < 10; ops++) {
			Integer e;
			do {
				e = Graphs.randEdge(g1, rand);
			} while (!parallelEdges && g1.getEdge(g1.edgeTarget(e), g1.edgeSource(e)) != null);

			Integer u = g1.edgeSource(e), v = g1.edgeTarget(e);
			g1.reverseEdge(e);
			assertEquals(v, g1.edgeSource(e));
			assertEquals(u, g1.edgeTarget(e));

			g2.removeEdge(e);
			g2.addEdge(v, u, e);
			assertEquals(g2, g1);
		}

		if (!parallelEdges) {
			Integer e = Graphs.randEdge(g1, rand);
			if (g1.getEdge(g1.edgeTarget(e), g1.edgeSource(e)) == null) {
				Integer nonExistingEdge;
				do {
					nonExistingEdge = Integer.valueOf(rand.nextInt());
				} while (g1.edges().contains(nonExistingEdge) || nonExistingEdge.intValue() < 0);
				g1.addEdge(g1.edgeTarget(e), g1.edgeSource(e), nonExistingEdge);
			}

			assertThrows(IllegalArgumentException.class, () -> g1.reverseEdge(e));
		}
	}

	static void testMoveEdge(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		final long seed = 0x5aaa87a14dbb6a83L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		foreachBoolConfig(directed -> {
			final boolean selfEdges = graphImpl.get(true).isAllowSelfEdges();
			final boolean parallelEdges = graphImpl.get(true).isAllowParallelEdges();
			Graph<Integer, Integer> g1 = GraphsTestUtils.withImpl(
					GraphsTestUtils.randGraph(100, 300, true, selfEdges, parallelEdges, seedGen.nextSeed()), graphImpl);
			Graph<Integer, Integer> g2 = g1.copy(true, true);

			for (int ops = 0; ops < 20; ops++) {
				Integer e = Graphs.randEdge(g1, rand);
				if (ops == 3 && selfEdges)
					e = Graphs.selfEdges(g1).iterator().next();
				Integer oldSource = g1.edgeSource(e), oldTarget = g1.edgeTarget(e);

				Integer newSource, newTarget;
				for (;;) {
					newSource = Graphs.randVertex(g1, rand);
					newTarget = Graphs.randVertex(g1, rand);
					if (!selfEdges && newSource.equals(newTarget))
						continue;
					if (!parallelEdges && g1.getEdge(newSource, newTarget) != null)
						continue;
					break;
				}
				if (ops == 0) {
					newSource = oldSource;
					newTarget = oldTarget;
				} else if (ops == 1) {
					newSource = oldTarget;
					newTarget = oldSource;
				} else if (ops == 2 && selfEdges) {
					newTarget = newSource;
				}
				g1.moveEdge(e, newSource, newTarget);
				if (directed) {
					assertEquals(newSource, g1.edgeSource(e));
					assertEquals(newTarget, g1.edgeTarget(e));
				} else {
					assertTrue((newSource.equals(g1.edgeSource(e)) && newTarget.equals(g1.edgeTarget(e)))
							|| (newSource.equals(g1.edgeTarget(e)) && newTarget.equals(g1.edgeSource(e))));
				}

				g2.removeEdge(e);
				g2.addEdge(newSource, newTarget, e);

				for (Integer v : List.of(oldSource, oldTarget, newSource, newTarget)) {
					assertEquals(g1.outEdges(v).size(), g2.outEdges(v).size());
					assertEquals(g1.outEdges(v), g2.outEdges(v));
					assertEquals(g1.inEdges(v).size(), g2.inEdges(v).size());
					assertEquals(g1.inEdges(v), g2.inEdges(v));

					Set<Integer> iteratedEdges = new ObjectOpenHashSet<>();
					for (Integer e1 : g1.outEdges(v))
						assertTrue(iteratedEdges.add(e1));
					assertEquals(iteratedEdges, g2.outEdges(v));
					iteratedEdges.clear();
					for (Integer e1 : g1.inEdges(v))
						assertTrue(iteratedEdges.add(e1));
					assertEquals(iteratedEdges, g2.inEdges(v));
				}

				assertEquals(g2, g1);
			}

			if (!selfEdges) {
				Integer e = Graphs.randEdge(g1, rand);
				Integer v = Graphs.randVertex(g1, rand);
				assertThrows(IllegalArgumentException.class, () -> g1.moveEdge(e, v, v));
			}

			if (!parallelEdges) {
				Integer e = Graphs.randEdge(g1, rand);

				Integer newSource, newTarget;
				for (;;) {
					newSource = Graphs.randVertex(g1, rand);
					newTarget = Graphs.randVertex(g1, rand);
					if (!selfEdges && newSource.equals(newTarget))
						continue;
					if (!parallelEdges && g1.getEdge(newSource, newTarget) != null)
						continue;
					break;
				}

				Integer nonExistingEdge;
				do {
					nonExistingEdge = Integer.valueOf(rand.nextInt());
				} while (g1.edges().contains(nonExistingEdge) || nonExistingEdge.intValue() < 0);
				g1.addEdge(newSource, newTarget, nonExistingEdge);

				Integer newSource0 = newSource, newTarget0 = newTarget;
				assertThrows(IllegalArgumentException.class, () -> g1.moveEdge(e, newSource0, newTarget0));
			}
		});
	}

	static void testUndirectedMST(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		MinimumSpanningTreeTestUtils.testRandGraph(MinimumSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMDST(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		MinimumDirectedSpanningTreeTarjanTest.testRandGraph(MinimumDirectedSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMaxFlow(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		MaximumFlowTestUtils.testRandGraphs(MaximumFlow.newInstance(), graphImpl, seed, /* directed= */ true);
	}

	static void testUndirectedBipartiteMatching(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(
				MatchingAlgo.builder().setBipartite(true).setCardinality(true).build(), graphImpl, seed);
	}

	static void testUndirectedBipartiteMatchingWeighted(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl,
			long seed) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingAlgo.builder().setBipartite(true).build(),
				graphImpl, seed);
	}

	static void testRandOps(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, boolean directed, long seed) {
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

	private static void testRandOps(Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, boolean directed, int n,
			int m, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		boolean selfEdges = graphImpl.get(directed).isAllowSelfEdges();
		Graph<Integer, Integer> g = GraphsTestUtils
				.withImpl(GraphsTestUtils.randGraph(n, m, directed, selfEdges, false, seedGen.nextSeed()), graphImpl);
		final int opsNum = 128;
		testRandOps(g, opsNum, seedGen.nextSeed());
	}

	private static class RandWeighted<T> {
		private final List<T> elms = new ObjectArrayList<>();
		private final IntList weights = new IntArrayList();
		private int totalWeight;

		void add(T elm, int weight) {
			if (weight <= 0)
				throw new IllegalArgumentException();
			elms.add(elm);
			weights.add(weight);
			totalWeight += weight;
		}

		T get(Random rand) {
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
		private final String dataKey;
		private final boolean debugPrints = false;

		GraphTracker(Graph<Integer, Integer> g, String dataKey) {
			this.directed = g.isDirected();
			this.dataKey = dataKey;

			if (g instanceof IndexGraph) {
				((IndexGraph) g).addVertexRemoveListener(new IndexRemoveListener() {

					@Override
					public void swapAndRemove(int removedIdx, int swappedIdx) {
						/* we do only swap, remove is done outside */
						Vertex v1 = getVertex(removedIdx), v2 = getVertex(swappedIdx);
						v1.id = swappedIdx;
						v2.id = removedIdx;
						vertices.put(removedIdx, v2);
						vertices.put(swappedIdx, v1);
					}

					@Override
					public void removeLast(int removedIdx) {}
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
			return randElement(verticesArr, rand);
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
			return randElement(edges, rand);
		}

		@SuppressWarnings("unused")
		void clearEdges() {
			if (debugPrints)
				System.out.println("clearEdges()");
			edges.clear();
		}

		void checkEdgesEqual(Graph<Integer, Integer> g) {
			assertEquals(edgesNum(), g.edges().size());
			WeightsInt<Integer> edgeData = g.getEdgesWeights(dataKey);

			List<IntList> actual = new ObjectArrayList<>();
			List<IntList> expected = new ObjectArrayList<>();

			for (Integer e : g.edges()) {
				int u = g.edgeSource(e).intValue(), v = g.edgeTarget(e).intValue();
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edgeData.get(e);
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

	private static void testRandOps(Graph<Integer, Integer> g, int opsNum, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
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

		if (g.isDirected()) {
			opRand.add(GraphOp.RemoveEdgesInOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesInOfVertexUsingEdgeSet, 1);
			opRand.add(GraphOp.RemoveEdgesInOfVertexUsingIter, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertex, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertexUsingEdgeSet, 1);
			opRand.add(GraphOp.RemoveEdgesOutOfVertexUsingIter, 1);
			opRand.add(GraphOp.ReverseEdge, 6);
		}

		final String dataKey = "data";
		WeightsInt<Integer> edgeData = g.addEdgesWeights(dataKey, int.class);
		UniqueGenerator dataGen = new UniqueGenerator(seedGen.nextSeed());

		GraphTracker tracker = new GraphTracker(g, dataKey);
		for (int v : g.vertices()) {
			// final int data = dataGen.next();
			// edgeData.set(e, data);
			tracker.addVertex(v);
		}
		for (Integer e : g.edges()) {
			Integer u = g.edgeSource(e), v = g.edgeTarget(e);
			final int data = dataGen.next();
			edgeData.set(e, data);
			tracker.addEdge(tracker.getVertex(u.intValue()), tracker.getVertex(v.intValue()), data);
		}

		ToIntFunction<Set<Integer>> idSupplier = ids -> {
			for (;;) {
				int e = rand.nextInt();
				if (e >= 1 && !ids.contains(Integer.valueOf(e)))
					return e;
			}
		};
		Supplier<Integer> vertexSupplier = () -> Integer.valueOf(idSupplier.applyAsInt(g.vertices()));
		Supplier<Integer> edgeSupplier = () -> Integer.valueOf(idSupplier.applyAsInt(g.edges()));
		ToIntFunction<GraphTracker.Edge> getEdge = edge -> {
			int e = -1;
			for (EdgeIter<Integer, Integer> eit =
					g.getEdges(Integer.valueOf(edge.u.id), Integer.valueOf(edge.v.id)).iterator(); eit.hasNext();) {
				Integer e0 = eit.next();
				if (edge.data == edgeData.get(e0)) {
					e = e0.intValue();
					break;
				}
			}
			assertTrue(e != -1, "edge not found");
			return e;
		};

		opLoop: while (opsNum > 0) {
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
						if (!g.isAllowSelfEdges() && u == v)
							continue;
						if (!g.isAllowParallelEdges() && tracker.getEdge(u, v) != null)
							continue;
						break;
					}

					final int data = dataGen.next();
					Integer e = edgeSupplier.get();
					g.addEdge(Integer.valueOf(u.id), Integer.valueOf(v.id), e);
					edgeData.set(e, data);
					tracker.addEdge(u, v, data);
					break;
				}
				case RemoveEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					int e = getEdge.applyAsInt(edge);

					g.removeEdge(Integer.valueOf(e));
					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingOutIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex source = edge.u;

					Set<GraphTracker.Edge> iterationExpected = new ObjectOpenHashSet<>();
					for (Integer eOther : g.outEdges(Integer.valueOf(source.id))) {
						if (edgeData.get(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.get(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new ObjectOpenHashSet<>();
					for (EdgeIter<Integer, Integer> it = g.outEdges(Integer.valueOf(source.id)).iterator(); it
							.hasNext();) {
						Integer eOther = it.next();
						if (edgeData.get(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.get(eOther));
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
					Integer e = Integer.valueOf(getEdge.applyAsInt(edge));

					EdgeSet<Integer, Integer> edgeSet = g.outEdges(Integer.valueOf(source.id));
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
					Integer e = Integer.valueOf(getEdge.applyAsInt(edge));

					EdgeSet<Integer, Integer> edgeSet = g.inEdges(Integer.valueOf(target.id));
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
					Integer e = Integer.valueOf(getEdge.applyAsInt(edge));

					EdgeSet<Integer, Integer> edgeSet =
							g.getEdges(Integer.valueOf(source.id), Integer.valueOf(target.id));
					assertTrue(edgeSet.contains(e));

					Integer nonExistingEdge;
					do {
						nonExistingEdge = Integer.valueOf(rand.nextInt());
					} while (edgeSet.contains(nonExistingEdge));
					assertFalse(edgeSet.remove(nonExistingEdge));

					if (Set.of(e).equals(edgeSet) && rand.nextBoolean()) {
						edgeSet.clear();
						assertTrue(edgeSet.isEmpty());
						edgeSet.clear();
						assertTrue(edgeSet.isEmpty());
					} else {
						boolean removed = edgeSet.remove(e);
						assertTrue(removed);
						removed = edgeSet.remove(e);
						assertFalse(removed);
					}

					tracker.removeEdge(edge);
					break;
				}
				case RemoveEdgeUsingInIter: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					GraphTracker.Vertex target = edge.v;

					Set<GraphTracker.Edge> iterationExpected = new ObjectOpenHashSet<>();
					for (Integer eOther : g.inEdges(Integer.valueOf(target.id))) {
						if (edgeData.get(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.get(eOther));
							boolean duplication = !iterationExpected.add(edgeOther);
							assertFalse(duplication);
						}
					}
					boolean removed = false;
					Set<GraphTracker.Edge> iterationActual = new ObjectOpenHashSet<>();
					for (EdgeIter<Integer, Integer> it = g.inEdges(Integer.valueOf(target.id)).iterator(); it
							.hasNext();) {
						Integer eOther = it.next();
						if (edgeData.get(eOther) != edge.data) {
							GraphTracker.Edge edgeOther = tracker.getEdge(edgeData.get(eOther));
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
					g.removeEdgesOf(Integer.valueOf(u.id));
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.outEdges(Integer.valueOf(u.id)).clear();
					assertTrue(g.outEdges(Integer.valueOf(u.id)).isEmpty());
					g.inEdges(Integer.valueOf(u.id)).clear();
					assertTrue(g.inEdges(Integer.valueOf(u.id)).isEmpty());
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter<Integer, Integer> it = g.outEdges(Integer.valueOf(u.id)).iterator(); it.hasNext();) {
						it.next();
						it.remove();
					}
					for (EdgeIter<Integer, Integer> it = g.inEdges(Integer.valueOf(u.id)).iterator(); it.hasNext();) {
						it.next();
						it.remove();
					}
					tracker.removeEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeInEdgesOf(Integer.valueOf(u.id));
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.inEdges(Integer.valueOf(u.id)).clear();
					assertTrue(g.inEdges(Integer.valueOf(u.id)).isEmpty());
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesInOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter<Integer, Integer> it = g.inEdges(Integer.valueOf(u.id)).iterator(); it.hasNext();) {
						it.next();
						it.remove();
					}
					tracker.removeInEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.removeOutEdgesOf(Integer.valueOf(u.id));
					tracker.removeOutEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertexUsingEdgeSet: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					g.outEdges(Integer.valueOf(u.id)).clear();
					assertTrue(g.outEdges(Integer.valueOf(u.id)).isEmpty());
					tracker.removeOutEdgesOf(u);
					break;
				}
				case RemoveEdgesOutOfVertexUsingIter: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex u = tracker.getRandVertex(rand);
					for (EdgeIter<Integer, Integer> it = g.outEdges(Integer.valueOf(u.id)).iterator(); it.hasNext();) {
						it.next();
						it.remove();
					}
					tracker.removeOutEdgesOf(u);
					break;
				}
				case ReverseEdge: {
					if (tracker.edgesNum() == 0)
						continue;
					GraphTracker.Edge edge = tracker.getRandEdge(rand);
					if (edge.u != edge.v && g.getEdge(Integer.valueOf(edge.v.id), Integer.valueOf(edge.u.id)) != null
							&& !g.isAllowParallelEdges())
						continue;
					int e = getEdge.applyAsInt(edge);

					g.reverseEdge(Integer.valueOf(e));
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
					Integer v = vertexSupplier.get();
					g.addVertex(v);
					tracker.addVertex(v.intValue());
					break;
				}
				case RemoveVertex:
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex v = tracker.getRandVertex(rand);
					g.removeVertex(Integer.valueOf(v.id));
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

			if (g.isDirected()) {
				int totalOutDegree = 0, totalInDegree = 0;
				for (Integer v : g.vertices()) {
					int outDegree = g.outEdges(v).size(), inDegree = g.inEdges(v).size();
					assertTrue(inDegree >= 0);
					assertTrue(outDegree >= 0);
					totalOutDegree += outDegree;
					totalInDegree += inDegree;
				}
				assertEquals(g.edges().size(), totalOutDegree);
				assertEquals(g.edges().size(), totalInDegree);
			}

			opsNum--;
		}
	}

	private static class LabeledObj {
		private final String s;

		LabeledObj(String label) {
			this.s = Objects.requireNonNull(label);
		}

		@Override
		public String toString() {
			return s;
		}
	}

	static Object labeledObj(String label) {
		return new LabeledObj(label);
	}

}
