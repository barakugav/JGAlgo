package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.CyclesFinderJohnson;

public class CyclesFinderJohnsonTest extends TestBase {

	@Test
	public void testSimpleGraph() {
		CyclesFinderTestUtils.testSimpleGraph(new CyclesFinderJohnson());
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0x51f9f9bde92eef18L;
		CyclesFinderTestUtils.testRandGraphs(new CyclesFinderJohnson(), seed);
	}

}
