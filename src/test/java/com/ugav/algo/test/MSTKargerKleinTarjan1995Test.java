package com.ugav.algo.test;

import com.ugav.algo.MSTKargerKleinTarjan1995;

public class MSTKargerKleinTarjan1995Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan1995(nextRandSeed()));
	}

}
