package com.ugav.algo.test;

import com.ugav.algo.UnionFindArray;

public class UnionFindArrayTest extends TestUtils {

	@Test
	public static boolean randOps() {
		return UnionFindTestUtils.randOps(UnionFindArray::new);
	}

}
