package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumMatchingWeightedBipartiteSSSPTest extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x6d2c36b6f7f5d43fL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MaximumMatchingWeightedBipartiteSSSP(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0x131359e008ab11acL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MaximumMatchingWeightedBipartiteSSSP(), seed);
	}

}
