package com.ugav.algo.test;

import com.ugav.algo.HeapFibonacci;

public class HeapFibonacciTest {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(HeapFibonacci::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(HeapFibonacci::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(HeapFibonacci::new);
	}

	@Test
	public static boolean decreaseKey() {
		return HeapTestUtils.testDecreaseKey(HeapFibonacci::new);
	}

}
