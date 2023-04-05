package com.jgalgo.test;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.jgalgo.Array;

public class ArraysTest extends TestUtils {

	@Test
	public void testGetKthElementRandArrayUnique() {
		final SeedGenerator seedGen = new SeedGenerator(0xedf92ed1b59ae1e1L);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randPermutation(n, seedGen.nextSeed());
			testGetKthElement(a, seedGen.nextSeed());
		});
	}

	@Test
	public void testGetKthElementRandArrayNonunique() {
		final SeedGenerator seedGen = new SeedGenerator(0x97e45458f8daefd2L);
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			testGetKthElement(a, seedGen.nextSeed());
		});
	}

	private static void testGetKthElement(int[] a, long seed) {
		Random rand = new Random(seed);

		Integer[] A = toIntegerArr(a);
		int k = rand.nextInt(A.length);
		int actual = Array.getKthElement(A, k, null).intValue();

		java.util.Arrays.sort(a);
		int expected = a[k];

		Assertions.assertEquals(expected, actual, "Unexpected K'th elemet");
	}

	@Test
	public void testBucketPartition() {
		final SeedGenerator seedGen = new SeedGenerator(0x90fc97e52265ff44L);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = randArray(n, 0, n / 4, seedGen.nextSeed());
			Integer[] A = toIntegerArr(a);
			int bucketSize = rand.nextInt(n / 2) + 1;
			Array.bucketPartition(A, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b = 0; b < bucketNum; b++) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i = bucketBegin; i < bucketEnd; i++) {
					Assertions.assertTrue(a[bucketBegin] <= A[i].intValue() && A[i].intValue() <= a[bucketEnd - 1],
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
