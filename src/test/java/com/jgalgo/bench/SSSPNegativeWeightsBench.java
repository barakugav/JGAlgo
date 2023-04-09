package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
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

import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPBellmanFord;
import com.jgalgo.SSSPGoldberg1995;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.test.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class SSSPNegativeWeightsBench {

	@Param
	public GraphSize graphSize;
	private List<GraphArgs> graphs;

	@Setup(Level.Iteration)
	public void setup() {
		final SeedGenerator seedGen = new SeedGenerator(0x9814dcfe5851ab08L);
		Random rand = new Random(seedGen.nextSeed());
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(graphSize.n).m(graphSize.m).directed(true)
					.parallelEdges(true).selfEdges(true)
					.cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntNeg(g, seedGen.nextSeed());
			int source = rand.nextInt(g.vertices().size());
			graphs.add(new GraphArgs(g, w, source));
		}
	}

	private void benchSSSPNegativeWeights(Supplier<? extends SSSP> builder, Blackhole blackhole) {
		for (GraphArgs args : graphs) {
			SSSP algo = builder.get();
			SSSP.Result result = algo.calcDistances(args.g, args.w, args.source);
			blackhole.consume(result);
		}
	}

	@Benchmark
	public void benchSSSPNegativeWeightsBellmanFord(Blackhole blackhole) {
		benchSSSPNegativeWeights(SSSPBellmanFord::new, blackhole);
	}

	@Benchmark
	public void benchSSSPNegativeWeightsGoldberg1995(Blackhole blackhole) {
		benchSSSPNegativeWeights(SSSPGoldberg1995::new, blackhole);
	}

	public static enum GraphSize {
		v64_e256, v512_e4096, v4096_e16384;

		final int n, m;

		GraphSize() {
			String[] strs = toString().split("_");
			assert strs.length == 2;
			this.n = Integer.parseInt(strs[0].substring(1));
			this.m = Integer.parseInt(strs[1].substring(1));
		}
	}

	private static class GraphArgs {
		final Graph g;
		final EdgeWeightFunc.Int w;
		final int source;

		GraphArgs(Graph g, EdgeWeightFunc.Int w, int source) {
			this.g = g;
			this.w = w;
			this.source = source;
		}
	}
}
