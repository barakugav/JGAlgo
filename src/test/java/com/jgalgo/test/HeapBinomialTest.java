package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.HeapBinomial;

public class HeapBinomialTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0xe71c7e9f2765e4eaL;
		HeapTestUtils.testRandOps(HeapBinomial::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x55bb8b5e3f70d05aL;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0xbc597576efd52ccfL;
		HeapTestUtils.testMeld(HeapBinomial::new, seed);
	}

	@Test
	public void testDecreaseKey() {
		final long seed = 0xd7d8cf9389480696L;
		HeapTestUtils.testDecreaseKey(HeapBinomial::new, seed);
	}

}
