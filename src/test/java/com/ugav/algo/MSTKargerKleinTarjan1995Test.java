package com.ugav.algo;

public class MSTKargerKleinTarjan1995Test extends TestUtils {

	@Test
	public static void randGraph() {
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan1995(nextRandSeed()));
	}

}
