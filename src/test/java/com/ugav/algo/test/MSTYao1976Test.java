package com.ugav.algo.test;

import com.ugav.algo.MSTYao1976;

public class MSTYao1976Test {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTYao1976.getInstance());
	}

}
