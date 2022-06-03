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
	public static boolean findPredecessor() {
		return BSTTestUtils.testFindPredecessors(RedBlackTree::new);
	}

	@Test
	public static boolean findSuccessor() {
		return BSTTestUtils.testFindSuccessors(RedBlackTree::new);
	}

}
