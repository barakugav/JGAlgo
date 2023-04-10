package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.jgalgo.APSP;
import com.jgalgo.APSPFloydWarshall;
import com.jgalgo.APSPJohnson;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class APSPBench {

    @Param({ "|V|=64 |E|=256", "|V|=200 |E|=1200", "|V|=512 |E|=4096" })
    public String graphSize;
    private int n, m;

    private List<Pair<Graph, EdgeWeightFunc.Int>> graphs;

    @Setup(Level.Iteration)
    public void setup() {
        Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
        n = Integer.parseInt(graphSizeValues.get("|V|"));
        m = Integer.parseInt(graphSizeValues.get("|E|"));

        final SeedGenerator seedGen = new SeedGenerator(0xe9485d7a86646b18L);
        final int graphsNum = 20;
        graphs = new ArrayList<>(graphsNum);
        for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
            Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
                    .parallelEdges(true).selfEdges(true)
                    .cycles(true).connected(false).build();
            EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
            graphs.add(Pair.of(g, w));
        }
    }

    private void benchAPSPPositiveWeights(Supplier<? extends APSP> builder, Blackhole blackhole) {
        for (Pair<Graph, EdgeWeightFunc.Int> graph : graphs) {
            APSP algo = builder.get();
            APSP.Result result = algo.calcAllShortestPaths(graph.first(), graph.second());
            blackhole.consume(result);
        }
    }

    @Benchmark
    public void benchAPSPFloydWarshall(Blackhole blackhole) {
        benchAPSPPositiveWeights(APSPFloydWarshall::new, blackhole);
    }

    @Benchmark
    public void benchAPSPJohnson(Blackhole blackhole) {
        benchAPSPPositiveWeights(APSPJohnson::new, blackhole);
    }
}