package com.ugav.algo.test;

import com.ugav.algo.MSTKargerKleinTarjan1995;

public class MSTKargerKleinTarjan1995Test {

	@Test
	public static boolean randGraph() {
		return MSTTestUtils.testRandGraph(new MSTKargerKleinTarjan1995(TestUtils.nextRandSeed()));
	}

}
