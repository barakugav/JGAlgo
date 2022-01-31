package com.ugav.algo.test;

import com.ugav.algo.MSTPrim1957;

public class MSTPrim1957Test extends TestUtils {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTPrim1957.getInstance());
	}

}
