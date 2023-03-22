package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new);
	}

}