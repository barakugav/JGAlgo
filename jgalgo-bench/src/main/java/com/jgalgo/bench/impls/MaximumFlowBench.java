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
import com.jgalgo.FlowNetwork;
import com.jgalgo.MaximumFlow;
import com.jgalgo.Path;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MaximumFlowBench {

	List<MaxFlowTask> graphs;
	final int graphsNum = 31;
	final AtomicInteger graphIdx = new AtomicInteger();

	public void resetFlow() {
		for (MaxFlowTask graph : graphs)
			for (int e : graph.g.edges())
				graph.flow.setFlow(e, 0);
	}

	void benchMaxFlow(MaximumFlow.Builder builder, Blackhole blackhole) {
		MaxFlowTask graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		MaximumFlow algo = builder.build();
		double flow = algo.computeMaximumFlow(graph.g, graph.flow, graph.source, graph.sink);
		blackhole.consume(flow);
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends MaximumFlowBench {

		@Param({ "|V|=1000", "|V|=2000", "|V|=2500" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0x94fc6ec413f60392L);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randomGraphGnp(n, directed, seedGen.nextSeed());
				FlowNetwork.Int flow = randNetworkInt(g, seedGen.nextSeed());

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends MaximumFlowBench {

		@Param({ "|V|=3000", "|V|=4500", "|V|=6000" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0xdc6c4cf7f4d3843cL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, directed, seedGen.nextSeed());
				FlowNetwork.Int flow = randNetworkInt(g, seedGen.nextSeed());

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void DinicDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic-dynamic-trees"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo-dynamic-trees"), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends MaximumFlowBench {

		@Param({ "|V|=1500 |E|=5000", "|V|=2500 |E|=8000", "|V|=4000 |E|=16000" })
		public String args;

		// @Param({ "directed", "undirected" })
		public String directed = "directed";

		@Override
		@Setup(Level.Invocation)
		public void resetFlow() {
			super.resetFlow();
		}

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));
			boolean directed = this.directed.equals("directed");

			final SeedGenerator seedGen = new SeedGenerator(0x9716aede5cfa6eabL);
			Random rand = new Random(seedGen.nextSeed());
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				Graph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, directed, seedGen.nextSeed());
				FlowNetwork.Int flow = randNetworkInt(g, seedGen.nextSeed());

				IntIntPair sourceSink = chooseSourceSink(g, rand);
				graphs.add(new MaxFlowTask(g, flow, sourceSink.firstInt(), sourceSink.secondInt()));
			}
		}

		@Benchmark
		public void EdmondsKarp(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "edmonds-karp"), blackhole);
		}

		@Benchmark
		public void Dinic(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic"), blackhole);
		}

		@Benchmark
		public void DinicDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "dinic-dynamic-trees"), blackhole);
		}

		@Benchmark
		public void PushRelabelFifo(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo"), blackhole);
		}

		@Benchmark
		public void PushRelabelToFront(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-move-to-front"), blackhole);
		}

		@Benchmark
		public void PushRelabelHighestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-highest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelPartialAugment(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-partial-augment"), blackhole);
		}

		@Benchmark
		public void PushRelabelLowestFirst(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-lowest-first"), blackhole);
		}

		@Benchmark
		public void PushRelabelDynamicTrees(Blackhole blackhole) {
			benchMaxFlow(MaximumFlow.newBuilder().setOption("impl", "push-relabel-fifo-dynamic-trees"), blackhole);
		}
	}

	private static class MaxFlowTask {
		final Graph g;
		final FlowNetwork.Int flow;
		final int source;
		final int sink;

		MaxFlowTask(Graph g, FlowNetwork.Int flow, int source, int sink) {
			this.g = g;
			this.flow = flow;
			this.source = source;
			this.sink = sink;
		}
	}

	private static FlowNetwork.Int randNetworkInt(Graph g, long seed) {
		Random rand = new Random(seed);
		Weights.Int capacities = Weights.createExternalEdgesWeights(g, int.class);
		Weights.Int flows = Weights.createExternalEdgesWeights(g, int.class);
		FlowNetwork.Int flow = FlowNetwork.Int.createFromEdgeWeights(capacities, flows);
		for (int e : g.edges())
			flow.setCapacity(e, 5000 + rand.nextInt(16384));
		return flow;
	}

	private static IntIntPair chooseSourceSink(Graph g, Random rand) {
		int source, sink;
		for (int[] vs = g.vertices().toIntArray();;) {
			source = vs[rand.nextInt(vs.length)];
			sink = vs[rand.nextInt(vs.length)];
			if (source != sink && Path.findPath(g, source, sink) != null)
				return IntIntPair.of(source, sink);
		}
	}

}
