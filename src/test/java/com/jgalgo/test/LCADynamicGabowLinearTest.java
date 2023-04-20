package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.LCADynamicGabowLinear;

public class LCADynamicGabowLinearTest extends TestUtils {

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
