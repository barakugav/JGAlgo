package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.HeapPairing;

public class HeapPairingTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(HeapPairing::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(HeapPairing::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(HeapPairing::new);
	}

	@Test
	public void testDecreaseKey() {
		HeapTestUtils.testDecreaseKey(HeapPairing::new);
	}

}
