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

import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.MST;
import com.jgalgo.MSTBoruvka;
import com.jgalgo.MSTFredmanTarjan;
import com.jgalgo.MSTKargerKleinTarjan;
import com.jgalgo.MSTKruskal;
import com.jgalgo.MSTPrim;
import com.jgalgo.MSTYao;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntCollection;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MSTBench {

	@Param({ "|V|=100 |E|=100", "|V|=200 |E|=1000", "|V|=1600 |E|=10000", "|V|=6000 |E|=25000" })
	public String graphSize;
	private int n, m;

	private List<Pair<Graph, EdgeWeightFunc.Int>> graphs;

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0xe75b8a2fb16463ecL);
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			graphs.add(Pair.of(g, w));
		}
	}

	private void benchMST(Supplier<? extends MST> builder, Blackhole blackhole) {
		for (Pair<Graph, EdgeWeightFunc.Int> gw : graphs) {
			Graph g = gw.first();
			EdgeWeightFunc.Int w = gw.second();
			MST algo = builder.get();
			IntCollection mst = algo.computeMinimumSpanningTree(g, w);
			blackhole.consume(mst);
		}
	}

	@Benchmark
	public void benchMSTBoruvka(Blackhole blackhole) {
		benchMST(MSTBoruvka::new, blackhole);
	}

	@Benchmark
	public void benchMSTFredmanTarjan(Blackhole blackhole) {
		benchMST(MSTFredmanTarjan::new, blackhole);
	}

	@Benchmark
	public void benchMSTKruskal(Blackhole blackhole) {
		benchMST(MSTKruskal::new, blackhole);
	}

	@Benchmark
	public void benchMSTPrim(Blackhole blackhole) {
		benchMST(MSTPrim::new, blackhole);
	}

	@Benchmark
	public void benchMSTYao(Blackhole blackhole) {
		benchMST(MSTYao::new, blackhole);
	}

	@Benchmark
	public void benchMSTKargerKleinTarjan(Blackhole blackhole) {
		benchMST(MSTKargerKleinTarjan::new, blackhole);
	}

}
