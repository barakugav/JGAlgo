package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.HeapBinary;

public class HeapBinaryTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0xc8c79b6e3d880041L;
		HeapTestUtils.testRandOps(HeapBinary::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x2c46712aa83d74a2L;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0x92cd7f63c8322849L;
		HeapTestUtils.testMeld(HeapBinary::new, seed);
	}

}
