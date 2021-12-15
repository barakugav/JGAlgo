package com.ugav.algo.test;

import com.ugav.algo.MSTKruskal1956;

public class MSTKruskal1956Test {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTKruskal1956.getInstance());
	}

}
