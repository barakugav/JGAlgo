package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.LCAGabow2017;

public class LCAGabow2017Test extends TestUtils {

	@Test
	public void testFullBinaryTreesRandOps() {
		final long seed = 0xe7af3a14bd2eb884L;
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabow2017::new, seed);
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xae1b6bdbf6cd69dcL;
		LCADynamicTestUtils.randTrees(LCAGabow2017::new, seed);
	}

}
