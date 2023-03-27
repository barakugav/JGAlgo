package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.SSSPDial1969;

public class SSSPDial1969Test extends TestUtils {

	@Test
	public void testRandGraphDirectedPositiveInt() {
		SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPDial1969::new);
	}

	@Test
	public void testRandGraphUndirectedPositiveInt() {
		SSSPTestUtils.testSSSPUndirectedPositiveInt(SSSPDial1969::new);
	}

}
