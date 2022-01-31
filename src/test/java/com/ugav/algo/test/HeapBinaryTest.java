package com.ugav.algo.test;

import com.ugav.algo.HeapBinary;

public class HeapBinaryTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(HeapBinary::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(HeapBinary::new);
	}

}
