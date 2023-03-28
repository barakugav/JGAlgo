package com.ugav.jgalgo.bench;

import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.MST;
import com.ugav.jgalgo.MSTBoruvka1926;
import com.ugav.jgalgo.MSTFredmanTarjan1987;
import com.ugav.jgalgo.MSTKargerKleinTarjan1995;
import com.ugav.jgalgo.MSTKruskal1956;
import com.ugav.jgalgo.MSTPrim1957;
import com.ugav.jgalgo.MSTYao1976;
import com.ugav.jgalgo.test.GraphsTestUtils;
import com.ugav.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.ints.IntCollection;

public class MSTBench {

	private static void benchMST(Supplier<? extends MST> builder, Blackhole blackhole) {
		final long seed = 0xe75b8a2fb16463ecL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		int n = 128, m = 256;
		Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
		GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
		EdgeWeightFunc.Int w = g.edgesWeight("weight");

		MST algo = builder.get();
		IntCollection mst = algo.calcMST(g, w);
		blackhole.consume(mst);
	}

	@Benchmark
	public void benchMSTBoruvka1926(Blackhole blackhole) {
		benchMST(MSTBoruvka1926::new, blackhole);
	}

	@Benchmark
	public void benchMSTFredmanTarjan1987(Blackhole blackhole) {
		benchMST(MSTFredmanTarjan1987::new, blackhole);
	}

	@Benchmark
	public void benchMSTKruskal1956(Blackhole blackhole) {
		benchMST(MSTKruskal1956::new, blackhole);
	}

	@Benchmark
	public void benchMSTPrim1957(Blackhole blackhole) {
		benchMST(MSTPrim1957::new, blackhole);
	}

	@Benchmark
	public void benchMSTYao1976(Blackhole blackhole) {
		benchMST(MSTYao1976::new, blackhole);
	}

	@Benchmark
	public void benchMSTKargerKleinTarjan1995(Blackhole blackhole) {
		benchMST(MSTKargerKleinTarjan1995::new, blackhole);
	}

}
