package com.ugav.algo.test;

import java.util.Random;

import com.ugav.algo.Arrays;

public class ArraysTest {

	@Test
	public static boolean getKthElementRandArrayUnique() {
		int[][] phases = new int[][] { { 256, 8 }, { 128, 32 }, { 32, 128 }, { 16, 256 }, { 8, 1024 }, { 2, 4096 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int[] a = Utils.randPermutation(n, TestUtils.nextRandSeed());
			return testGetKthElement(a);
		});
	}

	@Test
	public static boolean getKthElementRandArrayNonunique() {
		int[][] phases = new int[][] { { 256, 8 }, { 128, 32 }, { 32, 128 }, { 16, 256 }, { 8, 1024 }, { 2, 4096 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int[] a = Utils.randArray(n, 0, n / 4, TestUtils.nextRandSeed());
			return testGetKthElement(a);
		});
	}

	private static boolean testGetKthElement(int[] a) {
		Random rand = new Random(TestUtils.nextRandSeed());

		Integer[] A = toIntegerArr(a);
		int k = rand.nextInt(A.length);
		int actual = Arrays.getKthElement(A, k, null);

		java.util.Arrays.sort(a);
		int expected = a[k];

		if (actual != expected) {
			TestUtils.printTestStr("Unexpected K'th elemet: " + actual + " != " + expected + "\n");
			return false;
		}
		return true;
	}

	@Test
	public static boolean bucketPartition() {
		Random rand = new Random(TestUtils.nextRandSeed());
		int[][] phases = new int[][] { { 256, 8 }, { 128, 32 }, { 32, 128 }, { 16, 256 }, { 8, 1024 }, { 2, 4096 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int[] a = Utils.randArray(n, 0, n / 4, TestUtils.nextRandSeed());
			Integer[] A = toIntegerArr(a);
			int bucketSize = rand.nextInt(n / 2) + 1;
			Arrays.bucketPartition(A, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b = 0; b < bucketNum; b++) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i = bucketBegin; i < bucketEnd; i++) {
					if (!(a[bucketBegin] <= A[i] && A[i] <= a[bucketEnd - 1])) {
						TestUtils.printTestStr("Bucket element " + A[i] + " is not in range [" + a[bucketBegin] + ", "
								+ a[bucketEnd - 1] + "]\n");
						return false;
					}
				}
			}

			return true;
		});
	}

	private static Integer[] toIntegerArr(int[] a) {
		Integer[] A = new Integer[a.length];
		for (int i = 0; i < a.length; i++)
			A[i] = a[i];
		return A;
	}

}
