package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
import com.jgalgo.HeapReferenceable;
import com.jgalgo.HeapFibonacci;
import com.jgalgo.HeapPairing;
import com.jgalgo.MSTPrim1957;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.ints.IntCollection;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class HeapReferenceableBench {

	@Param({ "|V|=64 |E|=256", "|V|=512 |E|=4096", "|V|=4096 |E|=16384" })
	public String graphSize;
	private int n, m;

	private List<GraphArgs> graphs;

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

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x88da246e71ef3dacL);
		Random rand = new Random(seedGen.nextSeed());
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int source = rand.nextInt(g.vertices().size());
			graphs.add(new GraphArgs(g, w, source));
		}
	}

	private void benchHeap(HeapReferenceable.Builder heapBuilder, Blackhole blackhole) {
		for (GraphArgs args : graphs) {
			/* SSSP */
			SSSP algo = new SSSPDijkstra(heapBuilder);
			SSSP.Result ssspRes = algo.computeShortestPaths(args.g, args.w, args.source);
			blackhole.consume(ssspRes);

			/* Prim MST */
			MSTPrim1957 mstAlgo = new MSTPrim1957();
			mstAlgo.setHeapBuilder(heapBuilder);
			IntCollection mst = mstAlgo.calcMST(args.g, args.w);
			blackhole.consume(mst);
		}
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
