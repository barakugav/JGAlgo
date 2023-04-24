package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumMatchingWeightedBipartiteHungarianMethodTest extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x10dff70f8efc00f1L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingWeightedBipartiteHungarianMethod::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x3f731f291383dd24L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MaximumMatchingWeightedBipartiteHungarianMethod::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0x3b7892e59492d431L;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MaximumMatchingWeightedBipartiteHungarianMethod::new,
				seed);
	}

}
