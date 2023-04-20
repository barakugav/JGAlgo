package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaximumMatchingWeightedBipartiteSSSP;

public class MaximumMatchingWeightedBipartiteSSSPTest extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x6d2c36b6f7f5d43fL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingWeightedBipartiteSSSP::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x131359e008ab11acL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MaximumMatchingWeightedBipartiteSSSP::new, seed);
	}

}
