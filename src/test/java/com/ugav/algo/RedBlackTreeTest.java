package com.ugav.algo;

public class RedBlackTreeTest extends TestUtils {

	@Test
	public static void randOps() {
		HeapTestUtils.testRandOps(RedBlackTree::new);
	}

	@Test
	public static void randOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(RedBlackTree::new);
	}

	@Test
	public static void meld() {
		HeapTestUtils.testMeld(RedBlackTree::new);
	}

	@Test
	public static void findSmallers() {
		BSTTestUtils.testFindSmallers(RedBlackTree::new);
	}

	@Test
	public static void findGreaters() {
		BSTTestUtils.testFindGreaters(RedBlackTree::new);
	}

	@Test
	public static void getPredecessor() {
		BSTTestUtils.testGetPredecessors(RedBlackTree::new);
	}

	@Test
	public static void getSuccessor() {
		BSTTestUtils.testGetSuccessors(RedBlackTree::new);
	}

}
