package com.jgalgo;

import org.junit.jupiter.api.Test;

public class UnionFindPtrTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0xbdaf148b7ef9991cL;
		UnionFindTestUtils.randOps(UnionFindPtr::new, seed);
	}

}
