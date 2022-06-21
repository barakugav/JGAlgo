package com.ugav.algo.test;

import com.ugav.algo.HeapBinary;

public class HeapBinaryTest extends TestUtils {

	@Test
	public static void randOps() {
		HeapTestUtils.testRandOps(HeapBinary::new);
	}

	@Test
	public static void randOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new);
	}

	@Test
	public static void meld() {
		HeapTestUtils.testMeld(HeapBinary::new);
	}

}
