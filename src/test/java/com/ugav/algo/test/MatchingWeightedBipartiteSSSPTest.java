package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedBipartiteSSSP;

public class MatchingWeightedBipartiteSSSPTest extends TestUtils {

	@Test
	public static boolean randBipartiteGraphsWeight1() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteSSSP::getInstance);
	}

	@Test
	public static boolean randBipartiteGraphsWeighted() {
		return MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteSSSP::getInstance);
	}

}
