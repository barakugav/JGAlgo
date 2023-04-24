package com.jgalgo;

import org.junit.jupiter.api.Test;

public class RedBlackTreeTest extends TestBase {

	@Test
	public void testRandOpsDefaultCompare() {
		final long seed = 0x445a02434b767d0fL;
		HeapTestUtils.testRandOpsDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testRandOpsCustomCompare() {
		final long seed = 0xdf0d20c96b18e76bL;
		HeapTestUtils.testRandOpsCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0xe00c6e6c7bbdf827L;
		HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new, seed);
	}

	@Test
	public void testExtractMax() {
		final long seed = 0x51537cb2cbca4774L;
		BSTTestUtils.testExtractMax(RedBlackTree::new, seed);
	}

	@Test
	public void testMeldDefaultCompare() {
		final long seed = 0x3fbbd70b1c035dedL;
		HeapTestUtils.testMeldDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testMeldCustomCompare() {
		final long seed = 0xb8f57d62b2818583L;
		HeapTestUtils.testMeldCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testDecreaseKeyDefaultCompare() {
		final long seed = 0x3ad0758602c2f656L;
		HeapTestUtils.testDecreaseKeyDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testDecreaseKeyCustomCompare() {
		final long seed = 0x0edcd3dd97c5f867L;
		HeapTestUtils.testDecreaseKeyCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testFindSmallersDefaultCompare() {
		final long seed = 0x4c57d0d410b7e9d6L;
		BSTTestUtils.testFindSmallersDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testFindSmallersCustomCompare() {
		final long seed = 0x73810ca9d38884ecL;
		BSTTestUtils.testFindSmallersCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testFindGreatersDefaultCompare() {
		final long seed = 0xec49fe38ca3ca19bL;
		BSTTestUtils.testFindGreatersDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testFindGreatersCustomCompare() {
		final long seed = 0x5939e3cdc2c3fb8fL;
		BSTTestUtils.testFindGreatersCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testGetPredecessorDefaultCompare() {
		final long seed = 0x941838c5890fae32L;
		BSTTestUtils.testGetPredecessorsDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testGetPredecessorCustomCompare() {
		final long seed = 0x6eec0c5157093ce4L;
		BSTTestUtils.testGetPredecessorsCustomCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testGetSuccessorDefaultCompare() {
		final long seed = 0xc68b22dd9e9afed9L;
		BSTTestUtils.testGetSuccessorsDefaultCompare(RedBlackTree::new, seed);
	}

	@Test
	public void testGetSuccessorCustomCompare() {
		final long seed = 0x89f85333bcca14e0L;
		BSTTestUtils.testGetSuccessorsCustomCompare(RedBlackTree::new, seed);
	}

}
