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

package com.jgalgo.internal.util;

import static com.jgalgo.internal.util.Range.range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import com.jgalgo.gen.GnmBipartiteGraphGenerator;
import com.jgalgo.gen.GnmGraphGenerator;
import com.jgalgo.gen.UniformTreeGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TestUtils {

	public static class PhasedTester {

		public static class Phase {

			int repeat = 1;
			int[] args;

			Phase() {}

			public Phase repeat(int r) {
				if (r <= 0)
					throw new IllegalArgumentException();
				this.repeat = r;
				return this;
			}

			public Phase withArgs(int... args) {
				if (args.length == 0)
					throw new IllegalArgumentException();
				this.args = args;
				return this;
			}
		}

		private final List<Phase> phases = new ArrayList<>();

		public Phase addPhase() {
			Phase phase = new Phase();
			phases.add(phase);
			return phase;
		}

		public void run(RunnableTestWith1Args test) {
			runPhases(phase -> test.run(phase.args[0]), 1);
		}

		public void run(RunnableTestWith2Args test) {
			runPhases(phase -> test.run(phase.args[0], phase.args[1]), 2);
		}

		public void run(RunnableTestWith3Args test) {
			runPhases(phase -> test.run(phase.args[0], phase.args[1], phase.args[2]), 3);
		}

		private void runPhases(Consumer<Phase> test, int runnableArgs) {
			for (int pIdx = 0; pIdx < phases.size(); pIdx++) {
				Phase phase = phases.get(pIdx);
				assertArgsNum(phase, runnableArgs);
				for (int repeat = 0; repeat < phase.repeat; repeat++) {
					try {
						test.accept(phase);
					} catch (Throwable ex) {
						System.err.println("failed at phase " + pIdx + " args=" + Arrays.toString(phase.args)
								+ " iteration " + repeat);
						throw ex;
					}
				}
			}

		}

		private static void assertArgsNum(Phase phase, int runnableArgs) {
			if (phase.args.length != runnableArgs)
				throw new IllegalArgumentException(
						"Phase had " + phase.args.length + " arguments, but runnable accept " + runnableArgs);
		}

		@FunctionalInterface
		public static interface RunnableTestWith1Args {
			public void run(int arg1);
		}

		@FunctionalInterface
		public static interface RunnableTestWith2Args {
			public void run(int arg1, int arg2);
		}

		@FunctionalInterface
		public static interface RunnableTestWith3Args {
			public void run(int arg1, int arg2, int arg3);
		}

	}

	public static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	public static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	public static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i = 0; i < a.length; i++)
			a[i] = nextInt(rand, from, to);
		return a;
	}

	public static int[] randPermutation(int n, long seed) {
		int[] a = new int[n];
		for (int i = 0; i < n; i++)
			a[i] = i;
		IntArrays.shuffle(a, new Random(seed ^ 0xb281dc30ae96a316L));
		return a;
	}

	public static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, long seed) {
		return randGraph(n, m, directed, true, true, seed);
	}

	public static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, boolean selfEdges,
			boolean parallelEdges, long seed) {
		Random rand = new Random(seed ^ 0xf0a3f27bf417a821L);
		boolean intGraph = rand.nextBoolean();
		return randGraph(n, m, directed, selfEdges, parallelEdges, intGraph, rand.nextLong() ^ 0xfca82e59f2c2acb3L);
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
		return randConnectedGraph(n, m, directed, true, true, seed);
	}

	@SuppressWarnings("boxing")
	public static Graph<Integer, Integer> randConnectedGraph(int n, int m, boolean directed, boolean selfEdges,
			boolean parallelEdges, long seed) {
		Random rand = new Random(seed ^ 0x97768b042d9d1453L);
		GraphFactory<Integer, Integer> graphFactory =
				rand.nextBoolean() ? IntGraphFactory.newUndirected() : GraphFactory.newUndirected();
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
			g = graphFactory.setDirected(true).newGraph();
			for (int v : range(n))
				g.addVertex(v);
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
		for (int v : range(n))
			g.addVertex(v);
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
		Graph<Integer, Integer> g = randTree(n, seed);
		if (g.edges().size() < m)
			throw new IllegalArgumentException();
		while (g.edges().size() > m)
			g.removeEdge(Graphs.randEdge(g, rand));
		return g;
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	public static Graph<Integer, Integer> copy(Graph<Integer, Integer> g,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl) {
		Graph<Integer, Integer> g2 = graphImpl.apply(g.isDirected());
		for (int v : g.vertices())
			g2.addVertex(v);
		for (int e : g.edges())
			g2.addEdge(g.edgeSource(e), g.edgeTarget(e), e);
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

	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	public static double nextDouble(Random rand, double from, double to) {
		return from + (to - from) * rand.nextDouble();
	}

	public static int nextInt(Random rand, int from, int to) {
		return from + rand.nextInt(to - from);
	}

	public static class SeedGenerator {
		private final Random rand;

		public SeedGenerator(long seed) {
			rand = new Random(seed ^ 0x9db7d6d04ce666aeL);
		}

		public long nextSeed() {
			return rand.nextLong() ^ 0x1df73569991aee99L;
		}
	}

	public static <T> T randElement(List<T> list, Random rand) {
		return list.get(rand.nextInt(list.size()));
	}

	public static int randElement(IntList list, Random rand) {
		return list.getInt(rand.nextInt(list.size()));
	}

	public static Graph<Integer, Integer> maybeIndexGraph(Graph<Integer, Integer> g, Random rand) {
		return rand.nextInt(3) == 0 ? g.indexGraph() : g;
	}

	public static void assertEqualsBool(boolean expected, boolean actual) {
		Assertions.assertEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	public static void assertNotEqualsBool(boolean expected, boolean actual) {
		Assertions.assertNotEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	public static void foreachBoolConfig(RunnableWith1BoolConfig test) {
		for (boolean cfg1 : BooleanList.of(false, true))
			test.run(cfg1);
	}

	public static void foreachBoolConfig(RunnableWith2BoolConfig test) {
		for (boolean cfg1 : BooleanList.of(false, true))
			for (boolean cfg2 : BooleanList.of(false, true))
				test.run(cfg1, cfg2);
	}

	public static void foreachBoolConfig(RunnableWith3BoolConfig test) {
		for (boolean cfg1 : BooleanList.of(false, true))
			for (boolean cfg2 : BooleanList.of(false, true))
				for (boolean cfg3 : BooleanList.of(false, true))
					test.run(cfg1, cfg2, cfg3);
	}

	public static void foreachBoolConfig(RunnableWith4BoolConfig test) {
		for (boolean cfg1 : BooleanList.of(false, true))
			for (boolean cfg2 : BooleanList.of(false, true))
				for (boolean cfg3 : BooleanList.of(false, true))
					for (boolean cfg4 : BooleanList.of(false, true))
						test.run(cfg1, cfg2, cfg3, cfg4);
	}

	@FunctionalInterface
	public static interface RunnableWith1BoolConfig {
		public void run(boolean cfg1);
	}

	@FunctionalInterface
	public static interface RunnableWith2BoolConfig {
		public void run(boolean cfg1, boolean cfg2);
	}

	@FunctionalInterface
	public static interface RunnableWith3BoolConfig {
		public void run(boolean cfg1, boolean cfg2, boolean cfg3);
	}

	@FunctionalInterface
	public static interface RunnableWith4BoolConfig {
		public void run(boolean cfg1, boolean cfg2, boolean cfg3, boolean cfg4);
	}

}
