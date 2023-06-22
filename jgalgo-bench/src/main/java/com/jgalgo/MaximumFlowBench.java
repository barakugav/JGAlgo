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
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.TestUtils.SeedGenerator;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class MaximumFlowBench {

	@Param({ "|V|=30 |E|=300", "|V|=200 |E|=1500", "|V|=800 |E|=10000" })
	public String args;

	private List<MaxFlowTask> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Trial)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		Random rand = new Random(seedGen.nextSeed());
		graphs = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();
			FlowNetwork.Int flow = randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (int[] vs = g.vertices().toIntArray();;) {
				source = vs[rand.nextInt(vs.length)];
				sink = vs[rand.nextInt(vs.length)];
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			graphs.add(new MaxFlowTask(g, flow, source, sink));
		}
	}

	@Setup(Level.Invocation)
	public void resetFlow() {
		for (MaxFlowTask graph : graphs) {
			for (int e : graph.g.edges())
				graph.flow.setFlow(e, 0);
		}
	}

	private void benchMaxFlow(Supplier<? extends MaximumFlow> builder, Blackhole blackhole) {
		MaxFlowTask graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		MaximumFlow algo = builder.get();
		double flow = algo.computeMaximumFlow(graph.g, graph.flow, graph.source, graph.sink);
		blackhole.consume(flow);
	}

	@Benchmark
	public void EdmondsKarp(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowEdmondsKarp::new, blackhole);
	}

	@Benchmark
	public void Dinic(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowDinic::new, blackhole);
	}

	@Benchmark
	public void DinicDynamicTrees(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowDinicDynamicTrees::new, blackhole);
	}

	@Benchmark
	public void PushRelabelFifo(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabelFifo::new, blackhole);
	}

	@Benchmark
	public void PushRelabelToFront(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabelToFront::new, blackhole);
	}

	@Benchmark
	public void PushRelabelHighestFirst(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabelHighestFirst::new, blackhole);
	}

	@Benchmark
	public void PushRelabelLowestFirst(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabelLowestFirst::new, blackhole);
	}

	@Benchmark
	public void PushRelabelDynamicTrees(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabelDynamicTrees::new, blackhole);
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
		FlowNetwork.Int flow = FlowNetwork.Int.createAsEdgeWeight(g);
		for (int e : g.edges())
			flow.setCapacity(e, rand.nextInt(16384));
		return flow;
	}

}
