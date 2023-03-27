package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingWeightedGabow2017;

public class MatchingWeightedGabow2017Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2017::new);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2017::new);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2017::new);
	}

	@Test
	public void testRandGraphsWeight1() {
		MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2017::new);
	}

	@Test
	public void testRandGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2017::new);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2017::new);
	}

}
