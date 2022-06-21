package com.ugav.algo.test;

import com.ugav.algo.MaxFlowPushRelabel;

public class MaxFlowPushRelabelTest extends TestUtils {

	@Test
	public static void randGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new);
	}
}
