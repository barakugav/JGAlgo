package com.ugav.algo.test;

import com.ugav.algo.SSSPBellmanFord;

public class SSSPBellmanFordTest extends TestUtils {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord::new);
	}

	@Test
	public static boolean randGraphNegativeInt() {
		return SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPBellmanFord::new);
	}

}
