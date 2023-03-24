package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

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
