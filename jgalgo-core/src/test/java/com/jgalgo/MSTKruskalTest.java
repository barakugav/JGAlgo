package com.jgalgo;

import org.junit.jupiter.api.Test;

public class MSTKruskalTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x977b0e2a72f4baa1L;
		MSTTestUtils.testRandGraph(MSTKruskal::new, seed);
	}

}
