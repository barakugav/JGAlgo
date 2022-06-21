package com.ugav.algo.test;

import com.ugav.algo.UnionFindPtr;

public class UnionFindPtrTest extends TestUtils {

	@Test
	public static void randOps() {
		UnionFindTestUtils.randOps(UnionFindPtr::new);
	}

}
