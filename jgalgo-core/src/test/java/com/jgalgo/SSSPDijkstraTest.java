package com.jgalgo;

import org.junit.jupiter.api.Test;

public class SSSPDijkstraTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x4c6096c679a03079L;
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDijkstra::new, seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0x97997bc1c8243730L;
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDijkstra::new, seed);
	}

}
