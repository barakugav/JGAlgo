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

import com.jgalgo.CyclesFinder;
import com.jgalgo.CyclesFinderJohnson;
import com.jgalgo.CyclesFinderTarjan;
import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.bench.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.bench.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 4, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class CyclesFinderBench {

	@Param({ "|V|=32 |E|=64", "|V|=64 |E|=140" })
	public String args;
	private int n, m;

	private List<Graph> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("|V|"));
		m = Integer.parseInt(argsMap.get("|E|"));

		final SeedGenerator seedGen = new SeedGenerator(0x29b0e6d2a833e386L);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();
			graphs.add(g);
		}
	}

	private void benchMST(Supplier<? extends CyclesFinder> builder, Blackhole blackhole) {
		Graph g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		CyclesFinder algo = builder.get();
		List<Path> cycles = algo.findAllCycles(g);
		blackhole.consume(cycles);
	}

	@Benchmark
	public void Johnson(Blackhole blackhole) {
		benchMST(CyclesFinderJohnson::new, blackhole);
	}

	@Benchmark
	public void Tarjan(Blackhole blackhole) {
		benchMST(CyclesFinderTarjan::new, blackhole);
	}

}
