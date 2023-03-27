package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.RedBlackTree;

public class RedBlackTreeTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(RedBlackTree::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(RedBlackTree::new);
	}

	@Test
	public void testFindSmallers() {
		BSTTestUtils.testFindSmallers(RedBlackTree::new);
	}

	@Test
	public void testFindGreaters() {
		BSTTestUtils.testFindGreaters(RedBlackTree::new);
	}

	@Test
	public void testGetPredecessor() {
		BSTTestUtils.testGetPredecessors(RedBlackTree::new);
	}

	@Test
	public void testGetSuccessor() {
		BSTTestUtils.testGetSuccessors(RedBlackTree::new);
	}

}
