package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MaximumMatchingBipartiteHopcroftKarp;

public class MaximumMatchingBipartiteHopcroftKarpTest extends TestUtils {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x16f0491558fa62f8L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingBipartiteHopcroftKarp::new, seed);
	}

}
