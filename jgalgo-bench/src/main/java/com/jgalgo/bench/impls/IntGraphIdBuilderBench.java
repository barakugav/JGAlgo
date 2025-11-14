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

import static com.jgalgo.internal.util.Range.range;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntGraphIdBuilderBench {

	public static class Query {

		private Random rand;
		private IntGraph g;

		void setupCreateGraph(String args, Supplier<IdBuilderInt> IdBuilderSupplier) {
			final long seed = 0x7a1521bf6435883cL;
			rand = new Random(seed);
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			final int n = Integer.parseInt(argsMap.get("|V|"));
			final int m = Integer.parseInt(argsMap.get("|E|"));

			IntGraphFactory factory = IntGraphFactory.directed().allowSelfEdges().allowParallelEdges();
			if (IdBuilderSupplier != null) {
				factory.setVertexBuilder(IdBuilderSupplier.get());
				factory.setEdgeBuilder(IdBuilderSupplier.get());
			}
			g = factory.newGraph();
			g.addVertices(range(n));
			for (int i = 0; i < m; i++)
				g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand));
		}

		private static final int OperationsPerInvocation = 1000;
		private final int[] queries = new int[OperationsPerInvocation];

		void setupCreateQueries() {
			for (int q : range(OperationsPerInvocation))
				queries[q] = Graphs.randEdge(g, rand);
		}

		void bench(Blackhole blackhole) {
			// assert OperationsPerInvocation % 2 == 0;
			for (int q : range(OperationsPerInvocation / 2)) {
				blackhole.consume(g.edgeSource(queries[q * 2 + 0]));
				blackhole.consume(g.edgeTarget(queries[q * 2 + 1]));
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Counter extends Query {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setupCreateGraph() {
				super.setupCreateGraph(args, null);
			}

			@Override
			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@Override
			@Benchmark
			@OperationsPerInvocation(Query.OperationsPerInvocation)
			public void bench(Blackhole blackhole) {
				super.bench(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Rand extends Query {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setupCreateGraph() {
				super.setupCreateGraph(args, () -> {
					final Random rand = new Random();
					return (IntSet idSet) -> {
						for (;;) {
							int id = rand.nextInt();
							if (id >= 1 && !idSet.contains(id))
								// We prefer non zero IDs because fastutil handle zero (null) keys
								// separately
								return id;
						}
					};
				});
			}

			@Override
			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@Override
			@Benchmark
			@OperationsPerInvocation(Query.OperationsPerInvocation)
			public void bench(Blackhole blackhole) {
				super.bench(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Increment0x61c88647 extends Query {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setupCreateGraph() {
				super.setupCreateGraph(args, () -> {
					var state = new Object() {
						int nextId = 1;
					};
					return idSet -> {
						for (;;) {
							int id = state.nextId;
							state.nextId += 0x61c88647;
							if (id >= 1 && !idSet.contains(id))
								// We prefer non zero IDs because fastutil handle zero (null) keys
								// separately
								return id;
						}
					};
				});
			}

			@Override
			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@Override
			@Benchmark
			@OperationsPerInvocation(Query.OperationsPerInvocation)
			public void bench(Blackhole blackhole) {
				super.bench(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class MurmurHash3 extends Query {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setupCreateGraph() {
				super.setupCreateGraph(args, () -> {
					var state = new Object() {
						long nextId = 1;
					};
					return idSet -> {
						for (;;) {
							int id = Long.hashCode(state.nextId);
							state.nextId = HashCommon.murmurHash3(state.nextId);
							if (id >= 1 && !idSet.contains(id))
								// We prefer non zero IDs because fastutil handle zero (null) keys
								// separately
								return id;
						}
					};
				});
			}

			@Override
			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@Override
			@Benchmark
			@OperationsPerInvocation(Query.OperationsPerInvocation)
			public void bench(Blackhole blackhole) {
				super.bench(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class Mult0x7ffb01f extends Query {

			@Param({ "|V|=100 |E|=300", "|V|=100 |E|=3000", "|V|=13000 |E|=50000" })
			public String args;

			@Setup(Level.Trial)
			public void setupCreateGraph() {
				super.setupCreateGraph(args, () -> {
					var state = new Object() {
						long nextId = 1;
					};
					return idSet -> {
						for (;;) {
							int id = Long.hashCode(state.nextId);
							state.nextId *= 0x7ffb01f;
							if (id >= 1 && !idSet.contains(id))
								// We prefer non zero IDs because fastutil handle zero (null) keys
								// separately
								return id;
						}
					};
				});
			}

			@Override
			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@Override
			@Benchmark
			@OperationsPerInvocation(Query.OperationsPerInvocation)
			public void bench(Blackhole blackhole) {
				super.bench(blackhole);
			}
		}

	}

}
