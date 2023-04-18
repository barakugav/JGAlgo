package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
import com.jgalgo.GraphBuilder;
import com.jgalgo.Graphs;
import com.jgalgo.MaxFlow;
import com.jgalgo.MaxFlowDinic;
import com.jgalgo.MaxFlowDinicDynamicTrees;
import com.jgalgo.MaxFlowEdmondsKarp;
import com.jgalgo.MaxFlowPushRelabel;
import com.jgalgo.MaxFlowPushRelabelDynamicTrees;
import com.jgalgo.MaxFlowPushRelabelHighestFirst;
import com.jgalgo.MaxFlowPushRelabelLowestFirst;
import com.jgalgo.MaxFlowPushRelabelToFront;
import com.jgalgo.test.MaxFlowTestUtils;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.ints.IntIterator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MaxFlowBench {

	@Param({ "|V|=30 |E|=300", "|V|=200 |E|=1500", "|V|=800 |E|=10000" })
	public String graphSize;
	private int n, m;

	private List<MaxFlowTask> graphs;

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

	@Setup(Level.Trial)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		Random rand = new Random(seedGen.nextSeed());
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(false).selfEdges(false)
					.cycles(true).connected(false).build();
			FlowNetwork.Int flow = MaxFlowTestUtils.randNetworkInt(g, seedGen.nextSeed());
			int source, sink;
			for (;;) {
				source = rand.nextInt(g.vertices().size());
				sink = rand.nextInt(g.vertices().size());
				if (source != sink && Graphs.findPath(g, source, sink) != null)
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

	private void benchMaxFlow(Supplier<? extends MaxFlow> builder, Blackhole blackhole) {
		for (MaxFlowTask graph : graphs) {
			MaxFlow algo = builder.get();
			double flow = algo.calcMaxFlow(graph.g, graph.flow, graph.source, graph.sink);
			blackhole.consume(flow);
		}
	}

	@Benchmark
	public void benchMaxFlowEdmondsKarp(Blackhole blackhole) {
		benchMaxFlow(MaxFlowEdmondsKarp::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowDinicWithLinkedGraph(Blackhole blackhole) {
		benchMaxFlow(() -> {
			MaxFlowDinic algo = new MaxFlowDinic();
			algo.experimental_setLayerGraphFactory(() -> GraphBuilder.newInstance("com.jgalgo.Linked"));
			return algo;
		}, blackhole);
	}

	@Benchmark
	public void benchMaxFlowDinicWithArrayGraph(Blackhole blackhole) {
		benchMaxFlow(() -> {
			MaxFlowDinic algo = new MaxFlowDinic();
			algo.experimental_setLayerGraphFactory(() -> GraphBuilder.newInstance("com.jgalgo.Array"));
			return algo;
		}, blackhole);
	}

	@Benchmark
	public void benchMaxFlowDinicDynamicTrees(Blackhole blackhole) {
		benchMaxFlow(MaxFlowDinicDynamicTrees::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowPushRelabel(Blackhole blackhole) {
		benchMaxFlow(MaxFlowPushRelabel::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowPushRelabelToFront(Blackhole blackhole) {
		benchMaxFlow(MaxFlowPushRelabelToFront::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowPushRelabelHighestFirst(Blackhole blackhole) {
		benchMaxFlow(MaxFlowPushRelabelHighestFirst::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowPushRelabelLowestFirst(Blackhole blackhole) {
		benchMaxFlow(MaxFlowPushRelabelLowestFirst::new, blackhole);
	}

	@Benchmark
	public void benchMaxFlowPushRelabelDynamicTrees(Blackhole blackhole) {
		benchMaxFlow(MaxFlowPushRelabelDynamicTrees::new, blackhole);
	}

}
