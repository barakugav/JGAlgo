/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.bench.impls;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.jgalgo.alg.LowestCommonAncestorDynamic;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class LCADynamicBench {

	@Param({ "|V|=64 M=256", "|V|=512 M=4096", "|V|=4096 M=16384" })
	public String args;
	private int n;

	private List<Collection<Op>> lcaOps;
	private final int graphsNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		n = Integer.parseInt(argsMap.get("|V|"));
		int m = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x66fed18e0b594b55L);
		lcaOps = new ObjectArrayList<>(graphsNum);
		for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
			Collection<Op> ops = generateRandOps(n, m, seedGen.nextSeed());
			lcaOps.add(ops);
		}
	}

	private void benchLCA(LowestCommonAncestorDynamic.Builder builder, Blackhole blackhole) {
		Collection<Op> ops = lcaOps.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		LowestCommonAncestorDynamic lca = builder.build();
		LowestCommonAncestorDynamic.Vertex[] vertices = new LowestCommonAncestorDynamic.Vertex[n];
		int verticesNum = 0;
		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				vertices[verticesNum++] = lca.initTree();

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				LowestCommonAncestorDynamic.Vertex parent = vertices[op.parent];
				vertices[verticesNum++] = lca.addLeaf(parent);

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;
				LowestCommonAncestorDynamic.Vertex x = vertices[op.x], y = vertices[op.y];
				LowestCommonAncestorDynamic.Vertex lcaRes = lca.findLowestCommonAncestor(x, y);
				blackhole.consume(lcaRes);

			} else {
				throw new IllegalStateException();
			}
		}
		blackhole.consume(lca);
		blackhole.consume(vertices);
	}

	@Benchmark
	public void GabowLinear(Blackhole blackhole) {
		benchLCA(LowestCommonAncestorDynamic.newBuilder().setOption("impl", "gabow-linear"), blackhole);
	}

	@Benchmark
	public void GabowSimple(Blackhole blackhole) {
		benchLCA(LowestCommonAncestorDynamic.newBuilder().setOption("impl", "gabow-simple"), blackhole);
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

		List<Op> ops = new ObjectArrayList<>();
		int verticesNum = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = verticesNum++;
		ops.add(new OpAddLeaf(root));
		verticesNum++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = rand.nextInt(verticesNum);
					ops.add(new OpAddLeaf(p));
					verticesNum++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(verticesNum);
					int y = rand.nextInt(verticesNum);
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
