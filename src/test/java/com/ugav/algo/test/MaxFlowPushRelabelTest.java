package com.ugav.algo.test;

import com.ugav.algo.MaxFlowPushRelabel;

public class MaxFlowPushRelabelTest extends TestUtils {

	@Test
	public static boolean randGraphs() {
		return MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new);
	}
}
