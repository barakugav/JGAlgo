package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MatchingBipartiteHopcroftKarp1973;

public class MatchingBipartiteHopcroftKarp1973Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphs() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingBipartiteHopcroftKarp1973::new);
	}

}
