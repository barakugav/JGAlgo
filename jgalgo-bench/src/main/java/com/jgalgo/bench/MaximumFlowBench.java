package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
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

import com.jgalgo.DiGraph;
import com.jgalgo.FlowNetwork;
import com.jgalgo.MaximumFlow;
import com.jgalgo.MaximumFlowDinic;
import com.jgalgo.MaximumFlowDinicDynamicTrees;
import com.jgalgo.MaximumFlowEdmondsKarp;
import com.jgalgo.MaximumFlowPushRelabel;
import com.jgalgo.MaximumFlowPushRelabelDynamicTrees;
import com.jgalgo.MaximumFlowPushRelabelHighestFirst;
import com.jgalgo.MaximumFlowPushRelabelLowestFirst;
import com.jgalgo.MaximumFlowPushRelabelToFront;
import com.jgalgo.Path;
import com.jgalgo.bench.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.bench.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.ints.IntIterator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MaximumFlowBench {

	@Param({ "|V|=30 |E|=300", "|V|=200 |E|=1500", "|V|=800 |E|=10000" })
	public String args;
	private int n, m;

	private List<MaxFlowTask> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Trial)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("|V|"));
		m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		Random rand = new Random(seedGen.nextSeed());
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(false).selfEdges(false)
					.cycles(true).connected(false).build();
			FlowNetwork.Int flow = randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (;;) {
				source = rand.nextInt(g.vertices().size());
				sink = rand.nextInt(g.vertices().size());
				if (source != sink && Path.findPath(g, source, sink) != null)
					break;
			}

			graphs.add(new MaxFlowTask(g, flow, source, sink));
		}
	}

	@Setup(Level.Invocation)
	public void resetFlow() {
		for (MaxFlowTask graph : graphs) {
			for (IntIterator it = graph.g.edges().iterator(); it.hasNext();) {
				int edge = it.nextInt();
				graph.flow.setFlow(edge, 0);
			}
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
	public void PushRelabel(Blackhole blackhole) {
		benchMaxFlow(MaximumFlowPushRelabel::new, blackhole);
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
		final DiGraph g;
		final FlowNetwork.Int flow;
		final int source;
		final int sink;

		MaxFlowTask(DiGraph g, FlowNetwork.Int flow, int source, int sink) {
			this.g = g;
			this.flow = flow;
			this.source = source;
			this.sink = sink;
		}
	}

	private static FlowNetwork.Int randNetworkInt(DiGraph g, long seed) {
		Random rand = new Random(seed);
		FlowNetwork.Int flow = FlowNetwork.Int.createAsEdgeWeight(g);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int cap = rand.nextInt(16384);
			flow.setCapacity(e, cap);
		}
		return flow;
	}

}
