package com.ugav.algo;

import org.junit.jupiter.api.Test;

public class SSSPBellmanFordTest extends TestUtils {

	@Test
	public void testRandGraphPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord::new);
	}

	@Test
	public void testRandGraphNegativeInt() {
		SSSPTestUtils.testSSSPDirectedNegativeInt(SSSPBellmanFord::new);
	}

}
