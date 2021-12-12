package com.ugav.algo.test;

import com.ugav.algo.MSTKruskal;

public class MSTKruskalTest {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTKruskal.getInstance());
	}

}
