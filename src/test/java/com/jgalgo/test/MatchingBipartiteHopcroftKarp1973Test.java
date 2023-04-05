package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MatchingBipartiteHopcroftKarp1973;

public class MatchingBipartiteHopcroftKarp1973Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x16f0491558fa62f8L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingBipartiteHopcroftKarp1973::new, seed);
	}

}
