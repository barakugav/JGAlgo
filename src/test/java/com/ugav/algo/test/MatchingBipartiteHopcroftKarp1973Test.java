package com.ugav.algo.test;

import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;

public class MatchingBipartiteHopcroftKarp1973Test {

	@Test
	public static boolean randBipartiteGraphs() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingBipartiteHopcroftKarp1973.getInstance());
	}

}
