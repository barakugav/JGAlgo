package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.HeapFibonacci;

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
