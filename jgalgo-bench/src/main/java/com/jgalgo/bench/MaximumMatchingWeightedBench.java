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

package com.jgalgo.bench;

import java.util.ArrayList;
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
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.Matching;
import com.jgalgo.MaximumMatching;
import com.jgalgo.bench.TestUtils.SeedGenerator;
import it.unimi.dsi.fastutil.Pair;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class MaximumMatchingWeightedBench {

	@Param({ "|V|=200 |E|=1500", "|V|=800 |E|=10000", "|V|=1500 |E|=3000" })
	public String args;

	private List<Pair<Graph, EdgeWeightFunc>> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Trial)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			graphs.add(Pair.of(g, w));
		}
	}

	private void benchMaximumMatchingWeighted(MaximumMatching algo, Blackhole blackhole) {
		Pair<Graph, EdgeWeightFunc> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Graph g = gw.first();
		EdgeWeightFunc w = gw.second();
		Matching matching = algo.computeMaximumWeightedMatching(g, w);
		blackhole.consume(matching);
	}

	private void benchMaximumMatchingWeightedPerfect(MaximumMatching algo, Blackhole blackhole) {
		Pair<Graph, EdgeWeightFunc> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Graph g = gw.first();
		EdgeWeightFunc w = gw.second();
		Matching matching = algo.computeMaximumWeightedPerfectMatching(g, w);
		blackhole.consume(matching);
	}

	private static MaximumMatching getAlgo(String name) {
		MaximumMatching algo = MaximumMatching.newBuilder().setOption("impl", name).build();
		if (!algo.getClass().getSimpleName().equals(name))
			throw new IllegalArgumentException("implementation not found: " + name);
		return algo;
	}

	@Benchmark
	public void MaximumMatchingWeightedGabow1990Simpler(Blackhole blackhole) {
		benchMaximumMatchingWeighted(getAlgo("MaximumMatchingWeightedGabow1990Simpler"), blackhole);
	}

	@Benchmark
	public void MaximumMatchingWeightedGabow1990(Blackhole blackhole) {
		benchMaximumMatchingWeighted(getAlgo("MaximumMatchingWeightedGabow1990"), blackhole);
	}

	@Benchmark
	public void MaximumMatchingWeightedGabow1990SimplerPerfect(Blackhole blackhole) {
		benchMaximumMatchingWeightedPerfect(getAlgo("MaximumMatchingWeightedGabow1990Simpler"), blackhole);
	}

	@Benchmark
	public void MaximumMatchingWeightedGabow1990Perfect(Blackhole blackhole) {
		benchMaximumMatchingWeightedPerfect(getAlgo("MaximumMatchingWeightedGabow1990"), blackhole);
	}

}
