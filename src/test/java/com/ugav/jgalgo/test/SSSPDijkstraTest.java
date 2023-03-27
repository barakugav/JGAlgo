package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPDijkstra;

public class SSSPDijkstraTest extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDijkstra::new);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDijkstra::new);
	}

}
