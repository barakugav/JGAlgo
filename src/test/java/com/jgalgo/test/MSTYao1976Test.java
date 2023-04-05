package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTYao1976;

public class MSTYao1976Test extends TestUtils {

	@Test
	public void testRandGraph() {
		final long seed = 0x9d78f3343689fe2dL;
		MSTTestUtils.testRandGraph(MSTYao1976::new, seed);
	}

}
