package com.ugav.algo.test;

import java.util.List;

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
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findPredecessor(RedBlackTree::new, n);
		});
	}

	@Test
	public static boolean findSuccessor() {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findSuccessor(RedBlackTree::new, n);
		});
	}

}
