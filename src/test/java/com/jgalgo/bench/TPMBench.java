package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
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
import com.jgalgo.TPM;
import com.jgalgo.TPMHagerup;
import com.jgalgo.test.GraphsTestUtils;
import com.jgalgo.test.TPMTestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TPMBench {

	@Param({ "64", "200", "5000" })
	public int n;

	private List<TPMArgs> graphs;

	private static class TPMArgs {
		final Graph tree;
		final EdgeWeightFunc w;
		final TPM.Queries queries;

		TPMArgs(Graph tree, EdgeWeightFunc w, TPM.Queries queries) {
			this.tree = tree;
			this.w = w;
			this.queries = queries;
		}
	}

	@Setup(Level.Iteration)
	public void setup() {
		final SeedGenerator seedGen = new SeedGenerator(0x28ddf3f2d9c5c873L);
		final int graphsNum = 20;
		graphs = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Graph t = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(t, seedGen.nextSeed());
			TPM.Queries queries = TPMTestUtils.generateRandQueries(n, n, seedGen.nextSeed());
			graphs.add(new TPMArgs(t, w, queries));

		}
	}

	private void benchTPM(Supplier<? extends TPM> builder, Blackhole blackhole) {
		TPM algo = builder.get();
		for (TPMArgs g : graphs) {
			int[] actual = algo.computeHeaviestEdgeInTreePaths(g.tree, g.w, g.queries);
			blackhole.consume(actual);
		}
	}

	@Benchmark
	public void benchTPMHagerup(Blackhole blackhole) {
		benchTPM(() -> {
			TPMHagerup algo = new TPMHagerup();
			algo.setBitsLookupTablesEnable(false);
			return algo;
		}, blackhole);
	}

	@Benchmark
	public void benchTPMHagerupWithBitsLookupTable(Blackhole blackhole) {
		benchTPM(() -> {
			TPMHagerup algo = new TPMHagerup();
			algo.setBitsLookupTablesEnable(true);
			return algo;
		}, blackhole);
	}

}
