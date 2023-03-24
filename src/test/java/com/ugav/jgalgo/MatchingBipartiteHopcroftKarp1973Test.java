package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

public class MatchingBipartiteHopcroftKarp1973Test extends TestUtils {

	@Test
	public void testRandBipartiteGraphs() {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingBipartiteHopcroftKarp1973::new);
	}

}
