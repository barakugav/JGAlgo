package com.ugav.algo.test;

import com.ugav.algo.SSSPBellmanFord;

public class SSSPBellmanFordTest extends TestUtils {

	@Test
	public static void randGraphPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord::new);
	}

	@Test
	public static void randGraphNegativeInt() {
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPBellmanFord::new);
	}

}
