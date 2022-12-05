package com.ugav.algo;

public class HeapFibonacciTest extends TestUtils {

	@Test
	public static void randOps() {
		HeapTestUtils.testRandOps(HeapFibonacci::new);
	}

	@Test
	public static void randOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapFibonacci::new);
	}

	@Test
	public static void meld() {
		HeapTestUtils.testMeld(HeapFibonacci::new);
	}

	@Test
	public static void decreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapFibonacci::new);
	}

}
