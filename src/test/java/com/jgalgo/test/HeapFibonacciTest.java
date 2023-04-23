package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.HeapFibonacci;

public class HeapFibonacciTest extends TestBase {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0xc5b0d9f99444efc5L;
		HeapTestUtils.testRandOpsDefaultCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0x73f5ac001997955cL;
		HeapTestUtils.testRandOpsCustomCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x194fe80fdf0b3e1bL;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapFibonacci::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xc801a24c2405c42dL;
		HeapTestUtils.testMeldDefaultCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0x631ed5c1813636efL;
		HeapTestUtils.testMeldCustomCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0xcadbabb0e01d6ea5L;
		HeapTestUtils.testDecreaseKeyDefaultCompare(HeapFibonacci::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x0a7f3203577b4cefL;
		HeapTestUtils.testDecreaseKeyCustomCompare(HeapFibonacci::new, seed);
	}

}
