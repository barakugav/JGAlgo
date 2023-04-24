package com.jgalgo;

import org.junit.jupiter.api.Test;

public class LCADynamicGabowLinearTest extends TestBase {

	@Test
	public void testFullBinaryTreesRandOps() {
		final long seed = 0xe7af3a14bd2eb884L;
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCADynamicGabowLinear::new, seed);
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xae1b6bdbf6cd69dcL;
		LCADynamicTestUtils.randTrees(LCADynamicGabowLinear::new, seed);
	}

}
