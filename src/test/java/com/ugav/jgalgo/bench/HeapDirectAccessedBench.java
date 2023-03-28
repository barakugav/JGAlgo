package com.ugav.jgalgo.bench;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import com.ugav.jgalgo.EdgeWeightFunc;
import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.HeapDirectAccessed;
import com.ugav.jgalgo.HeapFibonacci;
import com.ugav.jgalgo.HeapPairing;
import com.ugav.jgalgo.MST;
import com.ugav.jgalgo.MSTPrim1957;
import com.ugav.jgalgo.SSSP;
import com.ugav.jgalgo.SSSPDijkstra;
import com.ugav.jgalgo.test.GraphsTestUtils;
import com.ugav.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;
import com.ugav.jgalgo.test.TestUtils;

import it.unimi.dsi.fastutil.ints.IntCollection;

public class HeapDirectAccessedBench extends TestUtils {

	private static void benchHeap(HeapDirectAccessed.Builder heapBuilder, Blackhole blackhole) {
		/* SSSP */
		Supplier<? extends SSSP> ssspBuilder = () -> new SSSPDijkstra(heapBuilder);
		List<Phase> phases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 4096),
				phase(1, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			runSSSP(ssspBuilder, n, m, true, blackhole);
			runSSSP(ssspBuilder, n, m, false, blackhole);
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

			Graph g = GraphsTestUtils.randGraph(n, m);
			GraphsTestUtils.assignRandWeightsIntPos(g);
			EdgeWeightFunc.Int w = g.edgesWeight("weight");

			IntCollection mst = algo.calcMST(g, w);
			blackhole.consume(mst);
		});
	}

	static void runSSSP(Supplier<? extends SSSP> builder, int n, int m, boolean directed, Blackhole blackhole) {
		Random rand = new Random(nextRandSeed());

		Graph g = new RandomGraphBuilder().n(n).m(m).directed(directed).doubleEdges(true).selfEdges(true).cycles(true)
				.connected(false).build();
		GraphsTestUtils.assignRandWeightsIntPos(g);
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
