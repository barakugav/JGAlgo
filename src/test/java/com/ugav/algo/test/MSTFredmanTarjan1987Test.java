package com.ugav.algo.test;

import com.ugav.algo.MSTFredmanTarjan1987;

public class MSTFredmanTarjan1987Test {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTFredmanTarjan1987.getInstance());
	}

}
