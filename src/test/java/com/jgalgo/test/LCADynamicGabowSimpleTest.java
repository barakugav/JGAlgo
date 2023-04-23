package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.LCADynamicGabowSimple;

public class LCADynamicGabowSimpleTest extends TestBase {

	@Test
	public void testFullBinaryTreesRandOps() {
		final long seed = 0x86404fac670d965fL;
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCADynamicGabowSimple::new, seed);
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xe88bc85f0bcad617L;
		LCADynamicTestUtils.randTrees(LCADynamicGabowSimple::new, seed);
	}

}
