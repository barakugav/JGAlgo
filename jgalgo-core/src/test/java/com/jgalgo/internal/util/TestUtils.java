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

package com.jgalgo.internal.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import it.unimi.dsi.fastutil.ints.IntArrays;

public class TestUtils {

	public static class PhasedTester {

		public static class Phase {

			int repeat;
			int[] args;

			Phase() {}

			public Phase repeat(int r) {
				if (r <= 0)
					throw new IllegalArgumentException();
				this.repeat = r;
				return this;
			}

			public Phase withArgs(int... args) {
				if (args.length == 0)
					throw new IllegalArgumentException();
				this.args = args;
				return this;
			}
		}

		private final List<Phase> phases = new ArrayList<>();

		public Phase addPhase() {
			Phase phase = new Phase();
			phases.add(phase);
			return phase;
		}

		public void run(RunnableTestWith1Args test) {
			for (Phase phase : phases) {
				assertArgsNum(phase, 1);
				for (int repeat = phase.repeat; repeat > 0; repeat--)
					test.run(phase.args[0]);
			}
		}

		public void run(RunnableTestWith2Args test) {
			for (Phase phase : phases) {
				assertArgsNum(phase, 2);
				for (int repeat = phase.repeat; repeat > 0; repeat--)
					test.run(phase.args[0], phase.args[1]);
			}
		}

		public void run(RunnableTestWith3Args test) {
			for (Phase phase : phases) {
				assertArgsNum(phase, 3);
				for (int repeat = phase.repeat; repeat > 0; repeat--)
					test.run(phase.args[0], phase.args[1], phase.args[2]);
			}
		}

		private static void assertArgsNum(Phase phase, int runnableArgs) {
			if (phase.args.length != runnableArgs)
				throw new IllegalArgumentException(
						"Phase had " + phase.args.length + " arguments, but runnable accept " + runnableArgs);
		}

		@FunctionalInterface
		public static interface RunnableTestWith1Args {
			public void run(int arg1);
		}

		@FunctionalInterface
		public static interface RunnableTestWith2Args {
			public void run(int arg1, int arg2);
		}

		@FunctionalInterface
		public static interface RunnableTestWith3Args {
			public void run(int arg1, int arg2, int arg3);
		}

	}

	public static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	public static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	public static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i = 0; i < a.length; i++)
			a[i] = nextInt(rand, from, to);
		return a;
	}

	public static int[] randPermutation(int n, long seed) {
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

	public static double nextDouble(Random rand, double from, double to) {
		return from + (to - from) * rand.nextDouble();
	}

	public static int nextInt(Random rand, int from, int to) {
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
