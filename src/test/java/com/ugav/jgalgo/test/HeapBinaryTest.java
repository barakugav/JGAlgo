package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.HeapBinary;

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
