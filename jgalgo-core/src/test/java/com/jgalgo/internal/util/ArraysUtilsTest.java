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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

@SuppressWarnings("boxing")
public class ArraysUtilsTest extends TestBase {

	@Test
	public void testIntKthElementRandArrayUnique() {
		final long seed = 0xedf92ed1b59ae1e1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			int[] a = randPermutation(n, seedGen.nextSeed());
			testKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testObjKthElementRandArrayUnique() {
		final long seed = 0x7f7871365f84b52eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			Integer[] a = toIntegerArr(randPermutation(n, seedGen.nextSeed()));
			testKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testIntKthElementRandArrayNonUnique() {
		final long seed = 0x97e45458f8daefd2L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			testKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testObjKthElementRandArrayNonUnique() {
		final long seed = 0x6ee2228e9064ab3eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			Integer[] a = toIntegerArr(randArray(n, 0, n / 4, seedGen.nextSeed()));
			testKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testIntKthElementRandArraySameElm() {
		final long seed = 0x77b8bdd802380333L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(1);
		tester.addPhase().withArgs(32).repeat(1);
		tester.addPhase().withArgs(128).repeat(1);
		tester.addPhase().withArgs(256).repeat(1);
		tester.addPhase().withArgs(1024).repeat(1);
		tester.addPhase().withArgs(3849).repeat(1);
		tester.run(n -> {
			int[] a = new int[n];
			Arrays.fill(a, 6);
			testKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testObjKthElementRandArraySameElm() {
		final long seed = 0x656f2a7fcad2e9e8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(1);
		tester.addPhase().withArgs(32).repeat(1);
		tester.addPhase().withArgs(128).repeat(1);
		tester.addPhase().withArgs(256).repeat(1);
		tester.addPhase().withArgs(1024).repeat(1);
		tester.addPhase().withArgs(3849).repeat(1);
		tester.run(n -> {
			int[] a = new int[n];
			Arrays.fill(a, 6);
			testKthElement(toIntegerArr(a), seedGen.nextSeed());
		});
	}

	private static void testKthElement(int[] a, long seed) {
		int k = new Random(seed).nextInt(a.length);
		int actual = ArraysUtils.kthElement(a, k, null);

		java.util.Arrays.sort(a);
		assertEquals(a[k], actual);
	}

	private static void testKthElement(Integer[] a, long seed) {
		int k = new Random(seed).nextInt(a.length);
		int actual = ArraysUtils.kthElement(a, k, null);

		java.util.Arrays.sort(a);
		assertEquals(a[k], actual);
	}

	@Test
	public void testIntBucketPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0x90fc97e52265ff44L);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			int bucketSize = rand.nextInt(n / 2) + 1;
			ArraysUtils.bucketPartition(a, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b : range(bucketNum)) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i : range(bucketBegin, bucketEnd)) {
					assertTrue(a[bucketBegin] <= a[i] && a[i] <= a[bucketEnd - 1], "Bucket element " + a[i]
							+ " is not in range [" + a[bucketBegin] + ", " + a[bucketEnd - 1] + "]");
				}
			}
		});
	}

	@Test
	public void testObjBucketPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0x275079aa6f2fc7d7L);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			Integer[] a = toIntegerArr(randArray(n, 0, n / 4, seedGen.nextSeed()));
			int bucketSize = rand.nextInt(n / 2) + 1;
			ArraysUtils.bucketPartition(a, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b : range(bucketNum)) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i : range(bucketBegin, bucketEnd)) {
					assertTrue(a[bucketBegin] <= a[i] && a[i] <= a[bucketEnd - 1], "Bucket element " + a[i]
							+ " is not in range [" + a[bucketBegin] + ", " + a[bucketEnd - 1] + "]");
				}
			}
		});
	}

	@Test
	public void testIntPivotPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0x92f173634ff49309L);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			int pivot = a[rand.nextInt(a.length)];
			int firstGreater = ArraysUtils.pivotPartition(a, 0, a.length, pivot, null);

			int i = 0;
			for (; i < n; i++)
				if (!(a[i] < pivot))
					break;
			for (; i < n; i++)
				if (!(a[i] == pivot))
					break;
			assertEquals(i, firstGreater);
			for (; i < n; i++)
				if (!(a[i] > pivot))
					break;
			assertEquals(n, i);
		});
	}

	@Test
	public void testObjPivotPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0xd693bcfe6327104L);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(128).repeat(32);
		tester.addPhase().withArgs(256).repeat(16);
		tester.addPhase().withArgs(1024).repeat(8);
		tester.addPhase().withArgs(4567).repeat(2);
		tester.run(n -> {
			Integer[] a = toIntegerArr(randArray(n, 0, n / 4, seedGen.nextSeed()));
			int pivot = a[rand.nextInt(a.length)];
			int firstGreater = ArraysUtils.pivotPartition(a, 0, a.length, pivot, null);

			int i = 0;
			for (; i < n; i++)
				if (!(a[i] < pivot))
					break;
			for (; i < n; i++)
				if (!(a[i] == pivot))
					break;
			assertEquals(i, firstGreater);
			for (; i < n; i++)
				if (!(a[i] > pivot))
					break;
			assertEquals(n, i);
		});
	}

	private static Integer[] toIntegerArr(int[] a) {
		return IntStream.of(a).boxed().toArray(Integer[]::new);
	}

	@Test
	public void invalidIndex() {
		int[] a = range(100).toIntArray();
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, -1, 100, 7, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 0, 101, 7, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 57, 57, 0, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 57, 56, 0, null, false));
	}

}
