package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.HeapPairing;

public class HeapPairingTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x7a98aed671bf0c81L;
		HeapTestUtils.testRandOps(HeapPairing::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x25467ce9958980c1L;
		HeapTestUtils.testRandOpsAfterManyInserts(HeapPairing::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0xc3cd155dfa9d97f6L;
		HeapTestUtils.testMeld(HeapPairing::new, seed);
	}

	@Test
	public void testDecreaseKey() {
		final long seed = 0x90a80620c3ef1a43L;
		HeapTestUtils.testDecreaseKey(HeapPairing::new, seed);
	}

}
