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
import java.util.function.IntToDoubleFunction;
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
import com.jgalgo.alg.path.ShortestPathAStar;
import com.jgalgo.alg.path.ShortestPathHeuristicSt;
import com.jgalgo.alg.path.ShortestPathSingleSource;
import com.jgalgo.alg.path.ShortestPathSt;
import com.jgalgo.alg.path.ShortestPathStBidirectionalDijkstra;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShortestPathStBench {

	List<GraphArgs> graphs;
	final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	void benchStShortestPath(Object algo, Blackhole blackhole) {
		GraphArgs args = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		IntGraph g = args.g;
		IWeightFunction w = args.w;
		int source = args.source;
		int target = args.target;
		IntToDoubleFunction heuristic = args.heuristic;

		Object result;
		if (algo instanceof ShortestPathHeuristicSt) {
			result = ((ShortestPathHeuristicSt) algo).computeShortestPath(g, w, source, target, heuristic);
		} else if (algo instanceof ShortestPathSt) {
			result = ((ShortestPathSt) algo)
					.computeShortestPath(g, w, Integer.valueOf(source), Integer.valueOf(target));
		} else {
			throw new IllegalArgumentException();
		}
		blackhole.consume(result);
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends ShortestPathStBench {

		@Param({ "|V|=64 MaxWeight=64", "|V|=512 MaxWeight=50", "|V|=512 MaxWeight=600", "|V|=4096 MaxWeight=200",
				"|V|=4096 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0x42eee7954114ebcaL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphGnp(n, true, seedGen.nextSeed());
				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsInt(g, 0, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				IntToDoubleFunction randAdmissibleHeuristic = randAdmissibleHeuristic(g, w, target, seedGen.nextSeed());
				graphs.add(new GraphArgs(g, w, source, target, randAdmissibleHeuristic));
			}
		}

		@Benchmark
		public void BidirectionalDijkstra(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathStBidirectionalDijkstra(), blackhole);
		}

		@Benchmark
		public void AStar(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathAStar(), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends ShortestPathStBench {

		@Param({ "|V|=64 MaxWeight=64", "|V|=512 MaxWeight=50", "|V|=512 MaxWeight=600", "|V|=4096 MaxWeight=200",
				"|V|=4096 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0x462e686a3a46d469L);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, false, seedGen.nextSeed());
				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsInt(g, 0, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				IntToDoubleFunction randAdmissibleHeuristic = randAdmissibleHeuristic(g, w, target, seedGen.nextSeed());
				graphs.add(new GraphArgs(g, w, source, target, randAdmissibleHeuristic));
			}
		}

		@Benchmark
		public void BidirectionalDijkstra(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathStBidirectionalDijkstra(), blackhole);
		}

		@Benchmark
		public void AStar(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathAStar(), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends ShortestPathStBench {

		@Param({ "|V|=64 |E|=256 MaxWeight=64", "|V|=512 |E|=4096 MaxWeight=50", "|V|=512 |E|=4096 MaxWeight=600",
				"|V|=4096 |E|=16384 MaxWeight=200", "|V|=4096 |E|=16384 MaxWeight=6000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));
			int maxWeight = Integer.parseInt(argsMap.get("MaxWeight"));

			final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, true, seedGen.nextSeed());
				IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsInt(g, 0, maxWeight, seedGen.nextSeed());
				int source = Graphs.randVertex(g, rand);
				int target = Graphs.randVertex(g, rand);
				IntToDoubleFunction randAdmissibleHeuristic = randAdmissibleHeuristic(g, w, target, seedGen.nextSeed());
				graphs.add(new GraphArgs(g, w, source, target, randAdmissibleHeuristic));
			}
		}

		@Benchmark
		public void BidirectionalDijkstra(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathStBidirectionalDijkstra(), blackhole);
		}

		@Benchmark
		public void AStar(Blackhole blackhole) {
			benchStShortestPath(new ShortestPathAStar(), blackhole);
		}
	}

	private static class GraphArgs {
		final IntGraph g;
		final IWeightFunctionInt w;
		final int source;
		final int target;
		final IntToDoubleFunction heuristic;

		GraphArgs(IntGraph g, IWeightFunctionInt w, int source, int target, IntToDoubleFunction heuristic) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
			this.heuristic = heuristic;
		}
	}

	private static IntToDoubleFunction randAdmissibleHeuristic(IntGraph g, IWeightFunction w, int target, long seed) {
		Random rand = new Random(seed);
		if (g.isDirected())
			g = g.reverseView();

		IWeightsDouble w0 = IWeights.createExternalEdgesWeights(g, double.class);
		for (int e : g.edges())
			w0.set(e, w.weight(e) * (0.5 + rand.nextDouble() / 2));

		ShortestPathSingleSource.IResult ssspRes = (ShortestPathSingleSource.IResult) ShortestPathSingleSource
				.newInstance()
				.computeShortestPaths(g, w0, Integer.valueOf(target));
		return ssspRes::distance;
	}

}
