package com.ugav.algo.test;

import com.ugav.algo.UnionFindArray;

public class UnionFindArrayTest extends TestUtils {

	@Test
	public static void randOps() {
		UnionFindTestUtils.randOps(UnionFindArray::new);
	}

}
