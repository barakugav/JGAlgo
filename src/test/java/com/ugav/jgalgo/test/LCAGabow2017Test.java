package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.LCAGabow2017;

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
