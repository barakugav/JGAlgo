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
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.connect.StronglyConnectedComponentsAlgo;
import com.jgalgo.alg.connect.StronglyConnectedComponentsPathBasedDfs;
import com.jgalgo.alg.connect.StronglyConnectedComponentsTarjan;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class StrongConnectedComponentsBench {

	@Param({ "|V|=64 |E|=256", "|V|=200 |E|=1200", "|V|=512 |E|=4096" })
	public String args;

	private List<IntGraph> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe9485d7a86646b18L);
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			IntGraph g = GraphsTestUtils.randGraph(n, m, true, seedGen.nextSeed());
			graphs.add(g);
		}
	}

	private void benchSccs(StronglyConnectedComponentsAlgo algo, Blackhole blackhole) {
		IntGraph graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		IVertexPartition result = (IVertexPartition) algo.findStronglyConnectedComponents(graph);
		blackhole.consume(result);
	}

	@Benchmark
	public void pathBasedDfs(Blackhole blackhole) {
		benchSccs(new StronglyConnectedComponentsPathBasedDfs(), blackhole);
	}

	@Benchmark
	public void Tarjan(Blackhole blackhole) {
		benchSccs(new StronglyConnectedComponentsTarjan(), blackhole);
	}
}
