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

import static com.jgalgo.internal.util.Range.range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

public class TestUtils {

	public static class PhasedTester {

		public static class Phase {

			int repeat = 1;
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
			runPhases(phase -> test.run(phase.args[0]), 1);
		}

		public void run(RunnableTestWith2Args test) {
			runPhases(phase -> test.run(phase.args[0], phase.args[1]), 2);
		}

		public void run(RunnableTestWith3Args test) {
			runPhases(phase -> test.run(phase.args[0], phase.args[1], phase.args[2]), 3);
		}

		private void runPhases(Consumer<Phase> test, int runnableArgs) {
			for (int pIdx : range(phases.size())) {
				Phase phase = phases.get(pIdx);
				assertArgsNum(phase, runnableArgs);
				for (int repeat : range(phase.repeat)) {
					try {
						test.accept(phase);
					} catch (Throwable ex) {
						System.err
								.println("failed at phase " + pIdx + " args=" + Arrays.toString(phase.args)
										+ " iteration " + repeat);
						throw ex;
					}
				}
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
		for (int i : range(a.length))
			a[i] = nextInt(rand, from, to);
		return a;
	}

	public static int[] randPermutation(int n, long seed) {
		int[] a = range(n).toIntArray();
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

	public static <T> T randElement(List<T> list, Random rand) {
		return list.get(rand.nextInt(list.size()));
	}

	public static int randElement(IntList list, Random rand) {
		return list.getInt(rand.nextInt(list.size()));
	}

	public static int nonExistingInt(IntCollection elements, Random rand) {
		return nonExistingInt(elements, rand, false).intValue();
	}

	public static Integer nonExistingInt(Collection<Integer> elements, Random rand) {
		return nonExistingInt(elements, rand, false);
	}

	public static int nonExistingIntNonNegative(IntCollection elements, Random rand) {
		return nonExistingInt(elements, rand, true).intValue();
	}

	public static Integer nonExistingIntNonNegative(Collection<Integer> elements, Random rand) {
		return nonExistingInt(elements, rand, true);
	}

	private static Integer nonExistingInt(Collection<Integer> elements, Random rand, boolean positive) {
		for (;;) {
			int x;
			if (positive) {
				x = rand.nextInt(1 + 2 * elements.size());
			} else {
				int r = 4 + 4 * elements.size();
				x = rand.nextInt(r) - r / 2;
			}
			Integer e = Integer.valueOf(x);
			if (!elements.contains(e))
				return e;
		}
	}

	public static Graph<Integer, Integer> maybeIndexGraph(Graph<Integer, Integer> g, Random rand) {
		return rand.nextInt(3) == 0 ? g.indexGraph() : g;
	}

	public static void assertEqualsBool(boolean expected, boolean actual) {
		Assertions.assertEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	public static void assertNotEqualsBool(boolean unexpected, boolean actual) {
		Assertions.assertNotEquals(Boolean.valueOf(unexpected), Boolean.valueOf(actual));
	}

	public static void foreachBoolConfig(RunnableWith1BoolConfig test) {
		for (boolean cfg1 : new boolean[] { false, true })
			test.run(cfg1);
	}

	public static void foreachBoolConfig(RunnableWith2BoolConfig test) {
		for (boolean cfg1 : new boolean[] { false, true })
			for (boolean cfg2 : new boolean[] { false, true })
				test.run(cfg1, cfg2);
	}

	public static void foreachBoolConfig(RunnableWith3BoolConfig test) {
		for (boolean cfg1 : new boolean[] { false, true })
			for (boolean cfg2 : new boolean[] { false, true })
				for (boolean cfg3 : new boolean[] { false, true })
					test.run(cfg1, cfg2, cfg3);
	}

	public static void foreachBoolConfig(RunnableWith4BoolConfig test) {
		for (boolean cfg1 : new boolean[] { false, true })
			for (boolean cfg2 : new boolean[] { false, true })
				for (boolean cfg3 : new boolean[] { false, true })
					for (boolean cfg4 : new boolean[] { false, true })
						test.run(cfg1, cfg2, cfg3, cfg4);
	}

	@FunctionalInterface
	public static interface RunnableWith1BoolConfig {
		public void run(boolean cfg1);
	}

	@FunctionalInterface
	public static interface RunnableWith2BoolConfig {
		public void run(boolean cfg1, boolean cfg2);
	}

	@FunctionalInterface
	public static interface RunnableWith3BoolConfig {
		public void run(boolean cfg1, boolean cfg2, boolean cfg3);
	}

	@FunctionalInterface
	public static interface RunnableWith4BoolConfig {
		public void run(boolean cfg1, boolean cfg2, boolean cfg3, boolean cfg4);
	}

}
