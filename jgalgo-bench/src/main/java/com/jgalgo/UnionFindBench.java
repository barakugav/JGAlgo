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
import com.jgalgo.TestUtils.SeedGenerator;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class UnionFindBench {

	@Param({ "|V|=64 |E|=256", "|V|=512 |E|=4096", "|V|=4096 |E|=16384", "|V|=20000 |E|=50000" })
	public String args;

	private List<Pair<IndexGraph, int[]>> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xecbc984604fcd0afL);
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			IndexGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed()).indexGraph();
			WeightFunction.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			/*
			 * sort the edges in setup instead of using standard Kruskal MST implementation during benchmark to isolate
			 * union find operations
			 */
			int[] edges = g.edges().toIntArray();
			IntArrays.parallelQuickSort(edges, w);

			int[] edgesWithEndpoint = new int[edges.length * 3];
			for (int i = 0; i < edges.length; i++) {
				int e = edges[i];
				int u = g.edgeSource(e);
				int v = g.edgeTarget(e);
				edgesWithEndpoint[i * 3 + 0] = e;
				edgesWithEndpoint[i * 3 + 1] = u;
				edgesWithEndpoint[i * 3 + 2] = v;
			}

			graphs.add(Pair.of(g, edgesWithEndpoint));
		}
	}

	private void benchUnionFindByRunningMSTKruskal(UnionFind.Builder builder, Blackhole blackhole) {
		Pair<IndexGraph, int[]> graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		int[] mst = calcMSTKruskal(graph.first(), graph.second(), builder);
		blackhole.consume(mst);
	}

	private static int[] calcMSTKruskal(IndexGraph g, int[] edges, UnionFind.Builder ufBuilder) {
		/* !! assume the edge array is sorted by weight !! */
		int n = g.vertices().size();

		/* create union find data structure for each vertex */
		UnionFind uf = ufBuilder.expectedSize(n).build();
		for (int i = 0; i < n; i++)
			uf.make();

		/* iterate over the edges and build the MST */
		int[] mst = new int[n - 1];
		int mstSize = 0;
		for (int i = 0; i < edges.length / 3; i++) {
			int e = edges[i * 3 + 0];
			int u = edges[i * 3 + 1];
			int v = edges[i * 3 + 2];

			if (uf.find(u) != uf.find(v)) {
				uf.union(u, v);
				mst[mstSize++] = e;
			}
		}
		return mst;
	}

	@Benchmark
	public void UnionFindArrayMSTKruskal(Blackhole blackhole) {
		benchUnionFindByRunningMSTKruskal(UnionFind.newBuilder().setOption("impl", "array"), blackhole);
	}

	@Benchmark
	public void UnionFindPtrMSTKruskal(Blackhole blackhole) {
		benchUnionFindByRunningMSTKruskal(UnionFind.newBuilder().setOption("impl", "ptr"), blackhole);
	}

}
