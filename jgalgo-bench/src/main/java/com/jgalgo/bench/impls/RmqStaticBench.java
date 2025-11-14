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

import static com.jgalgo.internal.util.Range.range;
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
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.TestUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.internal.ds.RmqStatic;
import com.jgalgo.internal.ds.RmqStaticCartesianTrees;
import com.jgalgo.internal.ds.RmqStaticComparator;
import com.jgalgo.internal.ds.RmqStaticPlusMinusOne;
import com.jgalgo.internal.ds.RmqStaticPowerOf2Table;
import com.jgalgo.internal.ds.RmqStaticSimpleLookupTable;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RmqStaticBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class PreProcess {

		@Param({ "N=128", "N=2500", "N=15000" })
		public String args;
		public int n;

		private List<Pair<Integer, RmqStaticComparator>> arrays;
		private final int arrsNum = 31;
		private final AtomicInteger arrIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("N"));

			final SeedGenerator seedGen = new SeedGenerator(0xea7471a0349fe14eL);
			arrays = new ObjectArrayList<>();
			for (int aIdx = 0; aIdx < arrsNum; aIdx++) {
				int[] arr = TestUtils.randArray(n, seedGen.nextSeed());
				arrays.add(Pair.of(Integer.valueOf(n), RmqStaticComparator.ofIntArray(arr)));
			}
		}

		private void benchPreProcess(RmqStatic rmq, Blackhole blackhole) {
			Pair<Integer, RmqStaticComparator> arr = arrays.get(arrIdx.getAndUpdate(i -> (i + 1) % arrsNum));
			rmq.preProcessSequence(arr.second(), arr.first().intValue());
			blackhole.consume(rmq);
		}

		@Benchmark
		public void LookupTable(Blackhole blackhole) {
			benchPreProcess(new RmqStaticSimpleLookupTable(), blackhole);
		}

		@Benchmark
		public void PowerOf2Table(Blackhole blackhole) {
			benchPreProcess(new RmqStaticPowerOf2Table(), blackhole);
		}

		@Benchmark
		public void CartesianTrees(Blackhole blackhole) {
			benchPreProcess(new RmqStaticCartesianTrees(), blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class PreProcessPlusMinusOne {

		@Param({ "N=128", "N=2500", "N=15000" })
		public String args;
		public int n;

		private List<Pair<Integer, RmqStaticComparator>> arrays;
		private final int arrsNum = 31;
		private final AtomicInteger arrIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("N"));

			final Random rand = new Random(0x9fc881bb3f61bc29L);
			arrays = new ObjectArrayList<>();
			for (int aIdx = 0; aIdx < arrsNum; aIdx++) {
				int[] arr = new int[n];
				for (int i : range(1, n))
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				arrays.add(Pair.of(Integer.valueOf(n), RmqStaticComparator.ofIntArray(arr)));
			}
		}

		@Benchmark
		public void benchPreProcess(Blackhole blackhole) {
			Pair<Integer, RmqStaticComparator> arr = arrays.get(arrIdx.getAndUpdate(i -> (i + 1) % arrsNum));
			RmqStatic rmq = new RmqStaticPlusMinusOne();
			rmq.preProcessSequence(arr.second(), arr.first().intValue());
			blackhole.consume(rmq);
		}

	}

	public static class Query {

		int n;
		RmqStatic.DataStructure rmq;
		private long[] queriesAll;
		private int queryIdx;
		long[] queries = new long[OperationsPerInvocation];

		private static final int OperationsPerInvocation = 427;

		int[] randArray(int size, long seed) {
			return TestUtils.randArray(size, seed);
		}

		Pair<RmqStatic.DataStructure, long[]> createArray(RmqStatic rmq, int n) {
			final SeedGenerator seedGen = new SeedGenerator(0x5b3fba9dd26f2769L);

			int[] arr = randArray(n, seedGen.nextSeed());

			RmqStatic.DataStructure rmqDS = rmq.preProcessSequence(RmqStaticComparator.ofIntArray(arr), n);

			int queriesNum = n * 53;
			int[] queries = TestUtils.randArray(queriesNum * 2, 0, n, seedGen.nextSeed());
			for (int q = 0; q < queriesNum; q++) {
				int i = queries[q * 2 + 0];
				int j = queries[q * 2 + 1];
				if (j < i) {
					int temp = i;
					i = j;
					j = temp;
				}
				queries[q * 2 + 0] = i;
				queries[q * 2 + 1] = j;
			}
			long[] queries0 = new long[queriesNum];
			for (int q = 0; q < queriesNum; q++)
				queries0[q] = IntPair.of(queries[q * 2 + 0], queries[q * 2 + 1]);
			return Pair.of(rmqDS, queries0);
		}

		private void setupCreateArray(String args, RmqStatic rmq) {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("N"));

			Pair<RmqStatic.DataStructure, long[]> p = createArray(rmq, n);
			this.rmq = p.first();
			queriesAll = p.second();
			queryIdx = 0;
		}

		private void setupCreateQueries() {
			for (int qIdx : range(OperationsPerInvocation)) {
				queries[qIdx] = queriesAll[queryIdx];
				if (++queryIdx == queriesAll.length)
					queryIdx = 0;
			}
		}

		private void benchQuery(Blackhole blackhole) {
			for (long q : queries) {
				int queryI = IntPair.first(q);
				int queryJ = IntPair.second(q);
				int res = rmq.findMinimumInRange(queryI, queryJ);
				blackhole.consume(res);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LookupTable extends Query {

			@Param({ "N=128", "N=2500" })
			public String args;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				super.setupCreateArray(args, new RmqStaticSimpleLookupTable());
			}

			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@OperationsPerInvocation(Query.OperationsPerInvocation)
			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				super.benchQuery(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class PowerOf2Table extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				super.setupCreateArray(args, new RmqStaticPowerOf2Table());
			}

			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@OperationsPerInvocation(Query.OperationsPerInvocation)
			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				super.benchQuery(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class PlusMinusOne extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				super.setupCreateArray(args, new RmqStaticPlusMinusOne());
			}

			@Override
			int[] randArray(int size, long seed) {
				final Random rand = new Random(seed);
				int[] arr = new int[size];
				for (int i : range(1, n))
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				return arr;
			}

			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@OperationsPerInvocation(Query.OperationsPerInvocation)
			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				super.benchQuery(blackhole);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Measurement(iterations = 3, time = 300, timeUnit = TimeUnit.MILLISECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class CartesianTrees extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				super.setupCreateArray(args, new RmqStaticCartesianTrees());
			}

			@Setup(Level.Invocation)
			public void setupCreateQueries() {
				super.setupCreateQueries();
			}

			@OperationsPerInvocation(Query.OperationsPerInvocation)
			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				super.benchQuery(blackhole);
			}
		}

	}

}
