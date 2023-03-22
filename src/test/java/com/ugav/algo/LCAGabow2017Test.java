package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class LCAGabow2017Test extends TestUtils {

	@Test
	public void testFullBinaryTreesRandOps() {
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabow2017::new);
	}

	@Test
	public void testRandTrees() {
		LCADynamicTestUtils.randTrees(LCAGabow2017::new);
	}

}
