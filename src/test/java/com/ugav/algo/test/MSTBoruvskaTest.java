package com.ugav.algo.test;

import com.ugav.algo.MSTBoruvska;

public class MSTBoruvskaTest {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTBoruvska.getInstance());
	}

}
