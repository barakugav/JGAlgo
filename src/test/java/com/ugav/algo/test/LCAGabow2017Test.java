package com.ugav.algo.test;

import com.ugav.algo.LCAGabow2017;

public class LCAGabow2017Test extends TestUtils {

	@Test
	public static boolean fullBinaryTreesRandOps() {
		return LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabow2017::new);
	}

	@Test
	public static boolean randTrees() {
		initTestRand(0);
		return LCADynamicTestUtils.randTrees(LCAGabow2017::new);
	}

}
