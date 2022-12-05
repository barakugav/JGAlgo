package com.ugav.algo;

public class MatchingWeightedGabow2017Test extends TestUtils {

	@Test
	public static void randBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2017::new);
	}

	@Test
	public static void randBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2017::new);
	}

	@Test
	public static void randBipartiteGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2017::new);
	}

	@Test
	public static void randGraphsWeight1() {
		MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2017::new);
	}

	@Test
	public static void randGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2017::new);
	}

	@Test
	public static void randGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2017::new);
	}

}
