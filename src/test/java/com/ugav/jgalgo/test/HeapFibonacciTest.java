package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.HeapFibonacci;

public class HeapFibonacciTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0xc5b0d9f99444efc5L;
		HeapTestUtils.testRandOps(HeapFibonacci::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x194fe80fdf0b3e1bL;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapFibonacci::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0xc801a24c2405c42dL;
		HeapTestUtils.testMeld(HeapFibonacci::new, seed);
	}

	@Test
	public void testDecreaseKey() {
		final long seed = 0xcadbabb0e01d6ea5L;
		HeapTestUtils.testDecreaseKey(HeapFibonacci::new, seed);
	}

}
