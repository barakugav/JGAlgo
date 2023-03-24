package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class HeapBinaryTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(HeapBinary::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(HeapBinary::new);
	}

}
