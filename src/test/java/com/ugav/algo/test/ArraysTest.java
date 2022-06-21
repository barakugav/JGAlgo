package com.ugav.algo.test;

import java.util.List;
import java.util.Random;

import com.ugav.algo.Arrays;

@SuppressWarnings("boxing")
public class ArraysTest extends TestUtils {

	@Test
	public static void getKthElementRandArrayUnique() {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = Utils.randPermutation(n, nextRandSeed());
			testGetKthElement(a);
		});
	}

	@Test
	public static void getKthElementRandArrayNonunique() {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = Utils.randArray(n, 0, n / 4, nextRandSeed());
			testGetKthElement(a);
		});
	}

	private static void testGetKthElement(int[] a) {
		Random rand = new Random(nextRandSeed());

		Integer[] A = toIntegerArr(a);
		int k = rand.nextInt(A.length);
		int actual = Arrays.getKthElement(A, k, null).intValue();

		java.util.Arrays.sort(a);
		int expected = a[k];

		assertEq(expected, actual, "Unexpected K'th elemet");
	}

	@Test
	public static void bucketPartition() {
		Random rand = new Random(nextRandSeed());
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 1024),
				phase(2, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int[] a = Utils.randArray(n, 0, n / 4, nextRandSeed());
			Integer[] A = toIntegerArr(a);
			int bucketSize = rand.nextInt(n / 2) + 1;
			Arrays.bucketPartition(A, 0, n, null, bucketSize);

			java.util.Arrays.sort(a);
			int bucketNum = (n - 1) / bucketSize + 1;
			for (int b = 0; b < bucketNum; b++) {
				int bucketBegin = b * bucketSize;
				int bucketEnd = Math.min(bucketBegin + bucketSize, n);
				for (int i = bucketBegin; i < bucketEnd; i++) {
					assertTrue(a[bucketBegin] <= A[i].intValue() && A[i].intValue() <= a[bucketEnd - 1],
							"Bucket element ", A[i], " is not in range [", a[bucketBegin], ", ", a[bucketEnd - 1],
							"]\n");
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
