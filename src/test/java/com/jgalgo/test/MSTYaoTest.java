package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.MSTYao;

public class MSTYaoTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x9d78f3343689fe2dL;
		MSTTestUtils.testRandGraph(MSTYao::new, seed);
	}

}
