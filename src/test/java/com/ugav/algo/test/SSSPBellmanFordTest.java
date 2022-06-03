package com.ugav.algo.test;

import com.ugav.algo.SSSPBellmanFord;

public class SSSPBellmanFordTest extends TestUtils {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPAbstractTest.testSSSPDirectedPositiveInt(SSSPBellmanFord::new);
	}

	@Test
	public static boolean randGraphNegativeInt() {
		return SSSPAbstractTest.testSSSPDirectedNegativeInt(SSSPBellmanFord::new);
	}

}
