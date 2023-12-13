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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import com.jgalgo.gen.GnmBipartiteGraphGenerator;
import com.jgalgo.gen.GnmGraphGenerator;
import com.jgalgo.gen.UniformTreeGenerator;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.RandomIntUnique;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {}

	public static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, long seed) {
		return GraphsTestUtils.randGraph(n, m, directed, true, true, seed);
	}

	public static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, boolean selfEdges,
			boolean parallelEdges, long seed) {
		Random rand = new Random(seed ^ 0xf0a3f27bf417a821L);
		boolean intGraph = rand.nextBoolean();
		return GraphsTestUtils.randGraph(n, m, directed, selfEdges, parallelEdges, intGraph,
				rand.nextLong() ^ 0xfca82e59f2c2acb3L);
	}

	public static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, boolean selfEdges,
			boolean parallelEdges, boolean intGraph, long seed) {
		GnmGraphGenerator<Integer, Integer> gen =
				intGraph ? GnmGraphGenerator.newInstance() : GnmGraphGenerator.newIntInstance();
		gen.setSeed(seed);
		gen.setDirected(directed);
		gen.setSelfEdges(selfEdges);
		gen.setParallelEdges(parallelEdges);
		gen.setVertices(range(n));
		gen.setEdges(m, new AtomicInteger()::getAndIncrement);
		return gen.generateMutable();
	}

	public static Graph<Integer, Integer> randConnectedGraph(int n, int m, boolean directed, long seed) {
		return GraphsTestUtils.randConnectedGraph(n, m, directed, true, true, seed);
	}

	@SuppressWarnings("boxing")
	public static Graph<Integer, Integer> randConnectedGraph(int n, int m, boolean directed, boolean selfEdges,
			boolean parallelEdges, long seed) {
		Random rand = new Random(seed ^ 0x97768b042d9d1453L);
		GraphFactory<Integer, Integer> graphFactory =
				rand.nextBoolean() ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
		if (selfEdges)
			graphFactory.allowSelfEdges();
		if (parallelEdges)
			graphFactory.allowParallelEdges();

		Graph<Integer, Integer> g;
		if (!directed) {
			UniformTreeGenerator<Integer, Integer> gen =
					rand.nextBoolean() ? UniformTreeGenerator.newInstance() : UniformTreeGenerator.newIntInstance();
			gen.setSeed(rand.nextLong() ^ 0xb14ff0d42e1e9f91L);
			gen.setVertices(range(n));
			gen.setEdges(new AtomicInteger()::getAndIncrement);
			g = graphFactory.newCopyOf(gen.generateMutable(), true, true);
			if (g.edges().size() > m)
				throw new IllegalArgumentException();

			LongSet edges = parallelEdges ? null : new LongOpenHashSet();
			if (!parallelEdges) {
				for (int e : g.edges()) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					edges.add(u > v ? JGAlgoUtils.longPack(u, v) : JGAlgoUtils.longPack(v, u));
				}
			}
			while (g.edges().size() < m) {
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!selfEdges && u == v)
					continue;
				if (!parallelEdges && !edges.add(u > v ? JGAlgoUtils.longPack(u, v) : JGAlgoUtils.longPack(v, u)))
					continue;
				g.addEdge(u, v, g.edges().size());
			}

		} else {
			g = graphFactory.newGraph();
			g.addVertices(range(n));
			Stack<IntIntPair> stack = new ObjectArrayList<>();
			stack.push(IntIntPair.of(0, n));
			for (int[] vertices = range(n).toIntArray(); !stack.isEmpty();) {
				IntIntPair pair = stack.pop();
				int from = pair.leftInt(), to = pair.rightInt();
				if (to - from <= 1)
					continue;
				IntArrays.shuffle(vertices, from, to, rand);
				int mid = rand.nextInt(to - from - 1) + from + 1;
				int leftU = from + rand.nextInt(mid - from);
				int leftV = from + rand.nextInt(mid - from);
				int rightU = mid + rand.nextInt(to - mid);
				int rightV = mid + rand.nextInt(to - mid);
				g.addEdge(vertices[leftU], vertices[leftV], g.edges().size());
				g.addEdge(vertices[rightU], vertices[rightV], g.edges().size());
				stack.push(IntIntPair.of(from, mid));
				stack.push(IntIntPair.of(mid, to));
			}
			if (g.edges().size() > m)
				throw new IllegalArgumentException();

			LongSet edges = parallelEdges ? null : new LongOpenHashSet();
			if (!parallelEdges) {
				for (int e : g.edges()) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					edges.add(JGAlgoUtils.longPack(u, v));
				}
			}
			while (g.edges().size() < m) {
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!selfEdges && u == v)
					continue;
				if (!parallelEdges && !edges.add(JGAlgoUtils.longPack(u, v)))
					continue;
				g.addEdge(u, v, g.edges().size());
			}

		}
		if (!selfEdges)
			for (Integer e : g.edges())
				assert !g.edgeSource(e).equals(g.edgeTarget(e));
		if (!parallelEdges)
			assert !Graphs.containsParallelEdges(g);
		return g;
	}

	public static Graph<Integer, Integer> randBipartiteGraph(int n1, int n2, int m, boolean directed, long seed) {
		return randBipartiteGraph(n1, n2, m, directed, true, seed);
	}

	public static Graph<Integer, Integer> randBipartiteGraph(int n1, int n2, int m, boolean directed,
			boolean parallelEdges, long seed) {
		Random rand = new Random(seed ^ 0xffcc43f8e915afd3L);
		GnmBipartiteGraphGenerator<Integer, Integer> gen = rand.nextBoolean() ? GnmBipartiteGraphGenerator.newInstance()
				: GnmBipartiteGraphGenerator.newIntInstance();
		gen.setSeed(rand.nextLong() ^ 0xc4d03e052f81a257L);
		if (directed) {
			gen.setDirectedAll();
		} else {
			gen.setUndirected();
		}
		gen.setVertices(range(n1), range(n1, n1 + n2));
		gen.setEdges(m, new AtomicInteger()::getAndIncrement);
		gen.setParallelEdges(parallelEdges);
		return gen.generateMutable();
	}

	@SuppressWarnings("boxing")
	public static Graph<Integer, Integer> randDag(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		Random rand = new Random(seed ^ 0xf2440b5f17ee092fL);
		int[] topoOrder = randPermutation(n, rand.nextLong() ^ 0x67e18f9b87237f61L);
		Graph<Integer, Integer> g = rand.nextBoolean() ? IntGraph.newDirected() : Graph.newDirected();
		g.addVertices(range(n));
		while (g.edges().size() < m) {
			int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
			if (topoOrder[u] < topoOrder[v])
				g.addEdge(u, v, g.edges().size());
		}
		return g;
	}

	public static Graph<Integer, Integer> randTree(int n, long seed) {
		Random rand = new Random(seed ^ 0xb7d49b2e6d194893L);
		UniformTreeGenerator<Integer, Integer> gen =
				rand.nextBoolean() ? UniformTreeGenerator.newInstance() : UniformTreeGenerator.newIntInstance();
		gen.setSeed(rand.nextLong() ^ 0xce76e209073639caL);
		gen.setVertices(range(n));
		gen.setEdges(new AtomicInteger()::getAndIncrement);
		return gen.generateMutable();
	}

	public static Graph<Integer, Integer> randForest(int n, int m, long seed) {
		Random rand = new Random(seed ^ 0x492e6eb7b4ab4752L);
		Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seed);
		if (g.edges().size() < m)
			throw new IllegalArgumentException();
		while (g.edges().size() > m)
			g.removeEdge(Graphs.randEdge(g, rand));
		return g;
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	public static Graph<Integer, Integer> withImpl(Graph<Integer, Integer> g,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		Graph<Integer, Integer> g2 = graphImpl.apply(g.isDirected());
		g2.addVertices(g.vertices());
		g2.addEdges(EdgeSet.allOf(g));
		for (String weightKey : g.getVerticesWeightsKeys()) {
			Weights<Integer, Object> w1 = g.getVerticesWeights(weightKey);
			Weights<Integer, Object> w2 = g2.addVerticesWeights(weightKey, (Class<Object>) getWeightsType(w1));
			for (int v : g.vertices())
				w2.setAsObj(v, w1.getAsObj(v));
		}
		for (String weightKey : g.getEdgesWeightsKeys()) {
			Weights<Integer, Object> w1 = g.getEdgesWeights(weightKey);
			Weights<Integer, Object> w2 = g2.addEdgesWeights(weightKey, (Class<Object>) getWeightsType(w1));
			for (int e : g.edges())
				w2.setAsObj(e, w1.getAsObj(e));
		}
		return g2;
	}

	private static Class<?> getWeightsType(Weights<?, ?> w) {
		if (w instanceof WeightsByte)
			return byte.class;
		if (w instanceof WeightsShort)
			return short.class;
		if (w instanceof WeightsInt)
			return int.class;
		if (w instanceof WeightsLong)
			return long.class;
		if (w instanceof WeightsFloat)
			return float.class;
		if (w instanceof WeightsDouble)
			return double.class;
		if (w instanceof WeightsBool)
			return boolean.class;
		if (w instanceof WeightsChar)
			return char.class;
		assert w instanceof WeightsObj;
		return Object.class;
	}

	public static Boolean2ObjectFunction<Graph<Integer, Integer>> defaultGraphImpl(long seed) {
		if (new Random(seed).nextBoolean()) {
			return directed -> IntGraphFactory.newInstance(directed).newGraph();
		} else {
			return directed -> GraphFactory.<Integer, Integer>newInstance(directed).newGraph();
		}
	}

	public static <V, E> WeightsDouble<E> assignRandWeights(Graph<V, E> g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	public static <V, E> WeightsDouble<E> assignRandWeights(Graph<V, E> g, double minWeight, double maxWeight,
			long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		WeightsDouble<E> weight = g.addEdgesWeights("weight", double.class);
		for (E e : g.edges())
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		return weight;
	}

	public static <V, E> WeightsInt<E> assignRandWeightsIntPos(Graph<V, E> g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static <V, E> WeightsInt<E> assignRandWeightsIntNeg(Graph<V, E> g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	public static <V, E> WeightsInt<E> assignRandWeightsInt(Graph<V, E> g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, seed);
		WeightsInt<E> weight = g.addEdgesWeights("weight", int.class);
		for (E e : g.edges())
			weight.set(e, rand.next());
		return weight;
	}

	public static <V, E> WeightFunction<E> assignRandWeightsMaybeInt(Graph<V, E> g, int minWeight, int maxWeight,
			long seed) {
		if (new Random(seed).nextBoolean())
			return assignRandWeightsInt(g, minWeight, maxWeight, seed);
		WeightsDouble<E> w = assignRandWeights(g, minWeight, maxWeight, seed);
		/* floor weights to avoid floating points errors */
		for (E e : g.edges())
			w.set(e, (int) w.get(e));
		return w;
	}
}
