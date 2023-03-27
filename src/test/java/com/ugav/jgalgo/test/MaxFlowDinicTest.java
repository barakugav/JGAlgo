package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowDinic;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new);
	}

}