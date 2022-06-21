package com.ugav.algo.test;

import com.ugav.algo.MaxFlowDinic;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new);
	}

}