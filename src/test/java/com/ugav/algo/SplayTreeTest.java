package com.ugav.algo;

public class SplayTreeTest extends TestUtils {

	@Test
	public static void randOps() {
		HeapTestUtils.testRandOps(SplayTree::new);
	}

	@Test
	public static void randOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(SplayTree::new);
	}

	@Test
	public static void meld() {
		HeapTestUtils.testMeld(SplayTree::new);
	}

	@Test
	public static void meldWithOrder() {
		HeapTestUtils.testMeldWithOrderedValues(SplayTree::new);
	}

	@Test
	public static void split() {
		BSTTestUtils.testSplit(SplayTree::new);
	}

	@Test
	public static void findSmaller() {
		BSTTestUtils.testFindSmallers(SplayTree::new);
	}

	@Test
	public static void findGreater() {
		BSTTestUtils.testFindGreaters(SplayTree::new);
	}

	@Test
	public static void getPredecessor() {
		BSTTestUtils.testGetPredecessors(SplayTree::new);
	}

	@Test
	public static void getSuccessor() {
		BSTTestUtils.testGetSuccessors(SplayTree::new);
	}

}
