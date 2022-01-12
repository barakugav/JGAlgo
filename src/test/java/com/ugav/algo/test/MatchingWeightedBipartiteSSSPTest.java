package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedBipartiteSSSP;

public class MatchingWeightedBipartiteSSSPTest {

	@Test
	public static boolean randGraphs() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteSSSP.getInstance());
	}

}
