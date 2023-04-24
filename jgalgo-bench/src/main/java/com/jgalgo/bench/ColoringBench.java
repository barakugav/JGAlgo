package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.jgalgo.bench.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ColoringBench {

	@Param({ "|V|=100 |E|=100", "|V|=200 |E|=1000", "|V|=1600 |E|=10000" })
	public String args;
	private int n, m;

	private List<UGraph> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("|V|"));
		m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x566c25f996355cb4L);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			UGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			graphs.add(g);
		}
	}

	private void benchMST(Supplier<? extends Coloring> builder, Blackhole blackhole) {
		UGraph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		Coloring algo = builder.get();
		Coloring.Result res = algo.computeColoring(g);
		blackhole.consume(res);
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
