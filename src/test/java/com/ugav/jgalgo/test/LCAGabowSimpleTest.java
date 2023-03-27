package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.LCAGabowSimple;

public class LCAGabowSimpleTest extends TestUtils {

	@Test
	public void testFullBinaryTreesRandOps() {
		LCADynamicTestUtils.fullBinaryTreesRandOps(LCAGabowSimple::new);
	}

	@Test
	public void testRandTrees() {
		LCADynamicTestUtils.randTrees(LCAGabowSimple::new);
	}

}
