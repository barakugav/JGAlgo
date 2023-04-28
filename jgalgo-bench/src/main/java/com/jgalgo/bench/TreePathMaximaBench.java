package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
import com.jgalgo.TreePathMaxima;
import com.jgalgo.TreePathMaximaHagerup;
import com.jgalgo.bench.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class TreePathMaximaBench {

	@Param({ "N=128 M=128", "N=2500 M=2500", "N=15000 M=15000" })
	public String args;
	public int n, m;

	private List<TPMArgs> graphs;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("N"));
		m = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x28ddf3f2d9c5c873L);
		graphs = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Graph t = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(t, seedGen.nextSeed());
			TreePathMaxima.Queries queries = generateRandQueries(n, m, seedGen.nextSeed());
			graphs.add(new TPMArgs(t, w, queries));
		}
	}

	private void benchTPM(Supplier<? extends TreePathMaxima> builder, Blackhole blackhole) {
		TPMArgs g = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		TreePathMaxima algo = builder.get();
		int[] result = algo.computeHeaviestEdgeInTreePaths(g.tree, g.w, g.queries);
		blackhole.consume(result);
	}

	@Benchmark
	public void TPMHagerup(Blackhole blackhole) {
		benchTPM(() -> {
			TreePathMaximaHagerup algo = new TreePathMaximaHagerup();
			algo.setBitsLookupTablesEnable(false);
			return algo;
		}, blackhole);
	}

	@Benchmark
	public void TPMHagerupWithBitsLookupTable(Blackhole blackhole) {
		benchTPM(() -> {
			TreePathMaximaHagerup algo = new TreePathMaximaHagerup();
			algo.setBitsLookupTablesEnable(true);
			return algo;
		}, blackhole);
	}

	private static class TPMArgs {
		final Graph tree;
		final EdgeWeightFunc w;
		final TreePathMaxima.Queries queries;

		TPMArgs(Graph tree, EdgeWeightFunc w, TreePathMaxima.Queries queries) {
			this.tree = tree;
			this.w = w;
			this.queries = queries;
		}
	}

	private static TreePathMaxima.Queries generateRandQueries(int n, int m, long seed) {
		Random rand = new Random(seed);
		TreePathMaxima.Queries queries = new TreePathMaxima.Queries();
		for (int q = 0; q < m; q++)
			queries.addQuery(rand.nextInt(n), rand.nextInt(n));
		return queries;
	}

}
