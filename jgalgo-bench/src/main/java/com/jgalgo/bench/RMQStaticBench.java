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

import com.jgalgo.RMQStatic;
import com.jgalgo.RMQStaticCartesianTrees;
import com.jgalgo.RMQStaticComparator;
import com.jgalgo.RMQStaticLookupTable;
import com.jgalgo.RMQStaticPlusMinusOne;
import com.jgalgo.RMQStaticPowerOf2Table;
import com.jgalgo.bench.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;

public class RMQStaticBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class PreProcess {

		@Param({ "N=128", "N=2500", "N=15000" })
		public String args;
		public int n;

		private List<Pair<Integer, RMQStaticComparator>> arrays;
		private final int arrsNum = 31;
		private final AtomicInteger arrIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("N"));

			final SeedGenerator seedGen = new SeedGenerator(0xea7471a0349fe14eL);
			arrays = new ArrayList<>();
			for (int aIdx = 0; aIdx < arrsNum; aIdx++) {
				int[] arr = TestUtils.randArray(n, seedGen.nextSeed());
				arrays.add(Pair.of(Integer.valueOf(n), RMQStaticComparator.ofIntArray(arr)));
			}
		}

		private void benchPreProcess(Supplier<? extends RMQStatic> builder, Blackhole blackhole) {
			Pair<Integer, RMQStaticComparator> arr = arrays.get(arrIdx.getAndUpdate(i -> (i + 1) % arrsNum));
			RMQStatic rmq = builder.get();
			rmq.preProcessSequence(arr.second(), arr.first().intValue());
			blackhole.consume(rmq);
		}

		@Benchmark
		public void LookupTable(Blackhole blackhole) {
			benchPreProcess(RMQStaticLookupTable::new, blackhole);
		}

		@Benchmark
		public void PowerOf2Table(Blackhole blackhole) {
			benchPreProcess(RMQStaticPowerOf2Table::new, blackhole);
		}

		@Benchmark
		public void CartesianTrees(Blackhole blackhole) {
			benchPreProcess(RMQStaticCartesianTrees::new, blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
	@Fork(value = 1, warmups = 0)
	@State(Scope.Benchmark)
	public static class PreProcessPlusMinusOne {

		@Param({ "N=128", "N=2500", "N=15000" })
		public String args;
		public int n;

		private List<Pair<Integer, RMQStaticComparator>> arrays;
		private final int arrsNum = 31;
		private final AtomicInteger arrIdx = new AtomicInteger();

		@Setup(Level.Trial)
		public void setup() {
			Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
			n = Integer.parseInt(argsMap.get("N"));

			final Random rand = new Random(0x9fc881bb3f61bc29L);
			arrays = new ArrayList<>();
			for (int aIdx = 0; aIdx < arrsNum; aIdx++) {
				int[] arr = new int[n];
				for (int i = 1; i < n; i++)
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				arrays.add(Pair.of(Integer.valueOf(n), RMQStaticComparator.ofIntArray(arr)));
			}
		}

		@Benchmark
		public void benchPreProcess(Blackhole blackhole) {
			Pair<Integer, RMQStaticComparator> arr = arrays.get(arrIdx.getAndUpdate(i -> (i + 1) % arrsNum));
			RMQStatic rmq = new RMQStaticPlusMinusOne();
			rmq.preProcessSequence(arr.second(), arr.first().intValue());
			blackhole.consume(rmq);
		}

	}

	public static class Query {

		int[] randArray(int size, long seed) {
			return TestUtils.randArray(size, seed);
		}

		Pair<RMQStatic.DataStructure, int[]> createArray(Supplier<? extends RMQStatic> builder, int n) {
			final SeedGenerator seedGen = new SeedGenerator(0x5b3fba9dd26f2769L);

			int[] arr = randArray(n, seedGen.nextSeed());

			RMQStatic rmq = builder.get();
			RMQStatic.DataStructure rmqDS = rmq.preProcessSequence(RMQStaticComparator.ofIntArray(arr), n);

			int queriesNum = n;
			int[] queries = TestUtils.randArray(queriesNum * 2, 0, n, seedGen.nextSeed());
			for (int q = 0; q < queriesNum; q++) {
				int i = queries[q * 2];
				int j = queries[q * 2 + 1];
				if (j < i) {
					int temp = i;
					i = j;
					j = temp;
				}
				queries[q * 2] = i;
				queries[q * 2 + 1] = j;
			}

			return Pair.of(rmqDS, queries);
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class LookupTable extends Query {

			@Param({ "N=128", "N=2500" })
			public String args;
			public int n;

			private RMQStatic.DataStructure rmq;
			private int[] queries;
			private int queryIdx;
			private int queryI, queryJ;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				n = Integer.parseInt(argsMap.get("N"));

				Pair<RMQStatic.DataStructure, int[]> p = createArray(RMQStaticLookupTable::new, n);
				rmq = p.first();
				queries = p.second();
				queryIdx = 0;
			}

			@Setup(Level.Invocation)
			public void setupQueryIndices() {
				queryI = queries[queryIdx * 2 + 0];
				queryJ = queries[queryIdx * 2 + 1];
				queryIdx = (queryIdx + 1) % (queries.length / 2);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				int res = rmq.findMinimumInRange(queryI, queryJ);
				blackhole.consume(res);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class PowerOf2Table extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;
			public int n;

			private RMQStatic.DataStructure rmq;
			private int[] queries;
			private int queryIdx;
			private int queryI, queryJ;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				n = Integer.parseInt(argsMap.get("N"));

				Pair<RMQStatic.DataStructure, int[]> p = createArray(RMQStaticPowerOf2Table::new, n);
				rmq = p.first();
				queries = p.second();
				queryIdx = 0;
			}

			@Setup(Level.Invocation)
			public void setupQueryIndices() {
				queryI = queries[queryIdx * 2 + 0];
				queryJ = queries[queryIdx * 2 + 1];
				queryIdx = (queryIdx + 1) % (queries.length / 2);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				int res = rmq.findMinimumInRange(queryI, queryJ);
				blackhole.consume(res);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class PlusMinusOne extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;
			public int n;

			private RMQStatic.DataStructure rmq;
			private int[] queries;
			private int queryIdx;
			private int queryI, queryJ;

			@Override
			int[] randArray(int size, long seed) {
				final Random rand = new Random(seed);
				int[] arr = new int[size];
				for (int i = 1; i < n; i++)
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				return arr;
			}

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				n = Integer.parseInt(argsMap.get("N"));

				Pair<RMQStatic.DataStructure, int[]> p = createArray(RMQStaticPlusMinusOne::new, n);
				rmq = p.first();
				queries = p.second();
				queryIdx = 0;
			}

			@Setup(Level.Invocation)
			public void setupQueryIndices() {
				queryI = queries[queryIdx * 2 + 0];
				queryJ = queries[queryIdx * 2 + 1];
				queryIdx = (queryIdx + 1) % (queries.length / 2);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				int res = rmq.findMinimumInRange(queryI, queryJ);
				blackhole.consume(res);
			}
		}

		@BenchmarkMode(Mode.AverageTime)
		@OutputTimeUnit(TimeUnit.NANOSECONDS)
		@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
		@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
		@Fork(value = 1, warmups = 0)
		@State(Scope.Benchmark)
		public static class CartesianTrees extends Query {

			@Param({ "N=128", "N=2500", "N=15000" })
			public String args;
			public int n;

			private RMQStatic.DataStructure rmq;
			private int[] queries;
			private int queryIdx;
			private int queryI, queryJ;

			@Setup(Level.Iteration)
			public void setupCreateArray() {
				Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
				n = Integer.parseInt(argsMap.get("N"));

				Pair<RMQStatic.DataStructure, int[]> p = createArray(RMQStaticCartesianTrees::new, n);
				rmq = p.first();
				queries = p.second();
				queryIdx = 0;
			}

			@Setup(Level.Invocation)
			public void setupQueryIndices() {
				queryI = queries[queryIdx * 2 + 0];
				queryJ = queries[queryIdx * 2 + 1];
				queryIdx = (queryIdx + 1) % (queries.length / 2);
			}

			@Benchmark
			public void benchQuery(Blackhole blackhole) {
				int res = rmq.findMinimumInRange(queryI, queryJ);
				blackhole.consume(res);
			}
		}

	}

}
