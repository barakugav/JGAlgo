package com.ugav.algo.test;

import com.ugav.algo.SSSPDijkstra;

public class SSSPDijkstraTest extends TestUtils {

	@Test
	public static boolean randGraphDirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPDirectedPositiveInt(new SSSPDijkstra());
	}

	@Test
	public static boolean testSSSPUndirectedPositiveInt() {
		return SSSPAbstractTest.testSSSPUndirectedPositiveInt(new SSSPDijkstra());
	}

}
