package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

import com.jgalgo.RMQ;
import com.jgalgo.RMQComparator;
import com.jgalgo.RMQGabowBentleyTarjan1984;
import com.jgalgo.RMQLookupTable;
import com.jgalgo.RMQPlusMinusOneBenderFarachColton2000;
import com.jgalgo.RMQPowerOf2Table;
import com.jgalgo.test.TestUtils;
import com.jgalgo.test.TestUtils.SeedGenerator;

import it.unimi.dsi.fastutil.Pair;

public class RMQBench {

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class PreProcess {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<Integer, RMQComparator>> arrays;

		@Setup(Level.Trial)
		public void setup() {
			final SeedGenerator seedGen = new SeedGenerator(0xea7471a0349fe14eL);
			arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = TestUtils.randArray(arrSize, seedGen.nextSeed());
				arrays.add(Pair.of(Integer.valueOf(arrSize), RMQComparator.ofIntArray(arr)));
			}
		}

		private void benchPreProcess(Supplier<? extends RMQ> builder, Blackhole blackhole) {
			for (Pair<Integer, RMQComparator> arr : arrays) {
				RMQ rmq = builder.get();
				rmq.preProcessRMQ(arr.second(), arr.first().intValue());
				blackhole.consume(rmq);
			}
		}

		@Benchmark
		public void RMQLookupTable(Blackhole blackhole) {
			benchPreProcess(RMQLookupTable::new, blackhole);
		}

		@Benchmark
		public void RMQPowerOf2Table(Blackhole blackhole) {
			benchPreProcess(RMQPowerOf2Table::new, blackhole);
		}

		@Benchmark
		public void RMQGabowBentleyTarjan1984(Blackhole blackhole) {
			benchPreProcess(RMQGabowBentleyTarjan1984::new, blackhole);
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class PreProcessRMQPlusMinusOneBenderFarachColton2000 {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<Integer, RMQComparator>> arrays;

		@Setup(Level.Trial)
		public void setup() {
			final Random rand = new Random(0x9fc881bb3f61bc29L);
			arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = new int[arrSize];
				for (int i = 1; i < arrSize; i++)
					arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
				arrays.add(Pair.of(Integer.valueOf(arrSize), RMQComparator.ofIntArray(arr)));
			}
		}

		@Benchmark
		public void benchPreProcess(Blackhole blackhole) {
			for (Pair<Integer, RMQComparator> arr : arrays) {
				RMQ rmq = new RMQPlusMinusOneBenderFarachColton2000();
				rmq.preProcessRMQ(arr.second(), arr.first().intValue());
				blackhole.consume(rmq);
			}
		}

	}

	public static class QueryAbstract {

		int[] randArray(int size, long seed) {
			return TestUtils.randArray(size, seed);
		}

		List<Pair<RMQ, int[]>> createArrays(Supplier<? extends RMQ> builder, int arrSize) {
			final SeedGenerator seedGen = new SeedGenerator(0x5b3fba9dd26f2769L);

			List<Pair<RMQ, int[]>> arrays = new ArrayList<>();
			for (int arrNum = 20; arrNum-- > 0;) {
				int[] arr = randArray(arrSize, seedGen.nextSeed());

				RMQ rmq = builder.get();
				rmq.preProcessRMQ(RMQComparator.ofIntArray(arr), arrSize);

				int queriesNum = arrSize;
				int[] queries = TestUtils.randArray(queriesNum * 2, 0, arrSize, seedGen.nextSeed());
				for (int q = 0; q < queriesNum; q++) {
					int i = queries[q * 2];
					int j = queries[q * 2 + 1];
					if (j < i) {
						int temp = i;
						i = j;
						j = temp;
					}
					j++;
					queries[q * 2] = i;
					queries[q * 2 + 1] = j;
				}

				arrays.add(Pair.of(rmq, queries));
			}
			return arrays;
		}

		public void benchQuery(List<Pair<RMQ, int[]>> arrays, Blackhole blackhole) {
			for (Pair<RMQ, int[]> arr : arrays) {
				RMQ rmq = arr.first();
				int[] queries = arr.second();
				int queriesNum = queries.length / 2;
				for (int q = 0; q < queriesNum; q++) {
					int i = queries[q * 2];
					int j = queries[q * 2 + 1];
					int res = rmq.calcRMQ(i, j);
					blackhole.consume(res);
				}
			}
		}

	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class QueryRMQLookupTable extends QueryAbstract {

		@Param({ "128", "2500", "6000" })
		public int arrSize;
		private List<Pair<RMQ, int[]>> arrays;

		@Setup(Level.Iteration)
		public void setup() {
			arrays = createArrays(RMQLookupTable::new, arrSize);
		}

		@Benchmark
		public void benchQuery(Blackhole blackhole) {
			benchQuery(arrays, blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class QueryRMQPowerOf2Table extends QueryAbstract {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<RMQ, int[]>> arrays;

		@Setup(Level.Iteration)
		public void setup() {
			arrays = createArrays(RMQPowerOf2Table::new, arrSize);
		}

		@Benchmark
		public void benchQuery(Blackhole blackhole) {
			benchQuery(arrays, blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class QueryRMQPlusMinusOneBenderFarachColton2000 extends QueryAbstract {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<RMQ, int[]>> arrays;

		@Override
		int[] randArray(int size, long seed) {
			final Random rand = new Random(seed);
			int[] arr = new int[size];
			for (int i = 1; i < arrSize; i++)
				arr[i] = arr[i - 1] + (rand.nextBoolean() ? +1 : -1);
			return arr;
		}

		@Setup(Level.Iteration)
		public void setup() {
			arrays = createArrays(RMQPlusMinusOneBenderFarachColton2000::new, arrSize);
		}

		@Benchmark
		public void benchQuery(Blackhole blackhole) {
			benchQuery(arrays, blackhole);
		}
	}

	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
	@State(Scope.Benchmark)
	public static class QueryRMQGabowBentleyTarjan1984 extends QueryAbstract {

		@Param({ "128", "2500", "15000" })
		public int arrSize;
		private List<Pair<RMQ, int[]>> arrays;

		@Setup(Level.Iteration)
		public void setup() {
			arrays = createArrays(RMQGabowBentleyTarjan1984::new, arrSize);
		}

		@Benchmark
		public void benchQuery(Blackhole blackhole) {
			benchQuery(arrays, blackhole);
		}
	}

}
