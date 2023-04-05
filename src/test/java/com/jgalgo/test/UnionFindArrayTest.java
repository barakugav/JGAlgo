package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.UnionFindArray;

public class UnionFindArrayTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0x5d5d96d3d0365da7L;
		UnionFindTestUtils.randOps(UnionFindArray::new, seed);
	}

}
