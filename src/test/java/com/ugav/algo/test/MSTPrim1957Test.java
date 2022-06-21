package com.ugav.algo.test;

import com.ugav.algo.MSTPrim1957;

public class MSTPrim1957Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(MSTPrim1957::new);
	}

}
