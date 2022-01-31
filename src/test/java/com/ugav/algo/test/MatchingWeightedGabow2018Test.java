package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedGabow2018;

public class MatchingWeightedGabow2018Test extends TestUtils {

	@Test
	public static boolean randBipartiteGraphsWeight1() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeighted() {
		return MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeightedPerfect() {
		return MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randGraphsWeight1() {
		return MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randGraphsWeighted() {
		return MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randGraphsWeightedPerfect() {
		return MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2018.getInstance());
	}

}
