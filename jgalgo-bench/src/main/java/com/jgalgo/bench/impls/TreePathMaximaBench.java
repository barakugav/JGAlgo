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
import com.jgalgo.alg.TreePathMaxima;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class TreePathMaximaBench {

	@Param({ "N=128 M=128", "N=2500 M=2500", "N=15000 M=15000" })
	public String args;
	public int n, m;

	private List<TPMArgs> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("N"));
		m = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x28ddf3f2d9c5c873L);
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph tree = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(tree, seedGen.nextSeed());
			TreePathMaxima.Queries queries = generateRandQueries(tree, m, seedGen.nextSeed());
			graphs.add(new TPMArgs(tree, w, queries));
		}
	}

	private void benchTPM(TreePathMaxima.Builder builder, Blackhole blackhole) {
		TPMArgs g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		TreePathMaxima algo = builder.build();
		TreePathMaxima.Result result = algo.computeHeaviestEdgeInTreePaths(g.tree, g.w, g.queries);
		blackhole.consume(result);
	}

	@Benchmark
	public void TPMHagerup(Blackhole blackhole) {
		benchTPM(TreePathMaxima.newBuilder().setOption("bits-lookup-tables-enable", Boolean.FALSE), blackhole);
	}

	@Benchmark
	public void TPMHagerupWithBitsLookupTable(Blackhole blackhole) {
		benchTPM(TreePathMaxima.newBuilder().setOption("bits-lookup-tables-enable", Boolean.TRUE), blackhole);
	}

	private static class TPMArgs {
		final Graph tree;
		final WeightFunction w;
		final TreePathMaxima.Queries queries;

		TPMArgs(Graph tree, WeightFunction w, TreePathMaxima.Queries queries) {
			this.tree = tree;
			this.w = w;
			this.queries = queries;
		}
	}

	private static TreePathMaxima.Queries generateRandQueries(Graph tree, int m, long seed) {
		Random rand = new Random(seed);
		TreePathMaxima.Queries queries = TreePathMaxima.Queries.newInstance();
		int[] vs = tree.vertices().toIntArray();
		for (int q = 0; q < m; q++) {
			int i, j;
			do {
				i = rand.nextInt(vs.length);
				j = rand.nextInt(vs.length);
			} while (i == j);
			queries.addQuery(vs[i], vs[j]);
		}
		return queries;
	}

}
