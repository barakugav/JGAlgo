package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowEdmondsKarp;

public class MaxFlowEdmondsKarpTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new);
	}

}
