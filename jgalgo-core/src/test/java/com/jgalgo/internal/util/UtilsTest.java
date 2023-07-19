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
import java.util.Random;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;

public class UtilsTest extends TestBase {

	@Test
	public void testEqualRange() {
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
				} else {
					x = arr[rand.nextInt(n)];
				}

				int rangeBegin = -1;
				for (int i = 0; i < n; i++) {
					if (arr[i] == x) {
						rangeBegin = i;
						break;
					}
				}
				int rangeEnd = n;
				for (int i = rangeBegin; i < n; i++) {
					if (arr[i] != x) {
						rangeEnd = i;
						break;
					}
				}
				IntIntPair rangeExpected = rangeBegin == -1 ? null : IntIntPair.of(rangeBegin, rangeEnd);
				IntIntPair rangeActual = Utils.equalRange(0, n, x, i -> arr[i]);
				assertEquals(rangeExpected, rangeActual);
			}
		}
	}

}
