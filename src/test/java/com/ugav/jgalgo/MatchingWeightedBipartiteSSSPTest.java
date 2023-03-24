package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

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
