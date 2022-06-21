package com.ugav.algo.test;

import com.ugav.algo.LCAGabowSimple;

public class LCAGabowSimpleTest extends TestUtils {

	@Test
	public static void fullBinaryTreesRandOps() {
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabowSimple::new);
	}

	@Test
	public static void randTrees() {
		LCADynamicTestUtils.randTrees(LCAGabowSimple::new);
	}

}
