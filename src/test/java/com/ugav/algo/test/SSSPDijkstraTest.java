package com.ugav.algo.test;

import com.ugav.algo.SSSPDijkstra;

public class SSSPDijkstraTest {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDijkstra.getInstace());
	}

	@Test
	public static boolean testSSSPUndirectedPositiveInt() {
		return SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDijkstra.getInstace());
	}

}
