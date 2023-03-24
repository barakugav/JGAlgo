package com.ugav.jgalgo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

@SuppressWarnings("boxing")
class TestUtils {

	TestUtils() {
	}

	static class Phase {
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

	static Phase phase(int repeat, int... args) {
		return Phase.of(repeat, args);
	}

	@FunctionalInterface
	static interface TestRunnable {
		public void run(TestIterIdx testIter, int[] args);
	}

	static void runTestMultiple(Collection<Phase> phases, TestRunnable test) {
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

	static class TestIterIdx {
		final int phase, iter;

		private TestIterIdx(int phase, int iter) {
			this.phase = phase;
			this.iter = iter;
		}

		@Override
		public String toString() {
			return "P" + phase + " I" + iter;
		}
	}

	static long nextRandSeed() {
		return new Random().nextLong();// TODO
//		Pair<Long, Random> generator = seedGenerators.get(getTestFullname());
//		if (generator.e2 == null)
//			generator.e2 = new Random(generator.e1.longValue() ^ 0x555bfc5796f83a2dL);
//		return generator.e2.nextLong() ^ 0x3d61be24f3910c88L;
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

	static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i = 0; i < a.length; i++)
			a[i] = nextInt(rand, from, to);
		return a;
	}

	static int[] randPermutation(int n, long seed) {
		Random rand = new Random(seed ^ 0xb281dc30ae96a316L);

		boolean[] possibleValuesBitmap = new boolean[n];
		java.util.Arrays.fill(possibleValuesBitmap, true);

		int[] possibleValues = new int[n];
		for (int i = 0; i < n; i++)
			possibleValues[i] = i;
		int possibleValuesArrLen = n;
		int possibleValuesSize = n;
		int nextShrink = possibleValuesSize / 2;
		int[] possibleValuesNext = new int[nextShrink];

		int[] a = new int[n];
		for (int i = 0; i < n; i++) {
			if (possibleValuesSize == nextShrink && nextShrink > 4) {
				for (int j = 0, k = 0; k < nextShrink; j++) {
					if (possibleValuesBitmap[j])
						possibleValuesNext[k++] = possibleValues[j];
				}
				int[] temp = possibleValues;
				possibleValues = possibleValuesNext;
				possibleValuesNext = temp;
				possibleValuesArrLen = possibleValuesSize;

				java.util.Arrays.fill(possibleValuesBitmap, true);
				nextShrink = possibleValuesSize / 2;
			}

			int idx;
			do {
				idx = rand.nextInt(possibleValuesArrLen);
			} while (!possibleValuesBitmap[idx]);

			a[i] = possibleValues[idx];
			possibleValuesBitmap[idx] = false;
			possibleValuesSize--;
		}

		return a;
	}

	static int[] suffle(int[] a, long seed) {
		int[] p = randPermutation(a.length, seed);
		int[] r = new int[a.length];
		for (int i = 0; i < a.length; i++)
			r[i] = a[p[i]];
		return r;
	}

	static <T> T[] suffle(T[] a, long seed) {
		int[] p = randPermutation(a.length, seed);
		T[] r = java.util.Arrays.copyOf(a, a.length);
		for (int i = 0; i < a.length; i++)
			r[i] = a[p[i]];
		return r;
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

}
