package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RedBlackTree;

public class RedBlackTreeTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x445a02434b767d0fL;
		HeapTestUtils.testRandOps(RedBlackTree::new, seed);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		final long seed = 0xe00c6e6c7bbdf827L;
		HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new, seed);
	}

	@Test
	public void testMeld() {
		final long seed = 0x3fbbd70b1c035dedL;
		HeapTestUtils.testMeld(RedBlackTree::new, seed);
	}

	@Test
	public void testFindSmallers() {
		final long seed = 0x4c57d0d410b7e9d6L;
		BSTTestUtils.testFindSmallers(RedBlackTree::new, seed);
	}

	@Test
	public void testFindGreaters() {
		final long seed = 0xec49fe38ca3ca19bL;
		BSTTestUtils.testFindGreaters(RedBlackTree::new, seed);
	}

	@Test
	public void testGetPredecessor() {
		final long seed = 0x941838c5890fae32L;
		BSTTestUtils.testGetPredecessors(RedBlackTree::new, seed);
	}

	@Test
	public void testGetSuccessor() {
		final long seed = 0xc68b22dd9e9afed9L;
		BSTTestUtils.testGetSuccessors(RedBlackTree::new, seed);
	}

}
