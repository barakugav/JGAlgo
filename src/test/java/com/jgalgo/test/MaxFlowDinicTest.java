package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaxFlowDinic;

public class MaxFlowDinicTest extends TestUtils {

	@Test
	public void testRandGraphs() {
		final long seed = 0xa79b303ec46fd984L;
		MaxFlowTestUtils.testRandGraphs(MaxFlowDinic::new, seed);
	}

}