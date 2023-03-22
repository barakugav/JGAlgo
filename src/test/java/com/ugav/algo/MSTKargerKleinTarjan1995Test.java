package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class MSTKargerKleinTarjan1995Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan1995(nextRandSeed()));
	}

}
