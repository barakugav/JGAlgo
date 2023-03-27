package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPBellmanFord;

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
