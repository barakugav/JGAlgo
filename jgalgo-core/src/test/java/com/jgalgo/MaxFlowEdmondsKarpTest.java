package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowEdmondsKarpTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0x398eea4097bc0600L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new, seed);
	}

}
