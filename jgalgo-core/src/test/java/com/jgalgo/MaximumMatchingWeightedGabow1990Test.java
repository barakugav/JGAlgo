package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumMatchingWeightedGabow1990Test extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x2ab1588bd0eb62b2L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingWeightedGabow1990::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0xbffb50ae18bf664cL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MaximumMatchingWeightedGabow1990::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0xf5c0a210842d9f5eL;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MaximumMatchingWeightedGabow1990::new, seed);
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x67ead1b9c6600229L;
		MatchingUnweightedTestUtils.randGraphs(MaximumMatchingWeightedGabow1990::new, seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0x33a1793a0388c73bL;
		MatchingWeightedTestUtils.randGraphsWeighted(MaximumMatchingWeightedGabow1990::new, seed);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		final long seed = 0x625606329a1eb13cL;
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(MaximumMatchingWeightedGabow1990::new, seed);
	}

}
