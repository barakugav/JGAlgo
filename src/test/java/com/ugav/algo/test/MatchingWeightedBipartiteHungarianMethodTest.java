package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;

public class MatchingWeightedBipartiteHungarianMethodTest extends TestUtils {

	@Test
	public static boolean randBipartiteGraphsWeight1() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteHungarianMethod::getInstance);
	}

	@Test
	public static boolean randBipartiteGraphsWeighted() {
		return MatchingWeightedTestUtils
				.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::getInstance);
	}

	@Test
	public static boolean randBipartiteGraphsWeightedPerfect() {
		return MatchingWeightedTestUtils
				.randBipartiteGraphsWeightedPerfect(MatchingWeightedBipartiteHungarianMethod::getInstance);
	}

}
