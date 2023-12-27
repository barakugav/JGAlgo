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
import com.jgalgo.alg.MinimumSpanningTree;
import com.jgalgo.alg.ShortestPathSingleSource;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.ds.IntIntBinomialHeap;
import com.jgalgo.internal.ds.IntIntFibonacciHeap;
import com.jgalgo.internal.ds.IntIntPairingHeap;
import com.jgalgo.internal.ds.IntIntRedBlackTree;
import com.jgalgo.internal.ds.IntIntSplayTree;
import com.jgalgo.internal.ds.ReferenceableHeap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class ReferenceableHeapBench {

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

		final SeedGenerator seedGen = new SeedGenerator(0x88da246e71ef3dacL);
		Random rand = new Random(seedGen.nextSeed());
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			IntGraph g = GraphsTestUtils.randGraph(n, m, false, seedGen.nextSeed());
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = Graphs.randVertex(g, rand);
			graphs.add(new GraphArgs(g, w, source));
		}
	}

	private void benchHeap(ReferenceableHeap.Builder heapBuilder, Blackhole blackhole) {
		GraphArgs args = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));

		/* SSSP */
		ShortestPathSingleSource.Builder ssspBuilder = ShortestPathSingleSource.builder();
		ssspBuilder.setOption("heap-builder", heapBuilder);
		ShortestPathSingleSource ssspAlgo = ssspBuilder.build();
		ShortestPathSingleSource.IResult ssspRes = (ShortestPathSingleSource.IResult) ssspAlgo
				.computeShortestPaths(args.g, args.w, Integer.valueOf(args.source));
		blackhole.consume(ssspRes);

		/* Prim MST */
		MinimumSpanningTree.Builder mstBuilder = MinimumSpanningTree.builder();
		mstBuilder.setOption("heap-builder", heapBuilder);
		MinimumSpanningTree mstAlgo = mstBuilder.build();
		MinimumSpanningTree.IResult mst =
				(MinimumSpanningTree.IResult) mstAlgo.computeMinimumSpanningTree(args.g, args.w);
		blackhole.consume(mst);
	}

	@Benchmark
	public void Pairings(Blackhole blackhole) {
		benchHeap((keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntPairingHeap((IntComparator) comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		}, blackhole);
	}

	@Benchmark
	public void Fibonacci(Blackhole blackhole) {
		benchHeap((keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntFibonacciHeap((IntComparator) comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		}, blackhole);
	}

	@Benchmark
	public void Binomial(Blackhole blackhole) {
		benchHeap((keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntBinomialHeap((IntComparator) comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		}, blackhole);
	}

	@Benchmark
	public void RedBlackTree(Blackhole blackhole) {
		benchHeap((keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntRedBlackTree((IntComparator) comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		}, blackhole);
	}

	@Benchmark
	public void SplayTree(Blackhole blackhole) {
		benchHeap((keyType, valueType, comparator) -> {
			if (keyType == int.class && valueType == int.class)
				return new IntIntSplayTree((IntComparator) comparator);
			throw new UnsupportedOperationException("Unsupported heap type: " + keyType + ", " + valueType);
		}, blackhole);
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
