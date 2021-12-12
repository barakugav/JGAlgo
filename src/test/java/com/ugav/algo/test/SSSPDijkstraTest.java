package com.ugav.algo.test;

import com.ugav.algo.SSSPDijkstra;

public class SSSPDijkstraTest {

	@Test
	public static boolean basic() {
		return SSSPTestUtils.testSSSPPositive(SSSPDijkstra.getInstace());
	}

}
