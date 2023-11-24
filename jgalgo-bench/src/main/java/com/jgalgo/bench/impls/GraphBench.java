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
package com.jgalgo.bench.impls;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.TestUtils;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class GraphBench {

	public static class EdgesRead {

		private IndexGraph g;
		private IntIterator queryVIter;
		private LongIterator queryUVIter;

		void setup(String args, Function<IndexGraph, IndexGraph> graphImplementation, boolean directed,
				boolean allowParallelEdges, boolean allowSelfEdges) {
			final long seed = 0x8c91b913bfe81bb7L;
			final Random rand = new Random(seed);
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			final int n = Integer.parseInt(argsMap.get("|V|"));
			final int m = Integer.parseInt(argsMap.get("|E|"));

			g = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
			for (int v = 0; v < n; v++)
				g.addVertex();
			LongSet existingEdges = allowParallelEdges ? null : new LongOpenHashSet();
			for (int e = 0; e < m;) {
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!allowSelfEdges && u == v)
					continue;
				if (!allowParallelEdges) {
					int u1 = u, v1 = v;
					if (!directed && u1 < v1) {
						int tmp = u1;
						u1 = v1;
						v1 = tmp;
					}
					long key = JGAlgoUtils.longPack(u1, v1);
					if (!existingEdges.add(key))
						continue;
				}
				g.addEdge(u, v);
				e++;
			}
			g = graphImplementation.apply(g);

			int[] queryVertices = TestUtils.randArray(n * 16, 0, n, seed);
			queryVIter = circularIterator(IntImmutableList.of(queryVertices));

			int[] queryUVertices = TestUtils.randArray(n * 32, 0, n, seed);
			long[] queryUVertices0 = new long[queryUVertices.length / 2];
			for (int q = 0; q < queryUVertices0.length; q++) {
				int u, v;
				if (rand.nextBoolean()) {
					u = queryUVertices[q * 2 + 0];
					v = queryUVertices[q * 2 + 1];
				} else {
					int e = Graphs.randEdge(g, rand);
					u = g.edgeSource(e);
					v = g.edgeTarget(e);
					if (!directed && rand.nextBoolean()) {
						int tmp = u;
						u = v;
						v = tmp;
					}
				}
				queryUVertices0[q] = JGAlgoUtils.longPack(u, v);
			}
			queryUVIter = circularIterator(LongImmutableList.of(queryUVertices0));
		}

		void setup(String args, Supplier<IndexGraph> graphImplementation, boolean directed, boolean allowParallelEdges,
				boolean allowSelfEdges) {
			setup(args, g -> {
				assert g.isDirected() == directed;
				IndexGraph g1 = graphImplementation.get();
				assert g1.isDirected() == directed;
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					g1.addVertex();
				for (int m = g.edges().size(), e = 0; e < m; e++)
					g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
				return g1;
			}, directed, allowParallelEdges, allowSelfEdges);
		}

		private void benchOutEdges(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			blackhole.consume(edgeSet);
		}

		private void benchOutEdgesSize(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			blackhole.consume(edgeSet.size());
		}

		private void benchOutEdgesIteration(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();)
				blackhole.consume(eit.nextInt());
		}

		private void benchOutEdgesSource(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();) {
				eit.nextInt();
				blackhole.consume(eit.sourceInt());
			}
		}

		private void benchOutEdgesTarget(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();) {
				eit.nextInt();
				blackhole.consume(eit.targetInt());
			}
		}

		private void benchInEdges(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			blackhole.consume(edgeSet);
		}

		private void benchInEdgesSize(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			blackhole.consume(edgeSet.size());
		}

		private void benchInEdgesIteration(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();)
				blackhole.consume(eit.nextInt());
		}

		private void benchInEdgesSource(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();) {
				eit.nextInt();
				blackhole.consume(eit.sourceInt());
			}
		}

		private void benchInEdgesTarget(Blackhole blackhole) {
			int v = queryVIter.nextInt();
			IEdgeSet edgeSet = g.outEdges(v);
			for (IEdgeIter eit = edgeSet.iterator(); eit.hasNext();) {
				eit.nextInt();
				blackhole.consume(eit.targetInt());
			}
		}

		private void benchGetEdge(Blackhole blackhole) {
			long l = queryUVIter.nextLong();
			int u = JGAlgoUtils.long2low(l);
			int v = JGAlgoUtils.long2high(l);
			blackhole.consume(g.getEdge(u, v));
		}

		private void benchGetEdges(Blackhole blackhole) {
			long l = queryUVIter.nextLong();
			int u = JGAlgoUtils.long2low(l);
			int v = JGAlgoUtils.long2high(l);
			blackhole.consume(g.getEdges(u, v));
		}

		private void benchGetEdgesIteration(Blackhole blackhole) {
			long l = queryUVIter.nextLong();
			int u = JGAlgoUtils.long2low(l);
			int v = JGAlgoUtils.long2high(l);
			for (IEdgeIter eit = g.getEdges(u, v).iterator(); eit.hasNext();)
				blackhole.consume(eit.nextInt());
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class ArrayDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newDirected().setOption("impl", "array")::newGraph, true, true, true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class ArrayUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newUndirected().setOption("impl", "array")::newGraph, false, true, true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LinkedDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list")::newGraph, true, true,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LinkedUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list")::newGraph, false, true,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LinkedPtrDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list-ptr")::newGraph, true, true,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LinkedPtrUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list-ptr")::newGraph, false,
						true, true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class HashtableDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newDirected().setOption("impl", "hashtable")::newGraph, true, false,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class HashtableUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newUndirected().setOption("impl", "hashtable")::newGraph, false, false,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class MatrixDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newDirected().setOption("impl", "matrix")::newGraph, true, false, true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class MatrixUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				setup(args, IndexGraphFactory.newUndirected().setOption("impl", "matrix")::newGraph, false, false,
						true);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class CsrDirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				final boolean directed = true;
				final boolean allowParallelEdges = true;
				final boolean allowSelfEdges = true;
				setup(args, g -> {
					assert g.isDirected() == directed;
					IndexGraphBuilder g1 = IndexGraphBuilder.newDirected();
					for (int n = g.vertices().size(), v = 0; v < n; v++)
						g1.addVertex();
					for (int m = g.edges().size(), e = 0; e < m; e++)
						g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
					IndexGraph g1Graph = g1.build();
					assert g1Graph.isDirected() == directed;
					return g1Graph;
				}, directed, allowParallelEdges, allowSelfEdges);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class CsrUndirected extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				final boolean directed = false;
				final boolean allowParallelEdges = true;
				final boolean allowSelfEdges = true;
				setup(args, g -> {
					assert g.isDirected() == directed;
					IndexGraphBuilder g1 = IndexGraphBuilder.newUndirected();
					for (int n = g.vertices().size(), v = 0; v < n; v++)
						g1.addVertex();
					for (int m = g.edges().size(), e = 0; e < m; e++)
						g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
					IndexGraph g1Graph = g1.build();
					assert g1Graph.isDirected() == directed;
					return g1Graph;
				}, directed, allowParallelEdges, allowSelfEdges);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class CsrDirectedReindexed extends GraphBench.EdgesRead {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setup() {
				final boolean directed = true;
				final boolean allowParallelEdges = true;
				final boolean allowSelfEdges = true;
				setup(args, g -> {
					assert g.isDirected() == directed;
					IndexGraphBuilder g1 = IndexGraphBuilder.newDirected();
					for (int n = g.vertices().size(), v = 0; v < n; v++)
						g1.addVertex();
					for (int m = g.edges().size(), e = 0; e < m; e++)
						g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
					IndexGraph g1Graph = g1.reIndexAndBuild(true, true).graph();
					assert g1Graph.isDirected() == directed;
					return g1Graph;
				}, directed, allowParallelEdges, allowSelfEdges);
			}

			@Benchmark
			public void benchOutEdges(Blackhole blackhole) {
				super.benchOutEdges(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSize(Blackhole blackhole) {
				super.benchOutEdgesSize(blackhole);
			}

			@Benchmark
			public void benchOutEdgesIteration(Blackhole blackhole) {
				super.benchOutEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchOutEdgesSource(Blackhole blackhole) {
				super.benchOutEdgesSource(blackhole);
			}

			@Benchmark
			public void benchOutEdgesTarget(Blackhole blackhole) {
				super.benchOutEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchInEdges(Blackhole blackhole) {
				super.benchInEdges(blackhole);
			}

			@Benchmark
			public void benchInEdgesSize(Blackhole blackhole) {
				super.benchInEdgesSize(blackhole);
			}

			@Benchmark
			public void benchInEdgesIteration(Blackhole blackhole) {
				super.benchInEdgesIteration(blackhole);
			}

			@Benchmark
			public void benchInEdgesSource(Blackhole blackhole) {
				super.benchInEdgesSource(blackhole);
			}

			@Benchmark
			public void benchInEdgesTarget(Blackhole blackhole) {
				super.benchInEdgesTarget(blackhole);
			}

			@Benchmark
			public void benchGetEdge(Blackhole blackhole) {
				super.benchGetEdge(blackhole);
			}

			@Benchmark
			public void benchGetEdges(Blackhole blackhole) {
				super.benchGetEdges(blackhole);
			}

			@Benchmark
			public void benchGetEdgesIteration(Blackhole blackhole) {
				super.benchGetEdgesIteration(blackhole);
			}
		}

	}

	public static class Remove {

		IndexGraph g;
		int n, m;
		Random rand;

		void setup(String args, Function<IndexGraph, IndexGraph> graphImplementation, boolean directed,
				boolean allowParallelEdges, boolean allowSelfEdges) {
			final long seed = 0x850a14ff3dad400cL;
			rand = new Random(seed);
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("|V|"));
			m = Integer.parseInt(argsMap.get("|E|"));

			g = directed ? IndexGraph.newDirected() : IndexGraph.newUndirected();
			for (int v = 0; v < n; v++)
				g.addVertex();
			LongSet existingEdges = allowParallelEdges ? null : new LongOpenHashSet();
			for (int e = 0; e < m;) {
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!allowSelfEdges && u == v)
					continue;
				if (!allowParallelEdges) {
					int u1 = u, v1 = v;
					if (!directed && u1 < v1) {
						int tmp = u1;
						u1 = v1;
						v1 = tmp;
					}
					long key = JGAlgoUtils.longPack(u1, v1);
					if (!existingEdges.add(key))
						continue;
				}
				g.addEdge(u, v);
				e++;
			}
			g = graphImplementation.apply(g);
		}

		void setup(String args, Supplier<IndexGraph> graphImplementation, boolean directed, boolean allowParallelEdges,
				boolean allowSelfEdges) {
			setup(args, g -> {
				assert g.isDirected() == directed;
				IndexGraph g1 = graphImplementation.get();
				assert g1.isDirected() == directed;
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					g1.addVertex();
				for (int e = 0; e < m; e++)
					g1.addEdge(g.edgeSource(e), g.edgeTarget(e));
				return g1;
			}, directed, allowParallelEdges, allowSelfEdges);
		}

		void restoreGraph() {
			final boolean allowSelfEdges = g.isAllowSelfEdges();
			final boolean allowParallelEdges = g.isAllowParallelEdges();
			while (g.vertices().size() < n)
				g.addVertex();
			while (g.edges().size() < m) {
				int u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
				if (!allowSelfEdges && u == v)
					continue;
				if (!allowParallelEdges && g.getEdge(u, v) != -1)
					continue;
				g.addEdge(u, v);
			}
		}

		public static class Edge extends Remove {

			private IntIterator removeEIter;

			private static final int OperationsPerInvocation = 100;

			@Override
			void setup(String args, Function<IndexGraph, IndexGraph> graphImplementation, boolean directed,
					boolean allowParallelEdges, boolean allowSelfEdges) {
				super.setup(args, graphImplementation, directed, allowParallelEdges, allowSelfEdges);

				assert m > OperationsPerInvocation;
				int[] removeEdges = TestUtils.randArray(m * 16, 0, m - OperationsPerInvocation, rand.nextLong());
				removeEIter = circularIterator(IntImmutableList.of(removeEdges));
			}

			private void bench(Blackhole blackhole) {
				for (int o = 0; o < OperationsPerInvocation; o++) {
					int e = removeEIter.nextInt();
					g.removeEdge(e);
					blackhole.consume(e);
					blackhole.consume(g);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class ArrayDirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "array")::newGraph, true, true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class ArrayUndirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "array")::newGraph, false, true,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedDirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list")::newGraph, true, true,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedUndirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list")::newGraph, false,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedPtrDirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list-ptr")::newGraph, true,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedPtrUndirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list-ptr")::newGraph, false,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class HashtableDirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "hashtable")::newGraph, true, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class HashtableUndirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "hashtable")::newGraph, false,
							false, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class MatrixDirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "matrix")::newGraph, true, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class MatrixUndirected extends GraphBench.Remove.Edge {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "matrix")::newGraph, false, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

		}

		public static class Vertex extends Remove {

			private IntIterator removeVIter;

			private static final int OperationsPerInvocation = 50;

			@Override
			void setup(String args, Function<IndexGraph, IndexGraph> graphImplementation, boolean directed,
					boolean allowParallelEdges, boolean allowSelfEdges) {
				super.setup(args, graphImplementation, directed, allowParallelEdges, allowSelfEdges);

				assert n > OperationsPerInvocation;
				int[] removeVertices = TestUtils.randArray(n * 16, 0, n - OperationsPerInvocation, rand.nextLong());
				removeVIter = circularIterator(IntImmutableList.of(removeVertices));
			}

			private void bench(Blackhole blackhole) {
				for (int o = 0; o < OperationsPerInvocation; o++) {
					int v = removeVIter.nextInt();
					g.removeEdge(v);
					blackhole.consume(v);
					blackhole.consume(g);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class ArrayDirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "array")::newGraph, true, true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();
				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class ArrayUndirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "array")::newGraph, false, true,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedDirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list")::newGraph, true, true,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedUndirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list")::newGraph, false,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedPtrDirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "linked-list-ptr")::newGraph, true,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class LinkedPtrUndirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "linked-list-ptr")::newGraph, false,
							true, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class HashtableDirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "hashtable")::newGraph, true, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class HashtableUndirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "hashtable")::newGraph, false,
							false, true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class MatrixDirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newDirected().setOption("impl", "matrix")::newGraph, true, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Vertex.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

			@BenchmarkMode(Mode.AverageTime)
			@OutputTimeUnit(TimeUnit.NANOSECONDS)
			@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
			@Fork(value = 1, warmups = 0)
			@State(Scope.Benchmark)
			public static class MatrixUndirected extends GraphBench.Remove.Vertex {

				@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=1300 |E|=50000" })
				public String args;

				@Setup(Level.Trial)
				public void setup() {
					setup(args, IndexGraphFactory.newUndirected().setOption("impl", "matrix")::newGraph, false, false,
							true);
				}

				@Override
				@Setup(Level.Invocation)
				public void restoreGraph() {
					super.restoreGraph();

				}

				@Benchmark
				@OperationsPerInvocation(GraphBench.Remove.Edge.OperationsPerInvocation)
				public void bench(Blackhole blackhole) {
					super.bench(blackhole);
				}
			}

		}
	}

	static IntIterator circularIterator(IntIterable iterable) {
		return new IntIterator() {
			IntIterator it = iterable.iterator();

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public int nextInt() {
				int res = it.nextInt();
				if (!it.hasNext())
					it = iterable.iterator();
				return res;
			}
		};
	}

	static LongIterator circularIterator(LongIterable iterable) {
		return new LongIterator() {
			LongIterator it = iterable.iterator();

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public long nextLong() {
				long res = it.nextLong();
				if (!it.hasNext())
					it = iterable.iterator();
				return res;
			}
		};
	}

}
