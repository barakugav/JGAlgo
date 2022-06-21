package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedBipartiteSSSP;

public class MatchingWeightedBipartiteSSSPTest extends TestUtils {

	@Test
	public static void randBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteSSSP::new);
	}

	@Test
	public static void randBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteSSSP::new);
	}

}
