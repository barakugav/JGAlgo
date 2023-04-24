package com.jgalgo;

import org.junit.jupiter.api.Test;

public class HeapBinaryTest extends TestBase {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0xce76e918bde66ee3L;
		HeapTestUtils.testRandOpsDefaultCompare(HeapBinary::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xc8c79b6e3d880041L;
		HeapTestUtils.testRandOpsCustomCompare(HeapBinary::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x2c46712aa83d74a2L;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapBinary::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xa7e09a00be04a88bL;
		HeapTestUtils.testMeldDefaultCompare(HeapBinary::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0x92cd7f63c8322849L;
		HeapTestUtils.testMeldCustomCompare(HeapBinary::new, seed);
	}

}
