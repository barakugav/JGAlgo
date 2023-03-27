package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.MSTKargerKleinTarjan1995;

public class MSTKargerKleinTarjan1995Test extends TestUtils {

	@Test
	public void testRandGraph() {
		MSTTestUtils.testRandGraph(() -> new MSTKargerKleinTarjan1995(nextRandSeed()));
	}

}
