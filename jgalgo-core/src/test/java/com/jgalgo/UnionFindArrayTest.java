package com.jgalgo;

import org.junit.jupiter.api.Test;

public class UnionFindArrayTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0x5d5d96d3d0365da7L;
		UnionFindTestUtils.randOps(UnionFindArray::new, seed);
	}

}
