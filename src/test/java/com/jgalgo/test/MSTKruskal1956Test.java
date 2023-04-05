package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTKruskal1956;

public class MSTKruskal1956Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0x977b0e2a72f4baa1L;
		MSTTestUtils.testRandGraph(MSTKruskal1956::new, seed);
	}

}
