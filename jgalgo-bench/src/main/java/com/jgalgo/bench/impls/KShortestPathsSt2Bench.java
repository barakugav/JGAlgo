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
import com.jgalgo.alg.path.KShortestPathsSt2;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class KShortestPathsSt2Bench {

	private List<BenchArgs> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	private void setup(String args, boolean directed) {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));
		int k = Integer.parseInt(argsMap.get("k"));

		final SeedGenerator seedGen = new SeedGenerator(0xb6b1a069899f9baaL);
		final Random rand = new Random(seedGen.nextSeed());
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			IntGraph g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsInt(g, 0, 64, seedGen.nextSeed());
			int source = Graphs.randVertex(g, rand);
			int target = Graphs.randVertex(g, rand);
			graphs.add(new BenchArgs(g, w, source, target, k));
		}
	}

	@SuppressWarnings("boxing")
	private void benchAlgo(KShortestPathsSt2 algo, Blackhole blackhole) {
		BenchArgs args = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Object res = algo.computeKShortestPaths(args.g, args.w, args.source, args.target, args.k);
		blackhole.consume(res);
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Directed extends KShortestPathsSt2Bench {

		@Param({ "|V|=200 |E|=1500 k=5", "|V|=800 |E|=10000 k=15", "|V|=1500 |E|=3000 k=50" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			super.setup(args, true);
		}

		@Benchmark
		public void Yen(Blackhole blackhole) {
			KShortestPathsSt2.Builder builder = KShortestPathsSt2.builder();
			builder.setOption("impl", "yen");
			super.benchAlgo(builder.build(), blackhole);
		}

		@Benchmark
		public void HershbergerMaxelSuri(Blackhole blackhole) {
			KShortestPathsSt2.Builder builder = KShortestPathsSt2.builder();
			builder.setOption("impl", "hershberger-maxel-suri");
			builder.setOption("fast-replacement-threshold", 10);
			super.benchAlgo(builder.build(), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Undirected extends KShortestPathsSt2Bench {

		@Param({ "|V|=200 |E|=1500 k=5", "|V|=800 |E|=10000 k=15", "|V|=1500 |E|=3000 k=50" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			super.setup(args, false);
		}

		@Benchmark
		public void Yen(Blackhole blackhole) {
			KShortestPathsSt2.Builder builder = KShortestPathsSt2.builder();
			builder.setOption("impl", "yen");
			super.benchAlgo(builder.build(), blackhole);
		}

		@Benchmark
		public void KShortestPathsStKatohIbarakiMine(Blackhole blackhole) {
			KShortestPathsSt2.Builder builder = KShortestPathsSt2.builder();
			builder.setOption("impl", "katoh-ibaraki-mine");
			builder.setOption("fast-replacement-threshold", 10);
			super.benchAlgo(builder.build(), blackhole);
		}
	}

	private static class BenchArgs {
		final IntGraph g;
		final IWeightFunction w;
		final int source;
		final int target;
		final int k;

		BenchArgs(IntGraph g, IWeightFunction w, int source, int target, int k) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
			this.k = k;
		}

	}
}
