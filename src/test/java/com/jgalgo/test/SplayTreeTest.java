package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SplayTree;

public class SplayTreeTest extends TestUtils {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x29080f5f2aca1605L;
		HeapTestUtils.testRandOpsDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xa92d44b8205fbbdeL;
		HeapTestUtils.testRandOpsCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0x38f7e3242f52e2dcL;
		HeapTestUtils.testRandOpsAfterManyInserts(SplayTree::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0xe882a11221a54a22L;
		HeapTestUtils.testMeldDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xfeab1714f2e57cd4L;
		HeapTestUtils.testMeldCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testMeldWithOrderDefaultCompare() {
		final long seed = 0x0cb4bf9251d8145bL;
		HeapTestUtils.testMeldWithOrderedValuesDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testMeldWithOrderCustomCompare() {
		final long seed = 0x24c1f56e5fdbc5acL;
		HeapTestUtils.testMeldWithOrderedValuesCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testSplitDefaultCompare() {
		final long seed = 0x353e23967b348089L;
		BSTTestUtils.testSplitDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testSplitCustomCompare() {
		final long seed = 0x40b238cf34d778c0L;
		BSTTestUtils.testSplitCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x2e6a8902f634f8caL;
		HeapTestUtils.testDecreaseKeyDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0xb1db9f0001ff6a5aL;
		HeapTestUtils.testDecreaseKeyCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testFindSmallersDefaultCompare() {
		final long seed = 0x77f393a0a7508c84L;
		BSTTestUtils.testFindSmallersDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testFindSmallersCustomCompare() {
		final long seed = 0x99a37616f1023b0fL;
		BSTTestUtils.testFindSmallersCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testFindGreatersDefaultCompare() {
		final long seed = 0xf8ec8ed64600635fL;
		BSTTestUtils.testFindGreatersDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testFindGreaterCustomCompare() {
		final long seed = 0xf890218f3f5420a9L;
		BSTTestUtils.testFindGreatersCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testGetPredecessorDefaultCompare() {
		final long seed = 0x09395f66760a5c55L;
		BSTTestUtils.testGetPredecessorsDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testGetPredecessorCustomCompare() {
		final long seed = 0x2f8fd18ab64a2b15L;
		BSTTestUtils.testGetPredecessorsCustomCompare(SplayTree::new, seed);
	}

	@Test
	public void testGetSuccessorDefaultCompare() {
		final long seed = 0x7ec6e57911f958c1L;
		BSTTestUtils.testGetSuccessorsDefaultCompare(SplayTree::new, seed);
	}

	@Test
	public void testGetSuccessorCustomCompare() {
		final long seed = 0x782385e30e24c822L;
		BSTTestUtils.testGetSuccessorsCustomCompare(SplayTree::new, seed);
	}

}
