package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.jgalgo.bench.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.ints.IntArrays;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class LCADynamicBench {

	@Param({ "|V|=64 M=256", "|V|=512 M=4096", "|V|=4096 M=16384" })
	public String args;
	private int n, m;

	private List<Collection<Op>> lcaOps;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("|V|"));
		m = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x66fed18e0b594b55L);
		lcaOps = new ArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Collection<Op> ops = generateRandOps(n, m, seedGen.nextSeed());
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
	public void GabowLinear(Blackhole blackhole) {
		benchLCA(LCADynamicGabowLinear::new, blackhole);
	}

	@Benchmark
	public void GabowSimple(Blackhole blackhole) {
		benchLCA(LCADynamicGabowSimple::new, blackhole);
	}

	private static Collection<Op> generateRandOps(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		IntArrays.shuffle(opsOrder, rand);

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = rand.nextInt(nodesCount);
					ops.add(new OpAddLeaf(p));
					nodesCount++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(nodesCount);
					int y = rand.nextInt(nodesCount);
					ops.add(new OpLCAQuery(x, y));
					break;
				}
				default:
					throw new IllegalStateException();
			}
		}
		return ops;
	}

	private static class Op {
	}

	private static class OpInitTree extends Op {
	}

	private static class OpAddLeaf extends Op {
		final int parent;

		OpAddLeaf(int parent) {
			this.parent = parent;
		}
	}

	private static class OpLCAQuery extends Op {
		final int x, y;

		OpLCAQuery(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}