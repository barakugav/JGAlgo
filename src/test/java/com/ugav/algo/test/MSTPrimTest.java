package com.ugav.algo.test;

import com.ugav.algo.MSTPrim;

public class MSTPrimTest {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTPrim.getInstance());
	}

}
