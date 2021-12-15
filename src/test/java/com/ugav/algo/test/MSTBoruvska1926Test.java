package com.ugav.algo.test;

import com.ugav.algo.MSTBoruvska1926;

public class MSTBoruvska1926Test {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTBoruvska1926.getInstance());
	}

}
