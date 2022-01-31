package com.ugav.algo.test;

import com.ugav.algo.HeapBinomial;

public class HeapBinomialTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(HeapBinomial::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(HeapBinomial::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(HeapBinomial::new);
	}

	@Test
	public static boolean decreaseKey() {
		return HeapTestUtils.testDecreaseKey(HeapBinomial::new);
	}

}
