package com.ugav.algo.test;

import com.ugav.algo.MSTBoruvka1926;

public class MSTBoruvka1926Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(MSTBoruvka1926::new);
	}

}
