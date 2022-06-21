package com.ugav.algo.test;

import com.ugav.algo.MSTKruskal1956;

public class MSTKruskal1956Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(MSTKruskal1956::new);
	}

}
