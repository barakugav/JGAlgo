package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingWeightedGabow2017;

public class MatchingWeightedGabow2017Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x2ab1588bd0eb62b2L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2017::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0xbffb50ae18bf664cL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2017::new, seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0xf5c0a210842d9f5eL;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2017::new, seed);
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x67ead1b9c6600229L;
		MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2017::new, seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0x33a1793a0388c73bL;
		MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2017::new, seed);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		final long seed = 0x625606329a1eb13cL;
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2017::new, seed);
	}

}
