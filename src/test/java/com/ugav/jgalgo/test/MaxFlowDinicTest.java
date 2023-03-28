package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MaxFlowDinic;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new, seed);
	}

}