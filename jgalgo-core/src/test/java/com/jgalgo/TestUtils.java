package com.jgalgo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrays;

@SuppressWarnings("boxing")
public class TestUtils {

	public static class Phase {
		private final int repeat;
		private final int[] args;

		private Phase(int repeat, int[] args) {
			if (repeat < 0)
				throw new IllegalArgumentException();
			this.repeat = repeat;
			this.args = args;
		}

		static Phase of(int repeat, int... args) {
			return new Phase(repeat, args);
		}
	}

	public static Phase phase(int repeat, int... args) {
		return Phase.of(repeat, args);
	}

	@FunctionalInterface
	public static interface TestRunnable {
		public void run(TestIterIdx testIter, int[] args);
	}

	public static void runTestMultiple(Collection<Phase> phases, TestRunnable test) {
		int phaseIdx = 0;
		for (Phase phase : phases) {
			for (int iter = 0; iter < phase.repeat; iter++) {
				try {
					test.run(new TestIterIdx(phaseIdx, iter), phase.args);
				} catch (Throwable e) {
					System.err.println("Failed at phase " + phaseIdx + " iter " + iter);
					throw e;
				}
			}
			phaseIdx++;
		}
	}

	public static class TestIterIdx {
		public final int phase, iter;

		private TestIterIdx(int phase, int iter) {
			this.phase = phase;
			this.iter = iter;
		}

		@Override
		public String toString() {
			return "P" + phase + " I" + iter;
		}
	}

	static boolean doubleEql(double a, double b, double precise) {
		if (a < b)
			return b - a < precise;
		if (a > b)
			return a - b < precise;
		return true;
	}

	static void printArr(int a[]) {
		printArr(a, true);
	}

	static void printArr(int a[], boolean printIndicies) {
		for (int i = 0; i < a.length; i++)
			System.out.print("" + String.format("%03d", a[i]) + ", ");
		System.out.println();
		if (printIndicies) {
			for (int i = 0; i < a.length; i++)
				System.out.print("" + String.format("%03d", i) + ", ");
			System.out.println();
		}
	}

	public static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	public static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i = 0; i < a.length; i++)
			a[i] = nextInt(rand, from, to);
		return a;
	}

	static int[] randPermutation(int n, long seed) {
		int[] a = new int[n];
		for (int i = 0; i < n; i++)
			a[i] = i;
		IntArrays.shuffle(a, new Random(seed ^ 0xb281dc30ae96a316L));
		return a;
	}

	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	static double nextDouble(Random rand, double from, double to) {
		return from + (to - from) * rand.nextDouble();
	}

	static int nextInt(Random rand, int from, int to) {
		return from + rand.nextInt(to - from);
	}

	public static class SeedGenerator {
		private final Random rand;

		public SeedGenerator(long seed) {
			rand = new Random(seed ^ 0x9db7d6d04ce666aeL);
		}

		public long nextSeed() {
			return rand.nextLong() ^ 0x1df73569991aee99L;
		}
	}

}
