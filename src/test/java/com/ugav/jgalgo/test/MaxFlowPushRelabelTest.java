package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowPushRelabel;

public class MaxFlowPushRelabelTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0x87662c130902cf06L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowPushRelabel::new, seed);
	}
}
