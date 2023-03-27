package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingWeightedBipartiteSSSP;

public class MatchingWeightedBipartiteSSSPTest extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteSSSP::new);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteSSSP::new);
	}

}
