package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class HeapFibonacciTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(HeapFibonacci::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapFibonacci::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(HeapFibonacci::new);
	}

	@Test
	public void testDecreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapFibonacci::new);
	}

}
