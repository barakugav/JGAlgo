package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.UnionFindPtr;

public class UnionFindPtrTest extends TestUtils {

	@Test
	public void testRandOps() {
		final long seed = 0xbdaf148b7ef9991cL;
		UnionFindTestUtils.randOps(UnionFindPtr::new, seed);
	}

}
