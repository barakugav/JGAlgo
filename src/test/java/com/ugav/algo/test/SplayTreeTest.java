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
	public static boolean findPredecessor() {
		return BSTTestUtils.testFindPredecessors(SplayTree::new);
	}

	@Test
	public static boolean findSuccessor() {
		return BSTTestUtils.testFindSuccessors(SplayTree::new);
	}

}
