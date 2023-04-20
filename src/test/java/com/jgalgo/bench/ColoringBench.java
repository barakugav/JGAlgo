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

import com.jgalgo.Coloring;
import com.jgalgo.ColoringDSatur;
import com.jgalgo.ColoringDSaturHeap;
import com.jgalgo.ColoringGreedy;
import com.jgalgo.ColoringGreedyRandom;
import com.jgalgo.ColoringRecursiveLargestFirst;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ColoringBench {

	@Param({ "|V|=100 |E|=100", "|V|=200 |E|=1000", "|V|=1600 |E|=10000" })
	public String graphSize;
	private int n, m;

	private List<UGraph> graphs;

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x566c25f996355cb4L);
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			UGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			graphs.add(g);
		}
	}

	private void benchMST(Supplier<? extends Coloring> builder, Blackhole blackhole) {
		for (UGraph g : graphs) {
			Coloring algo = builder.get();
			Coloring.Result res = algo.computeColoring(g);
			blackhole.consume(res);
		}
	}

	@Benchmark
	public void benchColoringGreedy(Blackhole blackhole) {
		benchMST(ColoringGreedy::new, blackhole);
	}

	@Benchmark
	public void benchColoringGreedyRandom(Blackhole blackhole) {
		final SeedGenerator seedGen = new SeedGenerator(0xefeae78aba502d4aL);
		benchMST(() -> new ColoringGreedyRandom(seedGen.nextSeed()), blackhole);
	}

	@Benchmark
	public void benchColoringDSatur(Blackhole blackhole) {
		benchMST(ColoringDSatur::new, blackhole);
	}

	@Benchmark
	public void benchColoringDSaturHeap(Blackhole blackhole) {
		benchMST(ColoringDSaturHeap::new, blackhole);
	}

	@Benchmark
	public void benchColoringRecursiveLargestFirst(Blackhole blackhole) {
		benchMST(ColoringRecursiveLargestFirst::new, blackhole);
	}

}
