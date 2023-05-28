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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class ArraysUtilsTest extends TestBase {

	@Test
	public void testGetKthElementRandArrayUnique() {
		final long seed = 0xedf92ed1b59ae1e1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024), phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randPermutation(n, seedGen.nextSeed());
			testGetKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testGetKthElementRandArrayNonUnique() {
		final long seed = 0x97e45458f8daefd2L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024), phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			testGetKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testGetKthElementRandArraySameElm() {
		final long seed = 0x77b8bdd802380333L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(1, 8), phase(1, 32), phase(1, 128), phase(1, 256), phase(1, 1024), phase(1, 3849));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = new int[n];
			Arrays.fill(a, 6);
			testGetKthElement(a, seedGen.nextSeed());
		});
	}

	private static void testGetKthElement(int[] a, long seed) {
		Random rand = new Random(seed);

		Integer[] A = toIntegerArr(a);
		int k = rand.nextInt(A.length);
		int actual = ArraysUtils.getKthElement(A, k, null).intValue();

		java.util.Arrays.sort(a);
		int expected = a[k];

		assertEquals(expected, actual, "Unexpected K'th elemet");
	}

	@Test
	public void testBucketPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0x90fc97e52265ff44L);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases =
				List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024), phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			Integer[] A = toIntegerArr(a);
			int bucketSize = rand.nextInt(n / 2) + 1;
			ArraysUtils.bucketPartition(A, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b = 0; b < bucketNum; b++) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i = bucketBegin; i < bucketEnd; i++) {
					assertTrue(a[bucketBegin] <= A[i].intValue() && A[i].intValue() <= a[bucketEnd - 1],
							"Bucket element " + A[i] + " is not in range [" + a[bucketBegin] + ", " + a[bucketEnd - 1]
									+ "]");
				}
			}
		});
	}

	private static Integer[] toIntegerArr(int[] a) {
		Integer[] A = new Integer[a.length];
		for (int i = 0; i < a.length; i++)
			A[i] = Integer.valueOf(a[i]);
		return A;
	}

}
