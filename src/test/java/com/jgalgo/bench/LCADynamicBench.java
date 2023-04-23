package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.Collection;
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

import com.jgalgo.LCADynamic;
import com.jgalgo.LCADynamicGabowLinear;
import com.jgalgo.LCADynamicGabowSimple;
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

	@Param({ "|V|=64 M=256", "|V|=512 M=4096", "|V|=4096 M=16384" })
	public String graphSize;
	private int n, m;

	private List<Collection<Op>> lcaOps;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> graphSizeValues = BenchUtils.parseArgsStr(graphSize);
		n = Integer.parseInt(graphSizeValues.get("|V|"));
		m = Integer.parseInt(graphSizeValues.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x66fed18e0b594b55L);
		lcaOps = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Collection<Op> ops = LCADynamicTestUtils.generateRandOps(n, m, seedGen.nextSeed());
			lcaOps.add(ops);
		}
	}

	private void benchLCA(Supplier<? extends LCADynamic> builder, Blackhole blackhole) {
		Collection<Op> ops = lcaOps.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		LCADynamic lca = builder.get();
		LCADynamic.Node[] nodes = new LCADynamic.Node[n];
		int nodesNum = 0;
		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				nodes[nodesNum++] = lca.initTree();

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				LCADynamic.Node parent = nodes[op.parent];
				nodes[nodesNum++] = lca.addLeaf(parent);

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;
				LCADynamic.Node x = nodes[op.x], y = nodes[op.y];
				LCADynamic.Node lcaRes = lca.findLowestCommonAncestor(x, y);
				blackhole.consume(lcaRes);

			} else {
				throw new IllegalStateException();
			}
		}
		blackhole.consume(lca);
		blackhole.consume(nodes);
	}

	@Benchmark
	public void benchLCAGabow2017(Blackhole blackhole) {
		benchLCA(LCADynamicGabowLinear::new, blackhole);
	}

	@Benchmark
	public void benchLCAGabowSimple(Blackhole blackhole) {
		benchLCA(LCADynamicGabowSimple::new, blackhole);
	}

}
