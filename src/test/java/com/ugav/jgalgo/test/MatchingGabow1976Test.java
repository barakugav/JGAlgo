package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingGabow1976;

public class MatchingGabow1976Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphs() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::new);
	}

	@Test
	public void testRandGraphs() {
		MatchingUnweightedTestUtils.randGraphs(MatchingGabow1976::new);
	}

}
