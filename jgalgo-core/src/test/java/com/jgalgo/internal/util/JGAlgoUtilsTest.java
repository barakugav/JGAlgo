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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class JGAlgoUtilsTest extends TestBase {

	@Test
	public void lowerBound() {
		final long seed = 0x6ed849a7e9b7794cL;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i = 0; i < n; i++)
				arr[i] = rand.nextInt(n / 5);
			IntArrays.parallelQuickSort(arr);

			for (int repeat = 10; repeat-- > 0;) {
				final int x;
				if (repeat == 0) {
					x = arr[0];
				} else if (repeat == 1) {
					x = arr[arr.length - 1];
				} else if (rand.nextBoolean()) {
					x = arr[rand.nextInt(n)];
				} else {
					x = rand.nextInt(n / 2);
				}

				int lowerBound = -1;
				for (lowerBound = 0; lowerBound < n; lowerBound++)
					if (arr[lowerBound] >= x)
						break;
				int lowerBoundActual = JGAlgoUtils.lowerBound(0, n, x, i -> arr[i]);
				assertEquals(lowerBound, lowerBoundActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> JGAlgoUtils.lowerBound(1, 0, 0, i -> i));
	}

	@Test
	public void upperBound() {
		final long seed = 0x5212c8f86ed5446aL;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i = 0; i < n; i++)
				arr[i] = rand.nextInt(n / 5);
			IntArrays.parallelQuickSort(arr);

			for (int repeat = 10; repeat-- > 0;) {
				final int x;
				if (repeat == 0) {
					x = arr[0];
				} else if (repeat == 1) {
					x = arr[arr.length - 1];
				} else if (rand.nextBoolean()) {
					x = arr[rand.nextInt(n)];
				} else {
					x = rand.nextInt(n / 2);
				}

				int upperBound;
				for (upperBound = 0; upperBound < n; upperBound++)
					if (x < arr[upperBound])
						break;
				int upperBoundActual = JGAlgoUtils.upperBound(0, n, x, i -> arr[i]);
				assertEquals(upperBound, upperBoundActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> JGAlgoUtils.upperBound(1, 0, 0, i -> i));
	}

	@Test
	public void equalRange() {
		final long seed = 0xde27d22e9c129139L;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i = 0; i < n; i++)
				arr[i] = rand.nextInt(n / 5);
			IntArrays.parallelQuickSort(arr);

			for (int repeat = 10; repeat-- > 0;) {
				final int x;
				if (repeat == 0) {
					x = arr[0];
				} else if (repeat == 1) {
					x = arr[arr.length - 1];
				} else if (rand.nextBoolean()) {
					x = arr[rand.nextInt(n)];
				} else {
					x = rand.nextInt(n / 2);
				}

				int rangeBegin;
				for (rangeBegin = 0; rangeBegin < n; rangeBegin++)
					if (arr[rangeBegin] >= x)
						break;
				int rangeEnd;
				for (rangeEnd = 0; rangeEnd < n; rangeEnd++)
					if (x < arr[rangeEnd])
						break;
				IntIntPair rangeExpected = rangeBegin == n ? null : IntIntPair.of(rangeBegin, rangeEnd);
				IntIntPair rangeActual = JGAlgoUtils.equalRange(0, n, x, i -> arr[i]);
				assertEquals(rangeExpected, rangeActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> JGAlgoUtils.equalRange(1, 0, 0, i -> i));
	}

	@Test
	public void variant2OfA() {
		Variant2<String, Integer> v = Variant2.ofA("hello");
		assertTrue(v.contains(String.class));
		assertFalse(v.contains(Integer.class));
		assertNotEquals(Optional.empty(), v.getOptional(String.class));
		assertEquals(Optional.empty(), v.getOptional(Integer.class));
		assertEquals("hello", v.get(String.class));
		assertEquals("hello", v.map(s -> s, x -> String.valueOf(x)));
	}

	@Test
	public void variant2OfB() {
		Variant2<String, Integer> v = Variant2.ofB(Integer.valueOf(55));
		assertFalse(v.contains(String.class));
		assertTrue(v.contains(Integer.class));
		assertEquals(Optional.empty(), v.getOptional(String.class));
		assertNotEquals(Optional.empty(), v.getOptional(Integer.class));
		assertEquals(55, v.get(Integer.class));
		assertEquals("55", v.map(s -> s, x -> String.valueOf(x)));
	}

	@Test
	public void permutation0() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of();
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = JGAlgoUtils.permutations((List<Integer>) l);
			} else {
				permutations = JGAlgoUtils.permutations(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of();
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation1() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = JGAlgoUtils.permutations((List<Integer>) l);
			} else {
				permutations = JGAlgoUtils.permutations(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of(IntList.of(3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation2() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = JGAlgoUtils.permutations((List<Integer>) l);
			} else {
				permutations = JGAlgoUtils.permutations(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = Set.of(IntList.of(3, 7), IntList.of(7, 3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation3() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7, 12);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = JGAlgoUtils.permutations((List<Integer>) l);
			} else {
				permutations = JGAlgoUtils.permutations(new ArrayList<>(l));
			}
			Set<List<Integer>> actual = new ObjectOpenHashSet<>(permutations.iterator());
			Set<IntList> expected = new HashSet<>();
			expected.add(IntList.of(3, 7, 12));
			expected.add(IntList.of(3, 12, 7));
			expected.add(IntList.of(7, 3, 12));
			expected.add(IntList.of(7, 12, 3));
			expected.add(IntList.of(12, 3, 7));
			expected.add(IntList.of(12, 7, 3));
			assertEquals(expected, actual);
		});
	}

	@Test
	public void permutation4() {
		foreachBoolConfig(intList -> {
			IntList l = IntList.of(3, 7, 12, 8);
			Iterable<List<Integer>> permutations;
			if (intList) {
				permutations = JGAlgoUtils.permutations((List<Integer>) l);
			} else {
				permutations = JGAlgoUtils.permutations(new ArrayList<>(l));
			}
			Set<IntList> actual = new TreeSet<>();
			for (List<Integer> p : permutations) {
				boolean added = actual.add(new IntArrayList(p));
				assertTrue(added, "duplicate permutation: " + p);
			}
			Set<IntList> expected = new TreeSet<>();
			expected.add(IntList.of(3, 7, 12, 8));
			expected.add(IntList.of(3, 7, 8, 12));
			expected.add(IntList.of(3, 12, 7, 8));
			expected.add(IntList.of(3, 12, 8, 7));
			expected.add(IntList.of(3, 8, 7, 12));
			expected.add(IntList.of(3, 8, 12, 7));
			expected.add(IntList.of(7, 3, 12, 8));
			expected.add(IntList.of(7, 3, 8, 12));
			expected.add(IntList.of(7, 12, 3, 8));
			expected.add(IntList.of(7, 12, 8, 3));
			expected.add(IntList.of(7, 8, 3, 12));
			expected.add(IntList.of(7, 8, 12, 3));
			expected.add(IntList.of(12, 3, 7, 8));
			expected.add(IntList.of(12, 3, 8, 7));
			expected.add(IntList.of(12, 7, 3, 8));
			expected.add(IntList.of(12, 7, 8, 3));
			expected.add(IntList.of(12, 8, 3, 7));
			expected.add(IntList.of(12, 8, 7, 3));
			expected.add(IntList.of(8, 3, 7, 12));
			expected.add(IntList.of(8, 3, 12, 7));
			expected.add(IntList.of(8, 7, 3, 12));
			expected.add(IntList.of(8, 7, 12, 3));
			expected.add(IntList.of(8, 12, 3, 7));
			expected.add(IntList.of(8, 12, 7, 3));
			assertEquals(expected, actual);
		});
	}

}
