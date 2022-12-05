package com.ugav.algo;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new);
	}

}