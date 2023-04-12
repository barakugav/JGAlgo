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
import com.jgalgo.Graphs;
import com.jgalgo.MaxFlow;
import com.jgalgo.MaxFlowDinic;
import com.jgalgo.MaxFlowDinicDynamicTrees;
import com.jgalgo.MaxFlowEdmondsKarp;
import com.jgalgo.MaxFlowPushRelabel;
import com.jgalgo.MaxFlowPushRelabelDynamicTrees;
import com.jgalgo.MaxFlow.FlowNetwork;
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

    @Param({ "|V|=30 |E|=300", "|V|=200 |E|=1500", "|V|=1600 |E|=10000" })
    public String graphSize;
    private int n, m;

    private List<MaxFlowTask> graphs;

    private static class MaxFlowTask {
        final DiGraph g;
        final FlowNetwork flow;
        final int source;
        final int target;

        MaxFlowTask(DiGraph g, FlowNetwork flow, int source, int target) {
            this.g = g;
            this.flow = flow;
            this.source = source;
            this.target = target;
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
            FlowNetwork flow = MaxFlowTestUtils.randNetwork(g, seedGen.nextSeed());
            int source, target;
            for (;;) {
                source = rand.nextInt(g.vertices().size());
                target = rand.nextInt(g.vertices().size());
                if (source != target && Graphs.findPath(g, source, target) != null)
                    break;
            }

            graphs.add(new MaxFlowTask(g, flow, source, target));
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
            double flow = algo.calcMaxFlow(graph.g, graph.flow, graph.source, graph.target);
            blackhole.consume(flow);
        }
    }

    @Benchmark
    public void benchMaxFlowEdmondsKarp(Blackhole blackhole) {
        benchMaxFlow(MaxFlowEdmondsKarp::new, blackhole);
    }

    @Benchmark
    public void benchMaxFlowDinic(Blackhole blackhole) {
        benchMaxFlow(MaxFlowDinic::new, blackhole);
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
    public void benchMaxFlowPushRelabelDynamicTrees(Blackhole blackhole) {
        benchMaxFlow(MaxFlowPushRelabelDynamicTrees::new, blackhole);
    }

}
