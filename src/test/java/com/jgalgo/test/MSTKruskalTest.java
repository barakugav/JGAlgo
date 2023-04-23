package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTKruskal;

public class MSTKruskalTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x977b0e2a72f4baa1L;
		MSTTestUtils.testRandGraph(MSTKruskal::new, seed);
	}

}
