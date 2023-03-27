package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingWeightedBipartiteHungarianMethod;

public class MatchingWeightedBipartiteHungarianMethodTest extends TestUtils {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedBipartiteHungarianMethod::new);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedBipartiteHungarianMethod::new);
	}

}
