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

import java.util.Iterator;
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
import com.jgalgo.alg.MaximalCliques;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.GraphsTestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MaximalCliquesBench {

	List<IntGraph> graphs;
	final int graphsNum = 31;
	final AtomicInteger graphIdx = new AtomicInteger();

	void benchMaximalCliques(MaximalCliques.Builder builder, Blackhole blackhole) {
		IntGraph graph = graphs.get(graphIdx.getAndUpdate(i -> (i + 1) % graphsNum));
		MaximalCliques algo = builder.build();
		for (Iterator<IntSet> cliqueIter = algo.iterateMaximalCliques(graph); cliqueIter.hasNext();)
			blackhole.consume(cliqueIter.next());
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class Gnp extends MaximalCliquesBench {

		@Param({ "|V|=150", "|V|=500", "|V|=1000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));

			final SeedGenerator seedGen = new SeedGenerator(0x94fc6ec413f60392L);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphGnp(n, false, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		@Benchmark
		public void BronKerbosch(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch"), blackhole);
		}

		@Benchmark
		public void BronKerboschPivot(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch-pivot"), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class BarabasiAlbert extends MaximalCliquesBench {

		@Param({ "|V|=100", "|V|=400", "|V|=1000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));

			final SeedGenerator seedGen = new SeedGenerator(0xdc6c4cf7f4d3843cL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphBarabasiAlbert(n, false, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		@Benchmark
		public void BronKerbosch(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch"), blackhole);
		}

		@Benchmark
		public void BronKerboschPivot(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch-pivot"), blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class RecursiveMatrix extends MaximalCliquesBench {

		@Param({ "|V|=300 |E|=1000", "|V|=300 |E|=2500", "|V|=800 |E|=5000" })
		public String args;

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			int n = Integer.parseInt(argsMap.get("|V|"));
			int m = Integer.parseInt(argsMap.get("|E|"));

			final SeedGenerator seedGen = new SeedGenerator(0x9716aede5cfa6eabL);
			graphs = new ObjectArrayList<>(graphsNum);
			for (int gIdx = 0; gIdx < graphsNum; gIdx++) {
				IntGraph g = GraphsTestUtils.randomGraphRecursiveMatrix(n, m, false, seedGen.nextSeed());
				graphs.add(g);
			}
		}

		@Benchmark
		public void BronKerbosch(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch"), blackhole);
		}

		@Benchmark
		public void BronKerboschPivot(Blackhole blackhole) {
			benchMaximalCliques(MaximalCliques.newBuilder().setOption("impl", "bron-kerbosch-pivot"), blackhole);
		}
	}

	static Pair<IntCollection, IntCollection> chooseMultiSourceMultiSink(IntGraph g, Random rand) {
		final int n = g.vertices().size();
		final int sourcesNum;
		final int sinksNum;
		if (n < 2) {
			throw new IllegalArgumentException("too few vertices");
		} else if (n < 4) {
			sourcesNum = sinksNum = 1;
		} else if (n <= 6) {
			sourcesNum = sinksNum = 2;
		} else {
			sourcesNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
			sinksNum = Math.max(1, n / 6 + rand.nextInt(n / 6));
		}

		IntCollection sources = new IntOpenHashSet(sourcesNum);
		IntCollection sinks = new IntOpenHashSet(sinksNum);
		for (int[] vs = g.vertices().toIntArray();;) {
			if (sources.size() < sourcesNum) {
				int source = vs[rand.nextInt(vs.length)];
				if (!sinks.contains(source))
					sources.add(source);

			} else if (sinks.size() < sinksNum) {
				int sink = vs[rand.nextInt(vs.length)];
				if (!sources.contains(sink))
					sinks.add(sink);
			} else {
				break;
			}
		}
		return Pair.of(sources, sinks);
	}

}
