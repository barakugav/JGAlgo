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
import com.jgalgo.alg.IPath;
import com.jgalgo.alg.MinimumMeanCycle;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.RandomGraphBuilder;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class MinimumMeanCycleBench {

	@Param({ "|V|=30 |E|=300", "|V|=200 |E|=1500", "|V|=800 |E|=10000" })
	public String args;

	private List<Pair<IntGraph, IWeightFunctionInt>> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Trial)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xab5a0b68164b217fL);
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(false).build();
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			graphs.add(Pair.of(g, w));
		}
	}

	private void benchMinMeanCycle(MinimumMeanCycle algo, Blackhole blackhole) {
		Pair<IntGraph, IWeightFunctionInt> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		IntGraph g = gw.first();
		IWeightFunctionInt w = gw.second();
		IPath cycle = (IPath) algo.computeMinimumMeanCycle(g, w);
		blackhole.consume(cycle);
	}

	@Benchmark
	public void Howard(Blackhole blackhole) {
		benchMinMeanCycle(getAlgo("howard"), blackhole);
	}

	@Benchmark
	public void DasdanGupta(Blackhole blackhole) {
		benchMinMeanCycle(getAlgo("dasdan-gupta"), blackhole);
	}

	private static MinimumMeanCycle getAlgo(String implName) {
		MinimumMeanCycle.Builder builder = MinimumMeanCycle.newBuilder();
		builder.setOption("impl", implName);
		return builder.build();
	}

}
