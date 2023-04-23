package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.UnionFindPtr;

public class UnionFindPtrTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0xbdaf148b7ef9991cL;
		UnionFindTestUtils.randOps(UnionFindPtr::new, seed);
	}

}
