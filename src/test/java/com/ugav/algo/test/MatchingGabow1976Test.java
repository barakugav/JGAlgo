package com.ugav.algo.test;

import com.ugav.algo.MatchingGabow1976;

public class MatchingGabow1976Test extends TestUtils {

	@Test
	public static boolean randBipartiteGraphs() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::getInstance);
	}

	@Test
	public static boolean randGraphs() {
		return MatchingUnweightedTestUtils.randGraphs(MatchingGabow1976::getInstance);
	}

}
