package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

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
