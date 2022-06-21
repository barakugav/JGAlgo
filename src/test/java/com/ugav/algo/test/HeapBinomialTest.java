package com.ugav.algo.test;

import com.ugav.algo.HeapBinomial;

public class HeapBinomialTest extends TestUtils {

	@Test
	public static void randOps() {
		HeapTestUtils.testRandOps(HeapBinomial::new);
	}

	@Test
	public static void randOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new);
	}

	@Test
	public static void meld() {
		HeapTestUtils.testMeld(HeapBinomial::new);
	}

	@Test
	public static void decreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapBinomial::new);
	}

}
