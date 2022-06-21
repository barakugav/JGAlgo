package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;

public class MatchingWeightedBipartiteHungarianMethodTest extends TestUtils {

	@Test
	public static void randBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteHungarianMethod::new);
	}

	@Test
	public static void randBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new);
	}

	@Test
	public static void randBipartiteGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedBipartiteHungarianMethod::new);
	}

}
