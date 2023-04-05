package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SplayTree;

public class SplayTreeTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0xa92d44b8205fbbdeL;
		HeapTestUtils.testRandOps(SplayTree::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x38f7e3242f52e2dcL;
		HeapTestUtils.testRandOpsAfterManyInserts(SplayTree::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0xfeab1714f2e57cd4L;
		HeapTestUtils.testMeld(SplayTree::new, seed);
	}

	@Test
	public void testMeldWithOrder() {
		final long seed = 0x24c1f56e5fdbc5acL;
		HeapTestUtils.testMeldWithOrderedValues(SplayTree::new, seed);
	}

	@Test
	public void testSplit() {
		final long seed = 0x40b238cf34d778c0L;
		BSTTestUtils.testSplit(SplayTree::new, seed);
	}

	@Test
	public void testFindSmaller() {
		final long seed = 0x99a37616f1023b0fL;
		BSTTestUtils.testFindSmallers(SplayTree::new, seed);
	}

	@Test
	public void testFindGreater() {
		final long seed = 0xf890218f3f5420a9L;
		BSTTestUtils.testFindGreaters(SplayTree::new, seed);
	}

	@Test
	public void testGetPredecessor() {
		final long seed = 0x2f8fd18ab64a2b15L;
		BSTTestUtils.testGetPredecessors(SplayTree::new, seed);
	}

	@Test
	public void testGetSuccessor() {
		final long seed = 0x782385e30e24c822L;
		BSTTestUtils.testGetSuccessors(SplayTree::new, seed);
	}

}
