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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;

public class BinarySearchTest extends TestBase {

	@Test
	public void lowerBound() {
		final long seed = 0x6ed849a7e9b7794cL;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i : range(n))
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
				int lowerBoundActual = BinarySearch.lowerBound(0, n, x, i -> arr[i]);
				assertEquals(lowerBound, lowerBoundActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> BinarySearch.lowerBound(1, 0, 0, i -> i));
	}

	@Test
	public void upperBound() {
		final long seed = 0x5212c8f86ed5446aL;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i : range(n))
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
				int upperBoundActual = BinarySearch.upperBound(0, n, x, i -> arr[i]);
				assertEquals(upperBound, upperBoundActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> BinarySearch.upperBound(1, 0, 0, i -> i));
	}

	@Test
	public void equalRange() {
		final long seed = 0xde27d22e9c129139L;
		Random rand = new Random(seed);
		for (int iters = 100; iters-- > 0;) {
			final int n = 1000 + rand.nextInt(2000);
			final int[] arr = new int[n];
			for (int i : range(n))
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
				IntIntPair rangeActual = BinarySearch.equalRange(0, n, x, i -> arr[i]);
				assertEquals(rangeExpected, rangeActual);
			}
		}
		assertThrows(IllegalArgumentException.class, () -> BinarySearch.equalRange(1, 0, 0, i -> i));
	}

}
