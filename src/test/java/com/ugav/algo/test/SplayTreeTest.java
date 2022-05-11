package com.ugav.algo.test;

import java.util.List;

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
	public static boolean findPredecessor() {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findPredecessor(SplayTree::new, n);
		});
	}

	@Test
	public static boolean findSuccessor() {
		List<Phase> phases = List.of(phase(256, 8), phase(128, 32), phase(32, 128), phase(16, 256), phase(8, 4096));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			return BSTTestUtils.findSuccessor(SplayTree::new, n);
		});
	}

}
