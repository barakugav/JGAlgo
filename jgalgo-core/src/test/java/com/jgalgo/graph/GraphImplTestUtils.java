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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
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
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
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

@SuppressWarnings("boxing")
class GraphImplTestUtils extends TestUtils {

	@SafeVarargs
	static <K> Set<K> setOf(K... elms) {
		ObjectSet<K> set = new ObjectOpenHashSet<>();
		for (K e : elms)
			set.add(e);
		return ObjectSets.unmodifiable(set);
	}

	@SuppressWarnings("deprecation")
	static void testAddVertex(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int i : range(n)) {
				int v = i + 1;
				g.addVertex(Integer.valueOf(v));
				verticesSet.add(v);
			}
			assertEquals(verticesSet, g.vertices());
			assertEquals(IntSets.emptySet(), g.edges());

			assertThrows(NoSuchVertexException.class, () -> g.outEdges(6687));
		});
		foreachBoolConfig(directed -> {
			IndexGraph g = graphImpl.apply(directed).indexGraph();
			IdBuilderInt vBuilder = g.vertexBuilder();
			final int n = 87;
			for (int i : range(n)) {
				int expected = i;
				int actual1 = vBuilder.build(g.vertices());
				int actual2 = g.addVertexInt();
				assertEquals(expected, actual1);
				assertEquals(expected, actual2);
			}
			assertEquals(range(n), g.vertices());

			for (int i : range(20)) {
				if (i % 2 == 0) {
					g.addVertex(g.vertices().size());
				} else {
					assertThrows(IllegalArgumentException.class, () -> g.addVertex(g.vertices().size() * 2 + 7));
				}
			}
		});
	}

	static void addVerticesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0x4a4735619a4c9042L);

		/* addVertices() valid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = graphImpl.apply(directed);

				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int num = Math.min(rand.nextInt(5), n - vertices.size());
					List<Integer> vs = new ArrayList<>();
					while (vs.size() < num) {
						int v = rand.nextInt();
						if (v < 0 || vertices.contains(v) || vs.contains(v))
							continue;
						vs.add(v);
					}
					g.addVertices(vs);
					vertices.addAll(vs);
				}
				assertEquals(vertices, g.vertices());
			}
		});

		/* addVertices() sometimes with duplicate vertex (in added list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = graphImpl.apply(directed);

				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int num = Math.min(rand.nextInt(5), n - vertices.size());
					List<Integer> vs = new ArrayList<>();
					while (vs.size() < num) {
						int v = rand.nextInt();
						if (v < 0 || vertices.contains(v) || vs.contains(v))
							continue;
						vs.add(v);
					}
					if (vs.isEmpty() || rand.nextBoolean()) {
						g.addVertices(vs);
						vertices.addAll(vs);
					} else {
						vs.add(randElement(vs, rand)); /* duplicate element */
						Collections.shuffle(vs, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
					}
				}
				assertEquals(vertices, g.vertices());
			}
		});

		/* addVertices() sometimes with existing vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = graphImpl.apply(directed);

				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int num = Math.min(rand.nextInt(5), n - vertices.size());
					List<Integer> vs = new ArrayList<>();
					while (vs.size() < num) {
						int v = rand.nextInt();
						if (v < 0 || vertices.contains(v) || vs.contains(v))
							continue;
						vs.add(v);
					}
					if (vertices.isEmpty() || rand.nextBoolean()) {
						g.addVertices(vs);
						vertices.addAll(vs);
					} else {
						vs.add(Graphs.randVertex(g, rand)); /* duplicate element */
						Collections.shuffle(vs, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
					}
				}
				assertEquals(vertices, g.vertices());
			}
		});

		/* addVertices() sometimes with null vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = graphImpl.apply(directed);

				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int num = Math.min(rand.nextInt(5), n - vertices.size());
					List<Integer> vs = new ArrayList<>();
					while (vs.size() < num) {
						int v = rand.nextInt();
						if (v < 0 || vertices.contains(v) || vs.contains(v))
							continue;
						vs.add(v);
					}
					if (vs.isEmpty() || rand.nextBoolean()) {
						g.addVertices(vs);
						vertices.addAll(vs);
					} else {
						vs.add(null);
						Collections.shuffle(vs, rand);
						assertThrows(NullPointerException.class, () -> g.addVertices(vs));
					}
				}
				assertEquals(vertices, g.vertices());
			}
		});

		/* addVertices() index graph from range() */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					g.addVertices(range(verticesNum, verticesNum + num));
					verticesNum += num;
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() index graph from range() sometimes invalid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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

		/* addVertices() index graph from other graph vertices */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

				int verticesNum = 0;
				while (verticesNum < n) {
					int num = Math.min(rand.nextInt(5), n - verticesNum);
					IndexGraph g0 = IndexGraph.newUndirected();
					g0.addVertices(range(num));

					if (verticesNum == 0) {
						g.addVertices(g0.vertices());
						verticesNum += num;
					} else if (num > 0 && rand.nextBoolean()) {
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(g0.vertices()));
					} else {
						g.addVertices(range(verticesNum, verticesNum + num));
						verticesNum += num;
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() index graph from sorted list */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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

		/* addVertices() index graph from arbitrary collection */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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

		/* addVertices() index graph from arbitrary collection duplicate vertex (in list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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

		/* addVertices() index graph from arbitrary collection with existing vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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
						vs0.add(Graphs.randVertex(g, rand));
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs0));
					}
				}
				assertEquals(range(verticesNum), g.vertices());
			}
		});

		/* addVertices() index graph from arbitrary collection not in range */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				IndexGraph g = graphImpl.apply(directed).indexGraph();

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

	static void removeVerticesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0x36f43a2a1e51d79dL);

		Function<Boolean, Graph<Integer, Integer>> createGraph = directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(50 + rand.nextInt(100)));
			final int m = 100 + rand.nextInt(100);
			while (g.edges().size() < m) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!g.isAllowSelfEdges() && u.equals(v))
					continue;
				if (!g.isAllowParallelEdges() && g.containsEdge(u, v))
					continue;
				if (!g.isDirected() && u.intValue() > v.intValue()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				g.addEdge(u, v, Integer.valueOf(g.edges().size()));
			}
			return g;
		};

		/* removeVertices() valid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> vertices = new IntOpenHashSet(expectedGraph.vertices());
				while (vertices.size() > 0) {
					int num = Math.min(rand.nextInt(5), vertices.size());
					Set<Integer> vs = new HashSet<>();
					while (vs.size() < num)
						vs.add(Graphs.randVertex(g, rand));
					g.removeVertices(vs);
					vertices.removeAll(vs);
					for (Integer v : vs)
						expectedGraph.removeVertex(v);
					assertEquals(vertices, g.vertices());
					assertEquals(expectedGraph, g);
				}
			}
		});

		/* removeVertices() sometimes with duplicate vertex (in removed list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> vertices = new IntOpenHashSet(expectedGraph.vertices());
				while (vertices.size() > 0) {
					int num = Math.min(rand.nextInt(5), vertices.size());
					Set<Integer> vs = new HashSet<>();
					while (vs.size() < num)
						vs.add(Graphs.randVertex(g, rand));
					if (vs.isEmpty() || rand.nextBoolean()) {
						g.removeVertices(vs);
						vertices.removeAll(vs);
						for (Integer v : vs)
							expectedGraph.removeVertex(v);
						assertEquals(vertices, g.vertices());
						assertEquals(expectedGraph, g);
					} else {
						List<Integer> vs0 = new ArrayList<>(vs);
						vs0.add(randElement(vs0, rand)); /* duplicate element */
						Collections.shuffle(vs0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.removeVertices(vs0));
					}
				}
			}
		});

		/* removeVertices() sometimes with non existing vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> vertices = new IntOpenHashSet(expectedGraph.vertices());
				while (vertices.size() > 0) {
					int num = Math.min(rand.nextInt(5), vertices.size());
					Set<Integer> vs = new HashSet<>();
					while (vs.size() < num)
						vs.add(Graphs.randVertex(g, rand));
					if (rand.nextBoolean()) {
						g.removeVertices(vs);
						vertices.removeAll(vs);
						for (Integer v : vs)
							expectedGraph.removeVertex(v);
						assertEquals(vertices, g.vertices());
						assertEquals(expectedGraph, g);
					} else {
						vs.add(nonExistingVertex(g, rand)); /* non existing element */
						List<Integer> vs0 = new ArrayList<>(vs);
						Collections.shuffle(vs0, rand);
						assertThrows(NoSuchVertexException.class, () -> g.removeVertices(vs0));
					}
				}
			}
		});

		/* removeVertices() sometimes with null vertex */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> vertices = new IntOpenHashSet(expectedGraph.vertices());
				while (vertices.size() > 0) {
					int num = Math.min(rand.nextInt(5), vertices.size());
					Set<Integer> vs = new HashSet<>();
					while (vs.size() < num)
						vs.add(Graphs.randVertex(g, rand));
					if (rand.nextBoolean()) {
						g.removeVertices(vs);
						vertices.removeAll(vs);
						for (Integer v : vs)
							expectedGraph.removeVertex(v);
						assertEquals(vertices, g.vertices());
						assertEquals(expectedGraph, g);
					} else {
						List<Integer> vs0 = new ArrayList<>(vs);
						vs0.add(null);
						Collections.shuffle(vs0, rand);
						assertThrows(NullPointerException.class, () -> g.removeVertices(vs0));
					}
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	static void verticesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig((directed, index) -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			if (index)
				g = g.indexGraph();
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int v : range(n)) {
				g.addVertex(Integer.valueOf(v));
				verticesSet.add(v);
			}
			assertEquals(verticesSet, g.vertices());
			assertEquals(g.vertices(), verticesSet);
			assertEquals(verticesSet.hashCode(), g.vertices().hashCode());
			assertEquals(IntSets.emptySet(), g.edges());

			/* toArray() */
			Object[] arr1 = g.vertices().toArray();
			Integer[] arr2 = g.vertices().toArray(new Integer[0]);
			assertEquals(g.vertices(), Set.of(arr1)); /* Set.of() checks that there are no duplications */
			assertEquals(g.vertices(), Set.of(arr2));
			if (g.vertices() instanceof IntSet) {
				int[] arr3 = ((IntSet) g.vertices()).toIntArray();
				int[] arr4 = ((IntSet) g.vertices()).toIntArray(new int[0]);
				int[] arr5Input = new int[g.vertices().size()];
				int[] arr5 = ((IntSet) g.vertices()).toIntArray(arr5Input);
				int[] arr6Input = new int[g.vertices().size() + 7];
				Arrays.fill(arr6Input, -18);
				int[] arr6 = ((IntSet) g.vertices()).toIntArray(arr6Input);
				assertEquals(g.vertices(), IntSet.of(arr3)); /* IntSet.of() checks that there are no duplications */
				assertEquals(g.vertices(), IntSet.of(arr4));
				assertEquals(g.vertices(), IntSet.of(arr5));
				assertEquals(g.vertices(), IntSet.of(Arrays.copyOf(arr6, g.vertices().size())));
				assertTrue(arr5Input == arr5);
				assertTrue(arr6Input == arr6);
				for (int i : range(g.vertices().size(), arr6Input.length))
					assertEquals(-18, arr6[i]);
			}
		});
	}

	@SuppressWarnings("deprecation")
	static void testAddEdge(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Int2ObjectMap<int[]> edges = new Int2ObjectOpenHashMap<>();
			for (int uIdx : range(n)) {
				for (int vIdx : range(uIdx + 1, n)) {
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
			final int n = 100;
			IndexGraph g = graphImpl.apply(directed).indexGraph();
			g.addVertices(range(n));
			IdBuilderInt eBuilder = g.edgeBuilder();
			for (int i : range(n)) {
				int u = i, v = (i + 1) % n;
				int expected = g.edges().size();
				int actual1 = eBuilder.build(g.edges());
				int actual2 = g.addEdge(u, v);
				assertEquals(expected, actual1);
				assertEquals(expected, actual2);
			}
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			if (!g.isAllowSelfEdges()) {
				g.addVertex(0);
				assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 0, 0));
			}
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			if (!g.isAllowParallelEdges()) {
				g.addVertex(0);
				g.addVertex(1);
				g.addEdge(0, 1, 0);
				assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 1, 1));
				if (!directed)
					assertThrows(IllegalArgumentException.class, () -> g.addEdge(1, 0, 1));
			}
		});
		foreachBoolConfig(directed -> {
			IndexGraph g = graphImpl.apply(directed).indexGraph();
			final int n = 87;
			for (int i = 0; i < n; i++)
				g.addVertexInt();

			for (int i : range(20)) {
				int u = i, v = i + 1;
				if (i % 2 == 0) {
					g.addEdge(u, v, g.edges().size());
				} else {
					assertThrows(IllegalArgumentException.class, () -> g.addEdge(u, v, g.edges().size() * 2 + 7));
				}
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

	static EdgeSet<Integer, Integer> edgeSetFromList(List<Integer> ids, List<Pair<Integer, Integer>> endpoints,
			Random rand) {
		boolean containsNull = false;
		containsNull |= ids.contains(null);
		containsNull |= endpoints.stream().anyMatch(e -> e.first() == null || e.second() == null);
		if (containsNull || rand.nextBoolean()) {
			return new EdgeSetFromList<>(ids, endpoints);
		} else {
			return new IEdgeSetFromList(ids, endpoints);
		}
	}

	private static class EdgeSetFromList<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		private final List<E> ids;
		private final List<Pair<V, V>> endpoints;

		EdgeSetFromList(List<E> ids, List<Pair<V, V>> endpoints) {
			this.ids = ids;
			this.endpoints = endpoints;
			assert ids.size() == endpoints.size();
		}

		@Override
		public int size() {
			return ids.size();
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new EdgeIter<>() {
				int idx;

				@Override
				public boolean hasNext() {
					return idx < ids.size();
				}

				@Override
				public E next() {
					return ids.get(idx++);
				}

				@Override
				public E peekNext() {
					return ids.get(idx);
				}

				@Override
				public V source() {
					return endpoints.get(idx - 1).first();
				}

				@Override
				public V target() {
					return endpoints.get(idx - 1).second();
				}
			};
		}
	}

	private static class IEdgeSetFromList extends AbstractIntSet implements IEdgeSet {

		private final List<Integer> ids;
		private final List<Pair<Integer, Integer>> endpoints;

		IEdgeSetFromList(List<Integer> ids, List<Pair<Integer, Integer>> endpoints) {
			this.ids = ids;
			this.endpoints = endpoints;
			assert ids.size() == endpoints.size();
		}

		@Override
		public int size() {
			return ids.size();
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {
				int idx;

				@Override
				public boolean hasNext() {
					return idx < ids.size();
				}

				@Override
				public int nextInt() {
					return ids.get(idx++);
				}

				@Override
				public int peekNextInt() {
					return ids.get(idx);
				}

				@Override
				public int sourceInt() {
					return endpoints.get(idx - 1).first();
				}

				@Override
				public int targetInt() {
					return endpoints.get(idx - 1).second();
				}
			};
		}
	}

	private static List<Pair<Integer, Integer>> randEndpoints(Graph<Integer, Integer> g, int numberOfEdges,
			Random rand) {
		List<Pair<Integer, Integer>> edges = new ArrayList<>();
		while (edges.size() < numberOfEdges) {
			Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
			if (!g.isAllowSelfEdges() && u.equals(v))
				continue;
			if (!g.isDirected() && u.intValue() > v.intValue()) {
				Integer tmp = u;
				u = v;
				v = tmp;
			}
			Pair<Integer, Integer> endpoints = Pair.of(u, v);
			if (!g.isAllowParallelEdges() && (g.containsEdge(u, v) || edges.contains(endpoints)))
				continue;
			edges.add(endpoints);
		}
		return edges;
	}

	private static List<Integer> randEdgesIds(Graph<Integer, Integer> g, int numberOfEdges, Random rand) {
		List<Integer> ids = new ArrayList<>();
		if (g instanceof IndexGraph) {
			ids.addAll(range(g.edges().size(), g.edges().size() + numberOfEdges));
			if (rand.nextBoolean())
				Collections.shuffle(ids, rand);
		} else {
			while (ids.size() < numberOfEdges) {
				Integer e = Integer.valueOf(rand.nextInt());
				if (e.intValue() < 0 || g.edges().contains(e) || ids.contains(e))
					continue;
				ids.add(e);
			}
		}
		return ids;
	}

	private static void addEdgesToExpected(Map<Integer, Pair<Integer, Integer>> edgesMap,
			EdgeSet<Integer, Integer> edgeSet) {
		for (EdgeIter<Integer, Integer> eit = edgeSet.iterator(); eit.hasNext();) {
			Integer e = eit.next();
			Pair<Integer, Integer> endpoints = Pair.of(eit.source(), eit.target());
			Object oldVal = edgesMap.put(e, endpoints);
			assertNull(oldVal);
		}
	}

	private static void assertExpectedEdges(Map<Integer, Pair<Integer, Integer>> expectedEdges,
			Graph<Integer, Integer> g) {
		assertEquals(expectedEdges.keySet(), g.edges());
		for (Integer e : expectedEdges.keySet()) {
			Pair<Integer, Integer> endpoints = expectedEdges.get(e);
			assertEndpoints(g, e, endpoints.first(), endpoints.second());
		}
		Map<Integer, Set<Integer>> outEdges = new Int2ObjectOpenHashMap<>();
		Map<Integer, Set<Integer>> inEdges;
		if (g.isDirected()) {
			inEdges = new Int2ObjectOpenHashMap<>();
			for (var entry : expectedEdges.entrySet()) {
				Integer edge = entry.getKey();
				Integer source = entry.getValue().first();
				Integer target = entry.getValue().second();
				outEdges.computeIfAbsent(source, k -> new IntOpenHashSet()).add(edge);
				inEdges.computeIfAbsent(target, k -> new IntOpenHashSet()).add(edge);
			}
		} else {
			inEdges = outEdges;
			for (var entry : expectedEdges.entrySet()) {
				Integer edge = entry.getKey();
				Integer source = entry.getValue().first();
				Integer target = entry.getValue().second();
				outEdges.computeIfAbsent(source, k -> new IntOpenHashSet()).add(edge);
				outEdges.computeIfAbsent(target, k -> new IntOpenHashSet()).add(edge);
			}
		}
		for (Integer v : g.vertices()) {
			assertEquals(outEdges.getOrDefault(v, Set.of()), g.outEdges(v));
			assertEquals(inEdges.getOrDefault(v, Set.of()), g.inEdges(v));
		}
	}

	static void addEdgesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0xa6e1e4b317a0d1c1L);

		/* addEdges() valid */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size());
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
					g.addEdges(addedEdges);
					addEdgesToExpected(expectedEdges, addedEdges);
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with duplicate edge id (in added list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = graphImpl.apply(directed);
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean duplicateEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (duplicateEdge ? 2 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!duplicateEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedIds.set(0, addedIds.get(1)); /* duplicate id */
						Collections.shuffle(addedIds, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with existing edge */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean duplicateEdge = !expectedEdges.isEmpty() && rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (duplicateEdge ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!duplicateEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedIds.set(0, Graphs.randEdge(g, rand)); /* duplicate edge */
						Collections.shuffle(addedIds, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with null edge */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean nullEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (nullEdge ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!nullEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedIds.set(0, null); /* null edge */
						Collections.shuffle(addedIds, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(NullPointerException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() int graph sometimes with negative edge */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				if (!(g0 instanceof IntGraph))
					return;
				IntGraph g = (IntGraph) g0;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean nullEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (nullEdge ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!nullEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedIds.set(0, Integer.valueOf(-1)); /* negative edge */
						Collections.shuffle(addedIds, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with invalid endpoint */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean invalidEndpoint = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (invalidEndpoint ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!invalidEndpoint) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						Integer nonExistingVertex = nonExistingVertex(g, rand);
						if (rand.nextBoolean()) {
							addedEndpoints.set(0, Pair.of(nonExistingVertex, addedEndpoints.get(0).second()));
						} else {
							addedEndpoints.set(0, Pair.of(addedEndpoints.get(0).first(), nonExistingVertex));
						}
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(NoSuchVertexException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with self edge */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				if (g.isAllowSelfEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean selfEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (selfEdge ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!selfEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, Pair.of(addedEndpoints.get(0).first(), addedEndpoints.get(0).first()));
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with parallel edges with existing */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				if (g.isAllowParallelEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean parallelEdge = !expectedEdges.isEmpty() && rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (parallelEdge ? 1 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!parallelEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, expectedEdges.get(Graphs.randEdge(g, rand))); /* parallel */
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() sometimes with parallel edges in added list */
		foreachBoolConfig((directed, index) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g0 = graphImpl.apply(directed);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;
				if (g.isAllowParallelEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean parallelEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (parallelEdge ? 2 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!parallelEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, addedEndpoints.get(1)); /* parallel */
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() index graph unsorted sometimes not in range */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean notInRange = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (notInRange ? 1 : 0);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!notInRange) {
						List<Integer> addedIds = randEdgesIds(g, num, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else if (rand.nextBoolean()) {
						List<Integer> addedIds =
								new ArrayList<>(range(g.edges().size() - 1, g.edges().size() + num - 1));
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					} else {
						List<Integer> addedIds =
								new ArrayList<>(range(g.edges().size() + 1, g.edges().size() + num + 1));
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() index graph unsorted sometimes duplicate id in list */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean duplicateEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (duplicateEdge ? 2 : 0);
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!duplicateEdge) {
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						g.addEdges(addedEdges);
						addEdgesToExpected(expectedEdges, addedEdges);
					} else {
						addedIds.set(0, addedIds.get(1)); /* duplicate id */
						Collections.shuffle(addedEndpoints, rand);
						EdgeSet<Integer, Integer> addedEdges = edgeSetFromList(addedIds, addedEndpoints, rand);
						assertThrows(IllegalArgumentException.class, () -> g.addEdges(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdges() index graph from EdgeSet.of() */
		foreachBoolConfig((directed, fromIntGraph) -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size());
					List<Integer> addedIds = randEdgesIds(g, num, rand);
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					EdgeSet<Integer, Integer> addedEdges0 = edgeSetFromList(addedIds, addedEndpoints, rand);

					GraphFactory<Integer, Integer> factory =
							fromIntGraph ? IntGraphFactory.directed() : GraphFactory.directed();
					Graph<Integer, Integer> g0 = factory.allowSelfEdges().allowParallelEdges().newGraph();
					g0.addVertices(g.vertices());
					g0.addEdges(addedEdges0);
					EdgeSet<Integer, Integer> addedEdges = EdgeSet.allOf(g0);

					g.addEdges(addedEdges);
					addEdgesToExpected(expectedEdges, addedEdges);
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});
	}

	static void addEdgesReassignIdsTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0x96a8f7d4731b5c5cL);

		BiConsumer<Map<Integer, Pair<Integer, Integer>>, IEdgeSet> addEdgesToExpected = (edgesMap, edgeSet) -> {
			for (EdgeIter<Integer, Integer> eit = edgeSet.iterator(); eit.hasNext();) {
				eit.next(); /* edge id is ignored */
				int edgeId = edgesMap.size();
				Pair<Integer, Integer> endpoints = Pair.of(eit.source(), eit.target());
				Object oldVal = edgesMap.put(Integer.valueOf(edgeId), endpoints);
				assertNull(oldVal);
			}
		};

		/* addEdgesReassignIds() valid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size());
					/* completely random ids, they should be ignored */
					List<Integer> addedIds = new IntArrayList(randArray(num, rand.nextLong()));
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
					g.addEdgesReassignIds(addedEdges);
					addEdgesToExpected.accept(expectedEdges, addedEdges);
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdgesReassignIds() sometimes with invalid endpoint */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean invalidEndpoint = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (invalidEndpoint ? 1 : 0);
					/* completely random ids, they should be ignored */
					List<Integer> addedIds = new IntArrayList(randArray(num, rand.nextLong()));
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!invalidEndpoint) {
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						g.addEdgesReassignIds(addedEdges);
						addEdgesToExpected.accept(expectedEdges, addedEdges);
					} else {
						Integer nonExistingVertex = nonExistingVertex(g, rand);
						if (rand.nextBoolean()) {
							addedEndpoints.set(0, Pair.of(nonExistingVertex, addedEndpoints.get(0).second()));
						} else {
							addedEndpoints.set(0, Pair.of(addedEndpoints.get(0).first(), nonExistingVertex));
						}
						Collections.shuffle(addedEndpoints, rand);
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						assertThrows(NoSuchVertexException.class, () -> g.addEdgesReassignIds(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdgesReassignIds() sometimes with self edge */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				if (g.isAllowSelfEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean selfEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (selfEdge ? 1 : 0);
					/* completely random ids, they should be ignored */
					List<Integer> addedIds = new IntArrayList(randArray(num, rand.nextLong()));
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!selfEdge) {
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						g.addEdgesReassignIds(addedEdges);
						addEdgesToExpected.accept(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, Pair.of(addedEndpoints.get(0).first(), addedEndpoints.get(0).first()));
						Collections.shuffle(addedEndpoints, rand);
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						assertThrows(IllegalArgumentException.class, () -> g.addEdgesReassignIds(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdgesReassignIds() sometimes with parallel edges with existing */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				if (g.isAllowParallelEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean parallelEdge = !expectedEdges.isEmpty() && rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (parallelEdge ? 1 : 0);
					/* completely random ids, they should be ignored */
					List<Integer> addedIds = new IntArrayList(randArray(num, rand.nextLong()));
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!parallelEdge) {
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						g.addEdgesReassignIds(addedEdges);
						addEdgesToExpected.accept(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, expectedEdges.get(Graphs.randEdge(g, rand))); /* parallel */
						Collections.shuffle(addedEndpoints, rand);
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						assertThrows(IllegalArgumentException.class, () -> g.addEdgesReassignIds(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});

		/* addEdgesReassignIds() sometimes with parallel edges in added list */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				IndexGraph g = graphImpl.apply(directed).indexGraph();
				if (g.isAllowParallelEdges())
					return;
				g.addVertices(range(50 + rand.nextInt(100)));
				final int m = rand.nextInt(100);

				Map<Integer, Pair<Integer, Integer>> expectedEdges = new Int2ObjectOpenHashMap<>();
				while (expectedEdges.size() < m) {
					final boolean parallelEdge = rand.nextBoolean();
					final int num = Math.min(rand.nextInt(5), m - expectedEdges.size()) + (parallelEdge ? 2 : 0);
					/* completely random ids, they should be ignored */
					List<Integer> addedIds = new IntArrayList(randArray(num, rand.nextLong()));
					List<Pair<Integer, Integer>> addedEndpoints = randEndpoints(g, num, rand);
					if (!parallelEdge) {
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						g.addEdgesReassignIds(addedEdges);
						addEdgesToExpected.accept(expectedEdges, addedEdges);
					} else {
						addedEndpoints.set(0, addedEndpoints.get(1)); /* parallel */
						Collections.shuffle(addedEndpoints, rand);
						IEdgeSet addedEdges = new IEdgeSetFromList(addedIds, addedEndpoints);
						assertThrows(IllegalArgumentException.class, () -> g.addEdgesReassignIds(addedEdges));
					}
				}
				assertExpectedEdges(expectedEdges, g);
			}
		});
	}

	static void removeEdgesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0x9bf6ef1f132fa70eL);

		Function<Boolean, Graph<Integer, Integer>> createGraph = directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(50 + rand.nextInt(100)));
			final int m = 100 + rand.nextInt(100);
			while (g.edges().size() < m) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!g.isAllowSelfEdges() && u.equals(v))
					continue;
				if (!g.isAllowParallelEdges() && g.containsEdge(u, v))
					continue;
				if (!g.isDirected() && u.intValue() > v.intValue()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				g.addEdge(u, v, Integer.valueOf(g.edges().size()));
			}
			return g;
		};

		/* removeEdges() valid */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> edges = new IntOpenHashSet(expectedGraph.edges());
				while (edges.size() > 0) {
					int num = Math.min(rand.nextInt(5), edges.size());
					Set<Integer> es = new HashSet<>();
					while (es.size() < num)
						es.add(Graphs.randEdge(g, rand));
					g.removeEdges(es);
					edges.removeAll(es);
					for (Integer v : es)
						expectedGraph.removeEdge(v);
					assertEquals(edges, g.edges());
					assertEquals(expectedGraph, g);
				}
			}
		});

		/* removeEdges() sometimes with duplicate edge (in removed list) */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> edges = new IntOpenHashSet(expectedGraph.edges());
				while (edges.size() > 0) {
					int num = Math.min(rand.nextInt(5), edges.size());
					Set<Integer> es = new HashSet<>();
					while (es.size() < num)
						es.add(Graphs.randEdge(g, rand));
					if (es.isEmpty() || rand.nextBoolean()) {
						g.removeEdges(es);
						edges.removeAll(es);
						for (Integer v : es)
							expectedGraph.removeEdge(v);
						assertEquals(edges, g.edges());
						assertEquals(expectedGraph, g);
					} else {
						List<Integer> es0 = new ArrayList<>(es);
						es0.add(randElement(es0, rand)); /* duplicate element */
						Collections.shuffle(es0, rand);
						assertThrows(IllegalArgumentException.class, () -> g.removeEdges(es0));
					}
				}
			}
		});

		/* removeEdges() sometimes with non existing edge */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> edges = new IntOpenHashSet(expectedGraph.edges());
				while (edges.size() > 0) {
					int num = Math.min(rand.nextInt(5), edges.size());
					Set<Integer> es = new HashSet<>();
					while (es.size() < num)
						es.add(Graphs.randEdge(g, rand));
					if (rand.nextBoolean()) {
						g.removeEdges(es);
						edges.removeAll(es);
						for (Integer v : es)
							expectedGraph.removeEdge(v);
						assertEquals(edges, g.edges());
						assertEquals(expectedGraph, g);
					} else {
						es.add(nonExistingEdge(g, rand)); /* non existing element */
						List<Integer> es0 = new ArrayList<>(es);
						Collections.shuffle(es0, rand);
						assertThrows(NoSuchEdgeException.class, () -> g.removeEdges(es0));
					}
				}
			}
		});

		/* removeEdges() sometimes with null edge */
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				Graph<Integer, Integer> g = createGraph.apply(directed);
				Graph<Integer, Integer> expectedGraph = g.copy();

				Set<Integer> edges = new IntOpenHashSet(expectedGraph.edges());
				while (edges.size() > 0) {
					int num = Math.min(rand.nextInt(5), edges.size());
					Set<Integer> es = new HashSet<>();
					while (es.size() < num)
						es.add(Graphs.randEdge(g, rand));
					if (rand.nextBoolean()) {
						g.removeEdges(es);
						edges.removeAll(es);
						for (Integer v : es)
							expectedGraph.removeEdge(v);
						assertEquals(edges, g.edges());
						assertEquals(expectedGraph, g);
					} else {
						List<Integer> es0 = new ArrayList<>(es);
						es0.add(null);
						Collections.shuffle(es0, rand);
						assertThrows(NullPointerException.class, () -> g.removeEdges(es0));
					}
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	static void edgesTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig((directed, index) -> {
			final int n = 30;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			if (index)
				g = g.indexGraph();
			g.addVertices(range(n));
			List<Integer> vs = new ArrayList<>(g.vertices());

			IntSet edges = new IntOpenHashSet();
			for (int uIdx : range(n)) {
				for (int vIdx : range(uIdx + 1, n)) {
					Integer u = vs.get(uIdx), v = vs.get(vIdx);
					Integer e = Integer.valueOf(g.edges().size());
					g.addEdge(u, v, e);
					edges.add(e.intValue());
				}
			}
			assertEquals(edges, g.edges());
			assertEquals(g.edges(), edges);
			assertEquals(edges.hashCode(), g.edges().hashCode());

			/* test edges().toArray() on the way */
			Object[] arr1 = g.edges().toArray();
			Integer[] arr2 = g.edges().toArray(new Integer[0]);
			assertEquals(edges, Set.of(arr1)); /* Set.of() checks that there are no duplications */
			assertEquals(edges, Set.of(arr2));
			if (g.edges() instanceof IntSet) {
				int[] arr3 = ((IntSet) g.edges()).toIntArray();
				int[] arr4 = ((IntSet) g.edges()).toIntArray(new int[0]);
				int[] arr5Input = new int[g.edges().size()];
				int[] arr5 = ((IntSet) g.edges()).toIntArray(arr5Input);
				int[] arr6Input = new int[g.edges().size() + 7];
				Arrays.fill(arr6Input, -18);
				int[] arr6 = ((IntSet) g.edges()).toIntArray(arr6Input);
				assertEquals(edges, IntSet.of(arr3)); /* IntSet.of() checks that there are no duplications */
				assertEquals(edges, IntSet.of(arr4));
				assertEquals(edges, IntSet.of(arr5));
				assertEquals(edges, IntSet.of(Arrays.copyOf(arr6, g.edges().size())));
				assertTrue(arr5Input == arr5);
				assertTrue(arr6Input == arr6);
				for (int i : range(g.edges().size(), arr6Input.length))
					assertEquals(-18, arr6[i]);
			}
		});
	}

	static void testEndpoints(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final long seed = 0x62f7c169c6fbd294L;
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			final int n = 30;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			Map<Integer, IntIntPair> edges = new HashMap<>();
			while (g.edges().size() < 60) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!g.isAllowSelfEdges() && u.equals(v))
					continue;
				if (!g.isAllowParallelEdges() && g.containsEdge(u, v))
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

	static void getEdgeTest(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0xe2cdb0023327cb42L);
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Collection<Integer>, Integer> edges = new Object2ObjectOpenHashMap<>();
			for (int uIdx : range(n)) {
				for (int vIdx = directed ? 0 : uIdx; vIdx < n; vIdx++) {
					if (uIdx == vIdx && !g.isAllowSelfEdges())
						continue;
					if (rand.nextBoolean())
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
				assertEqualsBool(e.intValue() != -1, g.containsEdge(u, v));
			}
			for (int i = 0; i < 10; i++) {
				Integer u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				boolean expectedContains = g.edges().stream().anyMatch(e -> {
					Integer u0 = g.edgeSource(e), v0 = g.edgeTarget(e);
					if (g.isDirected()) {
						return u.equals(u0) && v.equals(v0);
					} else {
						return (u.equals(u0) && v.equals(v0)) || (u.equals(v0) && v.equals(u0));
					}
				});
				if (expectedContains) {
					assertNotNull(g.getEdge(u, v));
				} else {
					assertNull(g.getEdge(u, v));
				}
				assertEqualsBool(expectedContains, g.containsEdge(u, v));
			}
			for (int i = 0; i < 10; i++) {
				Integer nonExistingVertex = nonExistingVertex(g, rand);
				assertThrows(NoSuchVertexException.class,
						() -> g.getEdge(nonExistingVertex, Graphs.randVertex(g, rand)));
				assertThrows(NoSuchVertexException.class,
						() -> g.getEdge(Graphs.randVertex(g, rand), nonExistingVertex));
			}
		});
	}

	static void testGetEdgesOutIn(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final Random rand = new Random(0x55785cf48eb6bf43L);
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Integer, Set<Integer>> outEdges = new Object2ObjectOpenHashMap<>();
			Object2ObjectMap<Integer, Set<Integer>> inEdges = new Object2ObjectOpenHashMap<>();
			for (int uIdx : range(n)) {
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

			for (int i = 0; i < 10; i++) {
				Integer nonExistingVertex = nonExistingVertex(g, rand);
				assertThrows(NoSuchVertexException.class, () -> g.outEdges(nonExistingVertex));
				assertThrows(NoSuchVertexException.class, () -> g.inEdges(nonExistingVertex));
			}
		});
	}

	static void testGetEdgesSourceTarget(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			BiFunction<Integer, Integer, Collection<Integer>> key = directed ? List::of : GraphImplTestUtils::setOf;

			Object2ObjectMap<Collection<Integer>, Set<Integer>> edges = new Object2ObjectOpenHashMap<>();
			final int edgeRepeat = g.isAllowParallelEdges() ? 3 : 1;
			for (int repeat = 0; repeat < edgeRepeat; repeat++) {
				for (int uIdx : range(n)) {
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
			for (int uIdx : range(n)) {
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
				} while (key
						.apply(u, v)
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
				} while (key
						.apply(u, v)
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

			assertThrows(NoSuchVertexException.class,
					() -> g.getEdges(nonExistingVertex(g, rand), Graphs.randVertex(g, rand)));
			assertThrows(NoSuchVertexException.class,
					() -> g.getEdges(Graphs.randVertex(g, rand), nonExistingVertex(g, rand)));
		});
	}

	static void testEdgeIter(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2ObjectMap<Integer, Collection<Integer>> edges = new Object2ObjectOpenHashMap<>();
			for (int uIdx : range(n)) {
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
			for (int uIdx : range(n)) {
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

	static void testEdgeIterRemoveSingle(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final SeedGenerator seedGen = new SeedGenerator(0x95a73506247fe12L);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig((directed, outIn) -> {
			final boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			final boolean parallelEdges = graphImpl.apply(directed).isAllowParallelEdges();
			for (int ops = 0; ops < 20; ops++) {
				Graph<Integer, Integer> g = GraphsTestUtils
						.withImpl(
								GraphsTestUtils
										.randGraph(10, 30, directed, selfEdges, parallelEdges, seedGen.nextSeed()),
								graphImpl);

				Map<Integer, Set<Integer>> expectedOutEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Set<Integer>> expectedInEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Pair<Integer, Integer>> expectedEdgeEndpoints = new Object2ObjectOpenHashMap<>();
				for (Integer u : g.vertices()) {
					expectedOutEdges.put(u, new TreeSet<>());
					expectedInEdges.put(u, new TreeSet<>());
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedInEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedOutEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(Math.min(u, v), Math.max(u, v)));
					}
					expectedInEdges = expectedOutEdges;
				}

				Integer edgeToRemove = Graphs.randEdge(g, rand);
				expectedOutEdges.get(g.edgeSource(edgeToRemove)).remove(edgeToRemove);
				expectedInEdges.get(g.edgeTarget(edgeToRemove)).remove(edgeToRemove);
				expectedEdgeEndpoints.remove(edgeToRemove);
				boolean removed = false;
				EdgeSet<Integer, Integer> edgeSet =
						outIn ? g.outEdges(g.edgeSource(edgeToRemove)) : g.inEdges(g.edgeTarget(edgeToRemove));
				Set<Integer> iteratedEdges = new HashSet<>();
				Set<Integer> iteratedEdgesExpected = new HashSet<>(edgeSet);
				for (EdgeIter<Integer, Integer> eit = edgeSet.iterator(); eit.hasNext();) {
					Integer e = eit.next();
					iteratedEdges.add(e);
					if (e.equals(edgeToRemove)) {
						assertFalse(removed);
						eit.remove();
						removed = true;
					}
				}
				assertTrue(removed);
				assertEquals(iteratedEdgesExpected, iteratedEdges);

				for (Integer v : g.vertices()) {
					assertEquals(expectedOutEdges.get(v), new TreeSet<>(g.outEdges(v)));
					assertEquals(expectedInEdges.get(v), new TreeSet<>(g.inEdges(v)));

					int outEdgesSetSize = g.outEdges(v).size(), outEdgesIterated = 0;
					int inEdgesSetSize = g.inEdges(v).size(), inEdgesIterated = 0;
					for (Iterator<Integer> it = g.outEdges(v).iterator(); it.hasNext(); it.next())
						outEdgesIterated++;
					for (Iterator<Integer> it = g.inEdges(v).iterator(); it.hasNext(); it.next())
						inEdgesIterated++;
					assertEquals(outEdgesSetSize, outEdgesIterated);
					assertEquals(inEdgesSetSize, inEdgesIterated);
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(Math.min(u, v), Math.max(u, v)));
					}
				}
			}
		});
	}

	static void testEdgeIterRemoveAll(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final SeedGenerator seedGen = new SeedGenerator(0x95a73506247fe12L);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig((directed, outIn) -> {
			final boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			final boolean parallelEdges = graphImpl.apply(directed).isAllowParallelEdges();
			for (int ops = 0; ops < 20; ops++) {
				Graph<Integer, Integer> g = GraphsTestUtils
						.withImpl(
								GraphsTestUtils
										.randGraph(10, 30, directed, selfEdges, parallelEdges, seedGen.nextSeed()),
								graphImpl);

				Map<Integer, Set<Integer>> expectedOutEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Set<Integer>> expectedInEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Pair<Integer, Integer>> expectedEdgeEndpoints = new Object2ObjectOpenHashMap<>();
				for (Integer u : g.vertices()) {
					expectedOutEdges.put(u, new TreeSet<>());
					expectedInEdges.put(u, new TreeSet<>());
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedInEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedOutEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(Math.min(u, v), Math.max(u, v)));
					}
					expectedInEdges = expectedOutEdges;
				}

				EdgeSet<Integer, Integer> edgeSet =
						outIn ? g.outEdges(Graphs.randVertex(g, rand)) : g.inEdges(Graphs.randVertex(g, rand));
				for (Integer edgeToRemove : edgeSet) {
					expectedOutEdges.get(g.edgeSource(edgeToRemove)).remove(edgeToRemove);
					expectedInEdges.get(g.edgeTarget(edgeToRemove)).remove(edgeToRemove);
					expectedEdgeEndpoints.remove(edgeToRemove);
				}
				Set<Integer> iteratedEdges = new HashSet<>();
				Set<Integer> iteratedEdgesExpected = new HashSet<>(edgeSet);
				for (EdgeIter<Integer, Integer> eit = edgeSet.iterator(); eit.hasNext();) {
					Integer e = eit.next();
					iteratedEdges.add(e);
					eit.remove();
				}
				assertEquals(iteratedEdgesExpected, iteratedEdges);

				for (Integer v : g.vertices()) {
					assertEquals(expectedOutEdges.get(v), new TreeSet<>(g.outEdges(v)));
					assertEquals(expectedInEdges.get(v), new TreeSet<>(g.inEdges(v)));

					int outEdgesSetSize = g.outEdges(v).size(), outEdgesIterated = 0;
					int inEdgesSetSize = g.inEdges(v).size(), inEdgesIterated = 0;
					for (Iterator<Integer> it = g.outEdges(v).iterator(); it.hasNext(); it.next())
						outEdgesIterated++;
					for (Iterator<Integer> it = g.inEdges(v).iterator(); it.hasNext(); it.next())
						inEdgesIterated++;
					assertEquals(outEdgesSetSize, outEdgesIterated);
					assertEquals(inEdgesSetSize, inEdgesIterated);
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(Math.min(u, v), Math.max(u, v)));
					}
				}
			}
		});
	}

	static void testDegree(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		foreachBoolConfig(directed -> {
			final int n = 100;
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			g.addVertices(range(1, n + 1));
			List<Integer> vs = new ArrayList<>(g.vertices());

			Object2IntMap<Integer> degreeOut = new Object2IntOpenHashMap<>();
			Object2IntMap<Integer> degreeIn = new Object2IntOpenHashMap<>();
			for (int uIdx : range(n)) {
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

	static void testClear(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
			boolean parallelEdges = g.isAllowParallelEdges();

			int totalOpNum = 1000;
			while (totalOpNum > 0) {
				int ops = rand.nextInt(100);
				totalOpNum -= ops;

				int expectedN = 0;
				int expectedM = 0;

				for (int i : range(2)) {
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
							if (!parallelEdges && g.containsEdge(u, v))
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

	static void testClearEdges(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		Random rand = new Random(seed);
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = graphImpl.apply(directed);
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
							if (!parallelEdges && g.containsEdge(u, v))
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
				for (Integer u : g.vertices()) {
					assertEquals(0, g.outEdges(u).size());
					assertTrue(g.outEdges(u).isEmpty());
					assertEquals(0, g.inEdges(u).size());
					assertTrue(g.inEdges(u).isEmpty());
				}
			}
		});
	}

	static void testCopy(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (String copyType : List
				.of("origImpl", "array", "linked-list", "linked-list-ptr", "hashtable", "hashtable-multi", "matrix")) {
			foreachBoolConfig(directed -> {
				/* Create a random graph g */
				Graph<Integer, Integer> g = GraphsTestUtils
						.withImpl(GraphsTestUtils.randGraph(100, 300, directed, false, false, seedGen.nextSeed()),
								graphImpl);

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
					copy = GraphFactory
							.<Integer, Integer>newInstance(g.isDirected())
							.setOption("impl", copyType)
							.newCopyOf(g);
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

	static void testCopyWithWeights(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			Graph<Integer, Integer> g = GraphsTestUtils
					.withImpl(GraphsTestUtils.randGraph(100, 300, directed, false, false, seedGen.nextSeed()),
							graphImpl);

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

	static void testImmutableCopy(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			Graph<Integer, Integer> g = GraphsTestUtils
					.withImpl(GraphsTestUtils.randGraph(100, 300, directed, selfEdges, false, seedGen.nextSeed()),
							graphImpl);

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

	static void testImmutableCopyWithWeights(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			Graph<Integer, Integer> g = GraphsTestUtils
					.withImpl(GraphsTestUtils.randGraph(100, 300, directed, selfEdges, false, seedGen.nextSeed()),
							graphImpl);

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
		foreachBoolConfig((directed, mutable) -> {
			/* Create a random graph g */
			final boolean selfEdges = copyConstructor
					.apply(directed ? IndexGraph.newDirected() : IndexGraph.newUndirected())
					.isAllowSelfEdges();
			final boolean parallelEdges = copyConstructor
					.apply(directed ? IndexGraph.newDirected() : IndexGraph.newUndirected())
					.isAllowParallelEdges();
			IndexGraph g = GraphsTestUtils
					.randGraph(100, 300, directed, selfEdges, parallelEdges, seedGen.nextSeed())
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

			if (mutable) {
				g = g.copy(true, true);
			} else {
				g = g.immutableCopy(true, true);
			}

			/* check copy constructor */
			assertEquals(g, copyConstructor.apply(g));
			assertEquals(g, copyConstructor.apply(g.immutableCopy(true, true)));
			assertEquals(g, copyConstructor.apply(g.immutableView()));
			assertEquals(g, copyConstructor.apply(copyConstructor.apply(g)));

			if (!selfEdges) {
				IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
				IndexGraph g1 = factory.allowSelfEdges().newGraph();
				g1.addVertexInt();
				g1.addEdge(0, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraph g1 = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
				g1.addVertexInt();
				g1.addVertexInt();
				g1.addEdge(0, 1);
				g1.addEdge(0, 1);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraph g1 = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
				g1.addVertexInt();
				g1.addVertexInt();
				g1.addEdge(1, 0);
				g1.addEdge(1, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
		});
	}

	static void testBuilderConstructor(Function<IndexGraphBuilderImpl, IndexGraph> copyConstructor0, long seed) {
		Function<IndexGraphBuilder, IndexGraph> copyConstructor =
				builder -> copyConstructor0.apply((IndexGraphBuilderImpl) builder);
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(directed -> {
			/* Create a random graph g */
			final boolean selfEdges = copyConstructor.apply(IndexGraphBuilder.newInstance(directed)).isAllowSelfEdges();
			final boolean parallelEdges =
					copyConstructor.apply(IndexGraphBuilder.newInstance(directed)).isAllowParallelEdges();
			IndexGraph g = GraphsTestUtils
					.randGraph(100, 300, directed, selfEdges, parallelEdges, seedGen.nextSeed())
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
			assertEquals(g, copyConstructor.apply(IndexGraphBuilder.newCopyOf(g, true, true)));

			if (!selfEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertexInt();
				g1.addEdge(0, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertexInt();
				g1.addVertexInt();
				g1.addEdge(0, 1);
				g1.addEdge(0, 1);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
			if (!parallelEdges) {
				IndexGraphBuilder g1 = IndexGraphBuilder.newInstance(directed);
				g1.addVertexInt();
				g1.addVertexInt();
				g1.addEdge(1, 0);
				g1.addEdge(1, 0);
				assertThrows(IllegalArgumentException.class, () -> copyConstructor.apply(g1));
			}
		});
	}

	static void testRemoveEdge(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final SeedGenerator seedGen = new SeedGenerator(0x95a73506247fe12L);
		final Random rand = new Random(seedGen.nextSeed());

		foreachBoolConfig(directed -> {
			final boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			final boolean parallelEdges = graphImpl.apply(directed).isAllowParallelEdges();
			for (int ops = 0; ops < 20; ops++) {
				Graph<Integer, Integer> g = GraphsTestUtils
						.withImpl(
								GraphsTestUtils
										.randGraph(10, 30, directed, selfEdges, parallelEdges, seedGen.nextSeed()),
								graphImpl);

				Map<Integer, Set<Integer>> expectedOutEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Set<Integer>> expectedInEdges = new Object2ObjectOpenHashMap<>();
				Map<Integer, Pair<Integer, Integer>> expectedEdgeEndpoints = new Object2ObjectOpenHashMap<>();
				for (Integer u : g.vertices()) {
					expectedOutEdges.put(u, new TreeSet<>());
					expectedInEdges.put(u, new TreeSet<>());
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedInEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						expectedOutEdges.get(u).add(e);
						expectedOutEdges.get(v).add(e);
						expectedEdgeEndpoints.put(e, Pair.of(Math.min(u, v), Math.max(u, v)));
					}
					expectedInEdges = expectedOutEdges;
				}

				Integer edgeToRemove = Graphs.randEdge(g, rand);
				expectedOutEdges.get(g.edgeSource(edgeToRemove)).remove(edgeToRemove);
				expectedInEdges.get(g.edgeTarget(edgeToRemove)).remove(edgeToRemove);
				expectedEdgeEndpoints.remove(edgeToRemove);
				g.removeEdge(edgeToRemove);

				for (Integer v : g.vertices()) {
					assertEquals(expectedOutEdges.get(v), new TreeSet<>(g.outEdges(v)));
					assertEquals(expectedInEdges.get(v), new TreeSet<>(g.inEdges(v)));

					int outEdgesSetSize = g.outEdges(v).size(), outEdgesIterated = 0;
					int inEdgesSetSize = g.inEdges(v).size(), inEdgesIterated = 0;
					for (Iterator<Integer> it = g.outEdges(v).iterator(); it.hasNext(); it.next())
						outEdgesIterated++;
					for (Iterator<Integer> it = g.inEdges(v).iterator(); it.hasNext(); it.next())
						inEdgesIterated++;
					assertEquals(outEdgesSetSize, outEdgesIterated);
					assertEquals(inEdgesSetSize, inEdgesIterated);
				}
				if (directed) {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(u, v));
					}
				} else {
					for (Integer e : g.edges()) {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertEquals(expectedEdgeEndpoints.get(e), Pair.of(Math.min(u, v), Math.max(u, v)));
					}
				}
			}
		});
	}

	static void testReverseEdge(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		final boolean selfEdges = graphImpl.apply(true).isAllowSelfEdges();
		final boolean parallelEdges = graphImpl.apply(true).isAllowParallelEdges();
		Graph<Integer, Integer> g1 = GraphsTestUtils
				.withImpl(GraphsTestUtils.randGraph(100, 300, true, selfEdges, parallelEdges, seedGen.nextSeed()),
						graphImpl);
		Graph<Integer, Integer> g2 = g1.copy(true, true);

		for (int ops = 0; ops < 10; ops++) {
			Integer e;
			do {
				e = Graphs.randEdge(g1, rand);
			} while (!parallelEdges && g1.containsEdge(g1.edgeTarget(e), g1.edgeSource(e)));

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
			if (!g1.containsEdge(g1.edgeTarget(e), g1.edgeSource(e))) {
				Integer nonExistingEdge;
				do {
					nonExistingEdge = Integer.valueOf(rand.nextInt());
				} while (g1.edges().contains(nonExistingEdge) || nonExistingEdge.intValue() < 0);
				g1.addEdge(g1.edgeTarget(e), g1.edgeSource(e), nonExistingEdge);
			}

			assertThrows(IllegalArgumentException.class, () -> g1.reverseEdge(e));
		}
	}

	static void testMoveEdge(Function<Boolean, Graph<Integer, Integer>> graphImpl) {
		final long seed = 0x5aaa87a14dbb6a83L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		foreachBoolConfig(directed -> {
			final boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
			final boolean parallelEdges = graphImpl.apply(directed).isAllowParallelEdges();
			Graph<Integer, Integer> g1 = GraphsTestUtils
					.withImpl(
							GraphsTestUtils.randGraph(100, 300, directed, selfEdges, parallelEdges, seedGen.nextSeed()),
							graphImpl);
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
					if (!parallelEdges && g1.containsEdge(newSource, newTarget))
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
					if (!parallelEdges && g1.containsEdge(newSource, newTarget))
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
			if (!parallelEdges) {
				Integer e = Graphs.randEdge(g1, rand);
				g1.moveEdge(e, g1.edgeSource(e), g1.edgeTarget(e));
			}
		});
	}

	static void testUndirectedMST(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		MinimumSpanningTreeTestUtils.testRandGraph(MinimumSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMDST(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		MinimumDirectedSpanningTreeTarjanTest.testRandGraph(MinimumDirectedSpanningTree.newInstance(), graphImpl, seed);
	}

	static void testDirectedMaxFlow(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		MaximumFlowTestUtils.testRandGraphs(MaximumFlow.newInstance(), graphImpl, seed, /* directed= */ true);
	}

	static void testUndirectedBipartiteMatching(Function<Boolean, Graph<Integer, Integer>> graphImpl, long seed) {
		MatchingBipartiteTestUtils
				.randBipartiteGraphs(MatchingAlgo.builder().setBipartite(true).setCardinality(true).build(), graphImpl,
						seed);
	}

	static void testUndirectedBipartiteMatchingWeighted(Function<Boolean, Graph<Integer, Integer>> graphImpl,
			long seed) {
		MatchingWeightedTestUtils
				.randGraphsBipartiteWeighted(MatchingAlgo.builder().setBipartite(true).build(), graphImpl, seed);
	}

	static void testRandOps(Function<Boolean, Graph<Integer, Integer>> graphImpl, boolean directed, long seed) {
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

	private static void testRandOps(Function<Boolean, Graph<Integer, Integer>> graphImpl, boolean directed, int n,
			int m, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		boolean selfEdges = graphImpl.apply(directed).isAllowSelfEdges();
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

		RemoveEdge, RemoveEdges, RemoveEdgeUsingOutIter, RemoveEdgeUsingInIter, RemoveEdgeUsingOutEdgeSet, RemoveEdgeUsingInEdgeSet, RemoveEdgeUsingSourceTargetEdgeSet,

		RemoveEdgesOfVertex, RemoveEdgesOfVertexUsingEdgeSet, RemoveEdgesOfVertexUsingIter,

		RemoveEdgesInOfVertex, RemoveEdgesInOfVertexUsingEdgeSet, RemoveEdgesInOfVertexUsingIter,

		RemoveEdgesOutOfVertex, RemoveEdgesOutOfVertexUsingEdgeSet, RemoveEdgesOutOfVertexUsingIter,

		ReverseEdge,

		// ClearEdges,

		AddVertex, RemoveVertex, RemoveVertices,
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
		opRand.add(GraphOp.RemoveVertices, 1);

		opRand.add(GraphOp.AddEdge, 80);

		opRand.add(GraphOp.RemoveEdge, 3);
		opRand.add(GraphOp.RemoveEdges, 1);
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
				case RemoveEdges: {
					if (tracker.edgesNum() <= 1)
						continue;
					GraphTracker.Edge edge1 = tracker.getRandEdge(rand), edge2;
					int e1 = getEdge.applyAsInt(edge1), e2;
					do {
						edge2 = tracker.getRandEdge(rand);
						e2 = getEdge.applyAsInt(edge2);
					} while (e1 == e2);

					g.removeEdges(IntList.of(e1, e2));
					tracker.removeEdge(edge1);
					tracker.removeEdge(edge2);
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
					if (edge.u != edge.v && g.containsEdge(Integer.valueOf(edge.v.id), Integer.valueOf(edge.u.id))
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
				case RemoveVertex: {
					if (tracker.verticesNum() == 0)
						continue;
					GraphTracker.Vertex v = tracker.getRandVertex(rand);
					g.removeVertex(Integer.valueOf(v.id));
					tracker.removeVertex(v);
					break;
				}
				case RemoveVertices: {
					if (tracker.verticesNum() <= 1)
						continue;
					GraphTracker.Vertex v1 = tracker.getRandVertex(rand), v2;
					do {
						v2 = tracker.getRandVertex(rand);
					} while (v1 == v2);
					g.removeVertices(List.of(Integer.valueOf(v1.id), Integer.valueOf(v2.id)));
					tracker.removeVertex(v1);
					tracker.removeVertex(v2);
					break;
				}
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

	private static Integer nonExistingVertex(Graph<Integer, ?> g, Random rand) {
		for (;;) {
			Integer v = Integer.valueOf(rand.nextInt());
			if (!g.vertices().contains(v))
				return v;
		}
	}

	private static Integer nonExistingEdge(Graph<Integer, Integer> g, Random rand) {
		for (;;) {
			Integer e = Integer.valueOf(rand.nextInt());
			if (!g.edges().contains(e))
				return e;
		}
	}

}
