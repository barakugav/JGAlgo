package com.jgalgo;

import org.junit.jupiter.api.Test;

public class HeapBinomialTest extends TestBase {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x0600b0c18d6d97d4L;
		HeapTestUtils.testRandOpsDefaultCompare(HeapBinomial::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xe71c7e9f2765e4eaL;
		HeapTestUtils.testRandOpsCustomCompare(HeapBinomial::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x55bb8b5e3f70d05aL;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0x98473460a5541235L;
		HeapTestUtils.testMeldDefaultCompare(HeapBinomial::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xbc597576efd52ccfL;
		HeapTestUtils.testMeldCustomCompare(HeapBinomial::new, seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x553462f097149dc1L;
		HeapTestUtils.testDecreaseKeyDefaultCompare(HeapBinomial::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0xd7d8cf9389480696L;
		HeapTestUtils.testDecreaseKeyCustomCompare(HeapBinomial::new, seed);
	}

}
