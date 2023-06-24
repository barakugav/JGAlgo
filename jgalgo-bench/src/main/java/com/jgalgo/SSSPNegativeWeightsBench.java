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

import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class SSSPNegativeWeightsBench {

	@Param({ "|V|=64 |E|=256", "|V|=512 |E|=4096", "|V|=4096 |E|=16384" })
	public String args;

	private List<GraphArgs> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x9814dcfe5851ab08L);
		Random rand = new Random(seedGen.nextSeed());
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];
			graphs.add(new GraphArgs(g, w, source));
		}
	}

	private void benchSSSP(Supplier<? extends ShortestPathSingleSource> builder, Blackhole blackhole) {
		GraphArgs args = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		ShortestPathSingleSource algo = builder.get();
		ShortestPathSingleSource.Result result = algo.computeShortestPaths(args.g, args.w, args.source);
		blackhole.consume(result);
	}

	@Benchmark
	public void BellmanFord(Blackhole blackhole) {
		benchSSSP(ShortestPathSingleSourceBellmanFord::new, blackhole);
	}

	@Benchmark
	public void Goldberg(Blackhole blackhole) {
		benchSSSP(ShortestPathSingleSourceGoldberg::new, blackhole);
	}

	private static class GraphArgs {
		final Graph g;
		final WeightFunction.Int w;
		final int source;

		GraphArgs(Graph g, WeightFunction.Int w, int source) {
			this.g = g;
			this.w = w;
			this.source = source;
		}
	}
}
