package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.UnionFindPtr;

public class UnionFindPtrTest extends TestUtils {

	@Test
	public void testRandOps() {
		UnionFindTestUtils.randOps(UnionFindPtr::new);
	}

}
