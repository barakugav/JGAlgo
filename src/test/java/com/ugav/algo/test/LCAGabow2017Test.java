package com.ugav.algo.test;

import com.ugav.algo.LCAGabow2017;

public class LCAGabow2017Test extends TestUtils {

	@Test
	public static void fullBinaryTreesRandOps() {
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabow2017::new);
	}

	@Test
	public static void randTrees() {
		LCADynamicTestUtils.randTrees(LCAGabow2017::new);
	}

}
