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
import com.jgalgo.alg.ColoringAlgo;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class ColoringBench {

	@Param({ "|V|=100 |E|=100", "|V|=200 |E|=1000", "|V|=1600 |E|=10000" })
	public String args;

	private List<Graph> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x566c25f996355cb4L);
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			graphs.add(g);
		}
	}

	private void benchColoring(ColoringAlgo.Builder builder, Blackhole blackhole) {
		Graph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		ColoringAlgo algo = builder.build();
		ColoringAlgo.Result res = algo.computeColoring(g);
		blackhole.consume(res);
	}

	@Benchmark
	public void Greedy(Blackhole blackhole) {
		final long seed = 0xefeae78aba502d4aL;
		benchColoring(ColoringAlgo.newBuilder().setOption("impl", "greedy").setOption("seed", Long.valueOf(seed)),
				blackhole);
	}

	@Benchmark
	public void DSatur(Blackhole blackhole) {
		benchColoring(ColoringAlgo.newBuilder().setOption("impl", "dsatur"), blackhole);
	}

	@Benchmark
	public void RecursiveLargestFirst(Blackhole blackhole) {
		benchColoring(ColoringAlgo.newBuilder().setOption("impl", "rlf"), blackhole);
	}

}
