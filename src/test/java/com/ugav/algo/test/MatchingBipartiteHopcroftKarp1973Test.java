package com.ugav.algo.test;

import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;

public class MatchingBipartiteHopcroftKarp1973Test extends TestUtils {

	@Test
	public static void randBipartiteGraphs() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingBipartiteHopcroftKarp1973::new);
	}

}
