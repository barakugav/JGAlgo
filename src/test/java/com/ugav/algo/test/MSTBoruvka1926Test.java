package com.ugav.algo.test;

import com.ugav.algo.MSTBoruvka1926;

public class MSTBoruvka1926Test extends TestUtils {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(MSTBoruvka1926::new);
	}

}
