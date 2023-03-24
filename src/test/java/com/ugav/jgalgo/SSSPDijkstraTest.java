package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

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
