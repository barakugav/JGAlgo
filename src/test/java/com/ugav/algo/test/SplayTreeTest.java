package com.ugav.algo.test;

import com.ugav.algo.SplayTree;

public class SplayTreeTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return HeapTestUtils.testRandOps(SplayTree::new);
	}

	@Test
	public static boolean randOpsAfterManyInserts() {
		return HeapTestUtils.testRandOpsAfterManyInserts(SplayTree::new);
	}

	@Test
	public static boolean meld() {
		return HeapTestUtils.testMeld(SplayTree::new);
	}

	@Test
	public static boolean meldWithOrder() {
		return HeapTestUtils.testMeldWithOrderedValues(SplayTree::new);
	}

	@Test
	public static boolean split() {
		return BSTTestUtils.testSplit(SplayTree::new);
	}

	@Test
	public static boolean findSmaller() {
		return BSTTestUtils.testFindSmallers(SplayTree::new);
	}

	@Test
	public static boolean findGreater() {
		return BSTTestUtils.testFindGreaters(SplayTree::new);
	}

	@Test
	public static boolean getPredecessor() {
		return BSTTestUtils.testGetPredecessors(SplayTree::new);
	}

	@Test
	public static boolean getSuccessor() {
		return BSTTestUtils.testGetSuccessors(SplayTree::new);
	}

}
