package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedGabow2017;

public class MatchingWeightedGabow2017Test extends TestUtils {

	@Test
	public static boolean randBipartiteGraphsWeight1() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2017.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeighted() {
		return MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2017.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeightedPerfect() {
		return MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2017.getInstance());
	}

	@Test
	public static boolean randGraphsWeight1() {
		return MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2017.getInstance());
	}

	@Test
	public static boolean randGraphsWeighted() {
		return MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2017.getInstance());
	}

	@Test
	public static boolean randGraphsWeightedPerfect() {
		return MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2017.getInstance());
	}

}
