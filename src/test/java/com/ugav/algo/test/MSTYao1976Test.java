package com.ugav.algo.test;

import com.ugav.algo.MSTYao1976;

public class MSTYao1976Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(MSTYao1976::new);
	}

}
