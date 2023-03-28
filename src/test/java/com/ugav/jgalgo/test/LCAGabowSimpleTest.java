package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.LCAGabowSimple;

public class LCAGabowSimpleTest extends TestUtils {

	@Test
	public void testFullBinaryTreesRandOps() {
		final long seed = 0x86404fac670d965fL;
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabowSimple::new, seed);
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xe88bc85f0bcad617L;
		LCADynamicTestUtils.randTrees(LCAGabowSimple::new, seed);
	}

}
