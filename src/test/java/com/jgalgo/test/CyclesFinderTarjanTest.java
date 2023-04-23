package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.CyclesFinderTarjan;

public class CyclesFinderTarjanTest extends TestBase {

	@Test
	public void testSimpleGraph() {
		CyclesFinderTestUtils.testSimpleGraph(new CyclesFinderTarjan());
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0x80ea8f415c16dec1L;
		CyclesFinderTestUtils.testRandGraphs(new CyclesFinderTarjan(), seed);
	}

}
