package com.ugav.algo.test;

import com.ugav.algo.SSSPDijkstra;

public class SSSPDijkstraTest extends TestUtils {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDijkstra::new);
	}

	@Test
	public static boolean testSSSPUndirectedPositiveInt() {
		return SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDijkstra::new);
	}

}
