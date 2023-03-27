package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SplayTree;

public class SplayTreeTest extends TestUtils {

	@Test
	public void testRandOps() {
		HeapTestUtils.testRandOps(SplayTree::new);
	}

	@Test
	public void testRandOpsAfterManyInserts() {
		HeapTestUtils.testRandOpsAfterManyInserts(SplayTree::new);
	}

	@Test
	public void testMeld() {
		HeapTestUtils.testMeld(SplayTree::new);
	}

	@Test
	public void testMeldWithOrder() {
		HeapTestUtils.testMeldWithOrderedValues(SplayTree::new);
	}

	@Test
	public void testSplit() {
		BSTTestUtils.testSplit(SplayTree::new);
	}

	@Test
	public void testFindSmaller() {
		BSTTestUtils.testFindSmallers(SplayTree::new);
	}

	@Test
	public void testFindGreater() {
		BSTTestUtils.testFindGreaters(SplayTree::new);
	}

	@Test
	public void testGetPredecessor() {
		BSTTestUtils.testGetPredecessors(SplayTree::new);
	}

	@Test
	public void testGetSuccessor() {
		BSTTestUtils.testGetSuccessors(SplayTree::new);
	}

}
