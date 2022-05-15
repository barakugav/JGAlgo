package com.ugav.algo.test;

import com.ugav.algo.LCAGabowSimple;

public class LCAGabowSimpleTest extends TestUtils {

	@Test
	public static boolean fullBinaryTreesRandOps() {
		return LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabowSimple::new);
	}

	@Test
	public static boolean randTrees() {
		return LCADynamicTestUtils.randTrees(LCAGabowSimple::new);
	}

}
