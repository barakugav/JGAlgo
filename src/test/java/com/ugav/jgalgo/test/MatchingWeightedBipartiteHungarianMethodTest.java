package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingWeightedBipartiteHungarianMethod;

public class MatchingWeightedBipartiteHungarianMethodTest extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x10dff70f8efc00f1L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteHungarianMethod::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x3f731f291383dd24L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0x3b7892e59492d431L;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedBipartiteHungarianMethod::new,
				seed);
	}

}
