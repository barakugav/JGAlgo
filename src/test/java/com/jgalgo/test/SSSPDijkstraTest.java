package com.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.jgalgo.SSSPDijkstra;

public class SSSPDijkstraTest extends TestUtils {

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
