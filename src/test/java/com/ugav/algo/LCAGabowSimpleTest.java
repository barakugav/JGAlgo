package com.ugav.algo;

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
