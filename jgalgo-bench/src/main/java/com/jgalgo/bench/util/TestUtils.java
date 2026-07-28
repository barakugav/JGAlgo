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

package com.jgalgo.bench.util;

import static com.jgalgo.internal.util.Range.range;
import java.util.Iterator;
import java.util.Random;
import it.unimi.dsi.fastutil.ints.IntArrays;

public class TestUtils {

	public static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	public static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i : range(a.length))
			a[i] = nextInt(rand, from, to);
		return a;
	}

	static int[] randPermutation(int n, long seed) {
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
