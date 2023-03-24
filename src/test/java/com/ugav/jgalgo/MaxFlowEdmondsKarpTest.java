package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class MaxFlowEdmondsKarpTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new);
	}

}
