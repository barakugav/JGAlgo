package com.jgalgo.bench;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.HeapDirectAccessed;
import com.jgalgo.HeapFibonacci;
import com.jgalgo.HeapPairing;
import com.jgalgo.MST;
import com.jgalgo.MSTPrim1957;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.TestUtils;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntCollection;

public class HeapDirectAccessedBench extends TestUtils {

	private static void benchHeap(HeapDirectAccessed.Builder heapBuilder, Blackhole blackhole) {
		final long seed = 0x88da246e71ef3dacL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		/* SSSP */
		Supplier<? extends SSSP> ssspBuilder = () -> new SSSPDijkstra(heapBuilder);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			runSSSP(ssspBuilder, n, m, true, seedGen.nextSeed(), blackhole);
			runSSSP(ssspBuilder, n, m, false, seedGen.nextSeed(), blackhole);
		});

		/* Prim MST */
		Supplier<? extends MST> mstBuilder = () -> {
			MSTPrim1957 mst = new MSTPrim1957();
			mst.setHeapBuilder(heapBuilder);
			return mst;
		};
		phases = List.of(phase(1, 0, 0), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 1024, 4096), phase(2, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			MST algo = mstBuilder.get();

			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			EdgeWeightFunc.Int w = g.edgesWeight("weight");

			IntCollection mst = algo.calcMST(g, w);
			blackhole.consume(mst);
		});
	}

	static void runSSSP(Supplier<? extends SSSP> builder, int n, int m, boolean directed, long seed,
			Blackhole blackhole) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).doubleEdges(true)
				.selfEdges(true).cycles(true).connected(false).build();
		GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
		EdgeWeightFunc.Int w = g.edgesWeight("weight");
		int source = rand.nextInt(g.vertices().size());

		SSSP algo = builder.get();
		SSSP.Result ssspRes = algo.calcDistances(g, w, source);
		blackhole.consume(ssspRes);
	}

	@Benchmark
	public void benchHeapPairing(Blackhole blackhole) {
		benchHeap(HeapPairing::new, blackhole);
	}

	@Benchmark
	public void benchHeapFibonacci(Blackhole blackhole) {
		benchHeap(HeapFibonacci::new, blackhole);
	}

}
