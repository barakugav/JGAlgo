package com.ugav.algo;

public class SSSPDijkstraTest extends TestUtils {

	@Test
	public static void randGraphDirectedPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDijkstra::new);
	}

	@Test
	public static void testSSSPUndirectedPositiveInt() {
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDijkstra::new);
	}

}
