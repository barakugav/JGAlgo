package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.Collection;
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

import com.jgalgo.LCADynamic;
import com.jgalgo.LCAGabow2017;
import com.jgalgo.LCAGabowSimple;
import com.jgalgo.test.LCADynamicTestUtils;
import com.jgalgo.test.LCADynamicTestUtils.Op;
import com.jgalgo.test.LCADynamicTestUtils.OpAddLeaf;
import com.jgalgo.test.LCADynamicTestUtils.OpInitTree;
import com.jgalgo.test.LCADynamicTestUtils.OpLCAQuery;
import com.jgalgo.test.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class LCADynamicBench {

	@Param
	public GraphSize graphSize;

	public static enum GraphSize {
		v64_e256, v512_e4096, v4096_e16384;

		final int n, m;

		GraphSize() {
			String[] strs = toString().split("_");
			assert strs.length == 2;
			this.n = Integer.parseInt(strs[0].substring(1));
			this.m = Integer.parseInt(strs[1].substring(1));
		}
	}

	private List<Collection<Op>> lcaOps;

	@Setup(Level.Iteration)
	public void setup() {
		final SeedGenerator seedGen = new SeedGenerator(0x66fed18e0b594b55L);
		final int graphsNum = 20;
		lcaOps = new ArrayList<>(graphsNum);
		for (int graphIdx = 0; graphIdx < graphsNum; graphIdx++) {
			Collection<Op> ops = LCADynamicTestUtils.generateRandOps(graphSize.n, graphSize.m, seedGen.nextSeed());
			lcaOps.add(ops);
		}
	}

	private void benchLCA(Supplier<? extends LCADynamic<Void>> builder, Blackhole blackhole) {
		for (Collection<Op> ops : lcaOps) {
			LCADynamic<Void> lca = builder.get();
			List<LCADynamic.Node<Void>> nodes = new ArrayList<>();
			for (Op op0 : ops) {
				if (op0 instanceof OpInitTree) {
					nodes.add(lca.initTree(null));

				} else if (op0 instanceof OpAddLeaf) {
					OpAddLeaf op = (OpAddLeaf) op0;
					LCADynamic.Node<Void> parent = nodes.get(op.parent);
					nodes.add(lca.addLeaf(parent, null));

				} else if (op0 instanceof OpLCAQuery) {
					OpLCAQuery op = (OpLCAQuery) op0;
					LCADynamic.Node<Void> x = nodes.get(op.x), y = nodes.get(op.y);
					LCADynamic.Node<Void> lcaRes = lca.calcLCA(x, y);
					blackhole.consume(lcaRes);

				} else {
					throw new IllegalStateException();
				}
			}
			blackhole.consume(lca);
			blackhole.consume(nodes);
		}
	}

	@Benchmark
	public void benchLCAGabow2017(Blackhole blackhole) {
		benchLCA(LCAGabow2017::new, blackhole);
	}

	@Benchmark
	public void benchLCAGabowSimple(Blackhole blackhole) {
		benchLCA(LCAGabowSimple::new, blackhole);
	}

}
