package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MaximumMatchingBipartiteHopcroftKarpTest extends TestBase {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x16f0491558fa62f8L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingBipartiteHopcroftKarp::new, seed);
	}

}
