package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MatchingGabow1976Test extends TestBase {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x915c26f5de8fd97aL;
		MatchingBipartiteTestUtils.randBipartiteGraphs(MaximumMatchingGabow1976::new, seed);
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0x6809f5efef8504e9L;
		MatchingUnweightedTestUtils.randGraphs(MaximumMatchingGabow1976::new, seed);
	}

}