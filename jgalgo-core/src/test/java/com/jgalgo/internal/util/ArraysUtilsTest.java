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
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

@SuppressWarnings("boxing")
public class ArraysUtilsTest extends TestBase {

	@Test
	public void kthElementIntRandArrayUnique() {
		final long seed = 0xedf92ed1b59ae1e1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(reverse -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				int[] a = randPermutation(n, seedGen.nextSeed());
				kthElementTest(a, reverse, seedGen.nextSeed());
			});
		});
	}

	@Test
	public void kthElementObjRandArrayUnique() {
		final long seed = 0x7f7871365f84b52eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(reverse -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				Integer[] a = toIntegerArr(randPermutation(n, seedGen.nextSeed()));
				kthElementTest(a, reverse, seedGen.nextSeed());
			});
		});
	}

	@Test
	public void kthElementIntRandArrayNonUnique() {
		final long seed = 0x97e45458f8daefd2L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(reverse -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
				kthElementTest(a, reverse, seedGen.nextSeed());
			});
		});
	}

	@Test
	public void kthElementObjRandArrayNonUnique() {
		final long seed = 0x6ee2228e9064ab3eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		foreachBoolConfig(reverse -> {
			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				Integer[] a = toIntegerArr(randArray(n, 0, n / 4, seedGen.nextSeed()));
				kthElementTest(a, reverse, seedGen.nextSeed());
			});
		});
	}

	@Test
	public void kthElementIntRandArraySameElm() {
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
			kthElementTest(a, false, seedGen.nextSeed());
		});
	}

	@Test
	public void kthElementObjRandArraySameElm() {
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
			kthElementTest(toIntegerArr(a), false, seedGen.nextSeed());
		});
	}

	private static void kthElementTest(int[] a, boolean reverse, long seed) {
		final Random rand = new Random(seed);
		final IntComparator cmp = reverse ? (x, y) -> Integer.compare(y, x) : null;
		final int from = rand.nextInt(a.length);
		final int to = from + 1 + rand.nextInt(a.length - from);
		final int k = from + rand.nextInt(to - from);
		int actual = ArraysUtils.kthElement(a, from, to, k, cmp, false);

		int[] sorted = a.clone();
		Arrays.sort(sorted, from, to);
		if (reverse)
			IntArrays.reverse(sorted, from, to);
		assertEquals(sorted[k], actual);

		/* in place */
		int actual2 = ArraysUtils.kthElement(a, from, to, k, cmp, true);
		assertEquals(sorted[k], actual2);
		IntComparator cmp0 = cmp != null ? cmp : Integer::compare;
		for (int i : range(from, k))
			assertTrue(cmp0.compare(a[i], actual2) <= 0);
		for (int i : range(k + 1, to))
			assertTrue(cmp0.compare(a[i], actual2) >= 0);
	}

	private static void kthElementTest(Integer[] a, boolean reverse, long seed) {
		final Random rand = new Random(seed);
		final Comparator<Integer> cmp = reverse ? (x, y) -> y.compareTo(x) : null;
		final int from = rand.nextInt(a.length);
		final int to = from + 1 + rand.nextInt(a.length - from);
		final int k = from + rand.nextInt(to - from);
		int actual = ArraysUtils.kthElement(a, from, to, k, cmp, false);

		Integer[] sorted = a.clone();
		Arrays.sort(sorted, from, to);
		if (reverse)
			ObjectArrays.reverse(sorted, from, to);
		assertEquals(sorted[k], actual);

		/* in place */
		int actual2 = ArraysUtils.kthElement(a, from, to, k, cmp, true);
		assertEquals(sorted[k], actual2);
		Comparator<Integer> cmp0 = cmp != null ? cmp : Integer::compare;
		for (int i : range(from, k))
			assertTrue(cmp0.compare(a[i], actual2) <= 0);
		for (int i : range(k + 1, to))
			assertTrue(cmp0.compare(a[i], actual2) >= 0);
	}

	@Test
	public void bucketPartitionInt() {
		final SeedGenerator seedGen = new SeedGenerator(0x90fc97e52265ff44L);
		Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(reverse -> {
			final IntComparator cmp = reverse ? (x, y) -> Integer.compare(y, x) : null;

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
				ArraysUtils.bucketPartition(a, 0, n, cmp, bucketSize);

				if (cmp != null) {
					IntArrays.quickSort(a, cmp);
				} else {
					IntArrays.quickSort(a);
				}
				IntComparator cmp0 = cmp != null ? cmp : Integer::compare;
				int bucketNum = (n - 1) / bucketSize + 1;
				for (int b : range(bucketNum)) {
					int bucketBegin = b * bucketSize;
					int bucketEnd = Math.min(bucketBegin + bucketSize, n);
					for (int i : range(bucketBegin, bucketEnd)) {
						assertTrue(cmp0.compare(a[bucketBegin], a[i]) <= 0);
						assertTrue(cmp0.compare(a[i], a[bucketEnd - 1]) <= 0);
					}
				}
			});
		});

		/* negative bucket size */
		assertThrows(IllegalArgumentException.class,
				() -> ArraysUtils.bucketPartition(new int[] { 0 }, 0, 1, null, -1));
	}

	@Test
	public void bucketPartitionObj() {
		final SeedGenerator seedGen = new SeedGenerator(0x275079aa6f2fc7d7L);
		Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(reverse -> {
			final Comparator<Integer> cmp = reverse ? (x, y) -> y.compareTo(x) : null;

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
				ArraysUtils.bucketPartition(a, 0, n, cmp, bucketSize);

				if (cmp != null) {
					Arrays.sort(a, cmp);
				} else {
					Arrays.sort(a);
				}
				Comparator<Integer> cmp0 = cmp != null ? cmp : Integer::compare;
				int bucketNum = (n - 1) / bucketSize + 1;
				for (int b : range(bucketNum)) {
					int bucketBegin = b * bucketSize;
					int bucketEnd = Math.min(bucketBegin + bucketSize, n);
					for (int i : range(bucketBegin, bucketEnd)) {
						assertTrue(cmp0.compare(a[bucketBegin], a[i]) <= 0);
						assertTrue(cmp0.compare(a[i], a[bucketEnd - 1]) <= 0);
					}
				}
			});
		});

		/* negative bucket size */
		assertThrows(IllegalArgumentException.class,
				() -> ArraysUtils.bucketPartition(new Integer[] { 0 }, 0, 1, null, -1));
	}

	@Test
	public void pivotPartitionInt() {
		final SeedGenerator seedGen = new SeedGenerator(0x92f173634ff49309L);
		Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(reverse -> {
			final IntComparator cmp = reverse ? (x, y) -> Integer.compare(y, x) : null;

			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
				final int from = rand.nextInt(n);
				final int to = rand.nextInt(n - from) + from;
				int pivot = from < to ? a[from + rand.nextInt(to - from)] : a[from];
				int firstGreater = ArraysUtils.pivotPartition(a, from, to, pivot, cmp);

				IntComparator cmp0 = cmp != null ? cmp : Integer::compare;
				int i = from;
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) >= 0)
						break;
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) != 0)
						break;
				assertEquals(i, firstGreater);
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) <= 0)
						break;
				assertEquals(to, i);
			});
		});
	}

	@Test
	public void pivotPartitionObj() {
		final SeedGenerator seedGen = new SeedGenerator(0xd693bcfe6327104L);
		Random rand = new Random(seedGen.nextSeed());
		foreachBoolConfig(reverse -> {
			final Comparator<Integer> cmp = reverse ? (x, y) -> y.compareTo(x) : null;

			PhasedTester tester = new PhasedTester();
			tester.addPhase().withArgs(8).repeat(256);
			tester.addPhase().withArgs(32).repeat(128);
			tester.addPhase().withArgs(128).repeat(32);
			tester.addPhase().withArgs(256).repeat(16);
			tester.addPhase().withArgs(1024).repeat(8);
			tester.addPhase().withArgs(4567).repeat(2);
			tester.run(n -> {
				Integer[] a = toIntegerArr(randArray(n, 0, n / 4, seedGen.nextSeed()));
				final int from = rand.nextInt(n);
				final int to = rand.nextInt(n - from) + from;
				int pivot = from < to ? a[from + rand.nextInt(to - from)] : a[from];
				int firstGreater = ArraysUtils.pivotPartition(a, from, to, pivot, cmp);

				Comparator<Integer> cmp0 = cmp != null ? cmp : Integer::compare;
				int i = from;
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) >= 0)
						break;
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) != 0)
						break;
				assertEquals(i, firstGreater);
				for (; i < to; i++)
					if (cmp0.compare(a[i], pivot) <= 0)
						break;
				assertEquals(to, i);
			});
		});
	}

	private static Integer[] toIntegerArr(int[] a) {
		return IntStream.of(a).boxed().toArray(Integer[]::new);
	}

	@Test
	public void kthElementInvalidIndex() {
		int[] a = range(100).toIntArray();
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, -1, 100, 7, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 0, 101, 7, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 57, 57, 0, null, false));
		assertThrows(IndexOutOfBoundsException.class, () -> ArraysUtils.kthElement(a, 57, 56, 0, null, false));
	}

}
