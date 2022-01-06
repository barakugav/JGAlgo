package com.ugav.algo.test;

import com.ugav.algo.UnionFindPtr;

public class UnionFindPtrTest {

	@Test
	public static boolean randOps() {
		return UnionFindTestUtils.randOps(UnionFindPtr::new);
	}

}
