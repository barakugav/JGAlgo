package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MSTFredmanTarjanTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0xaa99618f74cc983aL;
		MSTTestUtils.testRandGraph(MSTFredmanTarjan::new, seed);
	}

}
