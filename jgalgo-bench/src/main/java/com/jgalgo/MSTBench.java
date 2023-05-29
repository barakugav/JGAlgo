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

package com.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
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
import com.jgalgo.TestUtils.SeedGenerator;
import it.unimi.dsi.fastutil.Pair;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class MSTBench {

	@Param({ "|V|=200 |E|=1000", "|V|=1600 |E|=10000", "|V|=6000 |E|=25000" })
	public String args;

	private List<Pair<Graph, WeightFunction.Int>> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			graphs.add(Pair.of(g, w));
		}
	}

	private void benchMST(Supplier<? extends MinimumSpanningTree> builder, Blackhole blackhole) {
		Pair<Graph, WeightFunction.Int> gw = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Graph g = gw.first();
		WeightFunction.Int w = gw.second();
		MinimumSpanningTree algo = builder.get();
		MinimumSpanningTree.Result mst = algo.computeMinimumSpanningTree(g, w);
		blackhole.consume(mst);
	}

	@Benchmark
	public void Boruvka(Blackhole blackhole) {
		benchMST(MinimumSpanningTreeBoruvka::new, blackhole);
	}

	@Benchmark
	public void FredmanTarjan(Blackhole blackhole) {
		benchMST(MinimumSpanningTreeFredmanTarjan::new, blackhole);
	}

	@Benchmark
	public void Kruskal(Blackhole blackhole) {
		benchMST(MinimumSpanningTreeKruskal::new, blackhole);
	}

	@Benchmark
	public void Prim(Blackhole blackhole) {
		benchMST(MinimumSpanningTreePrim::new, blackhole);
	}

	@Benchmark
	public void Yao(Blackhole blackhole) {
		benchMST(MinimumSpanningTreeYao::new, blackhole);
	}

	@Benchmark
	public void KargerKleinTarjan(Blackhole blackhole) {
		benchMST(MinimumSpanningTreeKargerKleinTarjan::new, blackhole);
	}

}
