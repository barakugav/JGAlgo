package com.ugav.algo.test;

import com.ugav.algo.RedBlackTree;

public class RedBlackTreeTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(RedBlackTree::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(RedBlackTree::new);
	}

	@Test
	public static boolean findSmallers() {
		return BSTTestUtils.testFindSmallers(RedBlackTree::new);
	}

	@Test
	public static boolean findGreaters() {
		return BSTTestUtils.testFindGreaters(RedBlackTree::new);
	}

	@Test
	public static boolean getPredecessor() {
		return BSTTestUtils.testGetPredecessors(RedBlackTree::new);
	}

	@Test
	public static boolean getSuccessor() {
		return BSTTestUtils.testGetSuccessors(RedBlackTree::new);
	}

}
