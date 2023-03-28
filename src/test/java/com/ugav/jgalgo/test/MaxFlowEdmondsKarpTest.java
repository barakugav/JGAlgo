package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowEdmondsKarp;

public class MaxFlowEdmondsKarpTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0x398eea4097bc0600L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new, seed);
	}

}
