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

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.alg.shortestpath.NegativeCycleException;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSourceBellmanFord;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSourceGoldberg;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SsspNegativeWeightsBench {

	List<GraphArgs> graphs;
	final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	void benchSssp(ShortestPathSingleSource algo, Blackhole blackhole) {
		GraphArgs args = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		try {
			ShortestPathSingleSource.IResult result = (ShortestPathSingleSource.IResult) algo
					.computeShortestPaths(args.g, args.w, Integer.valueOf(args.source));
			blackhole.consume(result);
		} catch (NegativeCycleException e) {
			blackhole.consume(e);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends SsspNegativeWeightsBench {

		@Param({ "|V|=64 MaxWeight=64", "|V|=512 MaxWeight=50", "|V|=512 MaxWeight=600", "|V|=1024 MaxWeight=200",
				"|V|=1024 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0x359f942eea9011efL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphGnp(n, true, seedGen.nextSeed());
				IWeightFunctionInt w =
						GraphsTestUtils.assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				graphs.add(new GraphArgs(g, w, source));
			}
		}

		@Benchmark
		public void BellmanFord(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceBellmanFord(), blackhole);
		}

		@Benchmark
		public void Goldberg(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceGoldberg(), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends SsspNegativeWeightsBench {

		@Param({ "|V|=64 MaxWeight=64", "|V|=512 MaxWeight=50", "|V|=512 MaxWeight=600", "|V|=4096 MaxWeight=200",
				"|V|=4096 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0x1b4f94ee86c9e5ceL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, true, seedGen.nextSeed());
				IWeightFunctionInt w =
						GraphsTestUtils.assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				graphs.add(new GraphArgs(g, w, source));
			}
		}

		@Benchmark
		public void BellmanFord(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceBellmanFord(), blackhole);
		}

		@Benchmark
		public void Goldberg(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceGoldberg(), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends SsspNegativeWeightsBench {

		@Param({ "|V|=64 |E|=256 MaxWeight=64", "|V|=512 |E|=4096 MaxWeight=50", "|V|=512 |E|=4096 MaxWeight=600",
				"|V|=4096 |E|=16384 MaxWeight=200", "|V|=4096 |E|=16384 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0xc0fd63fe73c6707aL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, true, seedGen.nextSeed());
				IWeightFunctionInt w =
						GraphsTestUtils.assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				graphs.add(new GraphArgs(g, w, source));
			}
		}

		@Benchmark
		public void BellmanFord(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceBellmanFord(), blackhole);
		}

		@Benchmark
		public void Goldberg(Blackhole blackhole) {
			benchSssp(new ShortestPathSingleSourceGoldberg(), blackhole);
		}
	}

	private static class GraphArgs {
		final IntGraph g;
		final IWeightFunctionInt w;
		final int source;

		GraphArgs(IntGraph g, IWeightFunctionInt w, int source) {
			this.g = g;
			this.w = w;
			this.source = source;
		}
	}
}
