package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class HeapBinomialTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(HeapBinomial::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(HeapBinomial::new);
	}

	@Test
	public void testDecreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapBinomial::new);
	}

}
