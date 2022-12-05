package com.ugav.algo;

public class MatchingGabow1976Test extends TestUtils {

	@Test
	public static void randBipartiteGraphs() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::new);
	}

	@Test
	public static void randGraphs() {
		MatchingUnweightedTestUtils.randGraphs(MatchingGabow1976::new);
	}

}
